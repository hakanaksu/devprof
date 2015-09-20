package org.softlang.devprof.extractor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.softlang.devprof.persistence.entities.ChangedFile;
import org.softlang.devprof.persistence.entities.ChangedLine;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.entities.Repository;
import org.softlang.devprof.persistence.entities.Version;
import org.softlang.devprof.persistence.logic.PersistenceHandler;

/**
 * 
 * @author Hakan Aksu
 *
 */
public class GITRepositoryExtractor extends RepositoryExtractor {

	String LOCALPATH;
	String TMPPATH;
	
	private PersistenceHandler persistenceHandler;
	
	public GITRepositoryExtractor(String url, String username, String password) {
		super(url, username, password);
		persistenceHandler = new PersistenceHandler();
		LOCALPATH = System.getProperty("user.home")+"/GitRepos/";			//user.home works definitely on MAC OS
		TMPPATH = System.getProperty("user.home")+"/GitRepos/tmp/";
	}

	
	@Override
	public Repository extractData() {
		
		// create folder 
		File destinationFile = createNewFolder(LOCALPATH + this.getUrl().substring(this.getUrl().lastIndexOf("/")+1,this.getUrl().lastIndexOf(".")));
		File tmpFile = createNewFolder(TMPPATH + this.getUrl().substring(this.getUrl().lastIndexOf("/")+1, this.getUrl().lastIndexOf(".")));
		//tmpFile.deleteOnExit();
		
		// clone repository 
		cloneRepo(this.getUrl(), destinationFile);
		
		// Open cloned repository		 
		Git git = openGit(destinationFile);
		
		// create REPOSITORY
		Repository repository = persistenceHandler.createRepository(buildRepository());
		
		// get all VERSIONS (log is ordered by reversed revision number)
		Iterable<RevCommit> log = getLogs(git);
		
		
		
		// count VERSIONS 
		int revision = 0;
		for (RevCommit revCommit : log) {
			revision++;
		}
		log = getLogs(git);
		// iterate over all VERSIONS
		for (RevCommit revCommit : log) {
			System.out.println("Version: "+ revision);
			if (revision > 434){
				revision--;
				continue;
			}   //3735 is unqualified (too much data)
				//2195 is unqualified (too much data)
			    //684 is unqualified (too much data)
				//54 is unqualfied (too much move of data)
			if(revision == -1)
				break;
			// create VERSION
			// Version
			Version version = persistenceHandler.createVersion(buildVersion(revCommit,repository,revision));
			repository.getVersions().add(version);
			
			// checkout revision
			checkoutRevision(git,revCommit);

			Set<String> projectPackagesWhichContainsJavaFiles = getProjectPackagesWhichContainsJavaFiles(destinationFile);

			// get diff
			OutputStream out = getDiff(git, revCommit);
				
			// write diff to file
			File diffFile = writeToFile(tmpFile.getAbsolutePath(),"/diff_"+revision+".txt",out);
			//System.out.println(diffFile.getName());
			
			// get CHANGEDFILES, CHANGEDLINES and PACKAGES from diff
			diffAnalyzer(destinationFile, diffFile, version, projectPackagesWhichContainsJavaFiles);
			revision--;		
		}
		
		
		//return null;
		return repository;
	}


