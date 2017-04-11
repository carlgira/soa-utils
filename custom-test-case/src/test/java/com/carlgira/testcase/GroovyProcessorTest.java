package com.carlgira.testcase;

import org.junit.Test;

public class GroovyProcessorTest {

    @Test
    public void testXMLGroovyProccesor(){
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

        GroovyProcessor.addNewShell("2");
        GroovyProcessor.evaluate("2", "num = Math.random()");
        System.out.println(GroovyProcessor.evaluate("2", "num"));

    }
}
