package com.carlgira.oracle.bpel.flowchart;

import com.carlgira.oracle.bpel.flowchart.managers.AuditTrailManager;
import com.carlgira.oracle.bpel.flowchart.managers.CompositeManager;
import com.carlgira.oracle.bpel.flowchart.managers.HtQuery;
import com.carlgira.oracle.bpel.flowchart.managers.HumanTaskManager;
import com.carlgira.oracle.bpel.flowchart.mermaid.Link;
import com.carlgira.oracle.bpel.flowchart.mermaid.Node;
import com.carlgira.oracle.bpel.flowchart.mermaid.TypeOfNode;
import com.carlgira.util.ServerConnection;
import com.oracle.schemas.bpel.audit_trail.Event;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.bpel.BPELInstance;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carlgira on 07/03/2016.
 * Class to modify a mermaid.js graph template using a real bpel instance.
 * Every nodeId of mermaid.js graph, must be the name of an activity inside of the bpel. (scopes, assigns, invoke, receives, switch etc)
 * There are 6 different types of nodeId (ht, init, fn, bpel, ws, obj) but the are only two types of behaviors, one for the human task (ht) and other for the other types.
 *    - The human task type, connects directly to the HumanTaskEngine and check if the human task has been executed and whats the outcome.
 *    - The other types use to the BpelInstace auditTrail and check if the nodes in the mermaid.js are executed.
 * To draw the links between the nodes, there is a little algorithm to see what nodes are active (an active node is a executed node) it simply adds some style to a link object between two active nodes (dashed green line)
 */
public class MermaidJSGraphBuilder {

    private Map<String, Node> nodes;
    private Map<String, List<Node>> nodesByType;
    private Map<String, List<Node>> links;
    private List<Link> listOfLinks;
    private List<String> restOfFile;
    private String linkStyle;
    private String graph;

    private ServerConnection serverConnection;
    private HumanTaskManager humanTaskManager;
    private CompositeManager compositeManager;
    private BPELInstance bpelInstance;

    /**
     * Constructor, needs the serverConnection object and the graphFile template.
     * @param serverConnection
     * @param graphFile
     * @throws Exception
     */
    public MermaidJSGraphBuilder(ServerConnection serverConnection, String graphFile) throws Exception {
        this.restOfFile = new ArrayList<>();
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
        this.listOfLinks = new ArrayList<>();
        this.nodesByType = new HashMap<>();
        this.serverConnection = serverConnection;

        for(String typeOfNode : TypeOfNode.LIST_OF_TYPES){
            this.nodesByType.put(typeOfNode, new ArrayList<Node>());
        }
        this.parseFile(graphFile);
        init();
    }

    /**
     * Init the compositeManager and the humanTaskManager
     * @throws Exception
     */
    private void init() throws Exception {
        this.humanTaskManager = new HumanTaskManager(this.serverConnection);
        if(!this.nodesByType.get(TypeOfNode.NODE_HT).isEmpty()){
            this.humanTaskManager.init();
        }

        this.compositeManager = new CompositeManager(this.serverConnection);
        this.compositeManager.init();
    }

    /**
     * It checks every node on the graph to see if the node it's executed.
     * @param compositeDN
     * @param componentName
     * @param componentId
     * @throws Exception
     */
    public void buildNodes(CompositeDN compositeDN, String componentName, String componentId) throws Exception {

        this.bpelInstance = compositeManager.getBPELById(compositeDN, componentName, componentId);

        if(bpelInstance == null){
            throw new Exception("BPEL Instance not found");
        }

        if(!this.nodesByType.get(TypeOfNode.NODE_HT).isEmpty()){
            HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, bpelInstance.getCompositeInstanceId());
            HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant");

            List<HtQuery> htQueryList = new ArrayList<>();
            htQueryList.add(htQuery);
            htQueryList.add(htQuery1);
            List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

            buildHTNodes(tasks);
        }

        String auditTrail = bpelInstance.getAuditTrail().toString();
        AuditTrailManager auditTrailManager = new AuditTrailManager(auditTrail);

