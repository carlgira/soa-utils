package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.CEvent;
import com.carlgira.util.Utils;
import com.oracle.schemas.bpel.audit_trail.AuditTrail;
import com.oracle.schemas.bpel.audit_trail.Event;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by carlgira on 08/03/2016.
 * Class to parse the content of a BPEL AuditTrail. Add some functions to lookup all the events.
 */
public class BpelAuditTrailManager implements IAuditTrailManager {

    private AuditTrail auditTrail;

    /**
     * Parse a xmlString to an auditTrail object
     * @param xmlContent The whole content of the audit trail
     */
    public BpelAuditTrailManager(String xmlContent) {

/*
        try(  PrintWriter out = new PrintWriter( "d:\\filename.txt" )  ){
            out.println( xmlContent );
        }
        catch (Exception e){
            e.printStackTrace();
        }
*/
        this.auditTrail = (AuditTrail) Utils.unmarshallString(xmlContent, AuditTrail.class);
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label
     * @param label The label of the node (the activity name in the bpel)
     * @return

    public Event getEvent(String label){
        for(Event eventType : this.auditTrail.getEvent()){
            if( eventType.getLabel() != null && (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " "))) {
                return eventType;
            }
        }
        return null;
    }
     */

    /**
     * Search inside the AuditTrail the last Event in time with the specified label
     * @param label The label of the node (the activity name in the bpel)
     * @return
     */
    public CEvent getLastEvent(String label) {
        Event e = null;
        for(Event eventType : this.auditTrail.getEvent()){
            if( eventType.getLabel() != null && (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " "))) {
                if(e == null){
                    e = eventType;
                }
                else{
                    if(eventType.getDate() != null){
                        Date date1 = Utils.parseDate(e.getDate());
                        Date date2 = Utils.parseDate(eventType.getDate());
                        if(date1 != null && date2 != null && date1.before(date2)){
                            e = eventType;
                        }
                    }
                }
            }
        }
        return new CEvent(e);
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label and in error state
     * @param label The label of the node (the activity name in the bpel)
     * @return

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
     */

    /**
     * Search inside the AuditTrail for a Event with the specified label and in error state
     * @param label The label of the node (the activity name in the bpel)
     * @return
     */
    public CEvent getLastEventWithError(String label){
        Event e = null;
        for(Event eventType : this.auditTrail.getEvent()){
            if(eventType.getState() != null){
                int state = Integer.parseInt(eventType.getState());
                if(eventType.getLabel()  != null &&
                        (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                        (state > 5 || state == 2 || state == 3  || state == 4)
                        ) {
                    if(e == null){
                        e = eventType;
                    }
                    else{
                        if(eventType.getDate() != null) {
                            Date date1 = Utils.parseDate(e.getDate());
                            Date date2 = Utils.parseDate(eventType.getDate());
                            if (date1 != null && date2 != null && date1.before(date2)) {
                                e = eventType;
                            }
                        }
                    }
                }
            }
        }
        return new CEvent(e);
    }

    /**
     * Search inside the AuditTrail for a Event with the specified label and specified state
     * @param label The label of the node (the activity name in the bpel)
     * @param state Number of the state of the node
     * @return

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
     */

    /**
     * Search inside the AuditTrail for a Event with the specified label and specified state
     * @param label The label of the node (the activity name in the bpel)
     * @param state Number of the state of the node
     * @return
     */
    public CEvent getLastEvent(String label, Integer state){
        Event e = null;
        for(Event eventType : this.auditTrail.getEvent()){
            if( (eventType.getLabel()  != null  && eventType.getState() != null)  &&
                    (label.equals(eventType.getLabel()) || eventType.getLabel().startsWith(label + " ")) &&
                    eventType.getState().equals("" + state)
                    ) {
                if(e == null){
                    e = eventType;
                }
                else{
                    if(eventType.getDate() != null) {
                        Date date1 = Utils.parseDate(e.getDate());
                        Date date2 = Utils.parseDate(eventType.getDate());
                        if (date1 != null && date2 != null && date1.before(date2)) {
                            e = eventType;
                        }
                    }
                }
            }
        }
        return new CEvent(e);
    }
}
