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
 * Synchronous
 */
public class CompositeTest04
{
  public static void main(String[] args)
    throws Exception
  {
    ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
    suth.setVerbose(true);

    Properties props = new Properties();
    props.load(new FileInputStream("service-test-04.properties"));

    String serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url"));
    String serviceRequest = "";
    BufferedReader br = new BufferedReader(new FileReader(props.getProperty("service.input.payload.file")));
    String line = "";
    while ((line = br.readLine()) != null)
      serviceRequest += line;
    
    XMLElement x = null;

    boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
    System.out.println("Service is " + (ok? "": "not ") + "up");

    ok = suth.validateServicePayload(props.getProperty("wsdl.url"), serviceRequest);
    if (ok)
    {
      System.out.println("Service Payload is OK");
      ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), props.getProperty("service.name"));
      System.out.println("Service name is " + (ok? "": "NOT ") + "OK");
      ok =
          suth.isPortNameOK(props.getProperty("wsdl.url"), props.getProperty("service.name"), props.getProperty("service.port"));
      System.out.println("Port name is " + (ok? "": "NOT ") + "OK");

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
          x.print(System.out);
        else
        {
          System.out.println("Response is null...");
        }
      }
      catch (Exception ex)
      {
        System.out.println(ex.toString());
      }
    }
    else
      System.out.println("Invalid payload");

    System.out.println("Done.");
  }
}
