package de.dixti.jarscan;

import java.io.IOException;
/**
 * A Scanner implementation is used by {@link JarScan} for a certain scan option.
 * E.g. all of the options -m,-f,-c have their own Scanner-Implementation.
 * While JarScan does the iterations over directories and archives, the Scanners
 * do the scan of one file or archive.
 *
 * @author Lars
 */
public interface Scanner {
    /**
     * Scans an archive.
     * @param jar the archive
     * @param jarResult the Result that should be used to set a message or an exception
     * @throws IOException
     */
    void scanJar(Jar jar, Result jarResult) throws IOException;
    /**
     * Scans a file.
     * @param fileStream the file
     * @param parentResult the parent result that represents an archive and should
     *  be used to create a new Result if the file is a hit
     * @throws IOException
     */
    void scanFile(FileStream fileStream, Result parentResult) throws IOException;

}
