package com.carlgira.oracle.bpel.flowchart;

import com.carlgira.oracle.bpel.flowchart.managers.*;
import com.carlgira.oracle.bpel.flowchart.mermaid.Link;
import com.carlgira.oracle.bpel.flowchart.mermaid.Node;
import com.carlgira.oracle.bpel.flowchart.mermaid.TypeOfNode;
import com.carlgira.util.CEvent;
import com.carlgira.util.ServerConnection;
import com.carlgira.util.Utils;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.ComponentInstance;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    private ComponentInstance componentInstance;

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

        this.componentInstance = compositeManager.getComponentById(compositeDN, componentName, componentId);

        if(componentInstance == null){
            throw new Exception("BPEL Instance not found");
        }

        if(!this.nodesByType.get(TypeOfNode.NODE_HT).isEmpty()){
            HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, componentInstance.getCompositeInstanceId());
            HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant");

            List<HtQuery> htQueryList = new ArrayList<>();
            htQueryList.add(htQuery);
            htQueryList.add(htQuery1);
            List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

            buildHTNodes(tasks);
        }

        String auditTrail = componentInstance.getAuditTrail().toString();
        IAuditTrailManager auditTrailManager = null;

        if(componentInstance.getServiceEngine().getEngineType().equals("bpel")){
            auditTrailManager = new BpelAuditTrailManager(auditTrail);
        }
        else{
            auditTrailManager = new BpmAuditTrailManager();
        }

        buildINITNodes(auditTrailManager);
        buildBPELNodes(auditTrailManager);
        buildFNNodes(auditTrailManager);
        buildOBJNodes(auditTrailManager);
        buildWSNodes(auditTrailManager);

        buildStyles();
    }

    /**
     * Check the mermaid graph against the task on the composite. If the task has been completed, the mermaid.js node graph is set to active.
     * @param taskList
     */
    private void buildHTNodes(List<Task> taskList) {

        List<Task> tasks = new ArrayList<>();

        for(int i=0;i<taskList.size();i++){
            Task task = taskList.get(i);
            for(int e=i+1;e<taskList.size();e++){
                if(taskList.get(i).getSystemAttributes().getTaskDefinitionId().equals(taskList.get(e).getSystemAttributes().getTaskDefinitionId())){
                    if(taskList.get(i).getSystemAttributes().getCreatedDate().before(taskList.get(e).getSystemAttributes().getCreatedDate())
                            ){
                        task = taskList.get(e);
                    }
                }
            }
            tasks.add(task);
        }

        for (Node node : this.nodesByType.get(TypeOfNode.NODE_HT)) {
            String taskId = node.id.substring(node.id.indexOf("_")+1);
            for (Task task : tasks) {
                String taskName = task.getTaskDefinitionId();
                taskName = taskName.substring(taskName.lastIndexOf("/")+1, taskName.length());
                if (taskName.equals(taskId)){
                    if (task.getSystemAttributes().getOutcome() != null && !task.getSystemAttributes().getOutcome().trim().isEmpty()) {

                        for (int i = 0; i < this.listOfLinks.size(); i++) {
                            Link link = this.listOfLinks.get(i);
                            if (link.beginNode.endsWith(node.id)) {
                                if(link.message == null){
                                    link.message = task.getSystemAttributes().getOutcome();
                                }
                                this.listOfLinks.set(i, link);
                                node.setActive(true);
                                node.createdDate = task.getSystemAttributes().getCreatedDate().getTime();
                                this.nodes.put(node.id, node);
                            }
                        }
                    }
                    else{
                        node.initiated = true;
                        this.nodes.put(node.id, node);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildOBJNodes(IAuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_OBJ);
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildFNNodes(IAuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_FN);
    }

    /**
     * Check the mermaid graph against the object in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active.
     * @param auditTrailManager
     */
    private void buildINITNodes(IAuditTrailManager auditTrailManager){
        activateNodes(auditTrailManager, TypeOfNode.NODE_INIT);
    }

    /**
     * It activates the nodes on the mermaid graph, using the completed events on the audit trail.
     * An activation means that an event was executed.
     * @param auditTrailManager
     * @param type
     */
    private void activateNodes(IAuditTrailManager auditTrailManager, String type){
        for(Node node : this.nodesByType.get(type)){
            CEvent e = null;
            if( (e = auditTrailManager.getLastEvent(node.id,5)) != null){
                node.setActive(true);
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
            else if( (e = auditTrailManager.getLastEvent(node.id)) != null){
                node.initiated = true;
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
        }
    }

    /**
     * Check the mermaid graph against the webservice call in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active, if the call return error the node style is changed.
     * @param auditTrailManager
     */
    private void buildWSNodes(IAuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get(TypeOfNode.NODE_WS)){
            CEvent e = null;
            if( (e = auditTrailManager.getLastEvent(node.id, 1)) != null){
                node.initiated = true;
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
            if((e = auditTrailManager.getLastEvent(node.id, 5)) != null){
                node.setActive(true);
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
            else if( (e = auditTrailManager.getLastEventWithError(node.id)) != null){
                node.setActive(true);
                node.createdDate = Utils.parseDate(e.getDate());
                node.state = Integer.parseInt(e.getState());
                this.nodes.put(node.id, node);
            }
        }
    }

    /**
     * Check the mermaid graph against a sub-bpel call in the bpel audit trail. If the activity has been completed, the mermaid.js node graph is set to active, if the call return error the node style is changed.
     * @param auditTrailManager
     */
    private void buildBPELNodes(IAuditTrailManager auditTrailManager){
        for(Node node : this.nodesByType.get(TypeOfNode.NODE_BPEL)){
            CEvent e = null;
            if((e = auditTrailManager.getLastEvent(node.id, 1)) != null){
                node.initiated = true;
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
            if((e = auditTrailManager.getLastEvent(node.id, 5)) != null){
                node.setActive(true);
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
            }
            else if( (e = auditTrailManager.getLastEventWithError(node.id)) != null) {
                node.setActive(true);
                node.state = Integer.parseInt(e.getState());
                node.createdDate = Utils.parseDate(e.getDate());
                this.nodes.put(node.id, node);
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
            if(rnode.state > 5 || rnode.state == 2 || rnode.state == 3 || rnode.state == 4){
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
        for(Node node : this.nodes.values()){
        }

        List<Integer> drawLinks = new ArrayList<>();

        for(int i= 0;i < this.listOfLinks.size();i++){
            Link link = this.listOfLinks.get(i);
            Node beginNode = this.nodes.get(link.beginNode.substring(link.beginNode.indexOf("_") +1));
            Node endNode = this.nodes.get(link.endNode.substring(link.endNode.indexOf("_")+1));
            if(beginNode.initiated && endNode.initiated && hasParents(beginNode) && checkNodeDates(beginNode, endNode)){
                drawLinks.add(i);


            }
            else if(this.links.get(beginNode.type + "_" + beginNode.id) != null &&
                    this.links.get(beginNode.type + "_" + beginNode.id).size() == 1 &&
                    beginNode.getActive() && hasParents(beginNode) && !isNodeInErrorState(beginNode) && checkNodeDates(beginNode, endNode)){
                drawLinks.add(i);
            }
        }

        List<Integer> linksToDraw = postLinkCheck(drawLinks);
        for(Integer linkToDraw : linksToDraw){
            this.restOfFile.add(this.linkStyle.replace("linkStyle 0", "linkStyle " + linkToDraw));
        }
    }


    private  List<Integer> postLinkCheck(List<Integer> drawLinks){
        List<Integer> result = new ArrayList<>();

        for(int i=0;i<drawLinks.size();i++){
            Link linkI = this.listOfLinks.get(drawLinks.get(i));
            Node beginNode = this.nodes.get(linkI.beginNode.substring(linkI.beginNode.indexOf("_") +1));
            if(beginNode.type.equals("init")){
                result.add(drawLinks.get(i));
               continue;
            }
            for(int e=0;e<drawLinks.size();e++){
                Link linkE = this.listOfLinks.get(drawLinks.get(e));
                Node endNode = this.nodes.get(linkE.endNode.substring(linkE.endNode.indexOf("_")+1));
                if(beginNode.id.equals(endNode.id)){
                    result.add(drawLinks.get(i));
                    break;
                }
            }
        }

        if(drawLinks.size() != result.size()){
            return postLinkCheck(result);
        }

        return result;
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
                    if(node.createdDate != null && parentNode.createdDate != null){
                        return node.createdDate.compareTo(parentNode.createdDate) >= 0;
                    }
                    return true;
                }
            }
        return false;
    }

    /**
     * Check that the beginNode has a date before that the endNode
     * @param beginNode
     * @param endNode
     * @return
     */
    private boolean checkNodeDates(Node beginNode, Node endNode){

        if(beginNode.createdDate == null || endNode.createdDate == null){
            return true;
        }
        return beginNode.createdDate.compareTo(endNode.createdDate) <= 0;
    }

    /**
     * Check if a node is in error state
     * @param node
     * @return
     */
    private boolean isNodeInErrorState(Node node){
        return node.state > 5 || node.state == 2 || node.state == 3  || node.state == 4;
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
            if(link.subGraph != null){
                response += link.subGraph  + "\n";
            }
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
        String subgraph = "";
        while (!flowChart.get(index).isEmpty()) {
            String line = flowChart.get(index);
            if(line.contains("subgraph ")){
                subgraph = line;
                index++;
                line = flowChart.get(index);
            }
            if(line.equals("end")){
                subgraph = line;
                index++;
                line = flowChart.get(index);
            }
            Link link = Link.parseLink(line);
            if(subgraph != null){
                link.subGraph = subgraph;
                subgraph = null;
            }
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
}
