package com.carlgira.oracle.bpel.test.model.testcase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlgira on 3/1/16.
 */
public class TestSuite {

    public String name;
    public List<TestCase> testCaseList = new ArrayList<TestCase>();
    public List<ServiceCall> mockServices = new ArrayList<ServiceCall>();

    public TestSuite(String name){
        this.name = name;
    }
}
