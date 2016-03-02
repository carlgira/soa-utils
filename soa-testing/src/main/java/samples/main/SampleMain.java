package samples.main;

import javax.xml.soap.SOAPConstants;
import javax.xml.ws.soap.SOAPBinding;

import oracle.xml.parser.v2.XMLElement;
import testhelper.ServiceUnitTestHelper;
import util.xmlutil.XMLUtilities;

/**
 * Tests the JAX-WS Client interface
 * Synchronous
 * Asynchronous 1 way
 * Asynchronous 2 way
 */
public class SampleMain
{

  // For the synchronous service
  private final static String SERVICE_NAME     = "DepartmentAMService";
  private final static String PORT             = SERVICE_NAME + "SoapHttpPort";
  
  private final static String SERVICE_ENDPOINT = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/" + SERVICE_NAME;
  private final static String WSDL_LOC         = SERVICE_ENDPOINT + "?WSDL";
  private final static String SERVICE_NS_URI   = "/oracle/ateam/serviceapp/model/common/";
  
  private final static String SERVICE_REQUEST = 
    "<ns1:getDepartments xmlns:ns1=\"/oracle/ateam/serviceapp/model/common/types/\">\n" + 
    "  <ns1:departmentId>10</ns1:departmentId>\n" + 
    "</ns1:getDepartments>";
  
  // For the asynchronous 2 way one
  private final static String SERVICE_NAME_2     = "AsyncBPELProcess";
  private final static String PORT_2             = "AsyncBPELProcess_pt";
  private final static String SERVICE_ENDPOINT_2 = "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AsyncBPEL!1.0*2008-09-25_09-59-26_194/AsyncBPEL_ep";
  private final static String WSDL_LOC_2         = SERVICE_ENDPOINT_2 + "?WSDL";
  private final static String SERVICE_NS_URI_2   = "http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL";
  
  private final static String SERVICE_REQUEST_2  = 
    "<ns1:AsyncBPELProcessRequest xmlns:ns1=\"http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL\">\n" + 
    "  <ns1:input>Oliv</ns1:input>\n" + 
    "</ns1:AsyncBPELProcessRequest>";
    
  private final static boolean one   = true, // Department AM Service (Sync)
                               two   = true, // Async 1 way
                               three = true, // Async 2 way
                               four  = true, // Hello world (SOAP 1.2)
                               five  = true, // Payload Validation + Sync call (old & new fashion)
                               six   = true, // Department AM Service (Sync) with timeout
                               seven = true, // Sync 1 way with timeout
                               eight = true; // Asyn 2 way with timeout
  
  public static void main(String[] args) throws Exception
  {
    try
    {
      XMLElement x = null;
      
      if (one)
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
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
      }
      if (two)
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        System.out.println("+-------+");
        System.out.println("|  two  |");
        System.out.println("+-------+");
        String svcEndpoint = suth.getEndpointURL(WSDL_LOC);
        suth.invokeASync1WayService(svcEndpoint, 
                                    WSDL_LOC, 
                                    SERVICE_NS_URI, 
                                    SERVICE_REQUEST, 
                                    SERVICE_NAME, 
                                    PORT);
        System.out.println("\nAsyncOneWay ok\n");
      }
      if (three)
      {        
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        System.out.println("+---------+");
        System.out.println("|  three  |");
        System.out.println("+---------+");
        long before = System.currentTimeMillis();
        String operation = "initiate";
        String httpPort = "2345";
        x = suth.invokeASync2WayService(SERVICE_ENDPOINT_2, 
                                        WSDL_LOC_2, 
                                        SERVICE_NS_URI_2, 
                                        SERVICE_REQUEST_2, 
                                        SERVICE_NAME_2, 
                                        PORT_2,
                                        operation,    // Operation
                                        httpPort);    // HTTP Port for the tiny server
        System.out.println("Async 2 way, response:"); 
        if (x != null)
          x.print(System.out);
        else
        {
          System.out.println("Nothing came back from JAX-WS");
          System.out.println("...but...");
//        System.out.println(XMLUtilities.prettyXMLPrint(ServiceUnitTestHelper.getAsyncResponse()));
          String resp = ServiceUnitTestHelper.getAsyncResponse();
          System.out.println(resp);
          System.out.println("...............");
          System.out.println("(Length:" + resp.length() + " character(s))");
          System.out.println("...............");
          long after = System.currentTimeMillis();
          System.out.println("Done in " + Long.toString(after - before) + " ms.");
        }
      }
      if (four)
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        System.out.println("+--------+");
        System.out.println("|  four  |");
        System.out.println("+--------+");
        String endPointURL = "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/HelloWorld!1.0*2007-08-24_22-10-25_602/client";    
        String wsdlURL = endPointURL + "?WSDL";
        String nsuri = "http://xmlns.oracle.com/HelloWorldApp/HelloWorld/HelloWorld";
        String serviceName = "client";
        String portName = "HelloWorldBinding1.2";
        String payload =
        "<ns1:HelloWorldProcessRequest xmlns:ns1=\"" + nsuri + "\">\n" + 
        "  <ns1:input>SOA UnitTesting</ns1:input>\n" + 
        "</ns1:HelloWorldProcessRequest>\n";
        
