package com.carlgira.util;

import com.oracle.schemas.bpel.audit_trail.Event;
/**
 * Created by cgiraldo on 04/05/2017.
 */
public class CEvent {

    private String date;
    private String state;

    public CEvent(Event e){
        this.setDate(e.getDate());
        this.setState(e.getState());
    }

    public String getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setState(String state) {
        this.state = state;
    }
}
