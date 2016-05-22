package de.dixti.jarscan.core;

import de.dixti.jarscan.FileStream;
import de.dixti.jarscan.Jar;
import de.dixti.jarscan.Result;
import de.dixti.jarscan.Scanner;
import java.io.*;

/**
 * This class is used to scan simple files.
 * @author Lars Fiedler
 */
public class FileScanner implements Scanner {
    private String searchString;
    private boolean verbose;
    //private final Pattern pattern;


    public FileScanner(String searchString, boolean verbose) {
        this.searchString = searchString;
        this.verbose = verbose;
        //pattern = Pattern.compile(".*" + searchString + ".*");
    }
    
    public void scanJar(Jar jar, Result jarResult) throws IOException {
        // do nothing
    }

    public void scanFile(FileStream fileStream, Result parentResult) throws IOException {
        Result result = new Result(fileStream.getPath(), false);
        scan(searchString, fileStream.getInputStream(), result);
        if(!result.getResultList().isEmpty()) {
            parentResult.add(result);
        }
    }

    /**
     * Scans the file and returns the line with the first occurence of searchString.
     * Note that the returned line is cut if it is longer than 100 + searchString.length().
     * @param searchString the String to search for
     * @return the first line that contains searchString
     * @throws java.io.IOException
     */
    public void scan(String searchString, InputStream in, Result result) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
        String line;
        while((line = reader.readLine()) != null) {
            /*if(pattern.matcher(line).matches()) {
                return formatLine(line, searchString);
            }*/
            if(line.contains(searchString)) {
                Result lineResult = new Result(result, line, false);
                if(!verbose) {  // stop at 1st occurrence:
                    return;
                }
            }
        }
    }
    
    /**
     * Shortens the line if it is greater than 100 + searchString.length().
     * <p>
     * 0--------index----searchStringEnd----------->
     * @param line
     * @param searchString
     * @return shorter line
     */
    private String formatLine(String line, String searchString) {
        int length = searchString.length();
        if(line.length() > 100 + length) {
            int index = line.indexOf(searchString);
            int searchStringEnd = index + length;
            if(index < 100) {
                line = line.substring(0, searchStringEnd);
            }else {  // index >= 100
                line = line.substring(searchStringEnd - 100, searchStringEnd);
            }
        }
        return line;
    }
   
}
