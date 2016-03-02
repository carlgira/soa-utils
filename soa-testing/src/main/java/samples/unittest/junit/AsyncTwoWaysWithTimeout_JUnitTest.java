package samples.unittest.junit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import java.io.StringReader;

import java.util.Properties;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.junit.Test;

import org.w3c.dom.NodeList;

import testhelper.ServiceUnitTestHelper;

import static org.junit.Assert.*;

public class AsyncTwoWaysWithTimeout_JUnitTest
{
  DOMParser parser = new DOMParser();
  
  public AsyncTwoWaysWithTimeout_JUnitTest()
  {
  }

  @Test
  public void testASyncTwoWay()
  {
    try
    {
      // Context setup
      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setVerbose(false);

      final Properties props = new Properties();
      props.load(new FileInputStream("service-test-03.properties"));

      String serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url"));
      String serviceRequestPayload = "";
      BufferedReader br =
        new BufferedReader(new FileReader(props.getProperty("service.input.payload.file")));
      String line = "";
      while ((line = br.readLine()) != null)
        serviceRequestPayload += line;

      // Now Testing
      XMLElement x = null;

      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      assertTrue("Service is down", ok);

      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), 
                                       serviceRequestPayload);
      assertTrue("Invalid payload", ok);
      if (ok)
      {
        ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name [" + props.getProperty("service.name") + "]", ok);
        ok = suth.isPortNameOK(props.getProperty("wsdl.url"), 
                               props.getProperty("service.name"),
                               props.getProperty("service.port"));
        assertTrue("Invalid Port Name [" + props.getProperty("service.port") + "]", ok);
        ok = suth.isOperationNameOK(props.getProperty("wsdl.url"), 
                                    props.getProperty("service.name"),
                                    props.getProperty("service.port"),
                                    props.getProperty("service.operation"));
        assertTrue("Invalid Operation Name [" + props.getProperty("service.operation") + "]", ok);
        try
        {
          long before = 0L, after = 0L;
          before = System.currentTimeMillis();
          x = suth.invokeASync2WayServiceWithTimeout(serviceEndPoint, 
                                                     props.getProperty("wsdl.url"),
                                                     props.getProperty("service.ns.uri"),
                                                     serviceRequestPayload,
                                                     props.getProperty("service.name"),
                                                     props.getProperty("service.port"),
                                                     props.getProperty("service.operation"),
                                                     "2345", 
                                                     500L);
          if (x != null)
          {
            x.print(System.out);
          }
          String resp = ServiceUnitTestHelper.getAsyncResponse();
          System.out.println(resp);
          String xpath = "//ns:result[./text() = 'Done']";
          parser.parse(new StringReader(resp));
          XMLDocument doc = parser.getDocument();
          NodeList nl = doc.selectNodes(xpath, new NSResolver()
            {
              public String resolveNamespacePrefix(String string)
              {
                return props.getProperty("service.ns.uri");
              }
            });
          assertTrue("Invalid response", nl.getLength() == 1);

          after = System.currentTimeMillis();
          System.out.println("Done in " + Long.toString(after - before) + " ms.");
        }
        catch (ServiceUnitTestHelper.TookTooLongException ttle)
        {
          fail(ttle.toString());
        }
        catch (Exception ex)
        {
          System.out.println("Make sure all threads are dead...");
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
    }
  }
}
