package com.carlgira.oracle.bpel.flowchart.managers;

import com.oracle.schemas.bpel.audit_trail.Event;
import oracle.soa.management.facade.bpel.BPELInstance;

import java.io.IOException;
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
        auditTrailManager = new AuditTrailManager(auditTrail);

        return auditTrailManager;
    }

    public void testGetEvent(){
        Event  event = auditTrailManager.getEvent("receiveInput");
        assertNotNull(event);
    }

    public void testGetEventStateCompleted(){
        Event  event = auditTrailManager.getEvent("receiveInput", 5);
        assertNotNull(event);
    }

    public void testGetEventError(){
        Event  event = auditTrailManager.getEventWithError("receiveInput");
        assertNotNull(event);
    }

    public static void main(String[] args) throws Exception {
        AuditTrailManagerTest auditTrailManagerTest = new AuditTrailManagerTest();
        auditTrailManagerTest.testAuditTrailManager("8530017");

        auditTrailManagerTest.testGetEvent();
        auditTrailManagerTest.testGetEventStateCompleted();
    }
}
