package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.JAXBMarshaller;
import com.oracle.schemas.bpel.audit_trail.AuditTrail;
import com.oracle.schemas.bpel.audit_trail.Event;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

/**
 * Created by carlgira on 08/03/2016.
 * Class to parse the content of a BPEL AuditTrail. Add some functions to lookup all the events.
 */
public class AuditTrailManager {

    private AuditTrail auditTrail;

    /**
     * Parse a xmlString to an auditTrail object
     * @param xmlContent The whole content of the audit trail
     */
    public AuditTrailManager(String xmlContent) {
        this.auditTrail = (AuditTrail) JAXBMarshaller.unmarshallString(xmlContent, AuditTrail.class);
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label
     * @param label The label of the node (the activity name in the bpel)
     * @return
     */
    public Event getEvent(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if( eventType.getLabel() != null && (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " "))) {
                return eventType;
            }
        }
        return null;
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label and in error state
     * @param label The label of the node (the activity name in the bpel)
     * @return
     */
    public Event getEventWithError(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if(eventType.getState() != null){
                int state = Integer.parseInt(eventType.getState());
                if(eventType.getLabel()  != null &&
                        (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                        (state > 5 || state == 2 || state == 3  || state == 4)
                        ) {
                    return eventType;
                }
            }

        }
        return null;
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label and specified state
     * @param label The label of the node (the activity name in the bpel)
     * @param state Number of the state of the node
     * @return
     */
    public Event getEvent(String label, Integer state){
        for(Event eventType : this.auditTrail.getEvent()){
            if( (eventType.getLabel()  != null  && eventType.getState() != null)  &&
                    (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                    eventType.getState().equals("" + state)
                    ) {
                return eventType;
            }
        }
        return null;
    }
}
