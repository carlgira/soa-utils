package com.carlgira.oracle.bpel.test.composite;


import com.carlgira.oracle.bpel.test.model.testcase.TestSuite;
import oracle.soa.management.facade.*;
import oracle.soa.management.util.ComponentInstanceFilter;

import javax.naming.Context;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by emateo on 03/03/2016.
 */
public class CompositeManager {

    private String regexPattern;
    private String value;
    private TestSuite testSuite;
    private String compositeId;
    private Locator locator;
    private Component lookupComponent;


    public CompositeManager(String regexPattern, String value, TestSuite testSuite) {
        this.regexPattern = regexPattern;
        this.value = value;
        this.testSuite = testSuite;
    }

    public static void main(String args[]) throws Exception {

        String testSuiteJson = "D:\\cgiraldo\\test\\ContratoREMIT.json";
        TestSuite testSuite = TestSuite.getTestSuite(testSuiteJson);

        CompositeManager compositeManager = new CompositeManager("Tarjeta>@value<\\/(.*):Tarjeta>", "51153JM_OMEL", testSuite);
        compositeManager.init();
        String compId = compositeManager.searchComposite();
    }

    public void init() throws Exception {
        Hashtable jndiProps = new Hashtable();
        jndiProps.put(Context.PROVIDER_URL, testSuite.server.serverUrl);
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.SECURITY_PRINCIPAL, testSuite.server.adminUser);
        jndiProps.put(Context.SECURITY_CREDENTIALS, testSuite.server.adminPassword);
        jndiProps.put("dedicated.connection","true");

        this.locator = LocatorFactory.createLocator(jndiProps);
        String componentName = testSuite.partition+"/"+testSuite.name+"!"+testSuite.version+"/"+testSuite.name+"";
        this.lookupComponent = locator.lookupComponent(componentName);
    }


   public  String searchComposite() throws Exception {
       ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
       compInstFilter.setMinCreationDate(new Date(System.currentTimeMillis() - 200000));

       List<ComponentInstance> compInstances = lookupComponent.getInstances(compInstFilter);

       if (compInstances != null) {
           Pattern r = Pattern.compile(regexPattern);
           for (ComponentInstance compInst : compInstances) {
               String auditTrail = compInst.getAuditTrail().toString();
               Matcher m = r.matcher(auditTrail);

               if(m.find()){
                   return compositeId = compInst.getCompositeInstanceId();
               }
           }
       }
       return null;
   }
}
