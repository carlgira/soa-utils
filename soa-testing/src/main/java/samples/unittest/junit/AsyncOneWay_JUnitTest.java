package samples.unittest.junit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import java.util.Properties;

import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.*;
import org.junit.Test;

import testhelper.ServiceUnitTestHelper;

public class AsyncOneWay_JUnitTest
{
  public AsyncOneWay_JUnitTest()
  {
  }

  @Test
  public void testASyncOneWay()
  {
    try
    {
      // Initialize the context...
      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setVerbose(false);
      
      Properties props = new Properties();
      props.load(new FileInputStream("service-test-01.properties"));
      
      String serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url"));
      String serviceRequest = "";
      BufferedReader br = new BufferedReader(new FileReader(props.getProperty("service.input.payload.file")));
      String line = "";
      while ((line = br.readLine()) != null)
        serviceRequest += line;

      // Now we're testing...        
      XMLElement x = null;

      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      assertTrue("Service is down", ok);

      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), 
                                       serviceRequest);
      assertTrue("Invalid payload", ok);
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
        }
        catch (Exception ex)
        {
          fail(ex.toString());
        }
      }
      System.out.println("Test: Done.");
    }
    catch (Exception ex)
    {
      fail(ex.toString());
    }
    finally
    {
      System.out.println("Test is finished.");
    }
  }
}