        try
        {
          x = suth.invokeSyncService(endPointURL, 
                                     wsdlURL, 
                                     nsuri, 
                                     payload, 
                                     serviceName, 
                                     portName, 
                                     SOAPConstants.SOAP_1_2_PROTOCOL);
          System.out.println("Response:\n" +
              XMLUtilities.prettyXMLPrint(XMLUtilities.xmlToString(x)));
        }
        catch (Exception ex)
        {
          System.err.println(ex.toString());
          // Try JAX-RPC...
          String response = suth.dynamicallyCallService(endPointURL, 
                                                        payload, 
                                                        SOAPConstants.SOAP_1_2_PROTOCOL);
          System.out.println("Response:\n" + response);        
        }
      }
      if (five) // Payload validation
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        String endPointURL = "";
        String payload = "";
        String response = "";
        boolean ok = true;

        System.out.println("+--------+");
        System.out.println("|  five  |");
        System.out.println("+--------+");
        // Some SOA Composite
        endPointURL = "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AssignService!1.0*2008-07-23_14-37-53_767/client";
        payload = 
        "<ns1:AssignInputRequest xmlns:ns1=\"http://www.oracle.apps.scm.doo.decomp.org\">\n" + 
        "    <ns1:Header>\n" + 
        "        <ns1:HeaderId>ONE</ns1:HeaderId>\n" + 
        "        <ns1:OrderNumber>TWO</ns1:OrderNumber>\n" + 
        "        <ns1:ObjectVersionNumber>123213</ns1:ObjectVersionNumber>\n" + 
        "        <ns1:OwnerId>12312312</ns1:OwnerId>\n" + 
        "        <ns1:Linescollection>\n" + 
        "            <ns1:Line>\n" + 
        "                <ns1:LineId>LINE-ID</ns1:LineId>\n" + 
        "                <ns1:FulfillLine>\n" + 
        "                    <ns1:HeaderId>H-LINE-ID</ns1:HeaderId>\n" + 
        "                    <ns1:FLineId>F-LINE-ID</ns1:FLineId>\n" + 
        "                    <ns1:ShipSetName>SHIP-SET-NAME-1</ns1:ShipSetName>\n" + 
        "                    <ns1:ParentLineId>Parent</ns1:ParentLineId>\n" + 
        "                    <ns1:RootParentLineId>Root</ns1:RootParentLineId>\n" + 
        "                </ns1:FulfillLine>\n" + 
        "            </ns1:Line>\n" + 
        "        </ns1:Linescollection>\n" + 
        "    </ns1:Header>\n" + 
        "</ns1:AssignInputRequest>";
        
        ok = suth.validateServicePayload("http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AssignService!1.0*2008-07-23_14-37-53_767/client?WSDL", 
                                       //  "http://www.oracle.apps.scm.doo.decomp.org", 
                                           payload);
        if (ok)
        {
          System.out.println("BPEL payload is valid.");
          // Legacy code
          response = suth.dynamicallyCallService(endPointURL, 
                                                     payload, 
                                                     SOAPConstants.SOAP_1_1_PROTOCOL);
          System.out.println("Response (1):\n" + response);    
          
          // New code
          String service  = "client";
          String portName = "BPELProcess1_pt";
          String nsURI    = "http://xmlns.oracle.com/GroupService/AssignService/BPELProcess1";
          x = suth.invokeSyncService(endPointURL, 
                                     endPointURL + "?WSDL", 
                                     nsURI, 
                                     payload, 
                                     service, 
                                     portName);
          System.out.println("Response (2):\n" + XMLUtilities.prettyXMLPrint(XMLUtilities.xmlToString(x)));
        }
        else
          System.out.println("BPEL Payload is Invalid! Not calling the service.");        
      }
      
      if (six) // With timeout
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        String endPointURL = "";
        String payload = "";
        String payloadURI = "";
        String WSDLUrl = "";
        String response = "";
        boolean ok = true;

        System.out.println("+-------+");
        System.out.println("|  six  |");
        System.out.println("+-------+");    
        
        endPointURL = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService";
        payloadURI = "/oracle/ateam/serviceapp/model/common/types/";
        payload = 
          "<ns1:getDepartments xmlns:ns1=\"" + payloadURI + "\">\n" + 
          "  <ns1:departmentId>40</ns1:departmentId>\n" + 
          "</ns1:getDepartments>";
        
        // 1 - Validating Payload
        WSDLUrl = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService?WSDL";

        ok = suth.validateServicePayload(WSDLUrl, /* payloadURI, */ payload);
        if (ok)
        {    
          System.out.println("--- Payload is valid ---");
          // 2 - Is service up
          if (suth.isServiceUp(WSDLUrl))
          {
            try
            {
              response = suth.invokeServiceFromWSDLwithTimeout(WSDLUrl, 
                                                               payload, 
                                                               SOAPConstants.SOAP_1_1_PROTOCOL, 
                                                               5000L);
              System.out.println("Response:\n" + response);
            }
            catch (ServiceUnitTestHelper.TookTooLongException ttle)
            {
              System.err.println(ttle.getMessage());
  //          ttle.printStackTrace();
            }
          }
          else
            System.out.println("Service is down...");
        }
        else
          System.out.println("Payload id INVALID! Skipping the call.");            
      }
      if (seven)
      {
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        System.out.println("+---------+");
        System.out.println("|  seven  |");
        System.out.println("+---------+");    
        
        try
        {
          String svcEndpoint = suth.getEndpointURL(WSDL_LOC);
          x = suth.invokeSyncServiceWithTimeout(svcEndpoint, 
                                                WSDL_LOC, 
                                                SERVICE_NS_URI, 
                                                SERVICE_REQUEST, 
                                                SERVICE_NAME, 
                                                PORT, 
                                                200L);
          System.out.println("Synchronous, response:");      
          x.print(System.out);
        }
        catch (Exception ex)
        {
          System.err.println(ex.toString());
        }
      }
      if (eight)
      {  
        ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
        System.out.println("+---------+");
        System.out.println("|  eight  |");
        System.out.println("+---------+");    
        
        try
        {
          String operation = "initiate";
          String httpPort  = "1234";
          x = suth.invokeASync2WayServiceWithTimeout(SERVICE_ENDPOINT_2, 
                                                     WSDL_LOC_2, 
                                                     SERVICE_NS_URI_2, 
                                                     SERVICE_REQUEST_2, 
                                                     SERVICE_NAME_2, 
                                                     PORT_2,
                                                     operation,
                                                     httpPort, 
                                                     500L);   
          System.out.println("Async 2 way, response:"); 
          if (x != null)
            x.print(System.out);
          else
          {
            System.out.println("Nothing came back from JAX-WS");
            System.out.println("...but...");
  //        System.out.println(XMLUtilities.prettyXMLPrint(ServiceUnitTestHelper.getAsyncResponse()));
            String resp = ServiceUnitTestHelper.getAsyncResponse();
            System.out.println(resp);
            System.out.println("...............");
            System.out.println("(Length:" + resp.length() + " character(s))");
          }
        }
        catch (ServiceUnitTestHelper.TookTooLongException ttle)
        {
          System.out.println(ttle.toString());
        }
        catch (Exception ex)
        {
          System.out.println("eight:" + ex.toString());
        }
      }
      
      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();      
    }
  }  
}
