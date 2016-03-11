package com.carlgira.oracle.bpel.flowchart;

import com.carlgira.oracle.bpel.flowchart.managers.AuditTrailManager;
import com.carlgira.oracle.bpel.flowchart.managers.CompositeManager;
import com.carlgira.oracle.bpel.flowchart.managers.HumanTaskManager;
import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.bpel.BPELInstance;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by emateo on 08/03/2016.
 */
public class BPELFlowChartPreviewTest {
    public static void main(String args[]) throws Exception {

        /*
        ClassLoader classLoader = BPELFlowChartPreviewTest.class.getClassLoader();

        Properties prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());

        String server = prop.getProperty("server");
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        File file = new File(classLoader.getResource("AltaAgentesGrafo.txt").getFile());

        BPELFlowChartPreview bpelFlowChartController = new BPELFlowChartPreview(serverConnection, file.getAbsolutePath());

        String componentName = "AltaAgentes";
        CompositeDN compositeDN = new CompositeDN("Agentes/AltaAgentes!5.5");
        String componentId = "8320045";

        bpelFlowChartController.buildNodes(compositeDN, componentName, componentId);
        bpelFlowChartController.drawLinks();

        System.out.println(bpelFlowChartController.writeResponse());



        File tempGraph = File.createTempFile("componentId", ".tmp");
        FileWriter fileWriter = new FileWriter(tempGraph);
        fileWriter.write(bpelFlowChartController.writeResponse());
        fileWriter.flush();
        */
        String nodePath = "D:\\Usuarios\\emateo\\apps\\nodejs-portable\\node\\";
        String nodeBin = nodePath + "node.exe";
        String mermaidJS = nodePath + "node_modules\\mermaid\\bin\\mermaid.js";
        String phatomJS = nodePath + "node_modules\\phantomjs\\lib\\phantom\\phantomjs.exe";
        String outputDir = "D:\\";
        String filePath = nodePath + "node_modules\\mermaid\\bin\\altaAgentes.mmd";


        String command = "cmd /c " + nodeBin + " " + mermaidJS + " -e " + phatomJS + " " + " -o " + outputDir + " " +  filePath ;
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        String imageDataString = Base64.encodeBase64String(Files.readAllBytes(Paths.get("D:\\altaAgentes.mmd.png")));

        System.out.println(imageDataString);

        // http://nbpm-prb.omel.es/bpel-flowchart-preview/Agentes/AltaAgentes/5.5/AltaAgente/flowchart.htm?bpelid=8320045
    }
}
