package com.carlgira.oracle.bpel.flowchart.managers;

import com.oracle.schemas.bpel.audit_trail.Event;
import oracle.soa.management.facade.bpel.BPELInstance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import static org.junit.Assert.*;

/**
 * Created by carlgira on 08/03/2016.
 */
public class AuditTrailManagerTest {

    private Properties prop;
    private ClassLoader classLoader;
    private AuditTrailManager auditTrailManager;

    public AuditTrailManagerTest() throws IOException {
        ClassLoader classLoader = AuditTrailManagerTest.class.getClassLoader();
        Properties prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());
    }

    public AuditTrailManager testAuditTrailManager(String compositeId) throws Exception {
        CompositeManagerTest compositeManagerTest = new CompositeManagerTest();
        BPELInstance  bpelInstance = compositeManagerTest.testCompositeManager(compositeId);

        assertNotNull(bpelInstance);

        String auditTrail = bpelInstance.getAuditTrail().toString();
        //System.out.println(auditTrail);

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("auof123.txt"));
        bufferedWriter.write(auditTrail);
        bufferedWriter.flush();
        bufferedWriter.close();


        auditTrailManager = new AuditTrailManager(auditTrail);

        return auditTrailManager;
    }

    public void testGetEvent(){
        Event  event = auditTrailManager.getLastEvent("receiveInput");
        assertNotNull(event);
    }

    public void testGetEventStateCompleted(){
        Event  event = auditTrailManager.getLastEvent("receiveInput", 5);
        assertNotNull(event);
    }

    public void testGetEventError(){
        Event  event = auditTrailManager.getLastEventWithError("receiveInput");
        assertNotNull(event);
    }

    public static void main(String[] args) throws Exception {
        AuditTrailManagerTest auditTrailManagerTest = new AuditTrailManagerTest();
        AuditTrailManager manager = auditTrailManagerTest.testAuditTrailManager("30014");


        //SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        //Date date = dt.parse("2015-10-29T09:26:36.225+01:00".substring(0,19));

       // auditTrailManagerTest.testGetEvent();
       // auditTrailManagerTest.testGetEventStateCompleted();
    }
}
