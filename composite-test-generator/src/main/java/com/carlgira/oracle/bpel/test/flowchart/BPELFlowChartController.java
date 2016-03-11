package com.carlgira.oracle.bpel.test.flowchart;

import com.carlgira.oracle.bpel.test.composite.CompositeManager;
import com.carlgira.oracle.bpel.test.ht.HtQuery;
import com.carlgira.oracle.bpel.test.ht.HumanTaskManager;
import com.carlgira.oracle.bpel.test.model.ServerConnection;
import com.carlgira.util.JAXBMarshaller;
import com.oracle.schemas.bpel.audit_trail.AuditTrail;
import com.oracle.schemas.bpel.audit_trail.Event;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.bpel.BPELInstance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by emateo on 07/03/2016.
 */
public class BPELFlowChartController {

    private Map<String, Node> nodes;
    private Map<String, List<Node>> nodesByType;
    private Map<String, List<Node>> links;
    private List<Link> listOfLinks;
    private List<String> restOfFile;
    private String linkStyle;
    private String graph;

    public BPELFlowChartController() {
        this.restOfFile = new ArrayList<>();
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
        this.listOfLinks = new ArrayList<>();
        this.nodesByType = new HashMap<>();


        this.nodesByType.put("ht", new ArrayList<Node>());
        this.nodesByType.put("init", new ArrayList<Node>());
        this.nodesByType.put("fn", new ArrayList<Node>());
        this.nodesByType.put("bpel", new ArrayList<Node>());
        this.nodesByType.put("ws", new ArrayList<Node>());
        this.nodesByType.put("obj", new ArrayList<Node>());
    }

    public Map<String, List<Node>> getNodesByType() {
        return nodesByType;
    }

    public void constructFlowChart(HumanTaskManager humanTaskManager, String compositeId) {

        HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, compositeId);
        HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant");

        List<HtQuery> htQueryList = new ArrayList<>();
        htQueryList.add(htQuery);
        htQueryList.add(htQuery1);

        List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

