package com.carlgira.oracle.bpel.test.composite;


import com.carlgira.oracle.bpel.test.model.ServerConnection;
import com.carlgira.oracle.bpel.test.model.testcase.CompositeComponent;
import com.carlgira.oracle.bpel.test.model.testcase.Server;
import com.carlgira.oracle.bpel.test.model.testcase.TestCase;
import com.carlgira.oracle.bpel.test.model.testcase.TestSuite;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.BPELInstance;
import oracle.soa.management.internal.facade.bpel.BPELInstanceImpl;
import oracle.soa.management.util.ActivityInstanceFilter;
import oracle.soa.management.util.ComponentInstanceFilter;
import oracle.soa.management.util.CompositeInstanceFilter;

import javax.naming.Context;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlgira on 03/03/2016.
 */
public class CompositeManager {

    private Locator locator;
    private ServerConnection serverConnection;

    public CompositeManager(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public static void main(String args[]) throws Exception {

        //String testSuiteJson = "/home/carlgira/files/HumanTaskComposite.json";
        String testSuiteJson = "D:\\cgiraldo\\test\\AltaAgentes.json";
        TestSuite testSuite = TestSuite.getTestSuite(testSuiteJson);

        String componentName = "AltaAgentes";
        CompositeDN compositeDN = new CompositeDN(testSuite.partition+"/"+testSuite.name+"!"+testSuite.version);

        ServerConnection serverConnection = new ServerConnection(testSuite.server.serverUrl, testSuite.server.adminUser, testSuite.server.adminPassword, testSuite.server.realm);

        CompositeManager compositeManager = new CompositeManager(serverConnection);
        compositeManager.init();
        //ComponentInstance compId = compositeManager.getCompositeByPayloadRegex("case_001");

        BPELInstance bpelInstance = compositeManager.getBPELById(compositeDN, componentName, "8320045");

        //System.out.println(bpelInstance.getAuditTrail());
        //Files.write(Paths.get("D:\\cgiraldo\\test\\AltaAgentesAudittrail.xml"), bpelInstance.getAuditTrail().toString().getBytes());

        for(ActivityInstance activityInstance : bpelInstance.getActivities()){
            System.out.println(activityInstance.getId());
        }
    }

    public void init() throws Exception {
        Hashtable jndiProps = new Hashtable();
        jndiProps.put(Context.PROVIDER_URL, this.serverConnection.server);
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.SECURITY_PRINCIPAL, this.serverConnection.adminUser);
        jndiProps.put(Context.SECURITY_CREDENTIALS, this.serverConnection.adminPassword);
        jndiProps.put("dedicated.connection","true");

        this.locator = LocatorFactory.createLocator(jndiProps);
    }

   public  ComponentInstance getComponentByPayloadRegex(CompositeDN compositeDN, String processName ,String paylodRegex, Long timeBefore) throws Exception {
       Component component = locator.lookupComponent(compositeDN, processName );
       ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
       compInstFilter.setMinCreationDate(new Date(System.currentTimeMillis() - timeBefore));

       List<ComponentInstance> compInstances = component.getInstances(compInstFilter);

       if (compInstances != null) {
           Pattern r = Pattern.compile(paylodRegex);
           for (ComponentInstance compInst : compInstances) {
               String auditTrail = compInst.getAuditTrail().toString();
               Matcher m = r.matcher(auditTrail);

               if(m.find()){
                   return compInst;
               }
           }
       }
       return null;
   }

    public BPELInstance getBPELById(CompositeDN compositeDN, String processName,String bpelId) throws Exception {
        Component component = locator.lookupComponent(compositeDN, processName );
        ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
        compInstFilter.setId(bpelId);

        List<ComponentInstance> compInstances = component.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return (BPELInstance)compInstances.get(0);
        }
        return null;
    }

    public CompositeInstance getCompositeById(CompositeDN compositeDN, String id) throws Exception {
        Composite composite = locator.lookupComposite(compositeDN);
        CompositeInstanceFilter compInstFilter = new CompositeInstanceFilter();
        compInstFilter.setId(id);

        List<CompositeInstance> compInstances = composite.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return compInstances.get(0);
        }
        return null;
    }
}
