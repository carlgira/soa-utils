package com.carlgira.soapui.projectgen;

import org.junit.Test;

/**
 * Test of the SoapUIProjectGenerator class
 *
 * @author Carlos Giraldo. carlgira@gmail.com
 */
public class SoapUIProjectGeneratorTest 
{
/**
    @Test
    public void testDirWithWSDLs() throws Exception 
    {
        SoapUIPlugin osbSoapUIPlugin = new SoapUIPlugin();
  
        osbSoapUIPlugin.setClientWSDLDir(System.getProperty("user.dir") + "/src/test/resources/services/HelloWorld1.wsdl");
        osbSoapUIPlugin.setMockWSDLDir(System.getProperty("user.dir") + "/src/test/resources/mocks/HelloWorld3.wsdl");
        osbSoapUIPlugin.setSoapUIProjectFileName(System.getProperty("user.dir") + "/target/test1SoapUiProject.xml");
        osbSoapUIPlugin.setTestPropertiesFile(System.getProperty("user.dir") + "/target/test1.properties");
        osbSoapUIPlugin.setProjectArtifacId("Test1SoapUiProject");
        osbSoapUIPlugin.setInitialMockPort("63000");
        osbSoapUIPlugin.execute();
    }
    
    
    @Test
    public void testDirs() throws Exception 
    {
        SoapUIPlugin osbSoapUIPlugin = new SoapUIPlugin();
        
    
        osbSoapUIPlugin.setClientWSDLDir(System.getProperty("user.dir") + "/src/test/resources/services");
        osbSoapUIPlugin.setMockWSDLDir(System.getProperty("user.dir") + "/src/test/resources/mocks");
        osbSoapUIPlugin.setSoapUIProjectFileName(System.getProperty("user.dir") + "/target/test2SoapUiProject.xml");
        osbSoapUIPlugin.setTestPropertiesFile(System.getProperty("user.dir") + "/target/test2.properties");
        osbSoapUIPlugin.setProjectArtifacId("Test2SoapUiProject");
        osbSoapUIPlugin.setInitialMockPort("63000");
        osbSoapUIPlugin.execute();
    }
    
    
    @Test
    public void testListOfWsdl() throws Exception 
    {
        SoapUIPlugin osbSoapUIPlugin = new SoapUIPlugin();
        
        osbSoapUIPlugin.setClientWSDLFiles(new String[]{"https://java-sample-programs.googlecode.com/svn-history/r15/trunk/WebService/WebContent/wsdl/HelloWorld.wsdl", System.getProperty("user.dir") + "/src/test/resources/services/HelloWorld1.wsdl"});
        osbSoapUIPlugin.setMockWSDLFiles(new String[]{System.getProperty("user.dir") + "/src/test/resources/mocks/HelloWorld3.wsdl"}) ;        
        osbSoapUIPlugin.setSoapUIProjectFileName(System.getProperty("user.dir") + "/target/test3SoapUiProject.xml");
        osbSoapUIPlugin.setTestPropertiesFile(System.getProperty("user.dir") + "/target/test3.properties");
        osbSoapUIPlugin.setProjectArtifacId("Test3SoapUiProject");
        osbSoapUIPlugin.setInitialMockPort("63000");
        osbSoapUIPlugin.execute();
    }   
    */
}
