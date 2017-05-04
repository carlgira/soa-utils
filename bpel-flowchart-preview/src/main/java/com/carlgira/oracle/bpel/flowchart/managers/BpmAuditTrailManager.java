package com.carlgira.oracle.bpel.flowchart.managers;

import com.carlgira.util.CEvent;

/**
 * Created by cgiraldo on 04/05/2017.
 */
public class BpmAuditTrailManager implements IAuditTrailManager {
    @Override
    public CEvent getLastEvent(String label) {
        return null;
    }

    @Override
    public CEvent getLastEventWithError(String label) {
        return null;
    }

    @Override
    public CEvent getLastEvent(String label, Integer state) {
        return null;
    }
}
