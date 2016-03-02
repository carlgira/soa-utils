package samples.unittest;

import java.io.FileReader;

import oracle.xml.parser.v2.XMLElement;
import testhelper.ServiceUnitTestHelper;

public class MelroyServiceClientTest
{
  ServiceUnitTestHelper helper;
  
  private final static String SERVICE_ENDPOINT_URL = "http://ukt65005fwks.uk.oracle.com:8988/ade/melperei_UnitTest02/fusionapps/hcm/per/components/people/core/model/classes/PersonService";
  private final static String SERVICE_WSDL_URL = SERVICE_ENDPOINT_URL + "?WSDL";
  private final static String PAYLOAD =
  "<ns1:getPerson xmlns:ns1=\"http://xmlns.oracle.com/oracle/apps/hcm/people/core/personService/types/\">\n" + 
  "    <ns1:personId>7814</ns1:personId>\n" + 
  "</ns1:getPerson>";
  private final static String PAYLOAD_FILE = "melroy-payload.xml";
  
  private final static String SERVICE_NS_URI = "http://xmlns.oracle.com/oracle/apps/hcm/people/core/personService/";
  private final static String SERVICE_NAME = "PersonService";
  private final static String PORT_NAME    = "PersonServiceSoapHttpPort";
  

  public MelroyServiceClientTest()
  {
    helper = new ServiceUnitTestHelper();
  }

  public void testIsServiceUp()
  {
    String url = SERVICE_WSDL_URL;
    boolean ok = helper.isServiceUp(url);
    System.out.println("Service is " + (ok?"":"not ") + "up");
  }
  
  public void testIfPayloadOK()
  {
    try
    {
      boolean ok = helper.validateServicePayload(SERVICE_WSDL_URL, PAYLOAD);
      System.out.println("Payload is " + (ok?"":"not ") + "valid");      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void testIfPayloadOK_v2()
  {
    try
    {
      boolean ok = helper.validateServicePayload(SERVICE_WSDL_URL, new FileReader(PAYLOAD_FILE));
      System.out.println("Payload in file is " + (ok?"":"not ") + "valid");      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void invokeService()
  {

    try
    {
      boolean ok = helper.isServiceNameOK(SERVICE_WSDL_URL, SERVICE_NAME);
      System.out.println("Service Name is " + (ok?"":"not ") + "OK");      
      ok = helper.isPortNameOK(SERVICE_WSDL_URL, SERVICE_NAME, PORT_NAME);
      System.out.println("Port Name is " + (ok?"":"not ") + "OK");      
      
      XMLElement response =
        helper.invokeSyncService(SERVICE_ENDPOINT_URL, 
                                 SERVICE_WSDL_URL,
                                 SERVICE_NS_URI, 
                                 PAYLOAD, 
                                 SERVICE_NAME, 
                                 PORT_NAME);
      // Test the response here
      response.print(System.out);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
 
  public void invokeService_v2()
  {

    try
    {
      boolean ok = helper.isServiceNameOK(SERVICE_WSDL_URL, SERVICE_NAME);
      System.out.println("Service Name is " + (ok?"":"not ") + "OK");      
      ok = helper.isPortNameOK(SERVICE_WSDL_URL, SERVICE_NAME, PORT_NAME);
      System.out.println("Port Name is " + (ok?"":"not ") + "OK");      
      
      XMLElement response =
        helper.invokeSyncService(SERVICE_ENDPOINT_URL, 
                                 SERVICE_WSDL_URL,
                                 SERVICE_NS_URI, 
                                 new FileReader(PAYLOAD_FILE), 
                                 SERVICE_NAME, 
                                 PORT_NAME);
      // Test the response here
      response.print(System.out);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args)  
  {
    MelroyServiceClientTest mrct = new MelroyServiceClientTest();
    mrct.testIsServiceUp();
    System.out.println("-----------------");
    // V1 (With payload as String)
    mrct.testIfPayloadOK();
    mrct.invokeService();
    System.out.println("-----------------");
    // V2 (With payload in a file)
    mrct.testIfPayloadOK_v2();
    mrct.invokeService_v2();
    System.out.println("-----------------");
  } 
}
