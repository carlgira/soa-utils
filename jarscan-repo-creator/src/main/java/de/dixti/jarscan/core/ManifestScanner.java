package de.dixti.jarscan.core;

import de.dixti.jarscan.FileStream;
import de.dixti.jarscan.Jar;
import de.dixti.jarscan.Result;
import de.dixti.jarscan.Scanner;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
/**
 * Scans a manifest file for a certain string.
 * @author Lars
 */
public class ManifestScanner implements Scanner {
    private String searchString;

    public ManifestScanner(String searchString) {
        this.searchString = searchString;
    }
    
    public void scanJar(Jar jar, Result jarResult) throws IOException {
        Manifest manifest = jar.getManifest();
        if (manifest == null) {
            return;
        }
        // Main Attributes:
        Attributes mainAtts = manifest.getMainAttributes();
        Iterator mainIt = mainAtts.keySet().iterator();
        while (mainIt.hasNext()) {
            Attributes.Name key = (Attributes.Name) mainIt.next();
            String value = mainAtts.getValue(key);
            String keyAndValue = key + ": " + value;
            if (keyAndValue.indexOf(searchString) >= 0) {
                jarResult.setMessage(keyAndValue);
            }
        }
        // Entries:
        Map<String, Attributes> entryMap = manifest.getEntries(); // key=<sectionName>, value=Attributes
        Iterator entryMapKeyIt = entryMap.keySet().iterator();  // section names
        while (entryMapKeyIt.hasNext()) {                       // iterate over sections
            String key = (String) entryMapKeyIt.next();         // section name
            if (("Name: " + key).indexOf(searchString) >= 0) {
                jarResult.setMessage("Name: " + key);
            }
            Attributes atts = (Attributes) entryMap.get(key); // Attributes of this section
            Iterator attKeyIt = atts.keySet().iterator();    // keys of attributes
            while (attKeyIt.hasNext()) {                      // iterate over attributes of this section 
                String attKey = ((Name) attKeyIt.next()).toString();
                String keyAndValue = attKey + ": " + atts.getValue(attKey);
                if (keyAndValue.indexOf(searchString) >= 0) {
                    jarResult.setMessage(keyAndValue);
                }
            }
        }
    }

    public void scanFile(FileStream fileStream, Result parentResult) {
        // do nothing
    }
}
