package com.carlgira.oracle.bpel.flowchart.controller;

import com.carlgira.oracle.bpel.flowchart.MermaidJSGraphBuilder;
import com.carlgira.oracle.bpel.flowchart.MainBPELPreview;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.util.Properties;

/**
 * Created by carlgira on 08/03/2016.
 * Class with two services to get the mermaid.js graph.
 * One service returns the text version of the graph so the browser render the image, the other service use the mermaid utility to create directly a png image, the rendered image is returned. (I just create this version for compatibility version of mermaid.js with ie9 and ie10)
 */

@Controller
public class ServletController {

    private static Properties properties;
    static{
        loadProperties();
    }

    /**
     * Service that returns a page with the mermaid.js in text (using a template graph of a bpel instance)
     * This service check the state of the bpel instance and updates the graph template to see the actual state of the bpel.
     * @param partition The Soa Partition where the bpel reside deployed
     * @param composite The CompositeName
     * @param version The version of the composite
     * @param bpel The bpel name
     * @param bpelid The bpelid of the instance
     * @return
     */
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchart")
    public ModelAndView flowChart(@PathVariable("partition") String partition,
                            @PathVariable("composite") String composite,
                            @PathVariable("version") String version,
                            @PathVariable("bpel") String bpel,
                            @RequestParam(value = "bpelid", required = true) String bpelid
                            ) {
        ModelAndView model = new ModelAndView("mermaid");
        model.addObject("process", bpel);

        MainBPELPreview mainBPELPreview = new MainBPELPreview(properties);
        try {
            mainBPELPreview.bpelPreviewGraph(partition, composite, version, bpel, bpelid);
        } catch (Exception e) {
            model.addObject("state", e.getMessage());
            return model;
        }
        MermaidJSGraphBuilder mermaidJSGraphBuilder = mainBPELPreview.getBpelFlowChartController();

        model.addObject("graph", mermaidJSGraphBuilder.writeResponse());
        model.addObject("state", translateState(mermaidJSGraphBuilder.getBpelInstance().getState()));

        return model;
    }

    /**
     * Service that returns a page with the mermaid.js in a image. (using a template graph of a bpel instance)
     * This service check the state of the bpel instance and updates the graph template to see the actual state of the bpel.
     * This version use the mermaid utility to create the png image from the graph file. (Make sure to install the mermaid utility and configure it in the property file)
     * @param partition The Soa Partition where the bpel reside deployed
     * @param composite The CompositeName
     * @param version The version of the composite
     * @param bpel The bpel name
     * @param bpelid The bpelid of the instance
     * @return
     */
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchartImg")
    public ModelAndView flowChartImgString(@PathVariable("partition") String partition,
                                  @PathVariable("composite") String composite,
                                  @PathVariable("version") String version,
                                  @PathVariable("bpel") String bpel,
                                  @RequestParam(value = "bpelid", required = true) String bpelid
    )  {

        ModelAndView model = new ModelAndView("mermaidImg");
        model.addObject("process", bpel);

        MainBPELPreview mainBPELPreview = new MainBPELPreview(properties);
        try {
            mainBPELPreview.bpelPreviewGraphImgString(partition, composite, version, bpel, bpelid);
        } catch (Exception e) {
            model.addObject("state", e.getMessage());
            return model;
        }
        MermaidJSGraphBuilder mermaidJSGraphBuilder = mainBPELPreview.getBpelFlowChartController();

        model.addObject("graph", mainBPELPreview.getImageString());
        model.addObject("state", translateState(mermaidJSGraphBuilder.getBpelInstance().getState()));

        return model;
    }

    /**
     * Service that returns a imgage with the mermaid.js graph. (
     * This service check the state of the bpel instance and updates the graph template to see the actual state of the bpel.
     * This version use the mermaid utility to create the png image from the graph file. (Make sure to install
     * @param partition The Soa Partition where the bpel reside deployed
     * @param composite The CompositeName
     * @param version The version of the composite
     * @param bpel The bpel name
     * @param bpelid The bpelid of the instance
     * @return
     * @throws Exception
     */
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/img")
    public ResponseEntity<byte[]> flowChartImg(@PathVariable("partition") String partition,
                                               @PathVariable("composite") String composite,
                                               @PathVariable("version") String version,
                                               @PathVariable("bpel") String bpel,
                                               @RequestParam(value = "bpelid", required = true) String bpelid) throws Exception {

        MainBPELPreview mainBPELPreview = new MainBPELPreview(properties);
        mainBPELPreview.bpelPreviewGraphImgString(partition, composite, version, bpel, bpelid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(mainBPELPreview.getImage(), headers,HttpStatus.OK);
    }


    /**
     * Load properties function
     */
    private static void loadProperties() {
        String configFile = System.getProperty("bpel-flowchart-preview");
        try {
            properties = new Properties();
            properties.load(new FileReader(new File(configFile)));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Translate the bpel state into a string
     * @param state
     * @return
     */
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
