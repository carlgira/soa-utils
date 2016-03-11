package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.bpel.BPELInstance;

import java.util.Properties;

/**
 * Created by emateo on 08/03/2016.
 */
public class CompositeManagerTest {

    public static void main(String args[]) throws Exception {

        ClassLoader classLoader = CompositeManagerTest.class.getClassLoader();

        Properties prop = new Properties();
        prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());

        String server = prop.getProperty("server");
        String user = prop.getProperty("user");
        String pass = prop.getProperty("password");
        String realm = prop.getProperty("realm");

        String partition = "Agentes";
        String composite = "AltaAgentes";
        String version = "5.5";
        String componentName = "AltaAgentes";

        String bpelid = "8320045";

        CompositeDN compositeDN = new CompositeDN(partition+"/"+composite+"!"+version);

        ServerConnection serverConnection = new ServerConnection(server, user, pass, realm);

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();

        BPELInstance bpelInstance = compositeManager.getBPELById(compositeDN, componentName, bpelid);
    }
}
