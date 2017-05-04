package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.CEvent;

/**
 * Created by cgiraldo on 04/05/2017.
 */
public interface IAuditTrailManager {

    public CEvent getLastEvent(String label);

    public CEvent getLastEventWithError(String label);

    public CEvent getLastEvent(String label, Integer state);
}
