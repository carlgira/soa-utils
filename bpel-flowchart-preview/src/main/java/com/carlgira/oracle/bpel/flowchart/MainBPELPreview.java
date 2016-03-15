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

/**
 * Created by carlgira on 15/03/2016.
 * Class with main functions to  process the bpel graph.
 */
public class MainBPELPreview {

    private String server,user,pass,realm,graph_dir,node_command ;

    private MermaidJSGraphBuilder bpelFlowChartController;
    private byte[] imgContent;

    public MainBPELPreview(Properties properties) {
        server = properties.getProperty("server");
        user = properties.getProperty("user");
        pass = properties.getProperty("password");
        realm = properties.getProperty("realm");
        graph_dir = properties.getProperty("graph_dir");
        node_command = properties.getProperty("node_command");
    }

    public void bpelPreviewGraph(String partition, String composite, String version, String bpel, String bpelid ) throws Exception {

        String templateGraphFile = partition + "." + composite + "." + version + "." + bpel;
        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        File file = new File(graph_dir + "/" + templateGraphFile  + ".txt");

        if (!file.exists()) {
            throw new Exception("No config files for input, " + templateGraphFile);
        }

        bpelFlowChartController = new MermaidJSGraphBuilder(serverConnection, file.getAbsolutePath());
        CompositeDN compositeDN = new CompositeDN(partition + "/" + composite + "!" + version);

        bpelFlowChartController.buildNodes(compositeDN, bpel, bpelid);
        bpelFlowChartController.drawLinks();
    }

    public void bpelPreviewGraphImgString(String partition, String composite, String version, String bpel, String bpelid ) throws Exception {

        try {
            bpelPreviewGraph(partition, composite, version, bpel, bpelid);

            String graph = graph_dir + "\\temp\\" + bpelid + ".mmd";
            File tempGraph = new File(graph);
            FileWriter fileWriter = new FileWriter(tempGraph);
            fileWriter.write(bpelFlowChartController.writeResponse());
            fileWriter.flush();
            fileWriter.close();

            String command = node_command;
            command = String.format(command, graph_dir + "\\temp", graph);

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            imgContent = Files.readAllBytes(Paths.get(graph + ".png"));

        }catch (IOException e){
            throw new Exception("Problem reading template graph file");
        }
        catch (InterruptedException e){
            throw new Exception("Problem executing rendering image");
        }
    }

    public void bpelPreviewGraphImg(String partition, String composite, String version, String bpel, String bpelid ) throws Exception {

        try {
            bpelPreviewGraph(partition, composite, version, bpel, bpelid);

            String graph = graph_dir + "\\temp\\" + bpelid + ".mmd";
            File tempGraph = new File(graph);
            FileWriter fileWriter = new FileWriter(tempGraph);
            fileWriter.write(bpelFlowChartController.writeResponse());
            fileWriter.flush();
            fileWriter.close();

            String command = node_command;
            command = String.format(command, graph_dir + "\\temp", graph);

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            this.imgContent = Files.readAllBytes(Paths.get(graph + ".png"));

        } catch (IOException e) {
            throw new Exception("Problem reading template graph file");
        } catch (InterruptedException e) {
            throw new Exception("Problem executing rendering image");
        }
    }

    public MermaidJSGraphBuilder getBpelFlowChartController() {
        return bpelFlowChartController;
    }

    public String getImageString() {
        return Base64.encodeBase64String(imgContent);
    }

    public byte[] getImage() {
        return imgContent;
    }
}