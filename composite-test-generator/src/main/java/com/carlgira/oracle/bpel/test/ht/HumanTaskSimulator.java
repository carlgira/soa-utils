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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class HumanTaskSimulator implements Runnable {

    private TestCase testCase;
    private HumanTaskManager humanTaskManager;
    private String compositeId;
    private Boolean running;

    public HumanTaskSimulator(TestCase testCase, HumanTaskManager humanTaskManager, String compositeId) {
        this.testCase = testCase;
        this.humanTaskManager = humanTaskManager;
        this.compositeId = compositeId;
        this.running = true;
    }

    @Override
    public void run() {
        Long time = System.currentTimeMillis();
        while(running){
            try{
                Thread.sleep(1000);
            }
            catch (Exception e){
            }

            if(System.currentTimeMillis() - time > this.testCase.timeout){
                running = false;
                break;
            }
            HtQuery htQuery = new HtQuery(TableConstants.WFTASK_COMPOSITEINSTANCEID_COLUMN, Predicate.OP_EQ, this.compositeId);
            List<Task> tasks =  this.humanTaskManager.getTasklist(htQuery);
            if(tasks.size() > 0){
                for(Task task : tasks){
                    for(HumanTask humanTask : this.testCase.humanTaskList){

                        if(task.getTaskDefinitionId().endsWith("/" + humanTask.name)){
                            try {
                                this.humanTaskManager.setTaskOutcome(task, humanTask.outcome );
                            } catch (WorkflowException e) {
                                e.printStackTrace();
                            } catch (StaleObjectException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws IOException, WorkflowException {

        //TestSuite testSuite1 = TestGenerator.genTestSuite("/tmp/Agentes/AltaAgentes");
        //new ObjectMapper().writeValue(new FileOutputStream("/tmp/AltaAgentes.json"), testSuite1);

        String server =  args[0]; //"t3://localhost:8001/soa-infra/";
        String user = args[1]; //"weblogic";
        String pass = args[2]; // "weblogic1";
        String realm = args[3]; // "jazn.com";
        String compositeId = args[4]; //"20001";
        String testSuiteJson = args[5];

        HumanTaskManager humanTaskManager = new HumanTaskManager(server, user, pass, realm);
        humanTaskManager.connectToServer();

        TestSuite testSuite = TestSuite.getTestSuite(testSuiteJson);

        HumanTaskSimulator humanTaskSimulator = new HumanTaskSimulator(testSuite.testCaseList.get(0), humanTaskManager, compositeId ) ;
        Thread t = new Thread(humanTaskSimulator);
        t.start();
    }
}
