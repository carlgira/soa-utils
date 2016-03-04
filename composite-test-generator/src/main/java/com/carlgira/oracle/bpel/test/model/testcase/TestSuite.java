package com.carlgira.oracle.bpel.test.model.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlgira on 3/1/16.
 */
public class TestSuite {

    public String name;
    public String partition;
    public String version;
    public Server server;
    public List<TestCase> testCaseList = new ArrayList<TestCase>();
    public List<ServiceCall> mockServices = new ArrayList<ServiceCall>();

    public TestSuite(){
    }

    public TestSuite(String name){
        this.name = name;
        this.server = new Server("weblogic", "welcome1", "t3://192.168.100.228:8001/soa-infra/", "jazn.com");
        this.partition = "default";
        this.version = "1.0";
    }

    public static TestSuite getTestSuite(String fileName) throws IOException {
        return new ObjectMapper().readValue(new File(fileName), TestSuite.class);
    }
}
