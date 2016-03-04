package com.carlgira.oracle.bpel.test.ht;

import com.carlgira.oracle.bpel.test.TestGenerator;
import com.carlgira.oracle.bpel.test.model.testcase.HumanTask;
import com.carlgira.oracle.bpel.test.model.testcase.TestCase;
import com.carlgira.oracle.bpel.test.model.testcase.TestSuite;
import com.fasterxml.jackson.databind.ObjectMapper;
import oracle.bpel.services.workflow.StaleObjectException;
import oracle.bpel.services.workflow.WorkflowException;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.dms.instrument.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HumanTaskSimulator implements Runnable {

    private TestCase testCase;
    private HumanTaskManager humanTaskManager;
    private String compositeId;
    private Boolean running;
    private Map<String, Boolean> procesedTask; // FIX filter by completed tasks
    private Logger logger; // = Logger.getLogger(HumanTaskSimulator.class.getName());

    public HumanTaskSimulator(TestCase testCase, HumanTaskManager humanTaskManager, String compositeId) {
        this.testCase = testCase;
        this.humanTaskManager = humanTaskManager;
        this.compositeId = compositeId;
        this.running = true;
        this.procesedTask = new HashMap<>();
        this.logger = Logger.getLogger(HumanTaskSimulator.class.getName());
        this.logger.setLevel(Level.ALL);
    }

    @Override
    public void run() {
        logger.fine("Starting HumanTaskSimulator");
        Long time = System.currentTimeMillis();
        while(running){
            try{
                Thread.sleep(1000);
            }
            catch (Exception e){
            }

            if(System.currentTimeMillis() - time > this.testCase.timeout){
                running = false;
                logger.fine("Timeout Reached");
                break;
            }
            HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, this.compositeId);
            HtQuery htQuery1 = new HtQuery(TableConstants.WFTASK_ENDDATE_COLUMN, Predicate.OP_AFTER, new Date(System.currentTimeMillis() - 300000));

            List<HtQuery> htQueryList = new ArrayList<>();
            htQueryList.add(htQuery);
            htQueryList.add(htQuery1);

            List<Task> tasks =  this.humanTaskManager.getTasklist(htQueryList);

            if(tasks.size() > 0){
                for(Task task : tasks){

                    System.out.println("TASK1 " + task.getSystemAttributes().getOutcome());

                    if(this.procesedTask.containsValue(task.getSystemAttributes().getTaskId())){
                        continue;
                    }
                    System.out.println("TASK2 " + task.getSystemAttributes().getOutcome());

                    this.procesedTask.put(task.getIdentificationKey(), true);



                    for(HumanTask humanTask : this.testCase.humanTaskList){

                        if(task.getTaskDefinitionId().endsWith("/" + humanTask.name)){
                            try {
                                this.humanTaskManager.setTaskOutcome(task, humanTask.outcome );
                            } catch (WorkflowException e) {

                            } catch (StaleObjectException e) {

                            }
                        }
                    }
                }
            }
        }
        logger.fine("End HumanTaskSimulator");
    }

    public static void main(String args[]) throws IOException, WorkflowException {

        TestSuite testSuite1 = TestGenerator.genTestSuite("D:\\Usuarios\\JDeveloper\\mywork\\BPEL_Desarrollo_11g\\Agentes\\DatosAgentes");
        new ObjectMapper().writeValue(new FileOutputStream("D:\\cgiraldo\\test\\DatosAgentes.json"), testSuite1);


/*
        String server =  "t3://192.168.100.228:8001/soa-infra/"; //"t3://localhost:8001/soa-infra/";192.168.239.228:8001/deadlock-detector-service
        String user = "bpeladmin"; //"weblogic";
        String pass = "bpeladmin"; // "weblogic1";
        String realm = "jazn.com"; // "jazn.com";
        String compositeId = "2000013"; //"2000005";
        String testSuiteJson = "D:\\cgiraldo\\test\\ContratoREMIT.json";

        HumanTaskManager humanTaskManager = new HumanTaskManager(server, user, pass, realm);
        humanTaskManager.connectToServer();

        TestSuite testSuite = TestSuite.getTestSuite(testSuiteJson);

        HumanTaskSimulator humanTaskSimulator = new HumanTaskSimulator(testSuite.testCaseList.get(0), humanTaskManager, compositeId ) ;
        Thread t = new Thread(humanTaskSimulator);
        t.start();*/
    }
}
