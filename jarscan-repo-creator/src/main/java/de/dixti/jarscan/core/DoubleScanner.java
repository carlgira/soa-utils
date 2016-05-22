package de.dixti.jarscan.core;

import de.dixti.jarscan.FileStream;
import de.dixti.jarscan.Jar;
import de.dixti.jarscan.Options;
import de.dixti.jarscan.Result;
import de.dixti.jarscan.Scanner;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Scans for doubles of .class-files.
 */
public class DoubleScanner implements Scanner {
    // Map with key=entryName and value="jar with the first occurrence of entryName"
    private Map<String,Result> resultMap = new HashMap<String,Result>(); // entryName -> jarResult
    private boolean verbose;

    public DoubleScanner(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Creates the following hierarchie of Results:
     * jarResult               (created by JarScan)
     *     |-fileResult        (created when file occurs the 2nd time)
     *           |-subResult1  (created when file occurs the 2nd time)
     *           |-subResult2  (created when file occurs the 3rd time)
     * 
     * @param fileStream
     * @param parentResult
     * @throws IOException
     */
    public void scanFile(FileStream fileStream, Result parentResult) throws IOException {
        String fileName = fileStream.getName();
        if(!fileName.endsWith(".class") || !parentResult.isArchiv()) {
            return;
        }
        Result jarResult = resultMap.get(fileName);
        if(jarResult == null) {  // 1st occurrence of entry
            resultMap.put(fileName, parentResult);
        //}else if(jarResult.getResultList().isEmpty()) {  // 2nd occurrence of entry
        }else if(getResult(jarResult, fileName) == null ) {  // 2nd occurrence of entry
            if(verbose || jarResult.getResultList().isEmpty()) { // if not verbose print only 1 file per archive
                Result fileResult = new Result(jarResult, fileName, false);
                Result subResult = new Result(fileResult, parentResult.getPath(), true);
            }
        }else {  // further occurrence of entry
            // get fileResult:
            Result fileResult = getResult(jarResult, fileName);
            Result subResult = new Result(fileResult, parentResult.getPath(), true);  
        }
    }

    public void scanJar(Jar jar, Result jarResult) throws IOException {
        // do nothing
    }

    private Result getResult(Result jarResult, String fileName) {
        for(Result result : jarResult.getResultList()) {
            if(result.getPath().endsWith(fileName)) {
                return result;
            }
        }
        return null;
    }

}