        buildINITNodes(auditTrailManager);
        buildBPELNodes(auditTrailManager);
        buildFNNodes(auditTrailManager);
        buildOBJNodes(auditTrailManager);
        buildWSNodes(auditTrailManager);

        buildStyles();
    }

    /**
     * Check the mermaid graph against the task on the composite. If the task has been completed, the mermaid.js node graph is set to active.
     * @param tasks
     */
    private void buildHTNodes(List<Task> tasks) {
        for (Node node : this.nodesByType.get(TypeOfNode.NODE_HT)) {
            for (Task task : tasks) {
                String taskId = node.id.substring(node.id.indexOf("_")+1);
                if (task.getTaskDefinitionId().contains(taskId)) {
                    if (task.getSystemAttributes().getOutcome() != null && !task.getSystemAttributes().getOutcome().trim().isEmpty()) {

                        for (int i = 0; i < this.listOfLinks.size(); i++) {
                            Link link = this.listOfLinks.get(i);
                            if (link.beginNode.contains(node.id)) {
                                link.message = task.getSystemAttributes().getOutcome();
                                this.listOfLinks.set(i, link);
                                node.setActive(true);
                                this.nodes.put(node.id, node);
                            }
                        }
                    }
                    else{
                        node.initiated = true;
                        this.nodes.put(node.id, node);
                    }
                }
            }
        }
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildOBJNodes(AuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_OBJ);
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildFNNodes(AuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_FN);
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildINITNodes(AuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_INIT);
    }

    /**
     * It activates the nodes on the mermaid graph, using the completed events on the audit trail.
     * An activation means that an event was executed.
     * @param auditTrailManager
     * @param type
     */
    private void activateNodes(AuditTrailManager auditTrailManager, String type){
        for(Node node : this.nodesByType.get(type)){

            if(auditTrailManager.getEvent(node.id,5) != null){
                node.setActive(true);
                this.nodes.put(node.id, node);
            }
            else if(auditTrailManager.getEvent(node.id) != null){
                node.initiated = true;
                this.nodes.put(node.id, node);
            }
        }
    }

    /**
     * Check the mermaid graph against the webservice call in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active, if the call return error the node style is changed.
     * @param auditTrailManager
     */
    private void buildWSNodes(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get(TypeOfNode.NODE_WS)){
            if(auditTrailManager.getEvent(node.id, 1) != null){
                node.initiated = true;
                this.nodes.put(node.id, node);
            }
            if(auditTrailManager.getEvent(node.id, 5) != null){
                node.setActive(true);
                this.nodes.put(node.id, node);
            }
            else {
                Event e = auditTrailManager.getEventWithError(node.id);
                if(e != null){
                    node.setActive(true);
                    node.state = Integer.parseInt(e.getState());
                    this.nodes.put(node.id, node);
                }
            }
        }
    }

    /**
     * Check the mermaid graph against a sub-bpel call in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active, if the call return error the node style is changed.
     * @param auditTrailManager
     */
    private void buildBPELNodes(AuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get(TypeOfNode.NODE_BPEL)){
            if(auditTrailManager.getEvent(node.id, 1) != null){
                node.initiated = true;
                this.nodes.put(node.id, node);
            }
            if(auditTrailManager.getEvent(node.id, 5) != null){
                node.setActive(true);
                this.nodes.put(node.id, node);
            }
            else {
                Event e = auditTrailManager.getEventWithError(node.id);
                if(e != null){
                    node.setActive(true);
                    node.state = Integer.parseInt(e.getState());
                    this.nodes.put(node.id, node);
                }
            }
        }
    }

    /**
     * Add style to all types of node. Each kind of node has a style class for a completed state or an error state.
     */
    private void buildStyles(){

        String[] htStyles = buildStyle(TypeOfNode.NODE_HT);
        String[] bpelStyles = buildStyle(TypeOfNode.NODE_BPEL);
        String[] wsStyles = buildStyle(TypeOfNode.NODE_WS);

        if(htStyles[0].length() > 0){
            htStyles[0] = htStyles[0].substring(0 , htStyles[0].length()-1);
            htStyles[0] = "class " + htStyles[0] + " " + TypeOfNode.NODE_HT + ";";
            this.restOfFile.add(htStyles[0]);
        }

        if(htStyles[1].length() > 0){
            htStyles[1] = htStyles[1].substring(0, htStyles[1].length() -1 );
            htStyles[1] = "class " + htStyles[1] + " " + TypeOfNode.NODE_HT + "Error";
            this.restOfFile.add(htStyles[1]);
        }

        String wsString = bpelStyles[0] + wsStyles[0];
        String wsStringError = bpelStyles[1] + wsStyles[1];

        if(wsString.length() > 0){
            wsString = wsString.substring(0 , wsString.length()-1);
            wsString = "class " + wsString + " " + TypeOfNode.NODE_WS  + ";";
            this.restOfFile.add(wsString);
        }

        if(wsStringError.length() > 0){
            wsStringError = wsStringError.substring(0, wsStringError.length() -1 );
            wsStringError = "class " + wsStringError + " " + TypeOfNode.NODE_WS  + "Error";
            this.restOfFile.add(wsStringError);
        }
        this.restOfFile.add("\n");
    }

    /**
     * Auximilar function to build the style of the nodes.
     * @param type
     * @return
     */
    private String[] buildStyle(String type){
        String okClass = "";
        String errorClass = "";
        for(Node node : this.nodesByType.get(type)){
            Node rnode = this.nodes.get(node.id);
            if(rnode.state > 5){
                errorClass+= rnode.type + "_" + rnode.id + ",";
            }
            else {
                okClass+=  rnode.type + "_" + rnode.id + ",";
            }
        }

        return new String[]{okClass, errorClass};
    }

    /**
     * The template graph file must have in their last line of the file, the "linkstyle 0", with the apropiated style for the executed link.
     * This functions adds styles to all links between active nodes.
     */
    public void drawLinks(){
        for(int i= 0;i < this.listOfLinks.size();i++){
            Link link = this.listOfLinks.get(i);
            Node beginNode = this.nodes.get(link.beginNode.substring(link.beginNode.indexOf("_") +1));
            Node endNode = this.nodes.get(link.endNode.substring(link.endNode.indexOf("_")+1));
            if(beginNode.initiated && endNode.initiated && hasParents(beginNode)){
                this.restOfFile.add(this.linkStyle.replace("linkStyle 0", "linkStyle " + i));
            }
            else if(this.links.get(beginNode.type + "_" + beginNode.id) != null &&
                    this.links.get(beginNode.type + "_" + beginNode.id).size() == 1 &&
                    beginNode.getActive() && hasParents(beginNode)){
                this.restOfFile.add(this.linkStyle.replace("linkStyle 0", "linkStyle " + i));
            }
        }
    }

    /**
     * Aditional checks to avoid problems with concurrent paths and repetitive names
     * @param node
     * @return
     */
    private boolean hasParents(Node node){

        if(node.type.equals(TypeOfNode.NODE_INIT)){
            return true;
        }
            for(Link link : this.listOfLinks){
                Node parentNode = this.nodes.get(link.beginNode.substring(link.beginNode.indexOf("_")+1));
                if(link.endNode.equals(node.type + "_" + node.id) && (parentNode.getActive() || parentNode.initiated)){
                    return true;
                }
            }
        return false;
    }

    /**
     * Writes the mermaid.js graph into a String
     * @return
     */
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

    /**
     * Parse a mermaid.js graph into Java Objects
     * @param fileName
     * @throws Exception
     */
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
                this.links.put(link.beginNode, listNodes);
            }
            index++;
        }
        index++;

        this.restOfFile = flowChart.subList(index, flowChart.size()-1);
        this.linkStyle = flowChart.get(flowChart.size() - 1);
    }

    public BPELInstance getBpelInstance() {
        return bpelInstance;
    }
}
