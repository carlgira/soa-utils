package com.carlgira.oracle.bpel.flowchart;

import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import static org.junit.Assert.*;

/**
 * Created by carlgira on 08/03/2016.
 */
public class MermaidJSGraphTest {

    private Properties prop;
    private ClassLoader classLoader;

    public MermaidJSGraphTest() throws IOException {
        classLoader = MermaidJSGraphTest.class.getClassLoader();
        prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());
    }

    public String testMermaidGraph(CompositeDN compositeDN, String bpel, String bpelid ) throws Exception {

        String server = prop.getProperty("server");
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        File file = new File(classLoader.getResource(compositeDN.getDomainName()+ "." + compositeDN.getCompositeName() + ".last." + bpel  + ".txt").getFile());

        MermaidJSGraphBuilder bpelFlowChartController = new MermaidJSGraphBuilder(serverConnection, file.getAbsolutePath());
        bpelFlowChartController.buildNodes(compositeDN, bpel, bpelid);
        bpelFlowChartController.drawLinks();

        return bpelFlowChartController.writeResponse();
    }

    public String testMermaidGraphImg(String bpelId, String graph) throws IOException, InterruptedException {

        String graph_dir = prop.getProperty("graph_dir");

        File tempGraph = new File(graph_dir + "/" + bpelId + ".mmd");
        FileWriter fileWriter = new FileWriter(tempGraph);
        fileWriter.write(graph);
        fileWriter.flush();
        fileWriter.close();

        String outputDir = classLoader.getResource(".").getFile() + "/..";

        String command = prop.getProperty("node_command");
        command = String.format(command, outputDir , tempGraph.getAbsolutePath());

        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        String imageDataString = ""; //FIX
                //Base64.encodeBase64String(Files.readAllBytes(Paths.get(outputDir + "/" + bpelId + ".mmd.png")));

        return imageDataString;
    }

    public static void main(String args[]) throws Exception {

        //DOC.AltaUnidadesOfertantes.last.AltaUnidadesOfertantes

        CompositeDN compositeDN = new CompositeDN("default/TestProject!1.0");
        String bpel = "TestBpel";
        String bpelid = "40002";


        MermaidJSGraphTest bpelFlowChartPreviewTest = new MermaidJSGraphTest();

        String graph = bpelFlowChartPreviewTest.testMermaidGraph(compositeDN, bpel, bpelid);
        System.out.println(graph);
        assertNotNull(graph);








        //String graphImg = bpelFlowChartPreviewTest.testMermaidGraphImg(bpelid,graph );
       // assertNotNull(graphImg);
    }
}
