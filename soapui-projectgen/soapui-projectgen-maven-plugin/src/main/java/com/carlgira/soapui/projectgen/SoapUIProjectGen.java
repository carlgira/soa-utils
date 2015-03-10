package com.carlgira.soapui.projectgen;

import com.carlgira.soapui.projectgen.util.SoapUIFileUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.x.form.XFormDialog;
import java.util.HashSet;
import static org.mockito.Mockito.mock;

/**
 * Utility class to generate a SoapUIProject for testing.
 * It receives two main inputs. One, the a list of WSLDs of the services to test, and in second place, a list of WSDLs that is going to be build as mock services. 
 * Once the soapUIProject is ready it can  execute the tests and start the mock services.
 * 
 * The idea is to be able to test a deployed service. The utility create a default test for the deployed service and it can creates mock services that the deployed service is going to call (Unit testing of a service)
 * Once the project is created the idea is to make a test like this
 *   1. Execute the sopaUIProject, start the mock services and run the configured tests.
 *   2. Execute the soapUITest, the deployed service is called.
 *   3. The deployed service calls other services, pointing to the endPoints of the mocked services
 *   4. The mocked services respond accordingly.
 *   5. The deployed service ends his logic and respond to the consumer.
 *   6. The test of sopaUIProject validates the input.
 *
 * @author Carlos Giraldo. carlgira@gmail.com 
 */
public class SoapUIProjectGen {

    /**
     * List of services to test
     */
    private Set<String> testWsdlsList;

    /**
     * List of services to mock
     */
    private Set<String> mockWsdlList;

    /**
     * Full path where to save the soapUIProject
     */
    private final String soapUIProjectFile;

    /**
     * Logical name of the sopaUIProject
     */
    private final String projectName;

    /**
     * Initial port for the mock services
     */
    private final Integer initialPort;

    /**
     * File object of the sopaUIProject
     */
    private final File projectFile;

    /**
     * SoapUIObject where the project is created
     */
    private final WsdlProject project;

    /**
     * Set of properties of the project (ip, port, endpoints)
     */
    private final Properties testProperties;

    /**
     * Name of the property file
     */
    private final String testPropertiesFiles;

    /**
     * File name of the startUp groovy script to load properties
     */
    private final static String FILE_SETUPSCRIPT = "load-properties.groovy";

    /**
     * Constructor of SoapUIProjectGenerator.
     *
     * @param testWSDLDir Directory of WSDLs that needs to create TestSuites.
     * @param servicesWSDLDir Directory of WSDLs that needs to create mock services
     * @param soapUIProjectFile FileName of the soapUIProject
     * @param projectName SoapUI projectName
     * @param testWsdlFiles Array of WSDLs files to create a TestSuite
     * @param mockWsdlFiles Array of WSDLs files to create mock Services
     * @param initialPort InitialPort for the mock services (Each created mock use diferent port)
     * @param testPropertiesFiles Name of the properties file of the soapUIProject
     * @throws Exception
     */
    public SoapUIProjectGen(String testWSDLDir, String servicesWSDLDir, String soapUIProjectFile, 
            String projectName, String[] testWsdlFiles, String[] mockWsdlFiles, Integer initialPort, 
            String testPropertiesFiles)
            throws Exception 
    {

        // Setting all variables
        this.soapUIProjectFile = soapUIProjectFile;
        this.projectName = projectName;
        this.initialPort = initialPort;
        this.testPropertiesFiles = testPropertiesFiles;
        testProperties = new Properties();
        projectFile = new File(this.soapUIProjectFile);

        SoapUI.setSoapUICore(new StandaloneSoapUICore(true));
        project = new WsdlProject();
        project.setName(this.projectName);
        this.testWsdlsList = new HashSet<String>();
        this.mockWsdlList = new HashSet<String>();

        // Load the test and the mock service WSDLs
        SoapUIFileUtility fileUtility = new SoapUIFileUtility();
        
        if(testWSDLDir != null)
        {
            this.testWsdlsList = fileUtility.findWSDLs(new File(testWSDLDir));
        }
        
        if(servicesWSDLDir != null)
        {
            this.mockWsdlList = fileUtility.findWSDLs(new File(servicesWSDLDir));
        }
        
        if (testWsdlFiles != null) 
        {
            this.testWsdlsList.addAll(Arrays.asList(testWsdlFiles));
        }

        if (mockWsdlFiles != null) 
        {
            this.mockWsdlList.addAll(Arrays.asList(mockWsdlFiles));
        }
    }

    /**
     * Main method to create the soapUIProject. It creates the testSuites and
     * the mock services.
     *
     * @return
     * @throws Exception
     */
    public boolean createSoapUIProject() throws Exception 
    {
        if (testWsdlsList.isEmpty() && mockWsdlList.isEmpty()) 
        {
            return false;
        }

        createServicesTest();
        createServicesMocks();        
        project.saveIn(projectFile);
        
        if(!testWsdlsList.isEmpty())
        {
            writeProperties();
        }
        
        return true;
    }

    /**
     * Add a property to the Project. That property can be used as a variable into the soapUIProject
     *
     * @param name
     * @param value
     */
    public void addProperty(String name, String value) 
    {
        testProperties.setProperty(name, value);
    }

