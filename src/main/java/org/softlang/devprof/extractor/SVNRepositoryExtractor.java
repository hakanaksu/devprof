package org.softlang.devprof.extractor;

import org.softlang.devprof.persistence.entities.ChangedFile;
import org.softlang.devprof.persistence.entities.ChangedLine;
import org.softlang.devprof.persistence.entities.Package;
import org.softlang.devprof.persistence.entities.Repository;
import org.softlang.devprof.persistence.entities.Version;
import org.softlang.devprof.persistence.logic.PersistenceHandler;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.wc2.ng.SvnDiffGenerator;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

/**
 *
 * @author Hakan Aksu
 */
public class SVNRepositoryExtractor extends RepositoryExtractor{

    private SVNRepository svnRepository;
    private SvnOperationFactory svnOperationFactory;
    private SVNClientManager svnClientManager;
    
    private PersistenceHandler persistenceHandler;
    
    public SVNRepositoryExtractor(String url) {
        super(url, "anonymous", "anonymous");
        setup();
    }

    public SVNRepositoryExtractor(String url, String username, String password) {
        super(url, username, password);
        setup();
    }
    
    private void setup(){
        persistenceHandler = new PersistenceHandler();
        DAVRepositoryFactory.setup( );
        try {
            svnRepository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( super.getUrl() ) );
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(super.getUsername(), super.getPassword());
            svnRepository.setAuthenticationManager(authManager);
            svnOperationFactory = new SvnOperationFactory();
            svnOperationFactory.setAuthenticationManager(new BasicAuthenticationManager(super.getUsername(), super.getPassword()));
            svnClientManager = SVNClientManager.newInstance(svnOperationFactory);
            System.out.println("Connected!");
        } catch (SVNException ex) {
            System.out.println("Connection problem!");
        }
    }
    
    
    @Override
    public Repository extractData(){
        Date begin;
        Date end;
        
        Date globalBegin = new Date();
        begin = new Date();
        //Repository
        Repository repository = persistenceHandler.createRepository(buildRepository());
        
        long previousRevision = 0;
        try {

            Collection logEntries = getAllLogEntries();
            end = new Date();
            System.out.println("Repo + LogEntries: "+(end.getTime()-begin.getTime()) +" ms");
            for (Iterator entries = logEntries.iterator( ); entries.hasNext( );) {
                SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
                
                begin = new Date();
                System.out.print("Version "+ logEntry.getRevision() + "/" + logEntries.size() + " : ");
                
                if (logEntry.getRevision()<701){
                    continue;
                }
                
                //Version
                Version version = persistenceHandler.createVersion(buildVersion(logEntry, repository));
                repository.getVersions().add(version);
                //persistenceHandler.refresh();
                end = new Date();
                System.out.println((end.getTime()-begin.getTime()) +" ms");
                
                begin = new Date();
                //projectPackagesWhichContainsJavaFiles - Package Tree of java Files
                Set<String> projectPackagesWhichContainsJavaFiles = getProjectPackagesWhichContainsJavaFiles(logEntry.getRevision());
                end = new Date();
                System.out.println("projectPackagesWhichContainsJavaFiles: "+(end.getTime()-begin.getTime()) +" ms");
                
                begin = new Date();
                Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
                end = new Date();
                System.out.println("ChangedFile: "+(end.getTime()-begin.getTime()) +" ms");
                for (Map.Entry<String, SVNLogEntryPath> entry : changedPaths.entrySet()) {
                    if(!entry.getKey().endsWith(".java")){
                        continue;
                    }
                    SVNLogEntryPath sVNLogEntryPath = entry.getValue();
                    
                    //ChangedFile
                    ChangedFile changedFile = persistenceHandler.createChangedFile(buildChangedFile(sVNLogEntryPath, version));
                    version.getChangedFiles().add(changedFile);
                    
                    if (!changedFile.getType().equals('D')){
                        begin = new Date();
                        //Package
                        Set<Package> packages = buildPackages(logEntry, sVNLogEntryPath, projectPackagesWhichContainsJavaFiles);
                        for (Package package1 : packages) {
                            package1.getChangedFiles().add(changedFile);
                        }
                        changedFile.getPackages().addAll(packages);
                        end = new Date();
                        System.out.println("Packages: "+(end.getTime()-begin.getTime()) +" ms");

                        begin = new Date();
                        //ChangedLine
                        List<ChangedLine> changedLines = persistenceHandler.createChangedLines(buildChangedLines(logEntry, sVNLogEntryPath, previousRevision, changedFile));
                        //changedFile.getChangedLines().addAll(changedLines);
                        end = new Date();
                        System.out.println("ChangedLines: "+(end.getTime()-begin.getTime()) +" ms");
                        //  persistenceHandler.refresh();
                    }
                }
                previousRevision = logEntry.getRevision();
            }
        } catch (SVNException ex) {
            Logger.getLogger(SVNRepositoryExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SVNRepositoryExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Date globalEnd = new Date();
        System.out.println("Total: " +(globalEnd.getTime()-globalBegin.getTime()) + " ms");
        return repository;
    }
    
    private Date svnDateToUtilDate(SVNDate svnDate) {
        return new Date(svnDate.getTime());
    }
    
    private Collection getAllLogEntries() throws SVNException {
        return svnRepository.log(new String[] {""}, null , 0, -1, true, true);
    }
    
    private Set<String> getProjectPackagesWhichContainsJavaFiles(final long revision) throws SVNException{
        try {
            final Set<String> projectPackages = new LinkedHashSet<String>();
            svnRepository.status(revision, "", SVNDepth.INFINITY, new ISVNReporterBaton() {
                public void report(ISVNReporter reporter) throws SVNException {
                    reporter.setPath("", null, revision, SVNDepth.INFINITY, true);
                    reporter.finishReport();
                }
            }, new ISVNEditor() {

                public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
                    if(path.endsWith(".java")){
                        projectPackages.add(path.substring(0,path.lastIndexOf("/")).replace("/", "."));
                    }
                }

				public void applyTextDelta(String arg0, String arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public OutputStream textDeltaChunk(String arg0, SVNDiffWindow arg1) throws SVNException {
					// TODO Auto-generated method stub
					return null;
				}

				public void textDeltaEnd(String arg0) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void abortEdit() throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void absentDir(String arg0) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void absentFile(String arg0) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void addDir(String arg0, String arg1, long arg2) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void changeDirProperty(String arg0, SVNPropertyValue arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void changeFileProperty(String arg0, String arg1, SVNPropertyValue arg2) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void closeDir() throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public SVNCommitInfo closeEdit() throws SVNException {
					// TODO Auto-generated method stub
					return null;
				}

				public void closeFile(String arg0, String arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void deleteEntry(String arg0, long arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void openDir(String arg0, long arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void openFile(String arg0, long arg1) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void openRoot(long arg0) throws SVNException {
					// TODO Auto-generated method stub
					
				}

				public void targetRevision(long arg0) throws SVNException {
					// TODO Auto-generated method stub
					
				}

            });
            return projectPackages;
        } finally {
        }
        
    }

    private Set<String> getPackageNames(ByteArrayOutputStream baos) throws IOException {
        String tmpFile = "src/tmp/packageNames.java";
        baos.writeTo(new FileOutputStream(tmpFile));
        PackageNameExtractor pnExtractor = new PackageNameExtractor(tmpFile);
        return pnExtractor.getFullPackageNamesWithoutClassNames();
    }
    
    private Repository buildRepository() {
        return new Repository(super.getUrl());
         //return persistenceHandler.createRepository(new Repository(super.getUrl()));
    }

    private Version buildVersion(SVNLogEntry logEntry, Repository repository) {
        String message = logEntry.getMessage();
        if (message != null && message.length()>255) {
            message = message.substring(0, 254);
        }
        return new Version(logEntry.getRevision(), 
                logEntry.getAuthor(), 
                message, 
                svnDateToUtilDate((SVNDate)logEntry.getDate()), 
                repository);
    }
    
    private ChangedFile buildChangedFile(SVNLogEntryPath sVNLogEntryPath, Version version) {
        return new ChangedFile(sVNLogEntryPath.getPath(),
                sVNLogEntryPath.getPath().substring(sVNLogEntryPath.getPath().lastIndexOf("/")+1), 
                sVNLogEntryPath.getType(),
                version
                );
    }
    
    private Set<Package> buildPackages(SVNLogEntry logEntry, SVNLogEntryPath sVNLogEntryPath, Set<String> projectPackagesWhichContainsJavaFiles) throws SVNException, IOException {
        Set<Package> result = new LinkedHashSet<Package>();
        SVNProperties fileProperties = new SVNProperties( );
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        svnRepository.getFile(sVNLogEntryPath.getPath(), logEntry.getRevision(), fileProperties, baos );
        Set<String> packageNames = getPackageNames(baos);
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
    
    private List<ChangedLine> buildChangedLines(SVNLogEntry logEntry, SVNLogEntryPath sVNLogEntryPath, long previousRevision, ChangedFile changedFile) throws IOException, SVNException {
        List<ChangedLine> result = new LinkedList<ChangedLine>();

        String tmpFile = "src/tmp/diff.txt";

        SvnDiffGenerator svnDiffGenerator = new SvnDiffGenerator();
        svnDiffGenerator.setBasePath(new File(""));

        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);

        SVNDiffClient svnDiffClient = svnClientManager.getDiffClient();
        svnDiffClient.doDiff(SVNURL.parseURIEncoded(super.getUrl()+sVNLogEntryPath.getPath()),
                null, 
                SVNRevision.create(previousRevision), 
                SVNRevision.create(logEntry.getRevision()), 
                SVNDepth.UNKNOWN, 
                false, 
                fileOutputStream);
        
        /*
        SvnDiff svnDiff = svnOperationFactory.createDiff();
        svnDiff.setDiffGenerator(svnDiffGenerator);
        svnDiff.setSource(
                SvnTarget.fromURL(SVNURL.parseURIEncoded(super.getUrl()+sVNLogEntryPath.getPath())), 
                SVNRevision.create(previousRevision),//logEntry.getRevision()-1), 
                SVNRevision.create(logEntry.getRevision()));
        svnDiff.setOutput(fileOutputStream);
        svnDiff.run();
        */
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)));
        
        reader.readLine();
        reader.readLine();
        reader.readLine();
        reader.readLine();
        reader.readLine();
        String line = reader.readLine();
        
        Pattern p = Pattern.compile("(^\\+|^-)(.+)");
        while(line != null) {
            Matcher m = p.matcher(line);
            if(m.find()) {
                ChangedLine changedLine = null;
                if (m.group(1).equals("+")){
                    changedLine = new ChangedLine(m.group(2).trim(),'A',changedFile);
                }
                if (m.group(1).equals("-")) {
                    changedLine = new ChangedLine(m.group(2).trim(),'D',changedFile);
                }
                if (changedLine != null && !changedLine.getChange().equals("")
                        && !changedLine.getChange().equals("/*")
                        && !changedLine.getChange().equals("*/")
                        && !changedLine.getChange().equals("*")
                        && !changedLine.getChange().equals("/**")
                        && !changedLine.getChange().equals("**/")
                        && !changedLine.getChange().equals("}")
                        && !changedLine.getChange().equals("{")
                        && !changedLine.getChange().equals("(")
                        &&!changedLine.getChange().equals(")")) {
                    if (changedLine.getChange().length() > 255) {
                        changedLine.setChange(changedLine.getChange().substring(0, 254));
                    }
                    changedFile.getChangedLines().add(changedLine);
                    result.add(changedLine);
                }
            }
            line = reader.readLine();
        }
        return result;
    }
}
