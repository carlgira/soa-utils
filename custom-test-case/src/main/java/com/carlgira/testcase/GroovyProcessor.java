package com.carlgira.testcase;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.xmlbeans.XmlObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Groovy processor to create dynamic requests with the Soa Suite Unit Test Framework
 */
public class GroovyProcessor {

    public static final Map<String,GroovyShell> groovyShells = new HashMap<String, GroovyShell>();

    private String testId;

    public GroovyProcessor(String testId){
        this.testId = testId;
        addNewShell(testId);
    }

    /**
     * Add new GroovyShell with a testId
     * @param testId
     * @return
     */
    public static boolean addNewShell(String testId){
        if(!groovyShells.containsKey(testId)){
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            groovyShells.put(testId, shell);
            return true;
        }
        return false;
    }

    /**
     * Delete a GroovyShell with a testId
     * @param testId
     * @return
     */
    public static boolean deleteShell(String testId){
        if(groovyShells.containsKey(testId)){
            groovyShells.remove(testId);
            return true;
        }
        return false;
    }

    /**
     * Evaluate a groovy expression inside of a groovyShell
     * @param testId
     * @param expr
     * @return
     */
    public static String evaluate(String testId, String expr){
        if(groovyShells.get(testId) == null){
            return "";
        }
        return groovyShells.get(testId).evaluate(expr).toString();
    }

    /**
     * Clear all shells saved shells
     */
    public static void cleanAll(){
        groovyShells.clear();
    }

    /**
     * Checks if a xmlstring has incrusted groovy
     * @param xmlObject
     * @return
     */
    public boolean hasGroovy(XmlObject xmlObject){
        String xmlString = xmlObject.toString();
        Pattern r = Pattern.compile("\\$\\{([^\\$]+)\\}", Pattern.DOTALL);
        Matcher m = r.matcher(xmlString);
        if(m.find()) {
            return true;
        }
        return false;
    }

    /**
     * Execute all the groovy code inside of a XML
     * @param xmlString
     * @return
     */
    public String processXML(String xmlString){
        String result = xmlString;
        Pattern r = Pattern.compile("\\$\\{([^\\$]+)\\}", Pattern.DOTALL);
        Matcher m = r.matcher(xmlString);

        while (m.find()) {
            String value = m.group(0);
            String expr =  evaluate(this.testId, m.group(1));
            result = result.replace(value, expr);
        }

        return result;
    }

    /**
     * Execute all the groovy code inside of a XML
     * @param xmlObject
     * @return
     */
    public String processXML(XmlObject xmlObject){
        return this.processXML(xmlObject.toString());
    }
}
