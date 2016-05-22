package de.dixti.jarscan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * Options contains all Options of JarScan.
 */
public class Options {
    private boolean verbose = false;
    private boolean recursive = false;
    private boolean manifestScan = false;
    private boolean doubleScan = false;
    private boolean fileScan = false;
    private boolean writeFiles = false;
    private boolean exportXSDs = false;
    private String searchString;
    private String classVersionString = null;
    private File dir = new File(".");
    private List<String> jarSuffixList = new ArrayList<String>();
    private List<String> fileExcludeSuffixList = new ArrayList<String>(); 
    private List<String> excludeSuffixList = new ArrayList<String>();

    /**
     * Creates an Options object with searchString as the String to search for
     * and with default values for all other options.
     * @param searchString the String to search for
     */
    public Options(String searchString) {
        this.searchString = searchString;
        init();
    }

    /**
     * This options write every file that was analyzed
     * @return
     */
    public boolean isWriteFiles() {
        return writeFiles;
    }

    /**
     * This option export every schema founded into a folder
     * @return
     */
    public boolean isExportXSDs() {
        return exportXSDs;
    }

    /**
     * Creates an Options object.
     */
    public Options() {
        init();
    }

    /**
     * Create an Options object with command line args
     * @param commandLine the command line args
     * @throws de.dixti.jarscan.JarScanException if any exception occurs
     */
    public Options(String[] commandLine) throws JarScanException {
        init();
        if(commandLine.length == 0) {
            throw new JarScanException("Missing argument: searchString");
        }
        for (int i = 0; i < commandLine.length; i++) {
            if(commandLine[i].startsWith("-")) {
                if(commandLine[i].equals("-v")) {
                    verbose = true;
                } else if(commandLine[i].equals("-r")) {
                    recursive = true;
                } else if(commandLine[i].equals("-m")) {
                    manifestScan = true;
                } else if(commandLine[i].equals("-double")) {
                    doubleScan = true;
                } else if(commandLine[i].equals("-writeFiles")) {
                    writeFiles = true;
                } else if(commandLine[i].equals("-exportXSDs")) {
                    new File("schemas").mkdir();
                    exportXSDs = true;
                } else if(commandLine[i].equals("-f")) {
                    fileScan = true;
                } else if(commandLine[i].equals("-c")) {
                    classVersionString = commandLine[i+1];
                    i++;
                } else if(commandLine[i].equals("-d")) {
                    if(i+1 < commandLine.length && !commandLine[i+1].startsWith("-")) {
                        dir = new File(commandLine[i+1]);
                        if(! dir.isDirectory()) {
                            throw new JarScanException(commandLine[i+1] + " is not a directory");
                        }
                        i++;
                    } else {
                        throw new JarScanException("-d must be followed by a directory");
                    }
                }else if(commandLine[i].equals("-s")) {
                    if(i+1 < commandLine.length && !commandLine[i+1].startsWith("-")) {
                        jarSuffixList.add(commandLine[i+1]);
                        i++;
                    } else {
                        throw new JarScanException("-s must be followed by an archive-suffix, e.g.: .par");
                    }
                }else if(commandLine[i].equals("-e")) {
                    if(i+1 < commandLine.length && !commandLine[i+1].startsWith("-")) {
                        excludeSuffixList.add(commandLine[i+1]);
                        i++;
                    } else {
                        throw new JarScanException("-e must be followed by an archive-suffix, e.g.: .par");
                    }
                } else {
                    throw new JarScanException("Unknown argument: " + commandLine[i]);
                }
            } else if(i == commandLine.length - 1) {  // commandLine start not with "-"
                searchString = commandLine[i];
            } else {
                throw new JarScanException("Invalid argument: " + commandLine[i]);
            }
        }
        checkArgs();
        // Change Suffix-Lists:
        jarSuffixList.removeAll(excludeSuffixList);
        fileExcludeSuffixList.addAll(excludeSuffixList);
    }

    /**
     * Checks if the combination of arguments is allowed.
     * @throws JarScanException if the combination is not allowed or arguments are missing
     */
    private void checkArgs() throws JarScanException {
        // Missing argument:
        if(searchString == null && classVersionString == null && !doubleScan) {
            throw new JarScanException("Missing argument: search string or class version string must be set");
        }
        // Check if combination of args is allowed:
        int argCounter = 0;
        if(fileScan) argCounter++;
        if(manifestScan) argCounter++;
        if(classVersionString != null) argCounter++;
        if(doubleScan) argCounter++;
        if(argCounter > 1) {
            throw new JarScanException("The arguments -f, -m, -c, -double cannot be combined.");
        }
        if(doubleScan && searchString != null) {
            throw new JarScanException("The arguments -double and <searchString> cannot be combined.");
        }
    }

