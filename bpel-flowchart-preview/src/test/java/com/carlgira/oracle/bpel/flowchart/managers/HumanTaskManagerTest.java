package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.bpel.services.workflow.StaleObjectException;
import oracle.bpel.services.workflow.WorkflowException;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.SystemAttributesType;
import oracle.bpel.services.workflow.task.model.Task;

import javax.swing.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by carlgira on 08/03/2016.
 */
public class HumanTaskManagerTest {

    private Properties prop;
    private ClassLoader classLoader;

    public HumanTaskManagerTest() throws IOException {
        classLoader = HumanTaskManagerTest.class.getClassLoader();
        prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());
    }

    public List<Task> testHumanTaskManager(String compositeId) throws WorkflowException {
        //String server = prop.getProperty("server");
        String server = "t3://192.168.100.204:8001/soa-infra/";
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        HumanTaskManager humanTaskManager = new HumanTaskManager(serverConnection);
        humanTaskManager.init();
        HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, compositeId);
        HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant"); // Not FYI

        List<HtQuery> htQueryList = new ArrayList<>();
        htQueryList.add(htQuery);
        htQueryList.add(htQuery1);

        return humanTaskManager.getTasklist(htQueryList);
    }


    public List<Task> testHumanTaskManagerTT() throws WorkflowException, ParseException {
        //String server = prop.getProperty("server");
        String server = "t3://192.168.100.204:8001/soa-infra/";
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String dateInString = "10-Jul-2014";
        Date date = formatter.parse(dateInString);



        HumanTaskManager humanTaskManager = new HumanTaskManager(serverConnection);
        humanTaskManager.init();
        HtQuery htQuery = new HtQuery(TableConstants.WFTASK_VERSIONREASON_COLUMN, Predicate.OP_EQ, "TASK_VERSION_REASON_INITIATED");
        HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "FYI");
        HtQuery htQuery2 = new HtQuery(TableConstants.WFTASK_ASSIGNEESDISPLAYNAME_COLUMN, Predicate.OP_EQ, "bpeladmin");
        HtQuery htQuery3 = new HtQuery(TableConstants.WFTASK_CREATEDDATE_COLUMN, Predicate.OP_LT, date);
        HtQuery htQuery4 = new HtQuery(TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, "ASSIGNED");

        // Not FYI

        List<HtQuery> htQueryList = new ArrayList<>();
        htQueryList.add(htQuery);
        htQueryList.add(htQuery1);
        htQueryList.add(htQuery2);
        htQueryList.add(htQuery3);
        htQueryList.add(htQuery4);

        return humanTaskManager.getTasklist(htQueryList);
    }

    public static void main(String args[]) throws WorkflowException, IOException, ParseException, StaleObjectException {

        String server = "t3://nbpm-om.omel.es:80/soa-infra/";
        String user = "bpeladmin";
        String pass = "bpeladmin";
        String realm = "jazn.com";

        ServerConnection serverConnection = new ServerConnection(server,user,pass, realm);

        HumanTaskManager humanTaskManager = new HumanTaskManager(serverConnection);
        humanTaskManager.init();

        String comId = "2920046";
        HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, comId);
        HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "Participant");

        List<HtQuery> htQueryList = new ArrayList<>();
        htQueryList.add(htQuery);
        htQueryList.add(htQuery1);
        List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

        for(Task task : tasks){
            System.out.println(task.getTaskDefinitionId() + " " + task.getSystemAttributes().getOutcome() + (task.getSystemAttributes().getOutcome() != null));
        }


        /*
        try {
            String server = "t3://192.168.100.204:8001/soa-infra/";
            String user = "bpeladmin";
            String pass = "bpeladmin";
            String realm = "jazn.com";

            ServerConnection serverConnection = new ServerConnection(server, user, pass, realm);
            HumanTaskManager humanTaskManager = new HumanTaskManager(serverConnection);
            humanTaskManager.init();


            int month = 9;
            while (month <= 9) {

                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String dateInString = "25-" + month + "-2015";
                Date date = formatter.parse(dateInString);

                System.out.println(dateInString);

                HtQuery htQuery = new HtQuery(TableConstants.WFTASK_VERSIONREASON_COLUMN, Predicate.OP_EQ, "TASK_VERSION_REASON_INITIATED");
                HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_WORKFLOWPATTERN_COLUMN, Predicate.OP_EQ, "FYI");
                //HtQuery htQuery2 = new HtQuery(TableConstants.WFTASK_ASSIGNEESDISPLAYNAME_COLUMN, Predicate.OP_EQ, "bpeladmin");
                HtQuery htQuery3 = new HtQuery(TableConstants.WFTASK_CREATEDDATE_COLUMN, Predicate.OP_LT, date);
                HtQuery htQuery4 = new HtQuery(TableConstants.WFTASK_STATE_COLUMN, Predicate.OP_EQ, "ASSIGNED");

                // Not FYI

                List<HtQuery> htQueryList = new ArrayList<>();
                htQueryList.add(htQuery);
                htQueryList.add(htQuery1);
                //htQueryList.add(htQuery2);
                htQueryList.add(htQuery3);
                htQueryList.add(htQuery4);

                List<Task> tasks = humanTaskManager.getTasklist(htQueryList);

                List<String> tasksId = new ArrayList<>();

                for(Task task : tasks){
                    tasksId.add(task.getSystemAttributes().getTaskId());
                }

                System.out.println(tasks.size());

                humanTaskManager.setTasksOutcome(tasksId, "WITHDRAW");
                //humanTaskManager.setTasksOutcome(tasksId, "DISMISS");

                if (tasks.size() < 200) {
                    month++;
                    continue;
                }
            }
            JOptionPane.showMessageDialog(null, "Terminado");
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error");
        }
        */

        //humanTaskManager.setTaskOutcome(tasks.get(0), "DISMISS");


/*
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String dateInString = "09-Jul-2014";
        Date date = formatter.parse(dateInString);

        String d = new Date(System.currentTimeMillis() - date.getTime() ).toString();

        System.out.println((System.currentTimeMillis() - date.getTime())/1000/86400 );
        System.out.println(d);
*/



    }
}
