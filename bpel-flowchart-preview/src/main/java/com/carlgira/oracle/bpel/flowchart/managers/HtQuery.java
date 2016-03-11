package com.carlgira.oracle.bpel.flowchart.managers;

import oracle.bpel.services.workflow.repos.Column;

/**
 * Created by carlgira on 03/03/2016.
 * Class to save a query of a Human task (like a codition in a SQL Query to the WFTASK table).
 * Contains a operation to execute over a column using a value
 */
public class HtQuery {

    /**
     * Any Column of the WFTASK table. Use the oracle.bpel.services.workflow.repos.TableConstants class to get the right value
     */
    public Column column; // oracle.bpel.services.workflow.repos.TableConstants;
    /**
     * Operation to execute in the query. Use the oracle.bpel.services.workflow.repos.Predicate class to get the right value
     */
    public Integer operator; // oracle.bpel.services.workflow.repos.Predicate
    /**
     * Value to use in the operation to compare against the column
     */
    public Object value;

    /**
     * Constructor
     * @param column
     * @param operator
     * @param value
     */
    public HtQuery(Column column, Integer operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }
}
