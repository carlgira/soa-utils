package unittests.patterns.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Properties;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.custommonkey.xmlunit.Diff;

import org.junit.Test;

import testhelper.Context;
import testhelper.ServiceUnitTestHelper;

import testing.util.GnlUtilities;

import unittests.util.Utilities;


// import org.w3c.dom.NodeList;


/**
 * Generic Unit Test for an Synchronous service.
 * A properties file name should be passed as a Systrem property named "properties.file.name"
 * Property "verbose" can be set to true or false
 * File master.properties could be present.
 * if a property named service.response.timeout, its value is in milleseconds
 *
 */
public class SynchronousServiceUnitTest extends TestCase implements ServiceUnitTestInterface
{
  protected boolean verbose = false;
  protected Properties props = new Properties();
  protected ServiceUnitTestHelper suth = null;
  protected String projectDirectory = ".";
  
  protected XMLElement responsePayload = null;
  
  public SynchronousServiceUnitTest()
  {
  }

  @Test
  public void testSync()
  {
    try
    {
      firstOfAll();
      // Init context
      suth = new ServiceUnitTestHelper();
      if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
        suth.startGUIThread();

      projectDirectory = System.getProperty("project.directory", ".");
      verbose = "true".equals(System.getProperty("verbose", "false"));
      suth.setVerbose(verbose);
      
      final Properties masterProps = Utilities.readMasterProperties(projectDirectory);

      String propertiesFilename = System.getProperty("properties.file.name", null); 
      if (propertiesFilename == null)
        fail("System Property \"properties.file.name\" should be set.");
      File propertiesFile = new File(projectDirectory, propertiesFilename);
      if (!propertiesFile.exists())
      {
        String cpFileName = GnlUtilities.searchAlongClasspath(propertiesFilename);
        if (cpFileName.trim().length() == 0)
          fail("\"" + propertiesFilename + "\" not found from \"" + System.getProperty("user.dir") + "\", nor along your classpath");
        else
        {
          propertiesFilename = cpFileName;
          propertiesFile = new File(cpFileName);
        }
      }
      else
        propertiesFilename = propertiesFile.getAbsolutePath();
      if (verbose)
        System.out.println("Loading Test Data from [" + propertiesFilename + "]");
      props.load(new FileInputStream(propertiesFilename));
      ArrayList<Properties> pa = new ArrayList<Properties>(2);
      pa.add(masterProps);
      pa.add(System.getProperties());
      props = Utilities.patchProperties(props, pa, true);
      
      // Is there security to apply?
      String userName = props.getProperty("username", null);
      String password = props.getProperty("password", null);
      if (userName != null || password != null)
      {
        if (password == null || userName == null)
          fail("you must provide username and password, or none.");
        else
        {
          suth.setPolicySecurity(true);
          suth.setUsername(userName);
          suth.setPassword(password);
          // Check if System variables are set
          // oracle.security.jps.config set to ./security/config/jps-config.xml
          String jpsConfig  = System.getProperty("oracle.security.jps.config", null);
          if (jpsConfig == null)
          {
            fail("You need to provide oracle.security.jps.config as System variable.");
          }
          else
          {
            if (verbose)
            {
              System.out.println("oracle.security.jps.config = " + jpsConfig);
              try
              {
                File f = new File(projectDirectory, jpsConfig);
                if (f.exists())
                {
                  System.out.println("jps-config: found.");
                  System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
                }
                else
                  System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
              }
              catch (Exception ex)
              {
                System.out.println("Security:" + ex.toString());
                onError(ex);
              }
            }
          }
        }
      }

      String wsdlURL = props.getProperty("wsdl.url");
//      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
//      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));      
//      wsdlURL = GnlUtilities.substituteVariable(wsdlURL, System.getProperties());
            
      String serviceEndPoint = null;
      try 
      { 
        serviceEndPoint = suth.getEndpointURL(wsdlURL); 
      }
      catch (Exception ex)
      {
        System.out.println("Problem getting the ServiceEndPoint URL. Is the service or server up?");
        onError(ex);
//      fail(ex.toString());
      }
      assertTrue("ServiceEndpoint from ${wsdl.url} returned null", serviceEndPoint != null);
      
      String serviceRequest = "";
      String payloadFileName = props.getProperty("service.input.payload.file", null);
      String payloadAsString = props.getProperty("service.input.payload.as.string", null);
      if (payloadFileName == null && payloadAsString == null)
        fail("Need an input payload (service.input.payload.file or service.input.payload.as.string)");
      
      if (payloadFileName != null)
      {
        BufferedReader br = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        { 
          line = br.readLine(); 
          if (line != null)
            serviceRequest += line;
          else
            keepReading = false;
        }
        br.close();
      }
      else
        serviceRequest = payloadAsString;

      serviceRequest = beforeInvoke(serviceRequest);
      
      // Beginning the test
      XMLElement x = null;

      boolean ok = suth.isServiceUp(wsdlURL);
      assertTrue("Service is down", ok);

      boolean goThrough = "true".equals(props.getProperty("move.on.if.payload.is.invalid", "false"));
      ok = suth.validateServicePayload(wsdlURL, serviceRequest);
      if (!ok)
      {
        if (!goThrough)
          fail("Invalid payload:\n" + serviceRequest);
        else
        {
          System.out.println("Payload invalid, but moving on anyway.");
          ok = true;
        }
      }
      if (ok)
      {
        ok = suth.isServiceNameOK(wsdlURL, 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name", ok);
        ok = suth.isPortNameOK(wsdlURL, 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid port name", ok);
        
        try
        {
          String timeout = props.getProperty("service.response.timeout", null);
          try
          {
            if (timeout == null)
              x = suth.invokeSyncService(serviceEndPoint, 
                                         wsdlURL, 
                                         props.getProperty("service.ns.uri"), 
                                         serviceRequest,
                                         props.getProperty("service.name"), 
                                         props.getProperty("service.port"));
            else
            {
              long timeoutValue = 0L;
              try { timeoutValue = Long.parseLong(timeout); }
              catch (NumberFormatException nfe)
              {
                fail("Unparsable timeout value [" + timeout + "]");
              }
              x = suth.invokeSyncServiceWithTimeout(serviceEndPoint, 
                                                    wsdlURL, 
                                                    props.getProperty("service.ns.uri"), 
                                                    serviceRequest,
                                                    props.getProperty("service.name"), 
                                                    props.getProperty("service.port"),
                                                    timeoutValue);
            }
          }
          catch (Exception ex)
          {
            if (ex instanceof SOAPFaultException)
            {
              SOAPFaultException sfe = (SOAPFaultException)ex;
              SOAPFault sf = sfe.getFault();
              if (sf instanceof XMLElement)
                x = (XMLElement)sf;
              else
              {
                String str = sf.toString();
                DOMParser parser = Context.getInstance().getParser();
                synchronized (parser)
                {
                  parser.parse(new StringReader(str));
                  XMLDocument doc = parser.getDocument();
                  x = (XMLElement)doc.getDocumentElement();
                }                
              }
            }
            else
              throw ex;
          }
          afterInvoke();
          if (x != null)
          {
            StringWriter sw = new StringWriter();
            x.print(sw);
            String returnedPayload = sw.toString();
            if (verbose)
              System.out.println("Sync response:\n" + returnedPayload);
            returnedPayload = afterReceive(returnedPayload);
            evaluate(returnedPayload);
          }
          else
          {
            assertTrue("No response...", (x != null));
          }
          responsePayload = x;
        }
        catch (Exception ex)
        {
          onError(ex);
//        fail(ex.toString());
        }
      }
      if (verbose)
        System.out.println("Done.");      
    }
    catch (Exception ex)
    {
      onError(ex);
//    fail(ex.toString());
    }
    finally
    {
      if (verbose)
        System.out.println("End-of-Test");
      if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
      {
        try
        {
          suth.getGUIThread().join();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }
  
  public void firstOfAll()
  {
  }

  public String beforeInvoke(String payload)
  {
    return payload;
  }

  public void afterInvoke()
  {
  }

  public void beforeReceive()
  {
  }

  public String afterReceive(String payload)
  {
    return payload;
  }

  public void evaluate(String payload)
  {
    String expectedOutput = "";
    String payloadFileName = props.getProperty("service.output.payload.file", null);
    String payloadAsString = props.getProperty("service.output.payload.as.string", null);
    
    if (payloadAsString == null && payloadFileName == null)
    {
      fail("No payload to refer to (service.output.payload.file, or service.output.payload.as.string");
    }
    if (payloadFileName != null)
    {
      try
      {
        BufferedReader outBr = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        boolean keepItUp = true;
        String line = "";
        while (keepItUp)              
        {
          line = outBr.readLine();
          if (line != null)
            expectedOutput += (line + "\n");
          else
            keepItUp = false;
        }
        outBr.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    else if (payloadAsString != null)
      expectedOutput = payloadAsString;
    
    try
    {
      // Use XMLUnit here
      String controlXML = expectedOutput.trim().replaceAll("\n", "");
      String testXML    = payload.trim().replaceAll("\n", "");
      while (controlXML.indexOf("> ") > 0)
        controlXML = controlXML.replaceAll("> ", ">");
      while (testXML.indexOf("> ") > 0)
        testXML = testXML.replaceAll("> ", ">");
      
      if (verbose) System.out.println("XMLUnit Comparing:\n===================\n" + controlXML + "\nwith:\n==================\n" + testXML + "\n==============");
//    assertXMLEqual("comparing test xml to control xml", controlXML, testXML);
      Diff xmlDiff = new Diff(controlXML, testXML);
      assertTrue("Pieces of XML are similar " + xmlDiff, xmlDiff.similar());      
    }
    catch (Exception ex)
    {
      fail(ex.toString());
    }
  }
  
  public void onError(Exception ex)
  {
    fail(ex.toString());
  }
  
  public XMLElement getResponsePayload()
  {
    return responsePayload;
  }

  /**
   * That is for tests
   * @param args
   */
  public static void main(String[] args)
  {
    SynchronousServiceUnitTest ssut = new SynchronousServiceUnitTest();
    System.setProperty("verbose", "true");
    System.setProperty("properties.file.name", "generic-service-test-synchronous.with.proxy.properties");
//  System.setProperty("properties.file.name", "generic-service-test-secured-synchronous.properties");
//  System.setProperty("properties.file.name", "doo.sync.properties");
//  System.setProperty("properties.file.name", "muljadi.properties");
//  System.setProperty("oracle.security.jps.config", "./security/config/jps-config.xml");
//  System.setProperty("properties.file.name", "liang.properties");
    ssut.testSync();
  }

}
