package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.bpel.BPELInstance;

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

        CompositeDN compositeDN = new CompositeDN("default/TestSoaProject!1.0");
        String componentName = "TestBPEL";

        ServerConnection serverConnection = new ServerConnection(server, user, pass, realm);

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();

        return compositeManager.getBPELById(compositeDN, componentName, bpelid);
    }

    public static void main(String args[]) throws Exception {
        CompositeManagerTest compositeManagerTest = new CompositeManagerTest();
        BPELInstance bpelInstance = compositeManagerTest.testCompositeManager("8530017");
        assertNotNull(bpelInstance);
    }
}
