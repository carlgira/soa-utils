package de.dixti.jarscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
/**
 * A FileStream is a common interface for a simple file or for a
 * file that in nested in an archive. You can determine if it is a simple file
 * by calling {@link #isTopLevel()}
 *
 * @see Jar
 * @author Lars
 */
public class FileStream {
    private InputStream in;
    private String path;
    private String name;
    private boolean isTopLevel;
    /**
     * Creates a FileStream based on an InputStream. If you use this constructor
     * toplevel is false.
     * @param in the InputStream
     * @param path the whole path of this FileStream (e.g. archive.zip/dir1/text1.txt)
     * @param name the name of the FileStream (e.g. text1.txt)
     */
    public FileStream(InputStream in, String path, String name) {
        this.in = in;
        this.path = path;
        this.name = name;
        isTopLevel = false;
    }

    /**
     * Creates a FileStream based on an InputStream of a file. If you use this constructor
     * toplevel is true.
     * @param file the file
     */
    public FileStream(File file) throws FileNotFoundException {
        this.in = new FileInputStream(file);
        this.path = file.getPath();
        this.name = file.getName();
        isTopLevel = true;
    }
    /**
     * Returns the InputStream this FileStream is based on.
     * @return the InputStream
     */
    public InputStream getInputStream() {
        return in;
    }
    /**
     * Returns true if this FileStream is based on a toplevel file (not a nested file).
     * @return true if this is a toplevel file
     */
    public boolean isTopLevel() {
        return isTopLevel;
    }
    /**
     * Returns the path of this FileStream.
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the name of this FileStream.
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * Closes the underliing InputStream. If this is a nested File be aware that this method also closes
     * the InputStream of the whole archive.
     */
    public void close() {
        try {
            in.close();
        } catch (IOException ex) {
        }  // cant do anything here
    }

    public String toString() {
        return path;
        /*try {
            String erg = "FILESTREAM:" + path + "\n";
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[32768];
            while ((len = in.read(buffer)) > 0) {
                byteOut.write(buffer, 0, len);
            }
            byteOut.close();
            erg += byteOut.toString() + "\n";
            return erg;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }*/
    }

}
