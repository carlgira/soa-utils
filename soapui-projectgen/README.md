# SoapUI Project Generator
I've been working with SoapUI for years, it's an excellent tool for testing.<br/>
Creating one project manually is nothing, but when you are into a big project and  lots of apps need their soapUI file it's  tedious to create them manually. <br/>
So, for the app template o archetype i wanted to add an utility to automatic create that SoapUI Project. I just wanted to create the skeleton of the soapUI file so later someone else could add all the tests. <br/>
The idea is to build a soapui project based on a list of wsdls to create the TestSuites and another list of Wsdls for the mock services. <br/><br/>

In this folder are three maven apps:<br/><br/>

<h3>soapui-projectgen-maven-plugin</h3>
It's a maven plugin to create the soapui project. It's posible to pass the WSDLs files or URLs one by one or just pass a entire directory of WSDLs to be procesed. <br/><br/>
It receives as input the next parameters: <br/>
    - <b>testWSDLDir:</b> Directory of WSDLs for create TestSuites<br/>
    - <b>mockWSDLDir:</b> Directory of WSDLs for create MockServices<br/>
    - <b>soapUIProjectFileName:</b> Absolute path for the soapUI project<br/>
    - <b>projectName:</b> Project Name of the soapUI project<br/>
    - <b>testWSDLFiles:</b> WSDL list of files to create MockServices<br/>
    - <b>mockWSDLFiles:</b> WSDL list of files to create MockServices<br/>
    - <b>testPropertiesFile:</b> Absolute path for the properties file (Endpoints of the services)<br/>
    - <b>initialMockPort:</b> Initial port for the mock services<br/><br/>

As output this plugin creates two files, the soapui project and a properties files to indicate the endpoint of services to test (This file must be modified manually). <br/><br/>

<h3>soapui-projectgen-tests</h3>
Has several examples of how to use the maven plugin in the pom.xml <br/>
To execute the maven plugin execute one of the possible maven profiles.<br/><br/>
  - mvn generate-sources -P soapui-projectgen-wsdl-dirs<br/><br/>
  
<h3>osb-soapui-test</h3>
This is a OSB (Oracle Service BUS) project to use the soapui project created. Look the pom.xml for the profiles "run-unit-test" or "run-integration-test". <br/> 
Install and deploy the project<br/> 
    1. Configure in the properties section all the variables of your enviroment.<br/> 
    2. Deploy. mvn clean package exec:exec<br/> 
    3. Test. mvn test -P run-unit-test<br/> <br/> 

The run-unit-test has two steps (soapui plugin executions)<br/>
    - Start mock services of soapui project<br/>
    - Execute testSuites<br/>

The reports of the tests are writen in the target directory

