package de.dixti.jarscan;

import de.dixti.jarscan.core.ClassVersionScanner;
import de.dixti.jarscan.core.ConsolePrinter;
import de.dixti.jarscan.core.DoubleScanner;
import de.dixti.jarscan.core.FileNameScanner;
import de.dixti.jarscan.core.FileScanner;
import de.dixti.jarscan.core.ManifestScanner;
import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

/**
 * JarScan is the central class for scanning archives. It conatins the
 * algorithms for scanning. Programmers who use JarScan as a library will
 * use this class.
 */
public class JarScan {
    private Options options;
    private Result rootResult;
    private Printer printer;
    private Scanner scanner;
    final static Logger logger = Logger.getRootLogger();
    private int counter = 0;

    /**
     * Creates an JarScan object with certain options.
     * @param options the options
     */
    public JarScan(Options options) {
        this.options = options;
        this.printer = new ConsolePrinter(options);
        String searchString = options.getSearchString();
        if(options.isManifestScan()) {
            scanner = new ManifestScanner(searchString);
        }else if(options.isDoubleScan()) {
            scanner = new DoubleScanner(options.isVerbose());
        }else if(options.isFileScan()) {
            scanner = new FileScanner(searchString, options.isVerbose());
        }else if(options.isClassVersionScan()) {
            scanner = new ClassVersionScanner(options.getClassVersionString(), options.isVerbose());
        }else {
            scanner = new FileNameScanner(searchString);
        }
   }

    /**
     * Checks if fileName ends with at least one suffix of list.
     * 
     * @param list the List of suffixes
     * @param fileName the fileName
     * @return true if fileName ends with at least one suffix of list
     */
    private boolean hasSuffix(List<String> list, String fileName) {
        for (String suffix : list) {
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Starts scanning.
     * @return a list with result information about the scanned jars
     */
    public Result scan() throws JarScanException {
        rootResult = new Result(null, options.getDir().getPath(), false);
        scanDirectory(options.getDir());
        if (printer != null) {
            printer.printSummary(rootResult);
        }
        return rootResult;
    }

    /**
     * Scans a directory recursively.
     * @param dir the directory
     * @throws de.dixti.jarscan.JarScanException
     */
    private void scanDirectory(File dir) throws JarScanException {
        File[] files = dir.listFiles();
        if (files == null) { // listFiles() returns null, if an IOException occurs
            rootResult.setThrowable(new IOException("Could not open: " + dir.getPath()));
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {     // directory
                scanDirectory(files[i]);
            } else if( !hasSuffix(options.getExcludeSuffixList(), files[i].getName()) ) { // if not excluded
                Jar jar = null;
                try {
                    jar = new Jar(files[i], options.getJarSuffixList());
                    // archive if no IOException occurs:
                    // every top level archive is a result; nested archives only get a result if they are hits
                    // objectgraph:   jarscanResult
                    //                     |- file with hit         --> created in scanFile()
                    //                     |- toplevel archive      --> created here
                    //                          |- file with hit    --> createed by Scanner
                    //                          |- archive with hit --> created in scanJar()
                    //                              |- ...
                    Result jarResult = new Result(rootResult, jar.getPath(), true);
                    scanJar(jar, jarResult);
                    if(printer != null) {
                        printer.printProgress(jarResult); // top level archives can be printed immediately
                    }
                } catch (IOException ex) {  // files[i] is a simple file or a dameged archive
                    if(hasSuffix(options.getJarSuffixList(), files[i].getName())) {  // damaged archive
                        Result jarResult = new Result(rootResult, files[i].getPath(), true);
                        jarResult.setMessage("Damaged archive!");
                        jarResult.setThrowable(ex);
                        printer.printProgress(jarResult); // top level files can be printed immediately
                    }
                    try {
                        scanFile(new FileStream(files[i]), rootResult); // only file hits get a result
                    }catch(FileNotFoundException ex2) { // should never happen
                        throw new JarScanException("An FileNotFoundException occured: " + files[i].getPath());
                    }
                }
            } // else do nothing; excludeList contains file suffix
        }
    }
    /**
     * Scans a Jar.
     * @param jar the Jar
     * @param jarResult the result object that describes the result of scanning the jarFile object
     */
    private void scanJar(Jar jar, Result jarResult) {
        if(hasSuffix(options.getFileExcludeSuffixList(), jar.getPath())) {
            return;
        }
        try {
            // Results for an archive are always created by the JarScan class.
            // Results for Files are created in the Scanner classes.
            // For every archive there is exactly one result.
            // scan Jar (without iteration):
            scanner.scanJar(jar, jarResult);
            // iterate over nested files:
            if(options.isWriteFiles()){
                logger.debug("ARCHIVE," + jar.getPath());
            }
            Object entry;
            while( (entry = jar.nextEntry()) != null) {
                if(entry instanceof FileStream) {  // entry is a file

                    if(options.isWriteFiles()){
                        logger.debug(((FileStream) entry).getName());
                    }
                    FileStream fileStream = (FileStream)entry;
                    if(options.isExportXSDs()){
                        if(fileStream.getName().contains(".xsd") || fileStream.getName().contains(".exsd")){
                            Files.copy(fileStream.getInputStream(), Paths.get("schemas/" + counter++ + "_" + fileStream.getName()));
                        }
                    }
                    scanFile((FileStream)entry, jarResult);
                }else if(options.isRecursive()) { // entry is a Jar
                    Result nestedJarResult = new Result(jarResult, ((Jar)entry).getPath(), true);
                    scanJar((Jar)entry, nestedJarResult);
                }
            }
        } catch (Throwable ex) {
            jarResult.setThrowable(ex);
        }
        try { if(jar.isTopLevelJar()) jar.close(); } catch (IOException ex) {}
    }
    
    /**
     * Scans a file.
     * @param fileStream the file as a FileStream
     * @param parentResult the parent result of scanning
     */
    private void scanFile(FileStream fileStream, Result parentResult) {
        if(hasSuffix(options.getFileExcludeSuffixList(), fileStream.getPath())) {
            return;
        }
        try {
            scanner.scanFile(fileStream, parentResult);
        } catch (Throwable ex) {
            Result result = new Result(parentResult, fileStream.getPath(), false);
            result.setThrowable(ex);
        }
        try { if(fileStream.isTopLevel()) fileStream.close(); } catch (Exception ex) {}
    }

    /**
     * Returns the Printer that JarScan uses to print the results. The Default-Printer
     * is {@link ConsolePrinter}.
     * @return the Printer
     */
    public Printer getPrinter() {
        return printer;
    }

    /**
     * Sets the Printer that JarScan uses to print the results. The Default-Printer
     * is {@link ConsolePrinter}. If you do not want that anything is printed 
     * set the Printer to null.  
     * @param printer the Printer
     **/
    public void setPrinter(Printer printer) {
        this.printer = printer;
    }

    /**
     * TestMethod.
     * @param args
     */
    public static void main(String[] args) throws Exception {
        //Options options = new Options(new String[] {"-d", "testjars", "-e",".class", "-r", "-f", "bla"});
        //Options options = new Options(new String[] {"-d", "C:\\projekt\\badiv\\lib","-double"});
        //Options options = new Options(new String[] {"-d", "testjars","-double","-v"});
        Options options = new Options(new String[] {"-d", "C:/projekt/jarscan/testjars", "-f","-e",".class","Line"});
        //Options options = new Options(new String[] {"-d", "C:/projekt/dixti/jarscan/testjars", "-c", ">1.1", "-v"});
        JarScan jarScan = new JarScan(options);
        jarScan.scan();
    }
}
