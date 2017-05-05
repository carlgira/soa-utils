package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.CEvent;
import com.carlgira.util.ServerConnection;
import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;
import oracle.bpm.client.BPMServiceClientFactory;
import oracle.bpm.services.common.exception.BPMException;
import oracle.bpm.services.instancequery.IAuditInstance;
import oracle.bpm.services.instancequery.IInstanceQueryService;

import java.util.*;

/**
 * Created by cgiraldo on 04/05/2017.
 */
public class BpmAuditTrailManager implements IAuditTrailManager {

    private List<IAuditInstance> auditInstances;

    public BpmAuditTrailManager(ServerConnection serverConnection, String id){
        getAuditData(serverConnection, id);
    }

    private void getAuditData(ServerConnection serverConnection, String id){
        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties = new HashMap<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String>();
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.CLIENT_TYPE,WorkflowServiceClientFactory.REMOTE_CLIENT);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, serverConnection.server);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL,serverConnection.adminUser);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS,serverConnection.adminPassword);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");

        BPMServiceClientFactory bpmServiceClientFactory = BPMServiceClientFactory.getInstance(properties, null, null);

        IBPMContext bpmContext = null;
        try {
            bpmContext = bpmServiceClientFactory.getBPMUserAuthenticationService().getBPMContextForAuthenticatedUser();
            auditInstances = bpmServiceClientFactory.getBPMServiceClient().getInstanceQueryService().queryAuditInstanceByProcessId(bpmContext, id);
        } catch (BPMException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CEvent getLastEvent(String label) {
        IAuditInstance event = null;
        for(IAuditInstance instance: auditInstances){
            if(instance.getLabel().equals(label)){
                if(event == null){
                    event = instance;
                }
                else if(instance.getCreateTime().getTime().after(event.getCreateTime().getTime()) || instance.getCreateTime().getTime().equals(event.getCreateTime().getTime())){
                    event = instance;
                }
            }
        }
        CEvent cEvent = null;
        if(event != null){
            cEvent = new CEvent();
            cEvent.setDate(event.getCreateTime().getTime().toString());
            cEvent.setState(translateState(event.getOperation().name()));
        }
        return cEvent;
    }

    @Override
    public CEvent getLastEventWithError(String label) {
        return getLastEvent(label, 8);
    }

    @Override
    public CEvent getLastEvent(String label, Integer state) {
        IAuditInstance event = null;
        for(IAuditInstance instance: auditInstances){
            if(instance.getLabel().equals(label)){
                if(event == null && translateState(instance.getOperation().name()).equals(state.toString()) ){
                    event = instance;
                }
                else if(event != null && translateState(instance.getOperation().name()).equals(state.toString()) &&
                        (instance.getCreateTime().getTime().after(event.getCreateTime().getTime()) || instance.getCreateTime().getTime().equals(event.getCreateTime().getTime()))){
                    event = instance;
                }
            }
        }
        CEvent cEvent = null;
        if(event != null){
            cEvent = new CEvent();
            cEvent.setDate(event.getCreateTime().getTime().toString());
            cEvent.setState(translateState(event.getOperation().name()));
        }
        return cEvent;
    }

    private String translateState(String istate){
        if(istate.equals("FLOW_NODE_IN")){
            return "1";
        }
        else if(istate.equals("FLOW_NODE_OUT")){
            return "5";
        }
        return "8";
     }
}
