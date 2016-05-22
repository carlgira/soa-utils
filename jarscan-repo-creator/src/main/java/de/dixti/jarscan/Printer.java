package de.dixti.jarscan;

/**
 * Interface for printing JarResults used by {@link JarScan}. JarScan uses by default
 * the {@link de.dixti.jarscan.core.ConsolePrinter}. You can set your own Printer using {@link JarScan#setPrinter(Printer)}.
 * Usually you do not need an own Printer. In most cases its enough to use the
 * root result that is returned by {@link JarScan#scan()}. Writing your own Printer
 * is useful if you want that every result is printed immediately. JarScan will
 * call {@link #printProgress(Result)} any time it scanned a single archive.
 * 
 * @see JarScan#setPrinter(Printer)
 * @author LF
 */
public interface Printer {
   /**
     * Called by JarScan any time it scanned a single archive.
     */
    void printProgress(Result result);
    /**
     * Called by JarScan after scanning all archives.
     * @param rootResult all the results of the scanning
     */
    void printSummary(Result rootResult);

}
