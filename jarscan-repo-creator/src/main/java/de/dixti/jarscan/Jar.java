package de.dixti.jarscan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * This class offers one API for JarFile and JarInputStream. Though JarFile and
 * JarInputStream have similar APIs they do not implement the same interface.
 * So Jar can be seen as a common interface over these classes.
 *
 * @author Lars Fiedler
 */
public class Jar {
    // the suffixes of nested files that represent an archive;
    // all files in an archive that end on one of these suffixes is meant to be an archive:
    public final List<String> suffixList;
    // always exactly one of jarFile and jarInputStream is null:
    private JarFile jarFile = null;
    private JarInputStream jarInputStream = null;
    private Enumeration<JarEntry> jarFileEntries; // the entries of jarFile
    private String path; // the path to this jar

    /**
     * Creates a Jar based on a File.
     * @param file the File
     * @param suffixList the suffixes that should be treated as Jar
     * @throws IOException
     */
    public Jar(File file, List<String> suffixList) throws IOException {
        this.jarFile = new JarFile(file);
        this.suffixList = suffixList;
        jarFileEntries = jarFile.entries();
        this.path = file.getPath();
    }

    /**
     * Creates a nested Jar based on a JarInputStream.
     * @param jarInputStream the JarInputStream
     * @param path the whole path of this jar (e.g. dir1/archive1.zip/dir2/archive2.jar)
     * @param suffixList the suffixes that should be treated as Jar
     */
    private Jar(JarInputStream jarInputStream, String path, List<String> suffixList) {
        this.jarInputStream = jarInputStream;
        this.path = path;
        this.suffixList = suffixList;
    }

    /**
     * Checks if fileName has a suffix contained in suffixList, what means it
     * is an archive.
     * @param fileName the fileName
     * @return true if fileName is meant to be an archive
     */
    private boolean hasSuffix(String fileName) {
        for (String suffix : suffixList) {
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if entry is a archive.
     * @param entry an JarEntry
     * @return true if entry is an archive
     * @throws java.io.IOException
     */
    private boolean isJar(JarEntry entry) throws IOException {
        return hasSuffix(entry.getName());
    }

    /**
     * Returns true if this Jar is a top-level archive, not a nested archive.
     * @return true if this is a top-level Jar
     */
    public boolean isTopLevelJar() {
        return jarFile != null;
    }
    /**
     * Returns the Manifest of this Jar.
     * @return the manifest
     * @throws java.io.IOException
     */
    public Manifest getManifest() throws IOException {
        if(jarFile != null) {
            return jarFile.getManifest();
        }else {
            return jarInputStream.getManifest();
        }
    }

    /**
     * Returns the next entry of the archive as a Jar or an FileStream.
     * If the entry is an archive then this mtehod returns a Jar. If it is a
     * simple File it returns a FilesStream.
     * @return the next entry
     * @throws java.io.IOException
     */
    public Object nextEntry() throws IOException {
        JarEntry entry;
        if(jarFile != null) {   // JarFile -----------------------------------
            if(jarFileEntries.hasMoreElements()) {
                entry = jarFileEntries.nextElement();
                InputStream in = jarFile.getInputStream(entry);
                if(!isJar(entry)) {  // JarInputStream treats simple files as archives without entry
                    return new FileStream(in, path + File.separator + entry.getName(), entry.getName());
                }else {
                    return entryToJar(entry);  // entry is an nested archive
                }
            }else {  // no more entries
                return null;
            }
        }else {    // JarInputStream -----------------------------------------
            entry = jarInputStream.getNextJarEntry(); // Note that getNextJarEntry() or getNextEntry() ignores the manifest
            if(entry == null) {  // no more entries
                return null;
            }
            if(isJar(entry)) {  // nested Jar
                return entryToJar(entry);
            }else {  // simple File
                return new FileStream(jarInputStream, path + File.separator + entry.getName(), entry.getName());
            }
        }
    }
    /**
     * Converts an entry that represents a nested archive to a Jar.
     * @param entry the entry
     * @return the nested Jar
     * @throws java.io.IOException
     */
    private Jar entryToJar(JarEntry entry) throws IOException {
        if(jarFile != null) {
            InputStream in = jarFile.getInputStream(entry);
            JarInputStream jin = new JarInputStream(in);
            return new Jar(jin, path + File.separator + entry.getName(), suffixList);
        }else {
            JarInputStream nestedIn = new JarInputStream(jarInputStream);
            return new Jar(nestedIn, path + File.separator + entry.getName(), suffixList);
        }
    }
    /**
     * Closes this Jar.
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        if(jarFile != null) {
            jarFile.close();
        }else {
            jarInputStream.close();
        }
    }
    /**
     * Returns the complete path of this Jar. If this is an nested archive the
     * path contains the complete path to this archive.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        String erg = path + "\n";
//        try {
//            Object entry;
//            while( (entry = nextEntry()) != null) {
//                erg += entry.toString();
//            }
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
        return erg;
    }

}

