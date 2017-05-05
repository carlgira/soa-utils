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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by carlgira on 08/03/2016.
 * Class with two services to get the mermaid.js graph.
 * One service returns the text version of the graph so the browser render the image, the other service use the mermaid utility to create directly a png image, the rendered image is returned. (I just create this version for compatibility version of mermaid.js with ie9 and ie10)
 */

@Controller
public class ServletController {

    public static Properties properties;
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
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchart.htm")
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
            mainBPELPreview.buildMermaidJSGraph(partition, composite, version, bpel, bpelid);
        } catch (Exception e) {
            model.addObject("state", e.getMessage());
            return model;
        }
        MermaidJSGraphBuilder mermaidJSGraphBuilder = mainBPELPreview.getBpelFlowChartController();

        model.addObject("graph", mermaidJSGraphBuilder.writeResponse());
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
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchartImg.htm")
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
            mainBPELPreview.buildMermaidJSGraphImage(partition, composite, version, bpel, bpelid);
        } catch (Exception e) {
            model.addObject("state", e.getMessage());
            return model;
        }
        MermaidJSGraphBuilder mermaidJSGraphBuilder = mainBPELPreview.getBpelFlowChartController();

        model.addObject("graph", mainBPELPreview.getImageBase64());

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
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/img.png")
    public ResponseEntity<byte[]> flowChartImg(@PathVariable("partition") String partition,
                                               @PathVariable("composite") String composite,
                                               @PathVariable("version") String version,
                                               @PathVariable("bpel") String bpel,
                                               @RequestParam(value = "bpelid", required = true) String bpelid) throws Exception {

        MainBPELPreview mainBPELPreview = new MainBPELPreview(properties);
        mainBPELPreview.buildMermaidJSGraphImage(partition, composite, version, bpel, bpelid);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(mainBPELPreview.getImage(), headers,HttpStatus.OK);
    }

    /**
     * Service that returns an imgage with the original bpm diagram.
     * @return
     * @throws Exception
     */
    @RequestMapping( path = "/{type}/bpmImg.png")
    public ResponseEntity<byte[]> flowChartBpmImg(@PathVariable("type") String type,
                                               @RequestParam(value = "bpmId", required = true) String bpmId) throws Exception {

        MainBPELPreview mainBPELPreview = new MainBPELPreview(properties);
        byte[] r = mainBPELPreview.buildBpmGraphImage(bpmId, type);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(r, headers,HttpStatus.OK);
    }

    /**
     * Service that returns the mermaid.js text. (
     * This service check the state of the bpel instance and updates the graph template to see the actual state of the bpel.
     * @param partition The Soa Partition where the bpel reside deployed
     * @param composite The CompositeName
     * @param version The version of the composite
     * @param bpel The bpel name
     * @param bpelid The bpelid of the instance
     * @return
     * @throws Exception
     */
    @RequestMapping( path = "/{partition}/{composite}/{version}/{bpel}/flowchartString.htm")
    @ResponseBody
    public String flowChartString(@PathVariable("partition") String partition,
                                     @PathVariable("composite") String composite,
                                     @PathVariable("version") String version,
                                     @PathVariable("bpel") String bpel,
                                     @RequestParam(value = "bpelid", required = true) String bpelid) throws Exception {
        MainBPELPreview mainBPELPreview = new MainBPELPreview(ServletController.properties);
        mainBPELPreview.buildMermaidJSGraph(partition, composite, version, bpel, bpelid);

        String salida = mainBPELPreview.getBpelFlowChartController().writeResponse();

        return salida;
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
}
