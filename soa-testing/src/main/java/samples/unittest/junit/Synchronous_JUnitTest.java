package samples.unittest.junit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import java.util.Properties;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.*;
import org.junit.Test;

import org.w3c.dom.NodeList;

import testhelper.ServiceUnitTestHelper;

public class Synchronous_JUnitTest
{
  DOMParser parser = new DOMParser();
  
  public Synchronous_JUnitTest()
  {
  }

  @Test
  public void testSync()
  {
    try
    {
      // Init context
      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setVerbose(false);

      final Properties props = new Properties();
      props.load(new FileInputStream("service-test-04.properties"));

      String serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url"));
      String serviceRequest = "";
      BufferedReader br = new BufferedReader(new FileReader(props.getProperty("service.input.payload.file")));
      String line = "";
      while ((line = br.readLine()) != null)
        serviceRequest += line;
      
      // Beginning the test
      XMLElement x = null;

      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      assertTrue("Service is down", ok);

      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), serviceRequest);
      assertTrue("Invalid payload", ok);
      if (ok)
      {
        ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name", ok);
        ok = suth.isPortNameOK(props.getProperty("wsdl.url"), 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid port name", ok);
        try
        {
          x = suth.invokeSyncService(serviceEndPoint, 
                                     props.getProperty("wsdl.url"), 
                                     props.getProperty("service.ns.uri"), 
                                     serviceRequest,
                                     props.getProperty("service.name"), 
                                     props.getProperty("service.port"));
          System.out.println("Sync response:");
          if (x != null)
          {
            x.print(System.out);
            /*
             * Expecting:
             * <processResponse xmlns="http://xmlns.oracle.com/SOA_D7B6/MultiPurposeSOAService/SyncFacade">
             *    <result xmlns="http://xmlns.oracle.com/SOA_D7B6/MultiPurposeSOAService/SyncFacade">Instantiated process #50012</result>
             * </processResponse>
             */
            NodeList nl = x.selectNodes("/ns:processResponse/ns:result[starts-with(./text(), 'Instantiated process #')]", new NSResolver()
              {
                public String resolveNamespacePrefix(String string)
                {
                  return props.getProperty("service.ns.uri");
                }
              });
            assertTrue("Invalid response", (nl.getLength() == 1));            
          }
          else
          {
            assertTrue("No response...", (x != null));
          }
        }
        catch (Exception ex)
        {
          fail(ex.toString());
        }
      }
      System.out.println("Done.");      
    }
    catch (Exception ex)
    {
      fail(ex.toString());
    }
    finally
    {
      System.out.println("End-of-Test");
    }
  }
}
