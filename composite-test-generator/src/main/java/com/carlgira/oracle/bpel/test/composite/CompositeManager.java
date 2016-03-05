package com.carlgira.oracle.bpel.test.composite;


import com.carlgira.oracle.bpel.test.model.testcase.CompositeComponent;
import com.carlgira.oracle.bpel.test.model.testcase.Server;
import com.carlgira.oracle.bpel.test.model.testcase.TestCase;
import com.carlgira.oracle.bpel.test.model.testcase.TestSuite;
import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.BPELInstance;
import oracle.soa.management.internal.facade.bpel.BPELInstanceImpl;
import oracle.soa.management.util.ActivityInstanceFilter;
import oracle.soa.management.util.ComponentInstanceFilter;
import oracle.soa.management.util.CompositeInstanceFilter;

import javax.naming.Context;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlgira on 03/03/2016.
 */
public class CompositeManager {

    private TestSuite testSuite;
    private Locator locator;
    private Component lookupComponent;
    private Composite lookupComposite;
    private CompositeComponent compositeComponent;


    public CompositeManager(TestSuite testSuite, CompositeComponent compositeComponent) {
        this.testSuite = testSuite;
        this.compositeComponent = compositeComponent;
    }

    public static void main(String args[]) throws Exception {

        String testSuiteJson = "/home/carlgira/files/HumanTaskComposite.json";
        TestSuite testSuite = TestSuite.getTestSuite(testSuiteJson);

        CompositeManager compositeManager = new CompositeManager(testSuite, testSuite.compositeComponents.get(0));
        compositeManager.init();
        //ComponentInstance compId = compositeManager.getCompositeByPayloadRegex("case_001");

        ComponentInstance compId = compositeManager.getComponentById("20001");
        BPELInstance bpelInstance = (BPELInstance)compId;

        System.out.println(bpelInstance.getAuditTrail());

        for(ActivityInstance activityInstance : bpelInstance.getActivities()){
            System.out.println(activityInstance.getId());
        }
    }

    public void init() throws Exception {
        Hashtable jndiProps = new Hashtable();
        jndiProps.put(Context.PROVIDER_URL, testSuite.server.serverUrl);
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.SECURITY_PRINCIPAL, testSuite.server.adminUser);
        jndiProps.put(Context.SECURITY_CREDENTIALS, testSuite.server.adminPassword);
        jndiProps.put("dedicated.connection","true");

        this.locator = LocatorFactory.createLocator(jndiProps);

        String compositeName = testSuite.partition+"/"+testSuite.name+"!"+testSuite.version;
        String componentName = compositeName + "/"+this.compositeComponent.name;

        this.lookupComponent = locator.lookupComponent(componentName);
        this.lookupComposite = locator.lookupComposite(compositeName);
    }


   public  ComponentInstance getCompositeByPayloadRegex(String value) throws Exception {
       ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
       compInstFilter.setMinCreationDate(new Date(System.currentTimeMillis() - 200000)); // Last 20 seconds

       List<ComponentInstance> compInstances = lookupComponent.getInstances(compInstFilter);

       if (compInstances != null) {
           Pattern r = Pattern.compile(this.compositeComponent.payloadRegexLookupComposite.replace("@value", value));
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

    public ComponentInstance getComponentById(String value) throws Exception {
        ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
        compInstFilter.setCompositeInstanceId(value);

        List<ComponentInstance> compInstances = lookupComponent.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return compInstances.get(0);
        }
        return null;
    }

    public CompositeInstance getCompositeById(String id) throws Exception {
        CompositeInstanceFilter compInstFilter = new CompositeInstanceFilter();
        compInstFilter.setId(id);

        List<CompositeInstance> compInstances = this.lookupComposite.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return compInstances.get(0);
        }

        BPELInstanceImpl bpelInstance = new BPELInstanceImpl();

        return null;
    }



    /*
    public List<ActivityInstance> getActivities(ActivityInstanceFilter filter)
            throws Exception
    {
        return this.locator.executeServiceEngineMethod(getServiceEngine(), "getActivities", new Object[] { filter });
    }
    */
}
