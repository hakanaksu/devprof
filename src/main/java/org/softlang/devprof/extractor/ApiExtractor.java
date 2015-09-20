package org.softlang.devprof.extractor;

import org.softlang.devprof.persistence.entities.API;
import org.softlang.devprof.persistence.entities.Classifier;
import org.softlang.devprof.persistence.entities.EnumConstant;
import org.softlang.devprof.persistence.entities.MethodEntity;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.logic.PersistenceHandler;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hakan Aksu
 */
public class ApiExtractor extends Extractor{

    private URL url;
    private JarURLConnection jarURLConnection;
    private URLClassLoader urlClassLoader;
    
    private PersistenceHandler persistenceHandler;

    public ApiExtractor(URL url) throws IOException {
        urlClassLoader = new URLClassLoader(new URL[] { url });
        this.url = new URL("jar", "", url + "!/");
        jarURLConnection = (JarURLConnection)this.url.openConnection();
        persistenceHandler = new PersistenceHandler();
    }
    
    @Override
    public Object extractData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public API extractData(String packageName, boolean withSubfolder){
        HashSet<String> set = new HashSet();
        
        LinkedHashSet<Classifier> classifiers = new LinkedHashSet<Classifier>();
        LinkedHashSet<MethodEntity> methods = new LinkedHashSet<MethodEntity>();
        LinkedHashSet<EnumConstant> enums = new LinkedHashSet<EnumConstant>();
        
        API api = new API();
        api.setJarName(jarURLConnection.getJarFileURL().toString().substring(jarURLConnection.getJarFileURL().toString().lastIndexOf("/")+1));
        api.setJarPath(jarURLConnection.getJarFileURL().toString());
        api = persistenceHandler.createAPI(api);
        try {
            JarFile jarFile = jarURLConnection.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            Package pack = persistenceHandler.getPackage(packageName);
            api.getPackages().add(pack);
            pack.setApi(api);
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().endsWith(".class") && jarEntry.getName().replace("/", ".").contains(packageName)  ) {
          
                    
                    String packageWithClassname = jarEntry.getName().replace("/", ".").substring(0,jarEntry.getName().lastIndexOf("."));
                    String packageWithoutClassname = packageWithClassname.substring(0,packageWithClassname.lastIndexOf("."));
                    
                    if(!withSubfolder && !packageWithoutClassname.equals(packageName)){
                        continue;
                    }
                    
                    try {                        
                        Class<?> mClass = urlClassLoader.loadClass(packageWithClassname);
                        if( !(mClass.isAnonymousClass() || mClass.isLocalClass() || mClass.isAnnotation() || mClass.isSynthetic()) ){
                            Classifier classifier = new Classifier(jarEntry.getName().substring(jarEntry.getName().lastIndexOf("/")+1));
                            classifiers.add(classifier);
                            if(mClass.isEnum()) {
                                EnumConstant enumEntity;
                                try{
	                                for(Object o: mClass.getEnumConstants()){
	                                    String s = (String) o.toString();
	                                    enumEntity = new EnumConstant(s);
	                                    enums.add(enumEntity);
	                                }
                                } catch (java.lang.ExceptionInInitializerError e){ System.out.println("java.lang.ExceptionInInitializerError");}
                            } else {
                                
                                Method[] methodArray = mClass.getMethods();
                                MethodEntity methodEntity;
                                for (Method method : methodArray) {
                                    if (!contains(method.getName())){
                                        methodEntity = new MethodEntity(method.getName());
                                        methods.add(methodEntity);
                                    }
                                }
                            }
                        }    
                    } catch (NoClassDefFoundError ex){
                        System.out.println("NoClassDefFoundError: " + jarEntry.getName());
                    } catch (java.lang.IncompatibleClassChangeError e){
                    	System.out.println("IncompatibleClassChangeError: " + jarEntry.getName());
                    }
                }
            }
            
            System.out.println(pack.getPackageName() + " Classifiers: "+classifiers.size());
            for (Classifier classifier : classifiers) {
                persistenceHandler.createClassifier(classifier, pack);
            }
            
            System.out.println(pack.getPackageName() + " Methods: "+methods.size());
            
            for (MethodEntity method : methods) {
                persistenceHandler.createMethodEntity(method, pack);
            }
            
            System.out.println(pack.getPackageName() + " Enums: "+enums.size());
            
            for (EnumConstant aEnum : enums) {
                persistenceHandler.createEnumEntity(aEnum, pack);
            }
            
            persistenceHandler.refresh();
            
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException ");
            Logger.getLogger(ApiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IOException");
            Logger.getLogger(ApiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        return null;
    }
    
    String[] keywordsArray = {"equals","toString","hashCode","getClass","wait","notify","notifyAll"}; 
    
    
    public boolean contains(String keyword){
        List<String> keys = new ArrayList<String>();
        keys.addAll(Arrays.asList(keywordsArray));
        return keys.contains(keyword);
    }
    
}