    /**
     * Writes all the sopaUIProperties into a file
     *
     * @throws IOException
     */
    public void writeProperties() throws IOException 
    {
        FileOutputStream testPropertiesStream = new FileOutputStream(this.testPropertiesFiles);
        testProperties.store(testPropertiesStream, null);
        testPropertiesStream.close();
    }

    /**
     * Create the test of the input services
     * @return Returns a WSDLProject
     * @throws Exception
     */
    private WsdlProject createServicesTest() throws Exception 
    {
        int ports = initialPort;
        
        // Iterate for every WSDL
        for (String wsdlFile : this.testWsdlsList) 
        {
            WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, wsdlFile);

            //Iterate for every PortType
            for (int j = 0; j < wsdls.length; j++) 
            {
                WsdlInterface wsdl = wsdls[j];
                int c = wsdl.getOperationCount();

                // Creates a test Suite
                WsdlTestSuite testSuite = project.addNewTestSuite(wsdl.getName() + "_TestSuite");

                WsdlMockService mockService = null;

                // If the WSDL is also on the list of mocked WSLDs create the mock service (It has to be done like this)
                if (this.mockWsdlList.contains(wsdlFile)) 
                {
                    mockService = project.addNewMockService(wsdl.getName() + "_Mock");
                    mockService.setPort(ports);
                    mockService.setPath("/mock" + wsdl.getName());
                    mockService.setHost("localhost");
                }

                if (project.getProperty(wsdl.getName() + ".path") == null) 
                {
                    addProperty(wsdl.getName() + ".path", "/mock" + wsdl.getName());
                }

                String reqContent = "";
                
                // Iterate over every operation of the WSDL
                for (int i = 0; i < c; i++) 
                {
                    // Creates the WsdlOperation and the request parameters
                    WsdlOperation op = wsdl.getOperationAt(i);

                    String opName = op.getName();
                    reqContent = op.createRequest(true);
                    WsdlRequest req = op.addNewRequest("Req_" + opName);
                    req.setRequestContent(reqContent);
                    String endPoint = "http://${#Project#server.hostname}:${#Project#server.listen.port" + "}${" + wsdl.getName() + ".path" + "}";

                    req.setEndpoint(endPoint);

                    // Configure the response of the mock of this operation
                    if (this.mockWsdlList.contains(wsdlFile)) 
                    {
                        WsdlMockOperation op1 = mockService.addNewMockOperation(op);
                        op1.addNewMockResponse("Resp_" + opName, true);
                    }
                    // Creates the testCase			
                    TestStepConfig testStepConfig = WsdlTestRequestStepFactory.createConfig(op, opName + "_TestCase");

                    WsdlTestCase testCase = testSuite.addNewTestCase(opName + "_TestCase");

                    // Creates the testStep with the soap request
                    WsdlTestRequestStep testStep = (WsdlTestRequestStep) testCase.addTestStep(testStepConfig);

                    // Creates the soap http assertion to validate the HTTP code 200 (Response OK) (Its a little tricky but it was the only way) 
                    ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion) testStep.addAssertion(ValidHttpStatusCodesAssertion.LABEL);
                    assertion.setName("ValidHttpResponse");

                    java.lang.reflect.Field statusAssertionCodesField = assertion.getClass().getDeclaredField("codes");
                    statusAssertionCodesField.setAccessible(true);
                    statusAssertionCodesField.set(assertion, "200");

                    java.lang.reflect.Field dialog = assertion.getClass().getDeclaredField("dialog");
                    dialog.setAccessible(true);
                    dialog.set(assertion, mock(XFormDialog.class));

                    assertion.configure();
                    // End of the Validation assertion

                    testStep.setName(opName + "_Step");
                    testStep.setPropertyValue("request", reqContent);
                    testStep.setPropertyValue("endpoint", endPoint);
                }
            }
        }
        SoapUIFileUtility fileUtility = new SoapUIFileUtility();
        project.setBeforeRunScript(fileUtility.loadFileToString(FILE_SETUPSCRIPT));

        return project;
    }

    /**
     * Create a list of mock services, using the list of WSDLs configured in the variable mockWsdlList
     *
     * @return Return a WsdlProject with the mock configuration
     * @throws Exception If something went bad
     */
    private WsdlProject createServicesMocks() throws Exception 
    {
        int ports = initialPort;
        // Iterates on every WSDLs
        for (String wsdlFile : this.mockWsdlList) 
        {
            // Checks if this WSDL is on the list of WSDLs for the TestSuites. A WSDL just can be added once to a SoapUIProject.
            if (this.testWsdlsList.contains(wsdlFile)) 
            {
                continue;
            }

            WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, wsdlFile);

            // Creates a mock service for the WSDL.
            for (int j = 0; j < wsdls.length; j++) 
            {
                WsdlInterface wsdl = wsdls[j];

                WsdlMockService mockService = project.addNewMockService(wsdl.getName() + "_Mock");
                ports++;
                mockService.setPort(ports);
                mockService.setPath("/mock" + wsdl.getName());
                mockService.setHost("localhost");

                int c = wsdl.getOperationCount();

                for (int i = 0; i < c; i++) 
                {
                    WsdlOperation op = wsdl.getOperationAt(i);
                    String opName = op.getName();

                    WsdlMockOperation op1 = mockService.addNewMockOperation(op);
                    op1.addNewMockResponse("Resp_" + opName, true);
                }
            }
        }
        return project;
    }

    
}
