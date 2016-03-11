package com.carlgira.oracle.bpel.test.flowchart;

import com.carlgira.util.JAXBMarshaller;
import com.oracle.schemas.bpel.audit_trail.AuditTrail;
import com.oracle.schemas.bpel.audit_trail.Event;

/**
 * Created by emateo on 08/03/2016.
 */
public class AuditTrailManager {

    private AuditTrail auditTrail;

    public AuditTrailManager(String xmlContent) {
        this.auditTrail = (AuditTrail) JAXBMarshaller.unmarshallString(xmlContent, AuditTrail.class);
    }

    public Event getEvent(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if( eventType.getLabel() != null && (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " "))) {
                return eventType;
            }
        }
        return null;
    }


    public Event getEventWithError(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if( (eventType.getLabel()  != null  && eventType.getState() != null)  &&
                    (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                    Integer.parseInt(eventType.getState()) > 5
                    ) {
                return eventType;
            }
        }
        return null;
    }


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
