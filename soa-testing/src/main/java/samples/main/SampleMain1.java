package samples.main;

import javax.xml.soap.SOAPConstants;
import javax.xml.ws.soap.SOAPBinding;

import oracle.xml.parser.v2.XMLElement;

import samples.main.handlers.CustomHandlerResolver;

import testhelper.ServiceUnitTestHelper;

import util.xmlutil.XMLUtilities;

/**
 * Tests the JAX-WS Client interface
 * Synchronous, with Handler
 */
public class SampleMain1
{
  // WSDL: http://fpp-ta04.us.oracle.com:8001/soa-infra/services/default/HelloWorldComposite!1.0*2bb22053-e06d-45b1-baaf-fdb20c76c2e0/client?WSDL
  // For the synchronous service
  private static final String SERVICE_NAME = "synchronousbpelprocess_client_ep";
  private static final String PORT = "SynchronousBPELProcess_pt";

  private static final String SERVICE_ENDPOINT =
    "http://130.35.95.19:7001/soa-infra/services/default/SOACompositeForInstallationTests/synchronousbpelprocess_client_ep";
  private static final String WSDL_LOC = SERVICE_ENDPOINT + "?WSDL";
  private static final String SERVICE_NS_URI =
  "http://xmlns.oracle.com/soatesthelper/SOACompositeForInstallationTests/SynchronousBPELProcess";

  private static final String SERVICE_REQUEST =
  "<ns1:process xmlns:ns1=\"http://xmlns.oracle.com/soatesthelper/SOACompositeForInstallationTests/SynchronousBPELProcess\">\n" + 
  "  <ns1:input>OATS</ns1:input>\n" +
  "</ns1:process>";

  public static void main(String[] args)
    throws Exception
  {
    try
    {
      XMLElement x = null;

      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setHandlerResolver(new CustomHandlerResolver());
      
      System.out.println("+-------+");
      System.out.println("|  one  |");
      System.out.println("+-------+");
      String svcEndpoint = suth.getEndpointURL(WSDL_LOC);
      x = suth.invokeSyncService(svcEndpoint, 
                                 WSDL_LOC, 
                                 SERVICE_NS_URI, 
                                 SERVICE_REQUEST,
                                 SERVICE_NAME, 
                                 PORT);
      System.out.println("Synchronous, response:");
      x.print(System.out);

      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
