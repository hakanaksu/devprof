package org.softlang.devprof.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Hakan Aksu
 */
public class PackageNameExtractor {
    
    private String pathOfJavaFile;
        
    private Set<String> fullPackageNamesWithClassNames;
    
    public PackageNameExtractor(String pathOfJavaFile) {
        this.pathOfJavaFile = pathOfJavaFile;
        initFullPackageNamesWithClassNames();
    }

    /**
     * vollständig
     * @return 
     */
    private boolean javaFileExist(){
        File file = new File(pathOfJavaFile);
        return file.exists();
    }
    
    /*
     *vollständig 
     */
    private void initFullPackageNamesWithClassNames(){
        fullPackageNamesWithClassNames = new LinkedHashSet<String>();
        if (javaFileExist()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfJavaFile)));
                String line = "";
                Pattern p = Pattern.compile("(^import )(.+)");
                while(line != null){
                    Matcher m = p.matcher(line.trim());
                    if (m.find()) {
                        String imp = m.group(2).trim();
                        if (imp.endsWith(";")) {
                            imp = imp.substring(0,imp.length()-1);
                        }
                        fullPackageNamesWithClassNames.add(imp);
                    }
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //kann nicht auftreten, da überprüft wird, ob die .java Datei vorhanden ist
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        
    /**
     * vollständig
     * @return 
     */
    public Set<String> getFullPackageNamesWithClassNames() {
        return fullPackageNamesWithClassNames;
    }
    
    /**
     * vollständig
     * @return 
     */
    public Set<String> getFullPackageNamesWithoutClassNames() {
        Set<String> fullPackageNamesWithoutClassNames = new LinkedHashSet<String>();
        for (String string : fullPackageNamesWithClassNames) {
            if (string.contains("."))
                fullPackageNamesWithoutClassNames.add(string.substring(0, string.lastIndexOf('.')));
            else
                fullPackageNamesWithoutClassNames.add(string);
        }
        return fullPackageNamesWithoutClassNames;
    }

    /**
     * vollständig
     * @return A List of Packages with the first two packagenames
     */
    public Set<String> getPackageNames() {
        Set<String> packageNames = new LinkedHashSet<String>();
        Set<String> fullPackageNamesWithoutClassNames = getFullPackageNamesWithoutClassNames();
        for (String string : fullPackageNamesWithoutClassNames) {
            String[] stringSplit = string.split(Pattern.quote( "." ));
            if (stringSplit.length >= 2)
                packageNames.add(stringSplit[0]+"."+stringSplit[1]);
            else
                packageNames.add(string);
        }
        return packageNames;
    }
    
    /**
     * vollständig
     * @param internPathsToJavaClasses
     * @return 
     */
    public Set<String> getExternFullPackageNamesWithClassNames(Set<String> internPathsToJavaClasses) {
        Set<String> externFullPackageNamesWithClassNames = new LinkedHashSet<String>();
        Pattern p;
        Matcher m;
        for (String packages : fullPackageNamesWithClassNames) {
            p = Pattern.compile(packages.replaceAll(Pattern.quote("."), "/"));
            boolean flag = true;
            for (String path : internPathsToJavaClasses) {
                m = p.matcher(path);
                if (m.find()) {
                    flag = false;
                    break;
                }
            }
            if(flag){
                externFullPackageNamesWithClassNames.add(packages);
            }
        }   
        return externFullPackageNamesWithClassNames;
    }
    
    /**
     * vollständig
     * @param internPathsToJavaClasses
     * @return 
     */
    public Set<String> getExternFullPackageNamesWithoutClassNames(Set<String> internPathsToJavaClasses) {
        
        Set<String> fullExternPackageNamesWithoutClassNames = new LinkedHashSet<String>();
        Set<String> fullExternPackageNamesWithClassNames = getExternFullPackageNamesWithClassNames(internPathsToJavaClasses);
        for (String string : fullExternPackageNamesWithClassNames) {
            if (string.contains("."))
                fullExternPackageNamesWithoutClassNames.add(string.substring(0, string.lastIndexOf('.')));
            else
                fullExternPackageNamesWithoutClassNames.add(string);
        }
        return fullExternPackageNamesWithoutClassNames;
    }
    
    /**
     * vollständig
     * @param internPathsToJavaClasses
     * @return 
     */
    public Set<String> getExternPackageNames(Set<String> internPathsToJavaClasses) {
        Set<String> externPackageNames = new LinkedHashSet<String>();
        Set<String> fullExternPackageNamesWithoutClassNames = getExternFullPackageNamesWithoutClassNames(internPathsToJavaClasses);
        for (String string : fullExternPackageNamesWithoutClassNames) {
            String[] stringSplit = string.split(Pattern.quote( "." ));
            if (stringSplit.length >= 2)
                externPackageNames.add(stringSplit[0]+"."+stringSplit[1]);
            else
                externPackageNames.add(string);
        }
        return externPackageNames;
    }
    
    /**
     * vollständig
     * @param pathOfJavaFile 
     */
    public boolean setPathOfJavaFile(String pathOfJavaFile) {
        File file = new File(pathOfJavaFile);
        if (file.exists()) {
            this.pathOfJavaFile = pathOfJavaFile;
            initFullPackageNamesWithClassNames();
            return true;
        } else {
            return false;
        }
    }
}
