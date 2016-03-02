package samples.main;

import javax.xml.soap.SOAPConstants;
import javax.xml.ws.soap.SOAPBinding;

import oracle.xml.parser.v2.XMLElement;

import testhelper.ServiceUnitTestHelper;

import util.xmlutil.XMLUtilities;

public class SampleMain2
{
  // For the asynchronous 2 way one
  private static final String SERVICE_NAME_2 = "FrontEndBPEL";
  private static final String PORT_2 = "FrontEndBPEL\"_pt";
  private static final String SERVICE_ENDPOINT_2 =
    "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/GuruWSProblem!1.0*2008-10-01_12-28-08_949/FrontEndBPEL_ep";
  private static final String WSDL_LOC_2 = SERVICE_ENDPOINT_2 + "?WSDL";
  private static final String SERVICE_NS_URI_2 =
    "http://xmlns.oracle.com/Application1/GuruWSProblem/FrontEndBPEL";

  private static final String SERVICE_REQUEST_2 =
    "<ns1:FrontEndBPELProcessRequest xmlns:ns1=\"http://xmlns.oracle.com/Application1/GuruWSProblem/FrontEndBPEL\">\n" + 
    "    <ns1:input>Oliv</ns1:input>\n" + 
    "</ns1:FrontEndBPELProcessRequest>";

  public static void main(String[] args)
    throws Exception
  {
    ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
    try
    {
      XMLElement x = null;

      long before = System.currentTimeMillis();
      String operation = "initiate";
      String httpPort = "2345";
      x =
          suth.invokeASync2WayService(SERVICE_ENDPOINT_2, WSDL_LOC_2, SERVICE_NS_URI_2,
                                      SERVICE_REQUEST_2, SERVICE_NAME_2,
                                      PORT_2, operation, // Operation
            httpPort); // HTTP Port for the tiny server
      System.out.println("Async 2 way, response:");
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
        System.out.println("(Length:" + resp.length() +
                           " character(s))");
        System.out.println("...............");
        long after = System.currentTimeMillis();
        System.out.println("Done in " + Long.toString(after - before) +
                           " ms.");
      }

      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
