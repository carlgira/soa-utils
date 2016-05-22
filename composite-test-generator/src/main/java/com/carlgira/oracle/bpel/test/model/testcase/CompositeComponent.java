package com.carlgira.oracle.bpel.test.model.testcase;

import com.carlgira.oracle.bpel.test.model.composite.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carlgira on 3/5/16.
 */
public class CompositeComponent {
    public String name;
    public String type;
    public String payloadRegexLookupComposite = "name>@value<\\/(.*):name>";
    public List<TestCase> testCaseList = new ArrayList<TestCase>();

    public CompositeComponent(){

    }

    public CompositeComponent(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public CompositeComponent(Component component) {
        this.name = component.getName();

        if(component.getImplementationBpel() != null){
            this.type = "bpel";
        }
        else if(component.getImplementationMediator() != null){
            this.type = "mediator";
        }
    }
}
