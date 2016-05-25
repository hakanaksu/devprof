package org.softlang.devprof.extractor;

import org.softlang.devprof.persistence.logic.PersistenceHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hakan Aksu
 */
public class MetricExtractor3 extends Extractor<Boolean>{

    private PersistenceHandler persistenceHandler;
    
    private static final String seperator=";";
    
    private int[] unqualifiedRevisions;
    
    public MetricExtractor3() {
    	persistenceHandler = new PersistenceHandler();
    	File file1 = new File("result/metrics");
    	file1.mkdirs();
    }
    
    public MetricExtractor3(int[] unqualifiedRevisions) {
        persistenceHandler = new PersistenceHandler();
        File file1 = new File("result/metrics");
        file1.mkdirs();
        this.unqualifiedRevisions = unqualifiedRevisions;
    }
    
    @Override
    public Boolean extractData() {
        
    	createRepositoryMetrics();
    	createAPIUsageinProject();
    	int n = 8; //the first 15 developer Sorted by Number of Version
    	createAPIMetrics(n);
    	createDomainMetrics(n);
    	createApiExperienceScore(n);
        return true;
    }


	private void createAPIUsageinProject() {
    	File file = new File("result/metrics/SysemInfo.csv");
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            fw.write("DOMAIN"+seperator+"REFS"+"\n");
            List<String> domains = persistenceHandler.getDomainSortedByName();
            String result = "";
            for (String domain : domains) {
            	result += domain;
				List<String> apis = persistenceHandler.getAPIs(domain);
				for (String api : apis) {
					result += seperator + api;
				}
				result += "\n";
			}            	
            fw.write(result+"\n\n"+"Domain"+seperator+"DRefs"+"\n");

            for (String domain : domains) {
				int dRefs = persistenceHandler.getDomainReferences(domain);
				fw.write(domain + seperator + dRefs + "\n");
			}
            fw.write("\n\n\nAPI"+seperator+"ARefs"+"\n");
            List<String> apis = persistenceHandler.getAPISortedByDomain();
            for (String api : apis) {
				int arefs = persistenceHandler.getApiReferences(api);
				fw.write(api+seperator+arefs+"\n");
			}

            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        }		
	}

	private void createRepositoryMetrics(){
    	File file = new File("result/metrics/repoMetrics.csv");
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            fw.write("developer"+seperator+"VER"+seperator+"CF"+seperator+"CL"+seperator+"QVER"+seperator+"QCF"+seperator+"QCL"+"\n");
            List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
            
            for (int i = 0; i < developers.size(); i++) {
				String developer = developers.get(i);
            	long ver = persistenceHandler.getNumberOfVersion(developer);
            	int cf = persistenceHandler.getNumberOfChangedFile(developer);
            	int cl = persistenceHandler.getNumberOfChangedLine(developer);
            	long qver = persistenceHandler.getNumberOfQualifiedVersion(developer, unqualifiedRevisions);
            	long qcf = persistenceHandler.getNumberOfQualifiedChangedFile(developer, unqualifiedRevisions);
            	long qcl = persistenceHandler.getNumberOfQualifiedChangedLine(developer, unqualifiedRevisions);
            	fw.write(developer+seperator+ver+seperator+cf+seperator+cl+seperator+qver+seperator+qcf+seperator+qcl+"\n");
			}
            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createAPIMetrics(int n){
    	List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
    	for (int i = 0; i < developers.size(); i++) {
    		if(i==(n+1))
    			break;
    		try {
    			String filepath = "result/metrics/"+developers.get(i)+"/";
    			File path = new File(filepath);
    			path.mkdirs();
    			File file = new File(filepath+"aMetrics.csv");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("API"+seperator+"APR"+seperator+"AFR"+seperator+"AR"+seperator+"AE"+"\n");
    			
    			List<String> apis = persistenceHandler.getAPISortedByDomain();
    			for (int j = 0; j < apis.size(); j++) {
    				int arInPackages = persistenceHandler.getApiReferencesInPackages(apis.get(j), developers.get(i));
    				int arInFiles = persistenceHandler.getApiReferencesInFiles(apis.get(j), developers.get(i));
    				int arFromApiDev = persistenceHandler.getApiReferences(apis.get(j),developers.get(i));
    				int aeFromApiDev = persistenceHandler.getApiElements(apis.get(j),developers.get(i));
					fw.write(apis.get(j)+seperator+arInPackages+seperator+arInFiles+seperator+arFromApiDev+seperator+aeFromApiDev+"\n");
				}  			
            	fw.close();
	        } catch (FileNotFoundException ex) {
	            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (IOException ex) {
	            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
	        }
    	}
    }
    
    private void createDomainMetrics(int n){
    	List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
    	for (int i = 0; i < developers.size(); i++) {
    		if(i==(n+1))
    			break;
    		try {
    			String filepath = "result/metrics/"+developers.get(i)+"/";
    			File path = new File(filepath);
    			path.mkdirs();
    			File file = new File(filepath+"dMetrics.csv");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("Domain"+seperator+"DPR"+seperator+"DFR"+seperator+"DR"+seperator+"DE"+"\n");
    			
    			List<String> apis = persistenceHandler.getDomainSortedByName();
    			for (int j = 0; j < apis.size(); j++) {
    				int drInPackages = persistenceHandler.getDomainReferencesInPackages(apis.get(j), developers.get(i));
    				int drInFiles = persistenceHandler.getDomainReferencesInFiles(apis.get(j), developers.get(i));
    				int drFromApiDev = persistenceHandler.getDomainReferences(apis.get(j),developers.get(i));
    				int deFromApiDev = persistenceHandler.getDomainElements(apis.get(j),developers.get(i));
    				fw.write(apis.get(j)+seperator+drInPackages+seperator+drInFiles+seperator+drFromApiDev+seperator+deFromApiDev+"\n");
    			}
    			fw.close();
    		} catch (FileNotFoundException ex) {
    			Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
    		} catch (IOException ex) {
    			Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
    		}
    	}
    }
    
	private void createApiExperienceScore(int n) {
    	File file = new File("result/metrics/experienceScore.csv");
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
            fw.write("api/developer");
            for (int i = 0; i < developers.size(); i++) {
            	if(i==n+1) break;
            	fw.write(seperator+developers.get(i));
            }
            	fw.write("\n");
            List<String> apis = persistenceHandler.getAPISortedByDomain();
            for (String api : apis) {
            	fw.write(api);
				int daeALL = persistenceHandler.getApiElements(api);
				int arALL = persistenceHandler.getApiReferences(api);
            	int aprALL = persistenceHandler.getApiReferencesInPackages(api);
            	int afrALL = persistenceHandler.getApiReferencesInFiles(api);
				for (int i = 0; i < developers.size(); i++) {
            		if(i==n+1) break;
            		String developer = developers.get(i);
            		int daeDEV = persistenceHandler.getApiElements(api, developer);
            		int arDEV = persistenceHandler.getApiReferences(api, developer);
            		int aprDEV = persistenceHandler.getApiReferencesInPackages(api, developer);
            		int afrDEV = persistenceHandler.getApiReferencesInFiles(api, developer);
            		int score =(int)(( ((double)daeDEV/(double)daeALL) + ((double)arDEV/(double)arALL) + ((double)aprDEV/(double)aprALL) + ((double)afrDEV/(double)afrALL)) * 25);
            		fw.write(seperator+score);
            	}
            	fw.write("\n");
			}
            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExtractor3.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
    
}
