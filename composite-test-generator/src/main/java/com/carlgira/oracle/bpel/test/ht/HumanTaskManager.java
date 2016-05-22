package com.carlgira.oracle.bpel.test.ht;

import oracle.bpel.services.workflow.StaleObjectException;
import oracle.bpel.services.workflow.WorkflowException;
import oracle.bpel.services.workflow.client.IWorkflowServiceClient;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;
import oracle.bpel.services.workflow.metadata.routingslip.model.*;
import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.repos.Ordering;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.ITaskAssignee;
import oracle.bpel.services.workflow.task.ITaskService;
import oracle.bpel.services.workflow.task.impl.TaskAssignee;
import oracle.bpel.services.workflow.task.model.IdentityTypeImpl;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.bpel.services.workflow.verification.IWorkflowContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Created by carlgira on 3/3/16.
 */
public class HumanTaskManager {

    protected IWorkflowServiceClient wfsvcClient;
    protected IWorkflowContext ctx;
    protected ITaskQueryService querySvc;

    private String serverURL;
    private String adminUser;
    private String adminPassword;
    private String realm;

    public HumanTaskManager(String serverURL, String adminUser, String adminPassword, String realm) {
        this.serverURL = serverURL;
        this.adminPassword = adminPassword;
        this.adminUser = adminUser;
        this.realm = realm;
    }

    public void connectToServer() throws WorkflowException {
        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties = new HashMap<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String>();
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.MODE, IWorkflowServiceClientConstants.MODE_DYNAMIC);

        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, this.serverURL);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL, this.adminUser);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS, this.adminPassword);
        wfsvcClient = WorkflowServiceClientFactory.getWorkflowServiceClient(WorkflowServiceClientFactory.REMOTE_CLIENT, properties, null);
        querySvc = wfsvcClient.getTaskQueryService();
        ctx = querySvc.authenticate(this.adminUser, this.adminPassword.toCharArray(), this.realm);
    }

    public void setTaskOutcome(Task task, String outcome) throws WorkflowException, StaleObjectException {

        if (task != null) {
            if (this.wfsvcClient != null) {
                ITaskService taskSvc = this.wfsvcClient.getTaskService();

                try{
                    task = taskSvc.acquireTask(ctx, task.getSystemAttributes().getTaskId());
                }
                catch (Exception e) {
                    task = querySvc.getTaskDetailsById(ctx, task.getSystemAttributes().getTaskId());
                }

                if (outcome.equals("REQUEST_INFORMATION")) {
                    String firstAssignee = "";
                    List<IdentityTypeImpl> assignees = task.getSystemAttributes().getAssignees();
                    if (assignees != null) {
                        Iterator<IdentityTypeImpl> assigneesIterator = assignees.iterator();
                        while (assigneesIterator.hasNext()) {
                            IdentityTypeImpl ident = assigneesIterator.next();
                            firstAssignee = ident.getId();
                        }
                    }
                    ITaskAssignee ta = new TaskAssignee(firstAssignee, false);
                    taskSvc.requestInfoForTask(this.ctx, task, ta);

                } else if (outcome.equals("ESCALATE")) {
                    taskSvc.escalateTask(this.ctx, task);
                } else if (outcome.equals("WITHDRAW")) {
                    taskSvc.withdrawTask(this.ctx, task);
                } else if (outcome.equals("SUSPEND")) {
                    taskSvc.suspendTask(this.ctx, task);
                } else if (outcome.equals("RESUME")) {
                    taskSvc.resumeTask(this.ctx, task);
                } else {
                    taskSvc.updateTaskOutcome(this.ctx, task.getSystemAttributes().getTaskId(), outcome);
                }
            }
        }
    }

    public List<Task> getTasklist(HtQuery htQuery) {
        List<HtQuery> htQueries = new ArrayList<HtQuery>();
        htQueries.add(htQuery);
        return getTasklist(htQueries);
    }

    public List<Task> getTasklist(List<HtQuery> htQuery) {
        List<Task> returnList = null;
        try {
            ITaskQueryService querySvc = wfsvcClient.getTaskQueryService();

            if (realm != null && realm.trim().length() > 0)
                ctx = querySvc.authenticate(this.adminUser, this.adminPassword.toCharArray(), this.realm);

            List queryColumns = new ArrayList<>();
            queryColumns.add(TableConstants.WFTASK_TASKID_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_TITLE_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_TASKDEFINITIONNAME_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_OUTCOME_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_ASSIGNEES_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());
            queryColumns.add(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN.getName());

            Predicate predicate = buildPredicate(htQuery);

            Ordering ordering = new Ordering(TableConstants.WFTASK_TASKNUMBER_COLUMN, true, false);

            List<Task> tasks = querySvc.queryTasks(ctx,
                    queryColumns,
                    null,  // emptyList,
                    ITaskQueryService.AssignmentFilter.ALL,
                    null,
                    predicate,
                    ordering,
                    0,
                    0);

            returnList = tasks;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnList;
    }

    protected Predicate buildPredicate(List<HtQuery> htQuerys) throws Exception {
        Predicate predicate = null;
        if(htQuerys != null && !htQuerys.isEmpty()){
            for (HtQuery htQuery : htQuerys) {
                Predicate predicateElement = new Predicate(htQuery.column, htQuery.operator, htQuery.value);
                if (predicate == null){
                    predicate = predicateElement; // Just the first time
                }
                else {
                    predicate = new Predicate(predicate, Predicate.AND, predicateElement);
                }
            }
        }
        return predicate;
    }

    public static void main(String args[])  throws WorkflowException
    {

        String server =  "t3://192.168.100.228:8001/soa-infra/"; //"t3://localhost:8001/soa-infra/";192.168.239.228:8001/deadlock-detector-service
        String user = "bpeladmin"; //"weblogic";
        String pass = "bpeladmin"; // "weblogic1";
        String realm = "jazn.com"; // "jazn.com";
        String compositeId = "1820046"; //"1820046";

        HumanTaskManager humanTaskManager = new HumanTaskManager(server, user, pass, realm);
        humanTaskManager.connectToServer();
        HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, compositeId);
        HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant");

        List<HtQuery> htQueryList = new ArrayList<>();
        htQueryList.add(htQuery);
        htQueryList.add(htQuery1);

        List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

        for(Task task: tasks){

            System.out.println(task.getTaskDefinitionId() + " " + task.getSystemAttributes().getOutcome()
            + " " + task.getSystemAttributes().getActivityName()
            + " " + task.getSystemAttributes().getActionDisplayName()
                    + " " + task.getCategory()
                    + " " + task.getSystemAttributes().getWorkflowPattern()
           );
        }

    }

}