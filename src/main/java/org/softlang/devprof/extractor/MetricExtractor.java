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
public class MetricExtractor extends Extractor<Boolean>{

    private PersistenceHandler persistenceHandler;
    
    private static final String seperator=";";
    
    private int[] unqualifiedRevisions;
    
    public MetricExtractor() {
    	persistenceHandler = new PersistenceHandler();
    	File file1 = new File("result/metrics");
    	File file2 = new File("result/resource");
    	file1.mkdirs();
    	file2.mkdirs();
    }
    
    public MetricExtractor(int[] unqualifiedRevisions) {
        persistenceHandler = new PersistenceHandler();
        File file1 = new File("result/metrics");
        File file2 = new File("result/resource");
        file1.mkdirs();
        file2.mkdirs();
        this.unqualifiedRevisions = unqualifiedRevisions;
    }
    
    @Override
    public Boolean extractData() {
        
    	createRepositoryMetrics();
    	int n = 15; //the first 15 developer Sorted by Number of Version
    	createAPIMetrics(n);
    	createDomainMetrics(n);
    	createDistinctAPIElements(n);
    	createDistinctDomainElements(n);
    	createApiExperienceScore(n);    	
    	
        return true;
    }


	private void createRepositoryMetrics(){
    	File file = new File("result/metrics/repoMetrics");
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
            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
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
    			File file = new File(filepath+"arMetrics");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("API"+seperator+"AR(API,DEV)"+seperator+"AR(API)"+"\n");
    			
    			List<String> apis = persistenceHandler.getAPISortedByDomain();
    			for (int j = 0; j < apis.size(); j++) {
    				int arFromApiDev = persistenceHandler.getApiReferences(apis.get(j),developers.get(i));
    				int arFromApi = persistenceHandler.getApiReferences(apis.get(j));
					fw.write(apis.get(j)+seperator+arFromApiDev+seperator+arFromApi+"\n");
				}
    			int arFromDev = persistenceHandler.getApiReferencesDeveloper(developers.get(i));
    			int ar = persistenceHandler.getApiReferences();
    			fw.write("SUM(arDEV - ar)"+seperator+arFromDev+seperator+ar+"\n");    			
            
            	fw.close();
	        } catch (FileNotFoundException ex) {
	            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (IOException ex) {
	            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
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
    			File file = new File(filepath+"drMetrics");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("Domain"+seperator+"DR(Domain,DEV)"+seperator+"DR(Domain)"+"\n");
    			
    			List<String> apis = persistenceHandler.getDomainSortedByName();
    			for (int j = 0; j < apis.size(); j++) {
    				int drFromApiDev = persistenceHandler.getDomainReferences(apis.get(j),developers.get(i));
    				int drFromApi = persistenceHandler.getDomainReferences(apis.get(j));
    				fw.write(apis.get(j)+seperator+drFromApiDev+seperator+drFromApi+"\n");
    			}
    			int arFromDev = persistenceHandler.getApiReferencesDeveloper(developers.get(i));
    			int ar = persistenceHandler.getApiReferences();
    			fw.write("SUM(drDEV - dr)"+seperator+arFromDev+seperator+ar+"\n");    			
    			
    			fw.close();
    		} catch (FileNotFoundException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		} catch (IOException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		}
    	}
    }
    
    private void createDistinctAPIElements(int n) {
    	List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
    	for (int i = 0; i < developers.size(); i++) {
    		if(i==(n+1))
    			break;
    		try {
    			String filepath = "result/metrics/"+developers.get(i)+"/";
    			File path = new File(filepath);
    			path.mkdirs();
    			File file = new File(filepath+"aeMetrics");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("Domain"+seperator+"AE(API,DEV)"+seperator+"AE(API)"+"\n");
    			
    			List<String> apis = persistenceHandler.getAPISortedByDomain();
    			for (int j = 0; j < apis.size(); j++) {
    				int aeFromApiDev = persistenceHandler.getApiElements(apis.get(j),developers.get(i));
    				int aeFromApi = persistenceHandler.getApiElements(apis.get(j));
    				fw.write(apis.get(j)+seperator+aeFromApiDev+seperator+aeFromApi+"\n");
    			}
    			int aeFromDev = persistenceHandler.getApiElementsDeveloper(developers.get(i));
    			int ae = persistenceHandler.getApiElements();
    			fw.write("aeDEV / ae"+seperator+aeFromDev+seperator+ae+"\n");    			
    			
    			fw.close();
    		} catch (FileNotFoundException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		} catch (IOException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		}
    	}
	}
    
    private void createDistinctDomainElements(int n) {
    	List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
    	for (int i = 0; i < developers.size(); i++) {
    		if(i==(n+1))
    			break;
    		try {
    			String filepath = "result/metrics/"+developers.get(i)+"/";
    			File path = new File(filepath);
    			path.mkdirs();
    			File file = new File(filepath+"deMetrics");
    			file.createNewFile();
    			FileWriter fw = new FileWriter(file, false);
    			fw.write("Domain"+seperator+"DE(Domain,DEV)"+seperator+"DE(Domain)"+"\n");
    			
    			List<String> apis = persistenceHandler.getDomainSortedByName();
    			for (int j = 0; j < apis.size(); j++) {
    				int deFromApiDev = persistenceHandler.getDomainElements(apis.get(j),developers.get(i));
    				int deFromApi = persistenceHandler.getDomainElements(apis.get(j));
    				fw.write(apis.get(j)+seperator+deFromApiDev+seperator+deFromApi+"\n");
    			}
    			int aeFromDev = persistenceHandler.getApiElementsDeveloper(developers.get(i));
    			int ae = persistenceHandler.getApiElements();
    			fw.write("deDEV / de"+seperator+aeFromDev+seperator+ae+"\n");    			
    			
    			fw.close();
    		} catch (FileNotFoundException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		} catch (IOException ex) {
    			Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
    		}
    	}
    }
    
	private void createApiExperienceScore(int n) {
    	File file = new File("result/metrics/experienceScore");
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, false);
            List<String> developers = persistenceHandler.getDeveloperSortedByNumberOfVersion();
            fw.write("api/developer");
            for (int i = 0; i < developers.size(); i++) {
            	if(i==n) break;
            	fw.write(seperator+developers.get(i));
            }
            	fw.write("\n");
            List<String> apis = persistenceHandler.getAPISortedByDomain();
            for (String api : apis) {
            	fw.write(api);
				int daeALL = persistenceHandler.getApiElements(api);
				int arALL = persistenceHandler.getApiReferences(api);
            	for (int i = 0; i < developers.size(); i++) {
            		if(i==n) break;
            		String developer = developers.get(i);
            		int daeDEV = persistenceHandler.getApiElements(api, developer);
            		int arDEV = persistenceHandler.getApiReferences(api, developer);
            		int score =(int)(( ((double)daeDEV/(double)daeALL) + ((double)arDEV/(double)arALL) ) * 50);
            		if(api.equals("android.text") && developer.equals("badlogicgames"))
            			System.out.println(daeALL +" "+arALL+ " " + daeDEV +" " + arDEV + " "+ score);
            		fw.write(seperator+score);
            	}
            	fw.write("\n");
			}
            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetricExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
    
}
