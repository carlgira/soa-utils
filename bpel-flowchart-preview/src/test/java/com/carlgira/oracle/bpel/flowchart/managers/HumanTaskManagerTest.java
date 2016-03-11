package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.bpel.services.workflow.WorkflowException;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by emateo on 08/03/2016.
 */
public class HumanTaskManagerTest {

    public static void main(String args[]) throws WorkflowException, IOException {

        ClassLoader classLoader = HumanTaskManagerTest.class.getClassLoader();

        Properties prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());

        String server = prop.getProperty("server");
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);
        String compositeId = "1820046";

        HumanTaskManager humanTaskManager = new HumanTaskManager(serverConnection);
        humanTaskManager.init();
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
                    + " " + task.getSystemAttributes().getWorkflowPattern()
            );
        }

    }
}