    /**
     * Inits the suffixList and the fileExcludeList.
     */
     private void init() {
        jarSuffixList.add(".jar");
        jarSuffixList.add(".zip");
        jarSuffixList.add(".war");
        jarSuffixList.add(".ear");
        jarSuffixList.add(".tar");
        fileExcludeSuffixList.add(".exe");
        fileExcludeSuffixList.add(".gif");
        fileExcludeSuffixList.add(".jpg");
        fileExcludeSuffixList.add(".jpeg");
        fileExcludeSuffixList.add(".png");
        fileExcludeSuffixList.add(".bmp");        
    }

     public boolean isFileNameScan() {
         return !fileScan && !manifestScan && isClassVersionScan();
     }
    /**
     * Sets the recursive-option.
     * @param recursive true if archives in archives shall be scanned
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Returns the recursive-option.
     * @return the recursive-option
     */
    public boolean isRecursive() {
        return recursive;
    }
    /**
     * Sets the scanFile-option.
     * @param fileScan true if text files shall be scanned
     */
    public void setFileScan(boolean fileScan) {
        this.fileScan = fileScan;
    }

    /**
     * Returns the scanFile-option.
     * @return the scanFile-option
     */
    public boolean isFileScan() {
        return fileScan;
    }

    /**
     * Sets the dir where scanning shall start.
     * @param dir the directory
     * @exception JarScanException if dir is not a directory
     */
    public void setDir(File dir) throws JarScanException {
        if (!dir.isDirectory()) {
            throw new JarScanException(dir.getPath() + " is not a directory!");
        }
        this.dir = dir;
    }

    /**
     * Returns the directory where scanning shall start.
     * @return the directory where scanning shall start
     */
    public File getDir() {
        return dir;
    }

    /**
     * Returns the List of suffixes that JarScan identifies as archives for 
     * damage checks. This suffixes are only relevant for damage checks.
     * Regardless of their suffixes jarscan recognizes all archives.
     * The returned list is not a copy. So you can add suffixes to it.
     * @return the suffixList
     */
    public List<String> getJarSuffixList() {
        return jarSuffixList;
    }
    /**
     * Sets the verbose-Flag that indicates that every scanned archive will be
     * printed.
     * @param verbose the verbose-Flag
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * Returns true if manifest files are scanned for an entry. Keys and values of every
     * entry are scanned.
     * @return true if manifest files are scanned
     */
    public boolean isManifestScan() {
        return manifestScan;
    }

    /**
     * Sets the flag that indicates if manifest files are scanned. 
     * @param manifestScan true if manifest files shall be scanned
     */
    public void setManifestScan(boolean manifestScan) {
        this.manifestScan = manifestScan;
    }

    /**
     * Returns true, if archives are scanned for doubled entries.
     * @return true if archives are scanned for doubled entries.
     */
    public boolean isDoubleScan() {
        return doubleScan;
    }

    /**
     * Sets the flag that indicates that doubles shall be scanned.
     * @param doubleScan true if it shall be scanned for doubles
     */
    public void setDoubleScan(boolean doubleScan) {
        this.doubleScan = doubleScan;
    }

    /**
     * Returns the List of suffixes that are excluded from scanning. If you do
     * not want archives with the suffix ".zip" to be scanned, you can add it
     * to the returned list. The returned list is no copy.
     * By default the list has size 0.
     * @return the list of archive suffixes that are ignored
     */
    public List<String> getExcludeSuffixList() {
        return excludeSuffixList;
    }
    /**
     * Returns the verbose flag that indicates that every scanned archive will be
     * printed.
     * @return the verbose flag
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Returns the String jarscan should search for
     * @return the searchString
     */
    public String getSearchString() {
        return searchString;
    }
    /**
     * Sets the SearchString.
     * @param searchString the searchString
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

     /**
     * Returns the List of suffixes that are excluded from scanning by default
     * if the -f option is set. E.g. if you do
     * not want files with the suffix ".class" to be scanned, you can add it
     * to the returned list. The returned list is no copy.
     * By default the files that are not scanned are ".exe", ".gif", ".jpg", ".jpeg", ".bmp", ".png"
     * @return the list of file suffixes that are ignored
     */   
    public List<String> getFileExcludeSuffixList() {
        return fileExcludeSuffixList;
    }

    /**
     * Returns the class version String that is used to determine the compiler version
     * of .class files. It must be something like: <1.5 or >1.5 or 1.5
     * @return the classVersionString
     */
    public String getClassVersionString() {
        return classVersionString;
    }

    /**
     * Sets the class version string.
     * @param classVersionString the classVersionString to set
     */
    public void setClassVersionString(String classVersionString) {
        this.classVersionString = classVersionString;
    }

    /**
     * Returns true if the -c option is set.
     * @return true if the -c option is set
     */
    public boolean isClassVersionScan() {
        return classVersionString != null;
    }
}