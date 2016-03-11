package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.JAXBMarshaller;
import com.oracle.schemas.bpel.audit_trail.AuditTrail;
import com.oracle.schemas.bpel.audit_trail.Event;

/**
 * Created by carlgira on 08/03/2016.
 * Class to parse the content of a BPEL AuditTrail. Add some functions to lookup all the events.
 */
public class AuditTrailManager {

    private AuditTrail auditTrail;

    /**
     * Parse a xmlString to an auditTrail object
     * @param xmlContent
     */
    public AuditTrailManager(String xmlContent) {
        this.auditTrail = (AuditTrail) JAXBMarshaller.unmarshallString(xmlContent, AuditTrail.class);
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label
     * @param label
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
     * @param label
     * @return
     */
    public Event getEventWithError(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if( (eventType.getLabel()  != null  && eventType.getState() != null)  &&
                    (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                    Integer.parseInt(eventType.getState()) > 5 // Any number greater than 5 is and error state
                    ) {
                return eventType;
            }
        }
        return null;
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label and specified state
     * @param label
     * @param state
     * @return
     */
    public Event getEvent(String label, String state){
        for(Event eventType : this.auditTrail.getEvent()){
            if( (eventType.getLabel()  != null  && eventType.getState() != null)  &&
                    (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                    eventType.getState().equals(state)
                    ) {
                return eventType;
            }
        }
        return null;
    }
}
