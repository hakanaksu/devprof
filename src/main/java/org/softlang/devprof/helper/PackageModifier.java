package org.softlang.devprof.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.softlang.devprof.persistence.entities.ChangedFile;
import org.softlang.devprof.persistence.entities.Domain;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.logic.PersistenceHandler;


/**
 * 
 * @author Hakan Aksu
 */
public class PackageModifier {

	PersistenceHandler persistenceHandler;
	
	public PackageModifier() {
		persistenceHandler = new PersistenceHandler();
	}
	
	public void eliminationStatic() {
		List<Package> resultList = persistenceHandler.getStaticPackageElements();
		for (Package pack : resultList) {
			System.out.println(pack.getPackageName());
		}
		for(int i = 0; i < resultList.size();i++){
			System.out.println("convert: " + resultList.get(i).getPackageName());
			int firstIndex = resultList.get(i).getPackageName().indexOf(' ')+1;
			String subString = resultList.get(i).getPackageName().substring(firstIndex);

			Package availablePack = persistenceHandler.getPackage(subString);
			System.out.println("To: " + subString + "\n");
			if(availablePack == null){
				resultList.get(i).setPackageName(subString);
				persistenceHandler.refresh(resultList.get(i));				
			} else {
				Set<ChangedFile> cls = resultList.get(i).getChangedFiles();
				availablePack.getChangedFiles().addAll(cls);
				persistenceHandler.refresh(availablePack);
				persistenceHandler.deletePackage(resultList.get(i));
			}
		}
	}
	
	public void eliminiationInternal(List<String> internalPackages) {
		persistenceHandler.deleteInternalPackages(internalPackages);
	}
	
	/**
	 * 
	 * @param map String The name of the package
	 * 			  Boolean true, if package exist in the database, false otherwise
	 */
	public void eliminationSubfolder(Map<String,Boolean> map) {
		
		Set<String> keys = map.keySet();
		for (String key : keys) {
			Package pack;
			
			boolean b = map.get(key);
			if (b) {
				pack = persistenceHandler.getPackage(key);
			} else {
				pack = persistenceHandler.createPackage(new Package(key));
			}
			
			List<Long> iDs = persistenceHandler.getPackage_ChangedFileIDs(key, true);
			List<ChangedFile> lisCF = persistenceHandler.getChangedFile(iDs);
			pack.getChangedFiles().addAll(lisCF);
			persistenceHandler.refresh();
			List<Package> subPacks = persistenceHandler.getPackages(key+".",true);
			for (Package p : subPacks) {
				persistenceHandler.deletePackage(p);
			}
		}
	}
	
	
	public void deletePackage(String packageName) {
		persistenceHandler.deletePackage(persistenceHandler.getPackage(packageName));
	}

	public void replacePackageReferences(String from, String to, boolean toPackageExists) {
		Package toPackage = null;
		if(toPackageExists){
			toPackage = persistenceHandler.getPackage(to);
		} else {
			toPackage = persistenceHandler.createPackage(new Package(to));
		}
		Set<ChangedFile> set = toPackage.getChangedFiles();
		
		
		Package fromPackage = persistenceHandler.getPackage(from);
		set.addAll(fromPackage.getChangedFiles());
		
		persistenceHandler.deletePackage(fromPackage);
		
		persistenceHandler.refresh();
	}
	
	
	public Domain addDomain(String name){
		Domain d = new Domain(name);
		persistenceHandler.addDomain(d);
		return d;
	}
	
	public void addApiToDomain(String[] apis, Domain domain){
		for (int i = 0; i < apis.length; i++) {
			Package p = persistenceHandler.getPackage(apis[i]);
			p.setDomain(domain);
			domain.getPackages().add(p);
		}
		persistenceHandler.refresh();
	}
	
	
	
	
}