        for (Node node : getNodesByType().get("ht")) {
            for (Task task : tasks) {
                String taskId = node.id.substring(node.id.indexOf("_")+1);
                if (task.getTaskDefinitionId().contains(taskId)) {
                    if (task.getSystemAttributes().getOutcome() != null && !task.getSystemAttributes().getOutcome().trim().isEmpty()) {

                        for (int i = 0; i < this.listOfLinks.size(); i++) {
                            Link link = this.listOfLinks.get(i);
                            if (link.beginNode.contains(node.id)) {
                                link.message = task.getSystemAttributes().getOutcome();
                                this.listOfLinks.set(i, link);
                                node.active = true;
                                this.nodes.put(node.id, node);
                            }
                        }
                    }
                }
            }
        }
    }

    public void constructObjFlowChart(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get("obj")){
            if(auditTrailManager.getEvent(node.id) != null){
                node.active = true;
                this.nodes.put(node.id, node);
            }
        }
    }

    public void constructEndFlowChart(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get("fn")){
            if(auditTrailManager.getEvent(node.id) != null){
                node.active = true;
                this.nodes.put(node.id, node);
            }
        }
    }

    public void constructWSFlowChart(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get("ws")){
            if(auditTrailManager.getEvent(node.id, "5") != null){
                node.active = true;
                this.nodes.put(node.id, node);
            }
            else {
                Event e = auditTrailManager.getEventWithError(node.id);
                if(e != null){
                    node.active = true;
                    node.state = Integer.parseInt(e.getState());
                    this.nodes.put(node.id, node);
                }
            }
        }
    }

    public void constructBPELFlowChart(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get("bpel")){
            if(auditTrailManager.getEvent(node.id, "5") != null){
                node.active = true;
                this.nodes.put(node.id, node);
            }
            else {
                Event e = auditTrailManager.getEventWithError(node.id);
                if(e != null){
                    node.active = true;
                    node.state = Integer.parseInt(e.getState());
                    this.nodes.put(node.id, node);
                }
            }
        }
    }

    public void buildStyles(){
        String htString = "";
        String htStringError = "";
        for(Node node : this.nodesByType.get("ht")){
            Node rnode = this.nodes.get(node.id);
            if(rnode.state > 5){
                htStringError+= rnode.type + "_" + rnode.id + ",";
            }
            else {
                htString+=  rnode.type + "_" + rnode.id + ",";
            }
        }

        if(htString.length() > 0){
            htString = htString.substring(0 , htString.length()-1);
            htString = "class " + htString + " ht;";
            this.restOfFile.add(htString);
        }


        if(htStringError.length() > 0){
            htStringError = htStringError.substring(0, htStringError.length() -1 );
            htStringError = "class " + htStringError + " htError";
            this.restOfFile.add(htStringError);
        }

        String wsString = "";
        String wsStringError = "";
        for(Node node : this.nodesByType.get("bpel")){
            Node rnode = this.nodes.get(node.id);
            if(rnode.state > 5){
                wsStringError+= rnode.type + "_" + rnode.id + ",";
            }
            else {
                wsString+=  rnode.type + "_" + rnode.id + ",";
            }
        }

        for(Node node : this.nodesByType.get("ws")){
            Node rnode = this.nodes.get(node.id);
            if(rnode.state > 5){
                wsStringError+= rnode.type + "_" + rnode.id + ",";
            }
            else {
                wsString+=  rnode.type + "_" + rnode.id + ",";
            }
        }

        if(wsString.length() > 0){
            wsString = wsString.substring(0 , wsString.length()-1);
            wsString = "class " + wsString + " ws;";
            this.restOfFile.add(wsString);
        }

        if(wsStringError.length() > 0){
            wsStringError = wsStringError.substring(0, wsStringError.length() -1 );
            wsStringError = "class " + wsStringError + " wsError";
            this.restOfFile.add(wsStringError);
        }
        this.restOfFile.add("\n");
    }

    public void drawLinks(){
        for(int i= 0;i < this.listOfLinks.size();i++){
            Link link = this.listOfLinks.get(i);
            Node beginNode = this.nodes.get(link.beginNode.substring(link.beginNode.indexOf("_") +1));
            Node endNode = this.nodes.get(link.endNode.substring(link.endNode.indexOf("_")+1));
            if(beginNode.active && endNode.active){
                this.restOfFile.add(this.linkStyle.replace("linkStyle 0", "linkStyle " + i));
            }
        }
    }

    public void drawInitLinks(){
        Node initNode = this.nodesByType.get("init").get(0);
        initNode.active = true;
    }

    public void drawLinksOfNode(Node node){
        for(int i = 0; i < this.listOfLinks.size();i++){
            Link link = this.listOfLinks.get(i);
            if( (node.type + "_" + node.id).equals(link.beginNode)) {
                this.restOfFile.add(this.linkStyle.replace("linkStyle 0", "linkStyle " + i));
            }
        }
    }

    public String writeResponse(){
        String response = this.graph + "\n";

        for(Node node : this.nodes.values()){
            response+= node.originalLine + "\n";
        }

        response+= "\n";

        for(Link link : this.listOfLinks){
            if(link.message == null){
                response += link.beginNode + "-->" + link.endNode + "\n";
            }
            else {
                response += link.beginNode + "-->" + "|"  +  link.message + "|" +  link.endNode + "\n";
            }
        }

        response+= "\n";

        for(String line : this.restOfFile){
            response += line + "\n";
        }

        return  response;
    }

    public static void main(String args[]) throws Exception {

        BPELFlowChartController bpelFlowChartController = new BPELFlowChartController();
        bpelFlowChartController.parseFile("D:\\cgiraldo\\test\\AltaAgentesGrafo.txt");
        bpelFlowChartController.drawInitLinks();


        String server = "t3://192.168.100.228:8001/soa-infra/"; //"t3://localhost:8001/soa-infra/";192.168.239.228:8001/deadlock-detector-service
        String user = "bpeladmin"; //"weblogic";
        String pass = "bpeladmin"; // "weblogic1";
        String realm = "jazn.com"; // "jazn.com";
        String compositeId = "1820046"; //"1820046";

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        HumanTaskManager humanTaskManager = new HumanTaskManager(server, user, pass, realm);
        humanTaskManager.connectToServer();


        String componentName = "AltaAgentes";
        CompositeDN compositeDN = new CompositeDN("Agentes/AltaAgentes!5.5");

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();

        BPELInstance bpelInstance = compositeManager.getBPELById(compositeDN, componentName, "8320045");


        String auditTrail = bpelInstance.getAuditTrail().toString();
        AuditTrailManager auditTrailManager = new AuditTrailManager(auditTrail);

        bpelFlowChartController.constructFlowChart(humanTaskManager, compositeId);
        bpelFlowChartController.constructObjFlowChart(auditTrailManager);
        bpelFlowChartController.constructWSFlowChart(auditTrailManager);
        bpelFlowChartController.constructBPELFlowChart(auditTrailManager);
        bpelFlowChartController.constructEndFlowChart(auditTrailManager);
        bpelFlowChartController.buildStyles();
        bpelFlowChartController.drawLinks();

        System.out.println(bpelFlowChartController.writeResponse());
    }

    public void parseFile(String fileName) throws Exception {

        List<String> flowChart = Files.readAllLines(Paths.get(fileName), Charset.forName("utf-8"));

        int index = 1;
        this.graph = flowChart.get(0);
        while (!flowChart.get(index).isEmpty()) {
            String line = flowChart.get(index);
            Node node = Node.parseNode(line);
            if (this.nodes.containsKey(node.id)) {
                throw new Exception("Key with same name already exists, " + node.id);
            }
            this.nodes.put(node.id, node);
            List<Node> nodesByTypeTemp = this.nodesByType.get(node.type);
            nodesByTypeTemp.add(node);
            this.nodesByType.put(node.type, nodesByTypeTemp);
            index++;
        }
        index++;
        while (!flowChart.get(index).isEmpty()) {
            String line = flowChart.get(index);
            Link link = Link.parseLink(line);
            this.listOfLinks.add(link);
            if (this.links.containsKey(link.beginNode)) {
                List<Node> listNodes = this.links.get(link.beginNode);
                listNodes.add(this.nodes.get(link.beginNode));
                this.links.put(link.beginNode, listNodes);
            } else {
                List<Node> listNodes = new ArrayList<>();
                listNodes.add(this.nodes.get(link.beginNode));
            }
            index++;
        }
        index++;

        this.restOfFile = flowChart.subList(index, flowChart.size()-1);
        this.linkStyle = flowChart.get(flowChart.size() - 1);

    }
}
