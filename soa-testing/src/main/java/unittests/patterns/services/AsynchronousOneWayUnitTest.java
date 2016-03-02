package unittests.patterns.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.*;
import org.junit.Test;

import testhelper.ServiceUnitTestHelper;

import testing.util.GnlUtilities;

import unittests.util.Utilities;

/**
 * Generic Unit Test for an Async One way service.
 * A properties file name should be passed as a Systrem property named "properties.file.name"
 * Property "verbose" can be set to true or false
 * File master.properties could be present.
 *
 */
public class AsynchronousOneWayUnitTest extends TestCase implements ServiceUnitTestInterface
{
  protected boolean verbose = false;
  protected Properties props = new Properties();
  protected ServiceUnitTestHelper suth = null;
  protected String projectDirectory = ".";
    
  public AsynchronousOneWayUnitTest()
  {
  }

  @Test
  public void testASyncOneWay()
  {
    try
    {
      firstOfAll();
      // Initialize the context...
      suth = new ServiceUnitTestHelper();
      if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
        suth.startGUIThread();

      projectDirectory = System.getProperty("project.directory", ".");
      verbose = "true".equals(System.getProperty("verbose", "false"));
      suth.setVerbose(verbose);
      
      final Properties masterProps = Utilities.readMasterProperties(projectDirectory);

      String propertiesFilename = System.getProperty("properties.file.name", null); // Was "service-test-02.properties"
      assertTrue("System Property \"properties.file.name\" should be set.", propertiesFilename != null);

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

//      String wsdlURL = props.getProperty("wsdl.url");
//      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
//      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));
//      props.setProperty("wsdl.url", wsdlURL);
      
      String serviceEndPoint = null;
      try 
      { 
        serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url")); 
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

      // Now we're testing...        
      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      assertTrue("Service is down", ok);

      boolean goThrough = "true".equals(props.getProperty("move.on.if.payload.is.invalid", "false"));
      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), 
                                       serviceRequest);
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
        ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name:[" + props.getProperty("service.name") + "]", ok);
        ok = suth.isPortNameOK(props.getProperty("wsdl.url"), 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid Port Name [" + props.getProperty("service.port") + "]", ok);

        try
        {
          suth.invokeASync1WayService(serviceEndPoint, 
                                      props.getProperty("wsdl.url"), 
                                      props.getProperty("service.ns.uri"), 
                                      serviceRequest,                                                     
                                      props.getProperty("service.name"), 
                                      props.getProperty("service.port"));
          afterInvoke();
        }
        catch (Exception ex)
        {
          onError(ex);
//        fail(ex.toString());
        }
      }
    }
    catch (Exception ex)
    {
      onError(ex);
    }
    finally
    {
      if (verbose)
        System.out.println("Test is finished.");
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
  }

  public void onError(Exception ex)
  {
    fail(ex!=null?ex.toString():"Null Exception");
  }
  
  public XMLElement getResponsePayload()
  {
    return null;
  }

  /**
   * That is for tests
   * @param args
   */
  public static void main(String[] args)
  {
    AsynchronousOneWayUnitTest aowut = new AsynchronousOneWayUnitTest();
    System.setProperty("verbose", "true");
//  System.setProperty("properties.file.name", "generic-service-test-async-one-way.properties");
    System.setProperty("properties.file.name", "doo.async.1.properties");
    aowut.testASyncOneWay();
  }
}
