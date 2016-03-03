package com.carlgira.oracle.bpel.test.model.testcase;

/**
 * Created by carlgira on 3/2/16.
 */
public class ServiceCall {
    public String name;
    public String wsdl;
    public Boolean mock = false;

    public ServiceCall(){
    }

    public ServiceCall(String name, String wsdl){
        this.name = name;
        this.wsdl = wsdl;
    }
}
