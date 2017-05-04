package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.ServerConnection;
import oracle.soa.management.CompositeDN;
import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.BPELInstance;
import oracle.soa.management.facade.flow.FlowInstance;
import oracle.soa.management.util.ComponentInstanceFilter;
import oracle.soa.management.util.CompositeInstanceFilter;
import oracle.soa.management.util.flow.FlowInstanceFilter;

import javax.naming.Context;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carlgira on 03/03/2016.
 * Class to control the connection to the SOA server to lookup for BPEL or Composite instances.
 */
public class CompositeManager {

    private Locator locator;
    private ServerConnection serverConnection;

    public CompositeManager(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * Create server connection
     * @throws Exception
     */
    public void init() throws Exception {
        Hashtable jndiProps = new Hashtable();
        jndiProps.put(Context.PROVIDER_URL, this.serverConnection.server);
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
        jndiProps.put(Context.SECURITY_PRINCIPAL, this.serverConnection.adminUser);
        jndiProps.put(Context.SECURITY_CREDENTIALS, this.serverConnection.adminPassword);
        jndiProps.put("dedicated.connection","true");

        this.locator = LocatorFactory.createLocator(jndiProps);
    }

    /**
     * Use with careful! . Looks for a component just specifying a regex expression that will be checked against the payload
     * It has a timeBefore parameter used as filter to check against instances executed in the last N previous miliseconds
     * @param compositeDN The compositeDN
     * @param processName BPEl process name
     * @param paylodRegex Payload regex to identify a request
     * @param timeBefore Time in milliseconds to include the composites initiated in that range
     * @return
     * @throws Exception
     */
   public ComponentInstance getComponentByPayloadRegex(CompositeDN compositeDN, String processName ,String paylodRegex, Long timeBefore) throws Exception {
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

    /**
     * Search in the engine for a BPElInstance using the bpelId
     * @param compositeDN The compositeDN
     * @param processName BPEL process name
     * @param bpelId BPEL id
     * @return
     * @throws Exception
     */
    public BPELInstance getBPELById(CompositeDN compositeDN, String processName,String bpelId) throws Exception {
        return (BPELInstance)getComponentById(compositeDN,processName, bpelId );
    }

    /**
     * Search for a component by the id
     * @param compositeDN
     * @param componentName
     * @param id
     * @return
     * @throws Exception
     */
    public ComponentInstance getComponentById(CompositeDN compositeDN, String componentName,String id) throws Exception {
        Component component = locator.lookupComponent(compositeDN, componentName );
        ComponentInstanceFilter compInstFilter = new ComponentInstanceFilter();
        compInstFilter.setId(id);

        List<ComponentInstance> compInstances = component.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return compInstances.get(0);
        }
        return null;
    }

    /**
     * Search in the engine for a CompositeInstance using the compositeId
     * @param compositeDN The compositeDN
     * @param compositeId Composite Id
     * @return
     * @throws Exception
     */
    public CompositeInstance getCompositeById(CompositeDN compositeDN, String compositeId) throws Exception {
        Composite composite = locator.lookupComposite(compositeDN);
        CompositeInstanceFilter compInstFilter = new CompositeInstanceFilter();
        compInstFilter.setId(compositeId);

        List<CompositeInstance> compInstances = composite.getInstances(compInstFilter);

        if (compInstances != null && !compInstances.isEmpty()) {
            return compInstances.get(0);
        }
        return null;
    }

    /**
     * Return a FlowInstance using a Flowid
     * @param flowId
     * @return
     * @throws Exception
     */
    public FlowInstance getFlowInstanceByFlowId(Long flowId) throws Exception {
        FlowInstanceFilter flowInstanceFilter = new FlowInstanceFilter();
        flowInstanceFilter.setFlowId(flowId);
        List<FlowInstance> flowInstances = locator.getFlowInstances(flowInstanceFilter);

        if(flowInstances == null || (flowInstances != null && flowInstances.size() != 1)){
            return null;
        }

        return flowInstances.get(0);
    }
}
