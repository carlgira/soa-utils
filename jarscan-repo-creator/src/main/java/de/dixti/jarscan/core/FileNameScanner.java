package de.dixti.jarscan.core;

import de.dixti.jarscan.FileStream;
import de.dixti.jarscan.Jar;
import de.dixti.jarscan.Result;
import de.dixti.jarscan.Scanner;
/**
 * This Scanner scans a file name for a certain string.
 * @author Lars
 */
public class FileNameScanner implements Scanner {
    private String searchString;

    public FileNameScanner(String searchString) {
        this.searchString = searchString;
    }

    public void scanFile(FileStream fileStream, Result parentResult) {
        if(fileStream.isTopLevel()) {
            return;
        }
        String name = fileStream.getName();
        if (name.indexOf(searchString) >= 0) {
            new Result(parentResult, fileStream.getPath(), false);
        }// else do nothing
    }

    public void scanJar(Jar jar, Result jarResult) {
        if(jar.isTopLevelJar()) {
            return;
        }
        String path = jar.getPath();
        if (path.indexOf(searchString) >= 0) {
            jarResult.setMessage("");
        } // else do nothing
    }
}
