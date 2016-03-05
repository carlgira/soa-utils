package com.carlgira.oracle.bpel.test;

import com.carlgira.oracle.bpel.test.model.composite.*;
import com.carlgira.oracle.bpel.test.model.composite.Component;
import com.carlgira.oracle.bpel.test.model.task.TaskDefinition;
import com.carlgira.oracle.bpel.test.model.testcase.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class TestGenerator {

    private String projectDir;
    private Composite composite;

    public TestGenerator(String projectDir) {
        this.projectDir = projectDir;
    }

    public static void main(String []args) throws IOException {
        TestSuite testSuite1 = genTestSuite("/tmp/Agentes/AltaAgentes");
        new ObjectMapper().writeValue(System.out, testSuite1);
    }

    public static TestSuite genTestSuite(String projectDir) throws IOException {
        TestGenerator testGenerator = new TestGenerator(projectDir);
        testGenerator.readComposite();
        TestSuite testSuite = testGenerator.generateSampleTestSuite();

        return testSuite;
    }

    public Composite readComposite() {
        if(composite == null){
            composite = (Composite)unmarshall(projectDir + "/composite.xml", Composite.class);
        }

        return composite;
    }

    protected TaskDefinition parseTaskDefinition(String fileName){
        return (TaskDefinition)unmarshall(fileName, TaskDefinition.class);
    }

    public TestSuite generateSampleTestSuite(){
        if(composite == null){
            return null;
        }

        TestSuite testSuite = new TestSuite(composite.getName());

        for(Service service : composite.getService()){
            TestCase testCase = new TestCase( service.getName() + "_case00");
            testCase.wsdl = service.getWsdlLocation();

            CompositeComponent compositeComponent = null;
            for(Component parentComponent : composite.getComponent()) {

                if (!existsWire(service.getName(), parentComponent.getName() + "/")) {
                    continue;
                }
                compositeComponent = new CompositeComponent(parentComponent);

                for (Component component : composite.getComponent()) {

                    if (!existsWire(parentComponent.getName(), component.getName())) {
                        continue;
                    }


                    if (component.getImplementationWorkflow() != null) {
                        String taskPath = projectDir + "/" + component.getImplementationWorkflow().getSrc();

                        TaskDefinition taskDefinition = parseTaskDefinition(taskPath);

                        String outcomes = "";
                        for (String outcome : taskDefinition.getWorkflowConfiguration().getOutcomes().getOutcome()) {
                            outcomes += "," + outcome;
                        }
                        outcomes = outcomes.substring(1);
                        HumanTask humanTask = new HumanTask(taskDefinition.getName(), outcomes);
                        testCase.humanTaskList.add(humanTask);
                    }
                }
                compositeComponent.testCaseList.add(testCase);
            }
            if(compositeComponent != null){
                testSuite.compositeComponents.add(compositeComponent);
            }
        }

        for(Reference reference : composite.getReference()){
            ServiceCall serviceCall = new ServiceCall(reference.getName(), reference.getWsdlLocation());
            testSuite.mockServices.add(serviceCall);
        }

        return testSuite;
    }

    public Boolean existsWire(String source, String target ){

        for(Wire wire : composite.getWire()){
            if(wire.getSourceUri().startsWith(source) && wire.getTargetUri().contains(target)){
                return true;
            }
        }
        return false;
    }




    public void marshall(Object object, OutputStream outputStream){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(object, outputStream );
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public Object unmarshall(String fileName, Class oClass ){
        Object result = null;
        try {
            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(oClass);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            result = jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }
}