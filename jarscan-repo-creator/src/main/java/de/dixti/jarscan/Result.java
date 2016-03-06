package de.dixti.jarscan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * A Result is a tree of Results. That means the root of the tree is a Result
 * and every leave is a Result. A Result can represent the root, an archive or
 * a simple file. Simple files do not have children. <br>
 * JarScan creates a Result for every archive, also nested archives if the
 * -r option is set. Jarscan only creates a Result for a simple file if it is a
 * hit.
 *
 * @author Lars
 */
public class Result {
    private Result parent = null;
    private String path;
    private List<Result> resultList = new ArrayList<Result>();
    private String message;
    private Throwable ex;
    private boolean archive;

    /**
     * Creates a Result that is added to his parent.
     * @param parent the parent or null if this Result should be the root
     * @param path the path of the represented archive or file
     * @param archive true if this represents an archive
     */
    public Result(Result parent, String path, boolean archive) {
        this.parent = parent;
        this.path = path;
        this.archive = archive;
        if(parent != null) {
            parent.add(this);
        }
    }

    /**
     * Creates a Result that is not added to a parent. Call add() at the parent
     * to add it to the parent.
     * @param path the path of the represented archive or file
     * @param archive true if this represents an archive
     */
    public Result(String path, boolean archive) {
        this(null, path, archive);
    }

    /**
     * Adds a child to this Result.
     * @param child
     */
    public void add(Result child) {
        resultList.add(child);
        child.setParent(this);
    }
    /**
     * Sets the parent of this result.
     * @param parent the parent
     */
    public void setParent(Result parent) {
        this.parent = parent;
    }

    /**
     * Returns recursively the number of archives, even nested archives and archives without a hit.
     * @return the number of archives
     */
    public int getArchiveCount() {
        int counter = (hasParent() ? 1 : 0);
        for (Result result : resultList) {
            if(result.isArchiv()) {
                counter += result.getArchiveCount();
            }
        }
        return counter;
    }
    /**
     * Returns true if this Result has a parent. This means it returns true if
     * this Result is not the root.
     * @return true if this Result has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }
    /**
     * Returns the level of this Result in the tree. The root Result has level 0.
     * its children have level 1 and so on.
     * @return the level
     */
    public int getLevel() {
        if(parent == null) { // toplevel archives have level 0
            return 0;
        }else {
            return parent.getLevel() + 1;
        }
    }
    /**
     * Returns recursively all archives with hit.
     * @return all archives with a hit
     */
    public List<Result> getArchivesWithHit() {
        List<Result> list = new ArrayList<Result>();
        for (Result result : resultList) {
            if(result.isArchiv() && result.getHitCount() > 0) {
                list.add(result);
            }
        }
        return list;
    }
    /**
     * Returns recursively all files with a hit.
     * @return all files with a hit
     */
    public List<Result> getFilesWithHit() {
        List<Result> list = new ArrayList<Result>();
        for (Result result : resultList) {
            if(result.isArchiv()) {
                list.addAll(result.getFilesWithHit());
            }else {
                list.add(result);
            }
        }
        return list;
    }
    /**
     * Returns true if this is an archive.
     * @return true if this is an archive
     */
    public boolean isArchiv() {
        return archive;
    }

    /**
     * Returns the message. The message can be null. Only hits and exceptions
     * have a message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * Sets the message. The message can be null.
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the exception that occured while scanning the archive or file
     * represented by this result.
     * @param ex the occurred exception
     */
    void setThrowable(Throwable ex) {
        this.ex = ex;
    }
    /**
     * Counts recursively the number of hits of archive and files.
     * @return the number of hits
     */
    public int getHitCount() {
        if(getLevel() != 0 && !archive) {  // files only produce Results if they are hits
            return 1;
        }
        int counter = 0;
        if(message != null && ex == null) {  // this archive is a hit
            counter++;
        }
        for(Result result : resultList) {  // nested hits
            counter += result.getHitCount();
        }
        return counter;
    }
    /**
     * Counts recursively the number of exceptions that occurred while scanning.
     * @return the number of exceptions
     */
    public int getThrowableCount() {
        int counter = 0;
        if(ex != null) {
            counter++;
        }
        for(Result result : resultList) {
            counter += result.getThrowableCount();
        }
        return counter;
    }
    
    /**
     * Returns the path of the archive.
     * <p>
     * Example: <br>
     * dir2\archive2.jar
     * @return the path of the scanned jar file
     */
    public String getPath() {
        return getFileName(path);
    }
    /**
     * Returns a list of all children Results.
     * @return the list of children
     */
    public List<Result> getResultList() {
        return resultList;
    }
    /**
     * Returns the Throwable if the scanning of the archive caused a exception. It
     * returns null if the archive was scanned without an exception.
     * @return the Throwable
     */
    public Throwable getThrowable() {
        return ex;
    }

    /**
     * Remove the './' at the beginning of a path.
     * @param name the path
     * @return the path withou './' at the beginning 
     */
    private String getFileName(String name) {
        /*if(name == null) {
            return null;
        }*/
        if(name.startsWith("." + File.separator)) {
            return name.substring(2);
        }else {
            return name;
        }
    }

    /**
     * Returns the path and the message as a String (<path>:<message>). <br>
     * Example: dir1/archive1.zip/textfile.txt: blabla
     * @param indent
     * @return path and message as a String
     */
    public String toString(String indent) {
        String str = indent + getPath();
        if(message == null || message.isEmpty()) {
            return str;
        }else {
            return str + ": " + message;
        }
    }
    @Override
    public String toString() {
        return path;
    }
}
