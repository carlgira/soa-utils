package com.carlgira.oracle.bpel.test.model.testcase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlgira on 3/1/16.
 */
public class TestCase {
    public String name;
    public String description = "TestCase description";
    public List<HumanTask> humanTaskList = new ArrayList<HumanTask>();
    public List<ServiceCall> servicesList = new ArrayList<ServiceCall>();

    public TestCase(){
    }
    public TestCase(String name){
        this.name = name;
    }
}
