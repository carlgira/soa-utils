package com.carlgira.oracle.bpel.flowchart.controller;

import com.carlgira.oracle.bpel.flowchart.BPELFlowChartPreview;
import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by carlgira on 08/03/2016.
 * Service with the call
 */

@Controller
public class ServletController {

    private static Properties properties;
    static{
        loadProperties();
    }

    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchart")
    public ModelAndView flowChart(@PathVariable("partition") String partition,
                            @PathVariable("composite") String composite,
                            @PathVariable("version") String version,
                            @PathVariable("bpel") String bpel,
                            @RequestParam(value = "bpelid", required = true) String bpelid
                            ) throws Exception {

        String server = properties.getProperty("server");
        String user = properties.getProperty("user");
        String pass = properties.getProperty("password");
        String realm = properties.getProperty("realm");
        String graph_dir = properties.getProperty("graph_dir");
        String graphFile =  partition + "." + composite + "." + version + "." + bpel;

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        File file = new File(graph_dir + "/" + graphFile  + ".txt");

        if(!file.exists()){
            throw new Exception("No config files for input, " + graphFile);
        }

        BPELFlowChartPreview bpelFlowChartController = new BPELFlowChartPreview(serverConnection, file.getAbsolutePath());

        CompositeDN compositeDN = new CompositeDN(partition + "/" + composite + "!" + version);

        bpelFlowChartController.buildNodes(compositeDN, bpel, bpelid);
        bpelFlowChartController.drawLinks();

        ModelAndView model = new ModelAndView("mermaid");
        model.addObject("graph", bpelFlowChartController.writeResponse());
        model.addObject("process", bpel);
        model.addObject("state", translateState(bpelFlowChartController.getBpelInstance().getState()));
        model.addObject("status", bpelFlowChartController.getBpelInstance().getStatus());

        return model;
    }

    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchartImg")
    public ModelAndView flowChartImg(@PathVariable("partition") String partition,
                                  @PathVariable("composite") String composite,
                                  @PathVariable("version") String version,
                                  @PathVariable("bpel") String bpel,
                                  @RequestParam(value = "bpelid", required = true) String bpelid
    ) throws Exception {

        String server = properties.getProperty("server");
        String user = properties.getProperty("user");
        String pass = properties.getProperty("password");
        String realm = properties.getProperty("realm");
        String graph_dir = properties.getProperty("graph_dir");
        String graphFile =  partition + "." + composite + "." + version + "." + bpel;

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        File file = new File(graph_dir + "/" + graphFile  + ".txt");

        if(!file.exists()){
            throw new Exception("No config files for input, " + graphFile);
        }

        BPELFlowChartPreview bpelFlowChartController = new BPELFlowChartPreview(serverConnection, file.getAbsolutePath());

        CompositeDN compositeDN = new CompositeDN(partition + "/" + composite + "!" + version);

        bpelFlowChartController.buildNodes(compositeDN, bpel, bpelid);
        bpelFlowChartController.drawLinks();

        ModelAndView model = new ModelAndView("mermaidImg");

        String nodePath = "D:\\Usuarios\\emateo\\apps\\nodejs-portable\\node\\";
        String nodeBin = nodePath + "node.exe";
        String mermaidJS = nodePath + "node_modules\\mermaid\\bin\\mermaid.js";
        String phatomJS = nodePath + "node_modules\\phantomjs\\lib\\phantom\\phantomjs.exe";
        String outputDir = "D:\\";
        String filePath = nodePath + "node_modules\\mermaid\\bin\\altaAgentes.mmd";


        File tempGraph = new File("");
        FileWriter fileWriter = new FileWriter(tempGraph);
        fileWriter.write(bpelFlowChartController.writeResponse());
        fileWriter.flush();

        String command = "cmd /c " + nodeBin + " " + mermaidJS + " -e " + phatomJS + " " + " -o " + outputDir + " " +  filePath ;
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        String imageDataString = Base64.encodeBase64String(Files.readAllBytes(Paths.get("D:\\altaAgentes.mmd.png")));



        model.addObject("graph", bpelFlowChartController.writeResponse());
        model.addObject("process", bpel);
        model.addObject("state", translateState(bpelFlowChartController.getBpelInstance().getState()));
        model.addObject("status", bpelFlowChartController.getBpelInstance().getStatus());

        return model;
    }

    public static void loadProperties() {
        String configFile = System.getProperty("bpel-flowchart-preview");
        try {
            properties = new Properties();
            properties.load(new FileReader(new File(configFile)));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public String translateState(int state){

        String result = "";

        switch (state)
        {
            case 0:
                result = "INITIATED";
                break;
            case 1:
                result = "OPEN_RUNNING";
                break;
            case 2:
                result = "OPEN_SUSPENDED";
                break;
            case 3:
                result = "OPEN_FAULTED";
                break;
            case 4:
                result = "CLOSED_PENDING_CANCEL";
                break;
            case 5:
                result = "CLOSED_COMPLETED";
                break;
            case 6:
                result = "CLOSED_FAULTED";
                break;
            case 7:
                result = "CLOSED_CANCELLED";
                break;
            case 8:
                result = "CLOSED_ABORTED";
                break;
            case 9:
                result = "CLOSED_STALE";
                break;
            case 10:
                result = "CLOSED_ROLLED_BACK";
                break;
        }
        return result;
    }
}
