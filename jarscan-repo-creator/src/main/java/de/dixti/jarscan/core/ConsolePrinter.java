package de.dixti.jarscan.core;

import de.dixti.jarscan.*;
import java.io.File;
import java.util.List;
/**
 * Prints the scanning results to the console.
 */
public class ConsolePrinter implements Printer {
    private Options options;
    
    public ConsolePrinter(Options options) {
        this.options = options;                
    }
    
    private String getFileName(String name) {
        if(name.startsWith("." + File.separator)) { 
            return name.substring(2);
        }else {
            return name;
        }
    }

    private String getIndent(Result result) {
        StringBuilder str = new StringBuilder();
        int level = result.getLevel();
        for (int i = 1; i < level; i++) {
            str.append("    ");
        }
        return str.toString();
    }

    private void printResult(Result result, String indent) {
        if(result.isArchiv()) {
            indent += "+";
        }
        System.out.println(result.toString(indent));
        Throwable ex = result.getThrowable();
        if(ex != null) {
            System.out.println(ex.getClass().getSimpleName() + ": " + getFileName(result.getPath()) + "; " + ex.getMessage());
        }
        // subResults:
        List<Result> resultList = result.getResultList();
        for (Result subResult : resultList) {
            printResult(subResult, getIndent(subResult));
        }
    }

    private void printRootResult(Result rootResult) {
         System.out.println();
         for (Result result : rootResult.getResultList()) {
             if(result.getHitCount() > 0 || result.getThrowable() != null) {
                 printResult(result, "");
             }
         }
    }
    
    public void printProgress(Result result) {
        System.out.print(".");
    }

    public void printSummary(Result rootResult) {
        printRootResult(rootResult);
        System.out.println("----------------------------------------------");
            System.out.println("Scanned archives: " + rootResult.getArchiveCount());
            System.out.println("Errors: " + rootResult.getThrowableCount());
        if(options.isFileScan() || options.getClassVersionString() != null) {
            System.out.println("Files with hits: " + rootResult.getHitCount());
        }else {
            System.out.println("Archives with hits: " + rootResult.getHitCount());
        }
    }
}
