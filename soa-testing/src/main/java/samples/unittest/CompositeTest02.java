package samples.unittest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import java.util.Properties;

import oracle.xml.parser.v2.XMLElement;

import testhelper.ServiceUnitTestHelper;

/**
 * Tests the JAX-WS Client interface
 * 
 * Data concerning the service are externalized in a properties file
 * 
 * Synchronous, and then asynchronous
 */
public class CompositeTest02
{
  public static void main(String[] args)
    throws Exception
  {
    ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
    suth.setVerbose(false);

    Properties props = new Properties();
    props.load(new FileInputStream("service-test-02.properties"));

    String serviceEndPoint =
      suth.getEndpointURL(props.getProperty("wsdl.url"));
    String serviceRequestPayload = "";
    BufferedReader br =
      new BufferedReader(new FileReader(props.getProperty("service.input.payload.file")));
    String line = "";
    while ((line = br.readLine()) != null)
      serviceRequestPayload += line;

    XMLElement x = null;

    boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
    System.out.println("Service is " + (ok? "": "not ") + "up");

    ok =
        suth.validateServicePayload(props.getProperty("wsdl.url"), serviceRequestPayload);
    if (ok)
    {
      System.out.println("Service Payload is OK");
      ok =
          suth.isServiceNameOK(props.getProperty("wsdl.url"), props.getProperty("service.name"));
      System.out.println("Service name is " + (ok? "": "NOT ") + "OK");
      ok =
          suth.isPortNameOK(props.getProperty("wsdl.url"), props.getProperty("service.name"),
                            props.getProperty("service.port"));
      System.out.println("Port name is " + (ok? "": "NOT ") + "OK");

      try
      {
        long before = System.currentTimeMillis();
        long after = 0L;
        String resp = "";
        if (true)
        {
          x = suth.invokeASync2WayService(serviceEndPoint,
                                          props.getProperty("wsdl.url"),
                                          props.getProperty("service.ns.uri"),
                                          serviceRequestPayload,
                                          props.getProperty("service.name"),
                                          props.getProperty("service.port"),
                                          props.getProperty("service.operation"),
                                          "1234");
            
          if (x != null)
            x.print(System.out);
          System.out.println("Async 2 way, response:"); 
          resp = ServiceUnitTestHelper.getAsyncResponse();
          System.out.println(resp);
          System.out.println("...............");
          System.out.println("(Length:" + resp.length() + " character(s))");
          System.out.println("...............");
          after = System.currentTimeMillis();
          System.out.println("Done in " + Long.toString(after - before) + " ms.");
        }
        // With Timeout
        before = System.currentTimeMillis();
        x = suth.invokeASync2WayServiceWithTimeout(serviceEndPoint,
                                                   props.getProperty("wsdl.url"),
                                                   props.getProperty("service.ns.uri"),
                                                   serviceRequestPayload,
                                                   props.getProperty("service.name"),
                                                   props.getProperty("service.port"),
                                                   props.getProperty("service.operation"),
                                                   "2345",
                                                   5000L);
          
        if (x != null)
          x.print(System.out);
        System.out.println("Async 2 way, response:"); 
        resp = ServiceUnitTestHelper.getAsyncResponse();
        System.out.println(resp);
        System.out.println("...............");
        System.out.println("(Length:" + resp.length() + " character(s))");
        System.out.println("...............");
        after = System.currentTimeMillis();
        System.out.println("Done in " + Long.toString(after - before) + " ms.");
      }
      catch (ServiceUnitTestHelper.TookTooLongException ttle)
      {
        System.out.println(ttle.toString());
//      ttle.printStackTrace();
      }
      catch (Exception ex)
      {
        System.out.println("Make sure all threads are dead...");
        System.out.println(ex.toString());
      }
    }
    else
      System.out.println("Invalid payload");

    System.out.println("Done.");
  }
}
