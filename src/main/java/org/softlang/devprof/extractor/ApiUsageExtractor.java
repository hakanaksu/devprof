package org.softlang.devprof.extractor;

import org.softlang.devprof.persistence.entities.ChangedLine;
import org.softlang.devprof.persistence.entities.Classifier;
import org.softlang.devprof.persistence.entities.EnumConstant;
import org.softlang.devprof.persistence.entities.MethodEntity;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.logic.PersistenceHandler;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Hakan Aksu
 */
public class ApiUsageExtractor extends Extractor {

    private PersistenceHandler persistenceHandler;
    
    public ApiUsageExtractor() {
        date = new Date();
        start = date.getTime();
        persistenceHandler = new PersistenceHandler();
        date = new Date();
        end = date.getTime();
        difference = end - start;
        System.out.println("create PersistenceHandler Object "+difference);
    }

        Date date = new Date();
        long start;
        long end;
        long difference;
    
    
    @Override
    public Object extractData() {
        return null;
    }
    public Object extractData(int startID, int endID, int[] unqualified) {
                
        List<ChangedLine> changedLines;
        List<Package> packages = new LinkedList<Package>();
        
        
        //Neue Variablen
        List<EnumConstant> enums = new LinkedList<EnumConstant>();
        List<Classifier> classifiers = new LinkedList<Classifier>();
        List<MethodEntity> methods = new LinkedList<MethodEntity>();
       
        date = new Date();
        long startbegin = date.getTime();
        
        
        date = new Date();
        start = date.getTime();
        
        // Nimm ale ChangeLines
        //changedLines = persistenceHandler.getChangedLines();
        //int startID = 230001;
        //int endID = 240000;
        changedLines = persistenceHandler.getChangedLines(startID,endID,unqualified);
        
        int i = startID;

        date = new Date();
        end = date.getTime();
        difference = end - start;
        System.out.println("Get ChangedLines "+difference + " ms Size: " + changedLines.size());
        
        // iteriere über alle ChangedLines
        ChangedLine previousChangedLine = null;
        for (ChangedLine changedLine : changedLines) {
            System.out.println(i++ + ": "+ changedLine.getId());

            
            if (previousChangedLine == null || !changedLine.getChangedFile().getFileName().equals(previousChangedLine.getChangedFile().getFileName()) ) {
                date = new Date();
                start = date.getTime();
                // Schau für diese ChangedLine an, welche Packages in der Klasse verwendet werden
                packages = persistenceHandler.getPackages(changedLine);

                date = new Date();
                end = date.getTime();
                difference = end - start;
                System.out.println("Packages for the ChangedLine " + difference + " ms Size: " + packages.size());

                // Nimm alle API Elemente aus den gefundenen Packages
                methods = new LinkedList<MethodEntity>();
                enums = new LinkedList<EnumConstant>();
                classifiers = new LinkedList<Classifier>();
                for (Package pack : packages) {
                    date = new Date();
                    start = date.getTime();
                    methods.addAll(persistenceHandler.getMethods(pack));
                    //neue Zeilen
                    enums.addAll(persistenceHandler.getEnums(pack));
                    classifiers.addAll(persistenceHandler.getClasses(pack));
                    date = new Date();
                    end = date.getTime();
                    difference = end - start;
                    System.out.println("Methods of the Packages " + difference + " ms Size: " + methods.size());
                    System.out.println("Classes of the Packages " + difference + " ms Size: " + classifiers.size());
                    System.out.println("Enums of the Packages " + difference + " ms Size: " + enums.size());
                }
            } else {
                //System.out.println("Zeile aus der gleichen Datei wie das Vorherige");
            }
           if (packages.isEmpty()){
               previousChangedLine = changedLine;
               continue;
           }
            
            //neue Zeilen
            Set<Long> listOfEnums = new HashSet<Long>();
            Set<Long> listOfClassifiers = new HashSet<Long>();
            Set<Long> listOfMethods = new HashSet<Long>(); 
            
            // iteriere über alle Methodennamen
            
            Set<String> tokenizedLines = tokenize(changedLine);
            
            for (String tokenizedLine : tokenizedLines) {
                if(tokenizedLine.contains("(")){
                    String[] split = tokenizedLine.split("\\(");
                    for (MethodEntity method : methods) {
                        if(split.length >= 1 && split[0].equals(method.getName())){
                            listOfMethods.add(method.getId());
                        }                        
                    }
                } else {
                    boolean flag = false; 
                    for (Classifier classifier : classifiers) {
                        String c = classifier.getName();
                        int first = 0;
                        int last = c.lastIndexOf(".");
                        if(c.contains("$")){
                            first = c.lastIndexOf("$")+1;
                        }
                        c = c.substring(first, last);
                        if(!c.matches("[1-9]*") && tokenizedLine.equals(c)){
                            listOfClassifiers.add(classifier.getId());
                            flag = true;
                        }
                    }
                    if(!flag){
                        for (EnumConstant enumaration : enums) {
                            if(tokenizedLine.equals(enumaration.getName())){
                                listOfEnums.add(enumaration.getId());
                            }
                        }
                    }
                }
            }
            
            
            persistenceHandler.createLineMethodRelation(listOfMethods,changedLine.getId());
            persistenceHandler.createLineClassRelation(listOfClassifiers,changedLine.getId());
            persistenceHandler.createLineEnumRelation(listOfEnums,changedLine.getId());
            previousChangedLine = changedLine;
        }
        
        
        date = new Date();
        end = date.getTime();
        
        difference = end - startbegin;
        System.out.println("Total time:" + difference);
        return null;
    }

    private Set<String> tokenize(ChangedLine changedLine) {
        String[] split = changedLine.getChange().split(regex);
        Set<String> result = new LinkedHashSet<String>();
        for (int i = 0; i < split.length; i++) {
            if (split[i].length()>0)
                result.add(split[i].trim());
        }
        return result;
    }
    
    String regex = "\\s"
            + "|\""
            + "|\\."
            + "|\\)"
            + "|="
            + "|;"
            + "|\\{"
            + "|\\}"
            + "|\\+"
            + "|-"
            + "|\\/"
            + "|\\*"
            + "|abstract"
            + "|assert"
            + "|boolean"
            + "|break"
            + "|byte"
            + "|case"
            + "|catch"
            + "|char"
            + "|class"
            + "|const"
            + "|continue"
            + "|default"
            + "|do"
            + "|double"
            + "|else"
            + "|enum"
            + "|extends"
            + "|final"
            + "|finally"
            + "|float"
            + "|for"
            + "|if"
            + "|goto"
            + "|implements"
            + "|import"
            + "|instanceof"
            + "|int"
            + "|interface"
            + "|long"
            + "|native"
            + "|new"
            + "|package"
            + "|private"
            + "|protected"
            + "|public"
            + "|return"
            + "|short"
            + "|static"
            + "|strictfp"
            + "|super"
            + "|switch"
            + "|synchronized"
            + "|this"
            + "|throw"
            + "|throws"
            + "|transient"
            + "|try"
            + "|void"
            + "|volatile"
            + "|while";
}
