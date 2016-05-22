package com.carlgira.oracle.bpel.test.ht;

import oracle.bpel.services.workflow.repos.Column;

public class HtQuery {
    public Column column; // oracle.bpel.services.workflow.repos.TableConstants;
    public Integer operator; // oracle.bpel.services.workflow.repos.Predicate
    public Object value;

    public HtQuery(Column column, Integer operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }
}