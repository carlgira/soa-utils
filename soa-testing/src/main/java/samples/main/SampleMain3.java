package samples.main;

import java.io.StringReader;

import javax.xml.soap.SOAPConstants;
import javax.xml.ws.soap.SOAPBinding;

import oracle.xml.parser.v2.XMLElement;

import testhelper.ServiceUnitTestHelper;

import util.xmlutil.XMLUtilities;

/**
 * Tests the JAX-WS Client interface
 *
 * Asynchronous 1 way
 * -- Asynchronous 2 way
 */
public class SampleMain3
{
  // WSDL : http://130.35.95.59:7101/B7aAsynchWebServices2-Model-context-root/MyAppModuleAMAsyncService?WSDL
  // For the synchronous service
  private static final String SERVICE_NAME = "MyAppModuleAMAsyncService";
  private static final String PORT = SERVICE_NAME + "SoapHttpPort";

  private static final String SERVICE_ENDPOINT =
    "http://130.35.95.59:7101/B7aAsynchWebServices2-Model-context-root/" +
    SERVICE_NAME;
  private static final String WSDL_LOC = SERVICE_ENDPOINT + "?WSDL";
  private static final String SERVICE_NS_URI =
    "http://xmlns.oracle.com/oracle/apps/";

  private static final String SERVICE_REQUEST =
    "<ns1:processOrderAsync xmlns:ns1=\"http://xmlns.oracle.com/oracle/apps/types/\">\n" + 
    "  <ns1:order>1234</ns1:order>\n" + 
    "</ns1:processOrderAsync>";

/*
  // For the asynchronous 2 way one
  private static final String SERVICE_NAME_2 = "AsyncBPELProcess";
  private static final String PORT_2 = "AsyncBPELProcess_pt";
  private static final String SERVICE_ENDPOINT_2 =
    "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AsyncBPEL!1.0*2008-09-25_09-59-26_194/AsyncBPEL_ep";
  private static final String WSDL_LOC_2 = SERVICE_ENDPOINT_2 + "?WSDL";
  private static final String SERVICE_NS_URI_2 =
    "http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL";

  private static final String SERVICE_REQUEST_2 =
    "<ns1:AsyncBPELProcessRequest xmlns:ns1=\"http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL\">\n" +
    "  <ns1:input>Oliv</ns1:input>\n" +
    "</ns1:AsyncBPELProcessRequest>";
*/

  public static void main(String[] args)
    throws Exception
  {
    try
    {
      XMLElement x = null;

      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();      
      suth.setVerbose(true);
      String svcEndpoint = suth.getEndpointURL(WSDL_LOC);
      
      // ASync Two Way
      if (true)
      {
        x = suth.invokeASync2WayService(svcEndpoint, 
                                        WSDL_LOC, 
                                        SERVICE_NS_URI, 
                                        SERVICE_REQUEST, 
                                        SERVICE_NAME, 
                                        PORT, 
                                        "processOrderAsync", 
                                        "1234");
        if (x != null)
          x.print(System.out);
        else
        {
          System.out.println("Nothing came back from JAX-WS");
          System.out.println("...but...");
  //      System.out.println(XMLUtilities.prettyXMLPrint(ServiceUnitTestHelper.getAsyncResponse()));
          String resp = ServiceUnitTestHelper.getAsyncResponse();
          System.out.println(resp);
          System.out.println("...............");
          System.out.println("(Length:" + resp.length() + " character(s))");
          System.out.println("...............");
  //      long after = System.currentTimeMillis();
  //      System.out.println("Done in " + Long.toString(after - before) + " ms.");
          System.out.println("ASync two ways Done.");
        }
      }
      // Async One Way
      
      suth.invokeASync1WayService(svcEndpoint,
                                  WSDL_LOC,
                                  SERVICE_NS_URI,
                                  new StringReader(SERVICE_REQUEST),
                                  SERVICE_NAME,                                  
                                  PORT,
                                  null,
                                  "processOrderAsync");

      System.out.println("ASync One Way, Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
