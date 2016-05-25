package org.softlang.devprof.persistence.logic;

import org.softlang.devprof.persistence.entities.API;
import org.softlang.devprof.persistence.entities.ChangedFile;
import org.softlang.devprof.persistence.entities.ChangedLine;
import org.softlang.devprof.persistence.entities.Classifier;
import org.softlang.devprof.persistence.entities.Domain;
import org.softlang.devprof.persistence.entities.EnumConstant;
import org.softlang.devprof.persistence.entities.MethodEntity;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.entities.Repository;
import org.softlang.devprof.persistence.entities.Version;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;

/**
 *
 * @author Hakan Aksu
 */
public class PersistenceHandler implements AutoCloseable{
    
    private final EntityManager em;

    public PersistenceHandler() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DeveloperProfilerPU");
        em = emf.createEntityManager();
    }
    
    private Date date(String d) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.parse(d);
        } catch (ParseException ex) {
            throw new RuntimeException("Wrong date " + d, ex);
        }
    }
    
    /**
     * used by SVNRepositoryExtractor
     * used by GITRepositoryExtractor
     * @param repository
     * @return the repository object from the database (if it exists), a new repository object (otherwise)
     */
    public Repository createRepository(Repository repository) {
        Repository r;
        try {
            r = getRepository(repository.getUrl());
        } catch (NoResultException e) {
            r = null;
        }
        if (r == null) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(repository);
            em.getTransaction().commit();
            return repository;
        } else {
            return r;
        }
    }
    
    
    /**
     * Used by this
     * 
     * @param url
     * @return the repository object from the database
     * @throws NoResultException if repository object doesn't exist. 
     */
    Repository getRepository(String url) {
        em.getTransaction().begin();
        Repository result = em.createQuery(
                    "select r from Repository r where r.url = :url",
                    Repository.class)
                    .setParameter("url", url)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used By SVNRepositoryExtractor
     * Used By GITRepositoryExtractor
     * 
     * @param version 
     * @return the version object from the database (if it exists), a new version object (otherwise)
     */
    public Version createVersion(Version version){
        Version v;
        try {
            v = getVersion(version.getRevision(), version.getRepository().getId());
        } catch (NoResultException e) {
            v = null;
        }
        if (v == null) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(version);
            em.getTransaction().commit();
            return version;
        } else {
            return v;
        }
    }
    
    /**
     * Used by this
     * 
     * @param revision
     * @param repositoryID
     * @return 
     */
    public Version getVersion(long revision, long repositoryID) {
        em.getTransaction().begin();
        Version result = em.createQuery(
                    "select v from Version v where v.revision = :revision and v.repository.id = :repositoryID",
                    Version.class)
                    .setParameter("revision", revision)
                    .setParameter("repositoryID", repositoryID)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by SVNRepositoryExtractor
     * Used by GITRepositoryExtractor
     * 
     * @param changedFile
     * @return ChangedFile
     */
    public ChangedFile createChangedFile(ChangedFile changedFile){
        ChangedFile c;
        try {
            c = getChangedFile(changedFile.getFileName(), changedFile.getPackageName(),changedFile.getVersion().getId());
        } catch (NoResultException e) {
            c = null;
        }
        if (c == null) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(changedFile);
            em.getTransaction().commit();
            return changedFile;
        } else {
            return c;
        }
    }
    
    /**
     * 
     * Used by this
     * 
     * @param fileName
     * @param packageName
     * @param versionID
     * @return ChangedFile
     */
    public ChangedFile getChangedFile(String fileName, String packageName, long versionID) {
        em.getTransaction().begin();
        ChangedFile result = em.createQuery(
                    "select c from ChangedFile c where c.fileName = :fileName and c.packageName = :packageName and c.version.id = :versionid",
                    ChangedFile.class)
                    .setParameter("fileName", fileName)
                    .setParameter("packageName", packageName)
                    .setParameter("versionid",versionID)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by nothing
     * 
     * @param changedLine
     * @return 
     */
    public ChangedLine createChangedLine(ChangedLine changedLine){
        em.getTransaction().begin();
        em.persist(changedLine);
        em.getTransaction().commit();
        return changedLine;
    }
    
    /**
     * Used by SVNRepositoryExtractor
     * Used by GITRepositoryExtractor
     * 
     * @param changedLines
     * @return 
     */
    public List<ChangedLine> createChangedLines(List<ChangedLine> changedLines){
        em.getTransaction().begin();
        for (ChangedLine changedLine : changedLines) {
            em.persist(changedLine);
        }
        em.getTransaction().commit();
        return changedLines;
    }
    
    /**
     * Used by SVNRepositoryExtractor
     * Used by GITRepositoryExtractor
     * 
     * @param pack
     * @return Package
     */
    public Package createPackage(Package pack) {
        Package p;
        try {
            p = getPackage(pack.getPackageName());
        } catch (NoResultException e) {
            p = null;
        }
        
        if (p == null) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(pack);
            em.getTransaction().commit();
            return pack;
        } else {
            return p;
        }
    }
    
    /**
     * Used by ApiExtractor
     * 
     * @param packageName
     * @return Package
     */
    public Package getPackage(String packageName) {
    	Package result;
    	if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        try {
        	result = em.createQuery(
                    "select r from Package r where r.packageName = :packageName",
                    Package.class)
                    .setParameter("packageName", packageName)
                    .getSingleResult();
        } catch(NoResultException e){
        	return null;
        }
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used for corrections
     * 
     * @param revision
     * @param repositoryID 
     */
    public void deleteVersion(long revision, long repositoryID){
        //Version v = getVersion(revision, repositoryID);
        em.getTransaction().begin();
        int result1 =em.createQuery("delete from ChangedLine cl where cl.changedFile.version.revision=:revision and cl.changedFile.version.repository.id=:repositoryID")
                .setParameter("revision", revision)
                .setParameter("repositoryID", repositoryID)
                .executeUpdate();
        int result2 =em.createQuery("delete from ChangedFile cf where cf.version.revision=:revision and cf.version.repository.id=:repositoryID")
                .setParameter("revision", revision)
                .setParameter("repositoryID", repositoryID)
                .executeUpdate();
        int result3 = em.createQuery("delete from Version v where v.revision=:revision and v.repository.id=:repositoryID")
                .setParameter("revision", revision)
                .setParameter("repositoryID", repositoryID)
                .executeUpdate();
        System.out.println("ChangedLines: "+result1+" ChangedFiles: "+result2 + "Version: "+result3);
        //em.remove(v);
        em.getTransaction().commit();
    }
    
    /**
     * Used by ApiExtractor
     * 
     * @param api
     * @return 
     */
    public API createAPI(API api){
        API a;
        try {
            a = getAPI(api.getJarName(),api.getJarPath());
        } catch (NoResultException e) {
            a = null;
        }
        if (a == null) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(api);
            em.getTransaction().commit();
            return api;
        } else {
            return a;
        }
    }
    
    /**
     * Used by ApiExtractor
     * 
     * @param jarName
     * @param jarPath
     * @return 
     */
    public API getAPI(String jarName, String jarPath){
        em.getTransaction().begin();
        API result = em.createQuery(
                    "select a from API a where a.jarName = :jarName and a.jarPath = :jarPath",
                    API.class)
                    .setParameter("jarName", jarName)
                    .setParameter("jarPath", jarPath)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by ApiExtractor
     * 
     * @param classifier
     * @param pack 
     * @return 
     */
    public Classifier createClassifier(Classifier classifier, Package pack){
        Classifier c;
        try {
            c = getClassifier(classifier.getName(), pack.getId());
        } catch (NoResultException e) {
            c = null;
        }
        if(c==null){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(classifier);
            em.getTransaction().commit();
            classifier.setPack(pack);
            return c;
        } else {
            return c;
        }
    }
    
    public Classifier getClassifier(String classifier, Long packID){
        em.getTransaction().begin();
        Classifier result = em.createQuery(
                    "select c from Classifier c where c.name = :classifier and c.pack.id = :packID",
                    Classifier.class)
                    .setParameter("classifier", classifier)
                    .setParameter("packID", packID)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by ApiExtractor
     * 
     * @param methodEntity
     * @param pack 
     * @return 
     */
    public MethodEntity createMethodEntity(MethodEntity methodEntity, Package pack){
        MethodEntity m;
        try {
            m = getMethod(methodEntity.getName(), pack.getId());
        } catch (NoResultException e) {
            m = null;
        }
        if(m==null){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(methodEntity);
            em.getTransaction().commit();
            methodEntity.setPack(pack);
            return methodEntity;
        } else {
            return m;
        }
    }

    public MethodEntity getMethod(String methodName, Long packID){
        em.getTransaction().begin();
        MethodEntity result = em.createQuery(
                    "select m from MethodEntity m where m.name = :methodName and m.pack.id = :packID",
                    MethodEntity.class)
                    .setParameter("methodName", methodName)
                    .setParameter("packID", packID)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
     /**
     * Used by ApiExtractor
     * 
     * @param enumEntity 
     * @param classifier 
     * @param pack 
     * @return 
     */
    
    public EnumConstant createEnumEntity(EnumConstant enumEntity, Package pack){
        EnumConstant en;
        try {
            en = getEnum(enumEntity.getName(), pack.getId());
        } catch (NoResultException e) {
            en = null;
        }
        if(en==null){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.persist(enumEntity);
            em.getTransaction().commit();
            pack.getEnums().add(enumEntity);
            enumEntity.setPack(pack);
            return enumEntity;
        } else {
            return en;
        }
    }
    
    /**
     * Used by this
     * 
     * @param name
     * @param packID
     * @return
     */
    public EnumConstant getEnum(String name, Long packID){
        em.getTransaction().begin();
        EnumConstant result = em.createQuery("select e from EnumConstant e where e.name = :name and e.pack.id = :packID",
                    EnumConstant.class)
                    .setParameter("name", name)
                    .setParameter("packID", packID)
                    .getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    

    /**
     * Used by APIUsageExtractor
     * 
     * @param startID
     * @param endID
     * @return 
     */
    public List<ChangedLine> getChangedLines(int startID, int endID, int[] unqualified){
        em.getTransaction().begin();
        List<ChangedLine> result;
        if(unqualified==null){
        	result = em.createNativeQuery("select * from CHANGEDLINE where ID >= "+startID+" and ID <= "+endID+" and ID not in (select distinct CHANGEDLINES_ID from METHODENTITY_CHANGEDLINE where CHANGEDLINES_ID >= "+startID+" and CHANGEDLINES_ID <= "+endID+") order by CHANGEDFILE_ID", ChangedLine.class)
        			.getResultList();
        } else {
        	String s = "" + unqualified[0];
        	if(unqualified.length>1){
        		for (int i = 1; i < unqualified.length; i++) {
        			s += ", "+unqualified[i];
				}
        	}
        	result = em.createNativeQuery("select CHANGEDLINE.ID, CHANGEDLINE.CHANGE, CHANGEDLINE.TYPE, CHANGEDLINE.CHANGEDFILE_ID from CHANGEDLINE,CHANGEDFILE,VERSION where CHANGEDFILE.ID = CHANGEDLINE.CHANGEDFILE_ID and CHANGEDFILE.VERSION_ID = VERSION.ID and VERSION.REVISION not in ("+s+") and CHANGEDLINE.ID >= "+startID+" and CHANGEDLINE.ID <= "+endID+" and CHANGEDLINE.ID not in (select distinct CHANGEDLINES_ID from METHODENTITY_CHANGEDLINE where CHANGEDLINES_ID >= "+startID+" and CHANGEDLINES_ID <= "+endID+") order by CHANGEDFILE_ID", ChangedLine.class)
        			.getResultList();        	
        }
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param changedLine
     * @return 
     */
    public List<Package> getPackages(ChangedLine changedLine){
        em.getTransaction().begin();
        List<Package> result;
        result = em.createNativeQuery("select p.ID, p.PACKAGENAME, p.API_ID, p.DOMAIN_ID " +
                    "from PACKAGE p, PACKAGE_CHANGEDFILE pcf, CHANGEDFILE cf, CHANGEDLINE cl " +
                    "where pcf.PACKAGES_ID = p.ID " +
                    "and pcf.CHANGEDFILES_ID = cf.ID " +
                    "and cf.ID = cl.CHANGEDFILE_ID " +
                    "and cl.ID = "+changedLine.getId(), Package.class)
                .getResultList();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param pack
     * @return 
     */
    public List<MethodEntity> getMethods(Package pack) {
        em.getTransaction().begin();
        List<MethodEntity> result;
        result = em.createNativeQuery("select distinct m.ID, m.NAME, m.PACK_ID from METHODENTITY m where m.PACK_ID = "+pack.getId(), MethodEntity.class)
                .getResultList();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param pack
     * @return 
     */
    public List<EnumConstant> getEnums(Package pack) {
        em.getTransaction().begin();
        List<EnumConstant> result;
        result = em.createNativeQuery("select distinct m.ID, m.NAME, m.PACK_ID from ENUMCONSTANT m where m.PACK_ID = "+pack.getId(), EnumConstant.class)
                .getResultList();
        em.getTransaction().commit();
        return result;    
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param pack
     * @return 
     */
    public List<Classifier> getClasses(Package pack) {
        em.getTransaction().begin();
        List<Classifier> result;
        result = em.createQuery("select c from Classifier c where c.pack.id = "+pack.getId(), Classifier.class)
                .getResultList();
        em.getTransaction().commit();
        return result;    
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param listOfMethods
     * @param changedLineID 
     */
    public void createLineMethodRelation(Set<Long> listOfMethods, Long changedLineID) {
        if (listOfMethods.size()>0){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            Long[] tmpArray;
            tmpArray = new Long[listOfMethods.size()];
            tmpArray = listOfMethods.toArray(tmpArray);
            String values = "("+tmpArray[0]+","+changedLineID+")";
            for (int i = 1; i < tmpArray.length; i++) {
                values += ",("+tmpArray[i]+","+changedLineID+")";
            }
            em.createNativeQuery("insert into METHODENTITY_CHANGEDLINE (METHODS_ID,CHANGEDLINES_ID) values "+values).executeUpdate();
            em.getTransaction().commit();
        }
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param listOfClassifiers 
     * @param changedLineID 
     */
    public void createLineClassRelation(Set<Long> listOfClassifiers, Long changedLineID) {
        if (listOfClassifiers.size()>0){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            Long[] tmpArray;
            tmpArray = new Long[listOfClassifiers.size()];
            tmpArray = listOfClassifiers.toArray(tmpArray);
            String values = "("+changedLineID+","+tmpArray[0]+")";
            for (int i = 1; i < tmpArray.length; i++) {
                values += ",("+changedLineID+","+tmpArray[i]+")";
            }
            em.createNativeQuery("insert into CLASSIFIER_CHANGEDLINE (CHANGEDLINES_ID,CLASSIFIER_ID) values "+values).executeUpdate();
            em.getTransaction().commit();
        }
    }
    
    /**
     * Used by APIUsageExtractor
     * 
     * @param listOfEnums 
     * @param changedLineID 
     */
    public void createLineEnumRelation(Set<Long> listOfEnums, Long changedLineID) {
        if (listOfEnums.size()>0){
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            Long[] tmpArray;
            tmpArray = new Long[listOfEnums.size()];
            tmpArray = listOfEnums.toArray(tmpArray);
            String values = "("+changedLineID+","+tmpArray[0]+")";
            for (int i = 1; i < tmpArray.length; i++) {
                values += ",("+changedLineID+","+tmpArray[i]+")";
            }
            em.createNativeQuery("insert into ENUMCONSTANT_CHANGEDLINE (CHANGEDLINES_ID,ENUMS_ID) values "+values).executeUpdate();
            em.getTransaction().commit();
        }
    }
    
    /**
     * refreshs the updated persistence objects
     * 
     */
    public void refresh() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        em.getTransaction().commit();
    }
    
    /**
     * refreshs an entry
     * 
     * @param o
     */
    public void refresh(Object o){
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        em.persist(o);
        em.getTransaction().commit();
    }
    
    public void close() {
        em.close();
    }


    /**
     * Used by MetricExtractor
     * 
     * @param developerName
     * @return
     */
    public long getNumberOfVersion(String developerName){
       try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            Long result = (Long) em.createQuery("SELECT COUNT(v.id) FROM Version v WHERE v.developer = :developer GROUP BY v.developer")
                    .setParameter("developer", developerName)
                    .getSingleResult();
            em.getTransaction().commit();
            return result;
        } catch(NoResultException e) {
            return (long)0;
        }
    }
    
    /**
     * Used by MetricExtractor
     * 
     * @return
     */
    public long getNumberOfVersion(){
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            Long result = (Long) em.createQuery("SELECT COUNT(v.id) FROM Version v WHERE v.developer is null GROUP BY v.developer")
                    .getSingleResult();
            em.getTransaction().commit();
            return result;
        } catch(NoResultException e) {
            return (long)0;
        }
    }
    
    /**
     * Used by MetricExtractor
     * 
     * @return
     */
    public TreeSet<String> getPackages() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        List<String> result = em.createQuery("SELECT p.packageName FROM Package p",String.class)
                .getResultList();
        em.getTransaction().commit();
        TreeSet<String> set = new TreeSet<String>();
        set.addAll(result);
        return set;
    }

    /**
     * Used by PackageModifier
     * 
     * @return
     */
    public List<Package> getStaticPackageElements(){
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            List<Package> result = em.createNativeQuery("select * from PACKAGE where PACKAGE.PACKAGENAME like 'static%' order by PACKAGE.PACKAGENAME",Package.class)
                    .getResultList();
            em.getTransaction().commit();
            return result;
        } catch(NoResultException e) {
            return null;
        } catch(NonUniqueResultException e) {
            return null;
        }
    }
    
    /**
     * Used by PackageModifier
     * 
     * @param internalPackages
     */
    public void deleteInternalPackages(List<String> internalPackages) {
    	if(internalPackages.size() > 0) {
	    	String internals = "'"+internalPackages.get(0)+"%'";
	    	if(internalPackages.size() > 1){
	    		for (int i = 1;i < internalPackages.size();i++) {					
	    			internals += " or PACKAGE.PACKAGENAME like '" + internalPackages.get(i) + "%'";
				}
	    	}
	    	
	        if (!em.getTransaction().isActive()) {
	            em.getTransaction().begin();
	        }
	        em.createNativeQuery("delete from PACKAGE_CHANGEDFILE where PACKAGE_CHANGEDFILE.PACKAGES_ID in (select PACKAGE.ID from PACKAGE where PACKAGE.PACKAGENAME like " + internals+")").executeUpdate();
	        em.createNativeQuery("delete from PACKAGE where PACKAGENAME like " + internals).executeUpdate();
	        em.getTransaction().commit();
    	}
    }
    
    /**
     * Used by PackageModifier
     * 
     * @param pack
     */
    public void deletePackage(Package pack) {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        em.createNativeQuery("delete from PACKAGE_CHANGEDFILE where PACKAGE_CHANGEDFILE.PACKAGES_ID = " + pack.getId()).executeUpdate();
        em.createNativeQuery("delete from PACKAGE where PACKAGENAME = " + "'" + pack.getPackageName()+ "'").executeUpdate();
        em.getTransaction().commit();
	}
    
    /**
     * Used by PackageModifier
     * 
     * @param packageName
     * @param subfolder
     * @return
     */
    public List<Long> getPackage_ChangedFileIDs(String packageName, boolean subfolder) {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        List<Long> result;
        if(subfolder) {
            result = em.createNativeQuery("select distinct PACKAGE_CHANGEDFILE.CHANGEDFILES_ID from PACKAGE_CHANGEDFILE where PACKAGE_CHANGEDFILE.PACKAGES_ID in (select PACKAGE.ID from PACKAGE where PACKAGE.PACKAGENAME like '"+packageName+"%')").getResultList();
        } else {
        	result = em.createNativeQuery("select distinct PACKAGE_CHANGEDFILE.CHANGEDFILES_ID from PACKAGE_CHANGEDFILE where PACKAGE_CHANGEDFILE.PACKAGES_ID in (select PACKAGE.ID from PACKAGE where PACKAGE.PACKAGENAME = '"+packageName+"')").getResultList();
        }
        em.getTransaction().commit();
    	return result;
    }
    
    /**
     * Used by PackageModifier
     * 
     * @param changedFileIDs
     * @return
     */
    public List<ChangedFile> getChangedFile(List<Long> changedFileIDs) {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        List<ChangedFile> result;
        String queryPart = changedFileIDs.get(0).toString();
        for (int i = 1; i < changedFileIDs.size(); i++) {
			queryPart += ", "+ changedFileIDs.get(i).toString();
		}
        result = em.createNativeQuery("select * from CHANGEDFILE where CHANGEDFILE.ID in ("+queryPart+")",ChangedFile.class).getResultList();
        em.getTransaction().commit();
    	return result;
    }
    
    /**
     * Used by PackageModifier
     * 
     * @param packageName
     * @param subfolder
     * @return
     */
    public List<Package> getPackages(String packageName, boolean subfolder) {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        List<Package> result;
        result = em.createNativeQuery("select * from PACKAGE where PACKAGE.PACKAGENAME like '"+packageName+"%'",Package.class).getResultList();
        em.getTransaction().commit();
    	return result;
    }
    
    /**
     * Used by MetricExtractor
     * 
     * @param developer
     * @return
     */
	public int getNumberOfChangedFile(String developer) {
		try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            int result = (int) em.createNativeQuery("SELECT COUNT(cf.id) FROM VERSION v, CHANGEDFILE cf WHERE v.developer = '"+developer+"' and cf.VERSION_ID = v.ID GROUP BY v.developer")
                    .getSingleResult();
            em.getTransaction().commit();
            return result;
        } catch(NoResultException e) {
            return 0;
        }
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param developer
	 * @return
	 */
	public int getNumberOfChangedLine(String developer) {
		try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            int result = (int) em.createNativeQuery("SELECT COUNT(cl.id) FROM VERSION v, CHANGEDFILE cf, CHANGEDLINE cl WHERE v.developer = '"+developer+"' and cf.VERSION_ID = v.ID and cf.ID = cl.CHANGEDFILE_ID GROUP BY v.developer")
                    .getSingleResult();
            em.getTransaction().commit();
            return result;
        } catch(NoResultException e) {
            return 0;
        }
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param developer
	 * @param unqualified
	 * @return
	 */
    public long getNumberOfQualifiedVersion(String developer, int[] unqualified){
    	String unqual = "";
    	if(unqualified != null && unqualified.length>0){
    		unqual += unqualified[0]+"";
    		for (int i = 1; i < unqualified.length; i++) {
				unqual += ","+unqualified[i];
			}
    	}
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        long result= (Long) em.createQuery("select count(distinct v.id) from Version v, ChangedFile cf where v.developer is not null and v.developer='"+developer+"' and v.id = cf.version.id and v.revision not in ("+unqual+")").getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by MetricExtractor
     * 
     * @param developer
     * @param unqualified
     * @return
     */
    public long getNumberOfQualifiedChangedFile(String developer, int[] unqualified){
    	String unqual = "";
    	if(unqualified != null && unqualified.length>0){
    		unqual += unqualified[0]+"";
    		for (int i = 1; i < unqualified.length; i++) {
				unqual += ","+unqualified[i];
			}
    	}
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        long result= (Long) em.createQuery("select count(cf.id) from Version v, ChangedFile cf where v.developer is not null and v.developer='"+developer+"' and v.id = cf.version.id and v.revision not in ("+unqual+")").getSingleResult();
        em.getTransaction().commit();
        return result;
    }
    
    /**
     * Used by MetricExtractor
     * 
     * @param developer
     * @param unqualified
     * @return
     */
    public long getNumberOfQualifiedChangedLine(String developer,int[] unqualified){
    	String unqual = "";
    	if(unqualified != null && unqualified.length>0){
    		unqual += unqualified[0]+"";
    		for (int i = 1; i < unqualified.length; i++) {
				unqual += ","+unqualified[i];
			}
    	}
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        long result= (Long) em.createQuery("select count(cl.id) from Version v, ChangedFile cf, ChangedLine cl where v.developer is not null and cl.changedFile.id = cf.id and v.developer='"+developer+"' and v.id = cf.version.id and v.revision not in ("+unqual+")").getSingleResult();
        em.getTransaction().commit();
        return result;
    }

    /**
     * Used by MetricExtractor
     * 
     * @return
     */
	public List<String> getDeveloperSortedByNumberOfVersion() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        List<String> result= em.createNativeQuery("select DEVELOPER from VERSION group by DEVELOPER order by count(ID) desc").getResultList();
        em.getTransaction().commit();
        return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @return
	 */
	public Domain addDomain(Domain domain) {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        em.persist(domain);
        em.getTransaction().commit();
        return domain;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @return
	 */
	public List<String> getAPISortedByDomain() {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result= em.createNativeQuery("select PACKAGE.PACKAGENAME from PACKAGE,DOMAIN where PACKAGE.DOMAIN_ID = DOMAIN.ID order by DOMAIN.DOMAIN, PACKAGE.PACKAGENAME").getResultList();
		em.getTransaction().commit();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @return
	 */
	public List<String> getAPIs(String domain) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result= em.createNativeQuery("select PACKAGE.PACKAGENAME from PACKAGE,DOMAIN where PACKAGE.DOMAIN_ID = DOMAIN.ID and DOMAIN.DOMAIN = '"+domain+"' order by PACKAGE.PACKAGENAME").getResultList();
		em.getTransaction().commit();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiReferences(String api, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();			
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @return
	 */
	public int getApiReferences(String api) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param dev
	 * @return
	 */
	public int getApiReferencesDeveloper(String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @return
	 */
	public int getApiReferences() {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int)em.createNativeQuery("select count(PACKAGE.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @param dev
	 * @return
	 */
	public int getDomainReferences(String domain, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int)em.createNativeQuery("select count(PACKAGE.ID) from DOMAIN, PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and VERSION.DEVELOPER = '"+dev+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();			
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int)em.createNativeQuery("select count(PACKAGE.ID) from DOMAIN, PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and VERSION.DEVELOPER = '"+dev+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int)em.createNativeQuery("select count(PACKAGE.ID) from DOMAIN, PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and VERSION.DEVELOPER = '"+dev+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @return
	 */
	public int getDomainReferences(String domain) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(DOMAIN.ID) from DOMAIN, PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(DOMAIN.ID) from DOMAIN, PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(DOMAIN.ID) from DOMAIN, PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiElements(String api, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @return
	 */
	public int getApiElements(String api) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.PACKAGENAME = '"+api+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by PACKAGE.PACKAGENAME").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param dev
	 * @return
	 */
	public int getApiElementsDeveloper(String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @return
	 */
	public int getApiElements() {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @return
	 */
	public List<String> getDomainSortedByName() {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result= em.createNativeQuery("select DOMAIN.DOMAiN from DOMAIN order by DOMAIN.DOMAIN").getResultList();
		em.getTransaction().commit();
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @param dev
	 * @return
	 */
	public int getDomainElements(String domain, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from DOMAIN, PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from DOMAIN, PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from DOMAIN, PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and VERSION.DEVELOPER = '"+dev+"' and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}

	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @return
	 */
	public int getDomainElements(String domain) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		int result1 = 0;
		int result2 = 0;
		int result3 = 0;
		try{
			result1 = (int) em.createNativeQuery("select count(distinct CLASSIFIER.ID) from DOMAIN, PACKAGE, CLASSIFIER, CLASSIFIER_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = CLASSIFIER.PACK_ID and CLASSIFIER.ID = CLASSIFIER_CHANGEDLINE.CLASSIFIER_ID and CLASSIFIER_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result2 = (int) em.createNativeQuery("select count(distinct METHODENTITY.ID) from DOMAIN, PACKAGE, METHODENTITY, METHODENTITY_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = METHODENTITY.PACK_ID and METHODENTITY.ID = METHODENTITY_CHANGEDLINE.METHODS_ID and METHODENTITY_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		try{
			result3 = (int) em.createNativeQuery("select count(distinct ENUMCONSTANT.ID) from DOMAIN, PACKAGE, ENUMCONSTANT, ENUMCONSTANT_CHANGEDLINE, CHANGEDLINE, CHANGEDFILE, VERSION where DOMAIN.DOMAIN = '"+domain+"' and DOMAIN.ID = PACKAGE.DOMAIN_ID and PACKAGE.ID = ENUMCONSTANT.PACK_ID and ENUMCONSTANT.ID = ENUMCONSTANT_CHANGEDLINE.ENUMS_ID and ENUMCONSTANT_CHANGEDLINE.CHANGEDLINES_ID = CHANGEDLINE.ID and CHANGEDLINE.CHANGEDFILE_ID = CHANGEDFILE.ID and CHANGEDFILE.VERSION_ID = VERSION.ID group by DOMAIN.DOMAIN").getSingleResult();
		} catch(NoResultException ex) {
		}
		em.getTransaction().commit();
		int result = result1 + result2 + result3;
		return result;
	}
	
	
	/*
	  
	  Unit Codes (Anzahl der Referenzen in Dateien) pro Domain
		SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.FILENAME;
		SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.FILENAME;
		SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.FILENAME;
		
	  Unit Codes (Anzahl der Referenzen in Dateien) pro API
		SELECT cf.FILENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.FILENAME;
		SELECT cf.FILENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.FILENAME;
		SELECT cf.FILENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.FILENAME;
		
		
		
	  Package References pro Domain
		SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.PACKAGENAME;
		SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.PACKAGENAME;
		SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = 'GUI' and d.ID = p.DOMAIN_ID and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY d.DOMAIN, cf.PACKAGENAME;
		
	  Package References pro API
		SELECT cf.PACKAGENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.PACKAGENAME;
		SELECT cf.PACKAGENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.PACKAGENAME;
		SELECT cf.PACKAGENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = 'java.awt' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = 'nathan.sweet' GROUP BY p.PACKAGENAME, cf.PACKAGENAME;
	 
	 
	 
	 
	 */
  
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @param dev
	 * @return
	 */
	public int getDomainReferencesInFiles(String domain, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME, cf.FILENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.FILENAME FROM DOMAIN d, PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiReferencesInFiles(String api, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param domain
	 * @param dev
	 * @return
	 */
	public int getDomainReferencesInPackages(String domain, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM DOMAIN d, PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE d.DOMAIN = '"+domain+"' and d.ID = p.DOMAIN_ID and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY d.DOMAIN, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiReferencesInPackages(String api, String dev) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf, VERSION v WHERE p.PACKAGENAME = '"+api+"' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID and cf.VERSION_ID = v.ID and v.DEVELOPER = '"+dev+"' GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiReferencesInFiles(String api) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.FILENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME, cf.FILENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}
	
	/**
	 * Used by MetricExtractor
	 * 
	 * @param api
	 * @param dev
	 * @return
	 */
	public int getApiReferencesInPackages(String api) {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
		List<String> result1;
		List<String> result2;
		List<String> result3;
		try{
			result1 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, CLASSIFIER c, CLASSIFIER_CHANGEDLINE ccl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = c.PACK_ID and c.ID = ccl.CLASSIFIER_ID and ccl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();			
		} catch(NoResultException ex) {
			result1 = new LinkedList<>();
		}
		try{
			result2 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, METHODENTITY m, METHODENTITY_CHANGEDLINE mcl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = m.PACK_ID and m.ID = mcl.METHODS_ID and mcl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result2 = new LinkedList<>();
		}
		try{
			result3 = em.createNativeQuery("SELECT cf.PACKAGENAME FROM PACKAGE p, ENUMCONSTANT e, ENUMCONSTANT_CHANGEDLINE ecl, CHANGEDLINE cl, CHANGEDFILE cf WHERE p.PACKAGENAME = '"+api+"' and p.ID = e.PACK_ID and e.ID = ecl.ENUMS_ID and ecl.CHANGEDLINES_ID = cl.ID and cl.CHANGEDFILE_ID = cf.ID GROUP BY p.PACKAGENAME, cf.PACKAGENAME").getResultList();
		} catch(NoResultException ex) {
			result3 = new LinkedList<>();
		}
		em.getTransaction().commit();
		int result = result1.size() + result2.size() + result3.size();
		return result;
	}


}
    