	private Iterable<RevCommit> getLogs(Git git) {		
		Iterable<RevCommit> log = null;
		try {
			log = git.log().call();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return log;
	}


	private File createNewFolder(String path) {
		File file = new File(path);
		if(file.exists() && !file.delete()) {
			System.err.println("Please delete "+file.getAbsolutePath()+" before starting the Extraction");
			System.exit(0);
		}
		file.mkdirs();
		return file;
	}

	private void cloneRepo(String cloneURL, File destionationFile) {
		try {
			Git.cloneRepository()
				.setURI(cloneURL)
				.setDirectory(destionationFile)
				.call();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Git openGit(File destinationFile) {
		Git git = null;
		try {
			git = Git.open(destinationFile);
		} catch (IOException e) {
			System.err.println("problems with Git.open()");
			e.printStackTrace();
		}
		return git;
	}

	private void checkoutRevision(Git git, RevCommit revCommit) {
		try {
			git.checkout().setCreateBranch(true).setName(revCommit.name()).setStartPoint(revCommit).call();
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CheckoutConflictException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Set<String> getProjectPackagesWhichContainsJavaFiles(File file) {
		Set<String> result = new HashSet<>();
		if(file.isDirectory() && file.listFiles().length == 0)
			return result;
		if(file.isDirectory() && file.listFiles().length > 0)
			for(File f:file.listFiles())
				if(f.isDirectory())
					result.addAll(getProjectPackagesWhichContainsJavaFiles(f));
				else
					if(f.getPath().endsWith(".java"))
						result.add(file.getPath().replace('/', '.'));
		return result;
	}
	
	private OutputStream getDiff(Git git, RevCommit revCommit) {
		OutputStream out = new ByteArrayOutputStream();
		ObjectReader reader = git.getRepository().newObjectReader();
		if(revCommit.getParentCount()>0){
			try {
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				oldTreeIter.reset(reader, revCommit.getParent(revCommit.getParentCount()-1).getTree());
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				newTreeIter.reset(reader, revCommit.getTree());
				//List<DiffEntry> diffs= 
						git.diff()
						.setContextLines(0)
						.setOutputStream(out)
						.setNewTree(newTreeIter)
						.setOldTree(oldTreeIter)
						.call();
			} catch (IncorrectObjectTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out;
	}
		
	private File writeToFile(String folder, String fileName, OutputStream out) {
		File dir = new File(folder);
		dir.mkdirs();
		File file = new File(folder+fileName);
		try {
			if(file.exists()) {
				file.delete();
				file.createNewFile();
			}
			FileWriter fw;
			fw = new FileWriter(file);
			fw.write(out.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//file.deleteOnExit();
		return file;
	}
	
	private void diffAnalyzer(File destinationFile, File diffFile, Version version, Set<String> projectPackagesWhichContainsJavaFiles) {
		ChangedFile changedFile = null;
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(diffFile)));
			
			while(reader.ready()){
				 
				String line = reader.readLine();
				
				if(line.startsWith("---")) {
					// new ChangedFile
					// read line beginning with "+++"
					String line2 = reader.readLine();
					String before = line.substring(5);
					String after = line2.substring(5);
					if (!before.endsWith(".java") && !after.endsWith(".java")){
						changedFile = null;
						break;
					}
					// A D C M or R
					String path = after;
					Character type;
					//Added File
					if (before.equals("dev/null")) {
						type = 'A';
						System.out.println(type + "   " +after);
					} else {
						//Deleted File
						if (after.equals("dev/null")){
							path = before;
							type = 'D';
							System.out.println(type + "   " +before);
						} else {
							// Changed File
							if (after.equals(before)) {
								type = 'C';
								System.out.println(type + "   " +after);
							} else {
								// Renamed File
								if(before.substring(0,before.lastIndexOf("/")).equals(after.substring(0, after.lastIndexOf("/")))) {
									type = 'R';
									System.out.println(type + "   " +before + "  " + after);
								} else {
									//Moved File
									type = 'M';
								}
							}
						}
					}
					changedFile = persistenceHandler.createChangedFile(buildChangedFile(version,path,type));
					version.getChangedFiles().add(changedFile);
					// identify Packages
					//projectPackagesWhichContainsJavaFiles - Package Tree of java Files
					if(!changedFile.getType().equals('D')) {
	                    //Package
	                    Set<Package> packages = buildPackages(destinationFile.getPath()+path, projectPackagesWhichContainsJavaFiles);
	                    for (Package package1 : packages) {
	                        package1.getChangedFiles().add(changedFile);
	                    }
	                    changedFile.getPackages().addAll(packages);
					}
				} else {
					if(changedFile != null){
						String change;
						Character type;
						ChangedLine changedLine = null;
						if(line.startsWith("+") && !changedFile.getType().equals('D')) {
							// Added ChangedLine
							change = line.substring(1);
							type = 'A';
							changedLine = buildChangedLine(change, type, changedFile);
							if(changedLine != null)
								persistenceHandler.createChangedLine(changedLine);
						} else if(line.startsWith("-") && !changedFile.getType().equals('D')){
							// Deleted ChangedLine
							change = line.substring(1);
							type = 'D';
							changedLine = buildChangedLine(change, type, changedFile);
							if(changedLine != null)
								persistenceHandler.createChangedLine(changedLine);
						}
					}
				}
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private Repository buildRepository() {
        return new Repository(super.getUrl());
    }

    private Version buildVersion(RevCommit revCommit, Repository repository, long revision) {
        
		PersonIdent user = revCommit.getCommitterIdent();
		
    	String message = revCommit.getFullMessage();
        if (message != null && message.length()>255) {
            message = message.substring(0, 254);
        }
        return new Version(revision, 
        		user.getName(), 
                message, 
                user.getWhen(), 
                repository);
    }
    
    private ChangedFile buildChangedFile(Version version, String path, Character type) {
        return new ChangedFile(path.substring(1, path.lastIndexOf("/")+1),
                path.substring(path.lastIndexOf("/")+1), 
                type,
                version
                );
    }
    
    private Set<Package> buildPackages(String path, Set<String> projectPackagesWhichContainsJavaFiles){
    	Set<Package> result = new LinkedHashSet<Package>();
        PackageNameExtractor pnExtractor = new PackageNameExtractor(path);
        Set<String> packageNames = pnExtractor.getFullPackageNamesWithoutClassNames();
        Package pack;
        for (String string : packageNames) {
            Pattern p = Pattern.compile(".*"+string+".*");
            boolean flag = false;
            for (String string2 : projectPackagesWhichContainsJavaFiles) {
                Matcher m = p.matcher(string2);
                if (m.find()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                pack = new Package(string);
                pack = persistenceHandler.createPackage(pack);
                result.add(pack);
            }
        }
        return result;
    }
    
    private ChangedLine buildChangedLine(String change1, Character type, ChangedFile changedFile) {
        String change = change1.trim();
    	if (change != null && !change.equals("")
                && !change.equals("/*")
                && !change.equals("*/")
                && !change.equals("*")
                && !change.equals("/**")
                && !change.equals("**/")
                && !change.equals("}")
                && !change.equals("{")
                && !change.equals("(")
                &&!change.equals(")")) {
            if (change.length() > 255) {
                change = change.substring(0, 254);
            }
            ChangedLine changedLine = new ChangedLine(change.trim(), type, changedFile);
            changedFile.getChangedLines().add(changedLine);
            return changedLine;
        }
        return null;
    }

}
