package de.dixti.jarscan.core;

import de.dixti.jarscan.FileStream;
import de.dixti.jarscan.Jar;
import de.dixti.jarscan.JarScanException;
import de.dixti.jarscan.Result;
import de.dixti.jarscan.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
/**
 * Scans a .class file for the compiler version.
 */
public class ClassVersionScanner implements Scanner {
    /**
     * how many bytes at beginning of class file we read<br> 4=ca-fe-ba-be + 2=minor + 2=major
     */
    private static final int CHUNK_LENGTH = 8;
    /**
     * expected first 4 bytes of a class file
     */
    private static final byte[] EXPECTED_MAGIC_NUMBER = {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};
    private String versionString;
    private Pattern pattern = Pattern.compile("(\\<|\\>)?1\\.[0-9]{1,2}"); // (<|>)?1.[0-9]{1,2}
    private int low;
    private int high;
    private Result lastParentResult;
    private boolean verbose;

    public ClassVersionScanner(String versionString, boolean verbose) throws JarScanException {
        if(! pattern.matcher(versionString).matches()) {
            throw new JarScanException("-c must be followed by something like" +
                        " 1.5 or \"<1.5\" or \">1.5\" (not: " + versionString + ")");
        }
        this.versionString = versionString;
        this.verbose = verbose;
        versionStringToInterval();
    }

    public void scanFile(FileStream fileStream, Result parentResult) throws IOException {
        if(!verbose && lastParentResult == parentResult) { // archives and toplevel .class-files are only scanned until there is a hit
            return;
        }
        if(! fileStream.getName().endsWith(".class")) {  // only .class files are scanned
            return;
        }
        String message = scan(fileStream.getInputStream());
        if(message != null) {
            Result result = new Result(parentResult, fileStream.getPath(), false);
            result.setMessage(message);
            lastParentResult = parentResult;
        }
    }

    public void scanJar(Jar jar, Result jarResult) throws IOException {
        // do nothing
    }

    private int convertVersionToCode(String version) {
        if(version.length() == 3) {
            return new Integer(version.substring(2,3)) + 44;
        }else {
            return new Integer(version.substring(2,4)) + 44;
        }
    }

    private String convertCodeToVersion(int code) {
        return "1." + new Integer(code - 44).toString();
    }
    /**
     * Checks if a .class Files in the jar has the version specified in versionString.
     */
    public void versionStringToInterval() {
        if(versionString.startsWith("<")) {
            low = convertVersionToCode("1.0");
            high = convertVersionToCode(versionString.substring(1, versionString.length())) -1;
        }else if(versionString.startsWith(">")) {
            low = convertVersionToCode(versionString.substring(1, versionString.length()))+1;
            high = convertVersionToCode("1.99");
        } else {
            low = convertVersionToCode(versionString);
            high = convertVersionToCode(versionString);
        }
    }
    /**
     * Checks if a .class Files in the jar have the version that is between low and high
     * @return the message line that will be printed onto the console
     */
    private String scan(InputStream in) throws IOException {
        byte[] chunk = new byte[CHUNK_LENGTH];
        int bytesRead = in.read(chunk, 0, CHUNK_LENGTH);
        if (bytesRead != CHUNK_LENGTH) {
            throw new IOException("Corrupt class file.");
        }
        // make sure magic number signature is as expected.
        for (int i = 0; i < EXPECTED_MAGIC_NUMBER.length; i++) {
            if (chunk[i] != EXPECTED_MAGIC_NUMBER[i]) {
                throw new IOException("Bad magic number!");
            }
        }
        // pick out big-endian ushort major version in last two bytes of chunk
        int major = ((chunk[CHUNK_LENGTH - 2] & 0xff) << 8) + (chunk[CHUNK_LENGTH - 1] & 0xff);
        // F I N A L L Y. All this has been leading up to this TEST
        if ( low <= major && major <= high) {
            //System.out.println(convertCodeToVersion(low) + "," + convertCodeToVersion(high));
            return convertCodeToVersion(major);
        }
        return null;
    }

}
