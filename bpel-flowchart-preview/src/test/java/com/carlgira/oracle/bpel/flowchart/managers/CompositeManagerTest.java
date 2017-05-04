package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.ComponentInstance;
import oracle.soa.management.facade.bpel.BPELInstance;
import oracle.soa.management.facade.flow.FlowInstance;

import java.io.IOException;
import java.util.Properties;
import static org.junit.Assert.*;

/**
 * Created by carlgira on 08/03/2016.
 */
public class CompositeManagerTest {

    private Properties prop;
    private ClassLoader classLoader;

    public CompositeManagerTest() throws IOException {
        ClassLoader classLoader = CompositeManagerTest.class.getClassLoader();
        prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());
    }

    public BPELInstance testCompositeManager(String bpelid) throws Exception {
        String server = prop.getProperty("server");
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        CompositeDN compositeDN = new CompositeDN("default/TestProject!1.0");
        String componentName = "TestBpel";

        ServerConnection serverConnection = new ServerConnection(server, user, pass, realm);

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();

        return compositeManager.getBPELById(compositeDN, componentName, bpelid);
    }

    public static void main(String args[]) throws Exception {
        String server = "t3://localhost:7003/soa-infra/";
        String user = "weblogic";
        String pass = "WELCOME1";
        String realm = "jazn.com";

        CompositeDN compositeDN = new CompositeDN("default/BpmProject!1.0");
        String componentName = "Process";

        ServerConnection serverConnection = new ServerConnection(server, user, pass, realm);

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();

        ComponentInstance componentInstance = compositeManager.getComponentById(compositeDN, componentName, "2");
        System.out.println(componentInstance.getServiceEngine().getEngineType());

        FlowInstance flowInstance = compositeManager.getFlowInstanceByFlowId(1L);

        for(ComponentInstance ci : flowInstance.getComponentInstances()){
            System.out.println(ci.getComponentName());
            System.out.println(ci.getAuditTrail());
            System.out.println();
        }
    }
}
