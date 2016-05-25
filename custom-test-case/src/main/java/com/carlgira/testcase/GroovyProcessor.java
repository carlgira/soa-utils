package com.carlgira.testcase;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.xmlbeans.XmlObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroovyProcessor {

    private GroovyShell groovyShell;

    private static Map<String,GroovyShell> groovyShells = new HashMap<String, GroovyShell>();

    public GroovyProcessor(String testId){

        if(!groovyShells.containsKey(testId)){
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            groovyShells.put(testId, shell);
        }

        this.groovyShell = groovyShells.get(testId);
    }

    public boolean hasGroovy(XmlObject xmlObject){
        String xmlString = xmlObject.toString();
        Pattern r = Pattern.compile("\\$\\{([^\\$]+)\\}", Pattern.DOTALL);
        Matcher m = r.matcher(xmlString);
        if(m.find()) {
            return true;
        }
        return false;
    }

    public String processXML(String xmlString){
        String result = xmlString;
        Pattern r = Pattern.compile("\\$\\{([^\\$]+)\\}", Pattern.DOTALL);
        Matcher m = r.matcher(xmlString);

        while (m.find()) {
            String value = m.group(0);
            String expr =  evaluate(m.group(1));
            result = result.replace(value, expr);
        }

        return result;
    }

    public String processXML(XmlObject xmlObject){
        return this.processXML(xmlObject.toString());
    }

    public String evaluate(String expr){
        return groovyShell.evaluate(expr).toString();
    }

    public static void main(String[] args){

        GroovyProcessor groovyProcessor = new GroovyProcessor("1");
        String xmlString = "<xml>\n" +
                "\t<a>${Math.random()}</a>\n" +
                "\t<b>2</b>\n" +
                "\t<c>${def num = Math.random()}</c>\n" +
                "\t<d>\n" +
                "\t\t<h>3</h>\n" +
                "\t\t<l>${\n" +
                "\t\t\tdef date = new Date()\n" +
                "\t\t\tnum = 123\n" +
                "\t\t\tsdf = new java.text.SimpleDateFormat(\"yyyy-MM-dd/\")\n" +
                "\t\t\tsdf.format(date)\n" +
                "\t\t}</l>\n" +
                "\t</d>\n" +
                "\t<e>5</e>\n" +
                "\t<f>${num}</f>\n" +
                "\t<g>7</g>\n" +
                "</xml>";

        String r = groovyProcessor.processXML(xmlString);

        System.out.println(r);

    }
}
