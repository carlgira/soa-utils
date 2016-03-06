
package de.dixti.jarscan;
/**
 * This is the Main-Class for using jarscan from the commandLine.
 */
public class Main {
    
     public static void main(String[] args) throws Exception {
         // create Options from command line
        Options options = null;
        try {
            options = new Options(args);
        } catch (JarScanException ex) {
            printUsageAndExit(ex.getMessage());
        }
        // create Jarscan:
        JarScan jarScan = new JarScan(options);
        jarScan.scan();
    }

    /**
     * Prints an error message and the usage and ends the program.
     * @param msg the error message
     */
    private static void printUsageAndExit(String msg) {
        System.out.println(msg);
        String usage = "\nUsage: jarscan [-options] <searchString>\n"
                       + "Scans all archives for a file name containing 'searchString' recursively from the current directory. "
                       + "\n"
                       + "Options:\n"
                       + "<searchString> part of the archive entry you are searching for\n"
                       + "-v verbose; if combined with -f,-c or -double,\n"
                       + "   jarscan prints all occurrences in a file, not only the first one\n"
                       + "-d <directory> start the search from <directory>\n"
                       + "-r recursive; scan even archives that are inside of an archive\n"
                       + "-f file; scan simple files for searchString; also simple files inside archives are scanned;\n"
                       + "   note that it only prints the first ocurrence per file;\n"
                       + "   use \"\" if searchString contains spaces (e.g. jarscan -f \"throws Exception\")\n"
                       + "-m manifest; scan only manifest files for searchString (in keys, values and sections)\n"
                       + "-c <javaVersion>; checks if the archives contain .class-Files that are compiled with \n"
                       + "   the specified java version;"
                       + "   Example: -c 1.5 checks if there is a .class-File that \n"
                       + "   was compiled with jdk 1.5. You can use '<' and '>' to search for version ranges\n"
                       + "   e.g: -c \"<1.5\" (Note that you must escape \"<\" and \">\".)\n"
                       + "-double searches for doubled classes in different archives.\n"
                       + "-e <suffix> exclude files from scanning that end on <suffix>\n"
                       + "-writeFiles Writes every analyzed file and folder, and the archive where are included\n"
                       + "-exportXSDs Exports every schema founded inside the archives\n"
                       + "-s <suffix> adds a suffix that should be treated as an archive. Jarscan only needs\n"
                       + "   this for nested archives and damaged archives. By default nested files with\n"
                       + "   suffix \".jar,.zip,.war,.ear,.tar\" are recognized as archives.\n"
                       + "\n"
                       + "Example: jarscan Date\n"
                       + "Scans all archives for a file named '*Date*'\n"
                       + "A result could be:\n"
                       + "testjars\\hibernate3.jar\n"
                       + "    org/hibernate/type/CalendarDateType.class\n"
                       + "    org/hibernate/type/DateType.class\n";
        System.out.println(usage);
        System.exit(1);
    }

}
