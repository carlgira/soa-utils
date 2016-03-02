package samples.unittest;


import oracle.xml.parser.v2.XMLElement;
import testhelper.ServiceUnitTestHelper;

/**
 * Tests the JAX-WS Client interface
 */
public class CompositeTest
{
  private final static String SERVICE_NAME     = "client";
  private final static String PORT             = "ControlsProcess_pt";
  
  private final static String SERVICE_ENDPOINT = "http://adc60037fems.us.oracle.com:8828/soa-infra/services/default/ControlsComposite!1.0*2008-05-28_21-43-49_359/" + SERVICE_NAME;
  private final static String WSDL_LOC         = SERVICE_ENDPOINT + "?WSDL";
  private final static String SERVICE_NS_URI   = "http://www.oracle.com/ns/grc";
  
  private final static String SERVICE_REQUEST = 
  "<ns2:Delegation xmlns:ns2=\"http://www.oracle.com/ns/grc\">" +
  "   <ns2:ObjectKey>100000010072001</ns2:ObjectKey>" +
  "   <ns2:ObjectID>10035</ns2:ObjectID>" +
  "   <ns2:EffectiveStartDate>1969-12-31-08:00</ns2:EffectiveStartDate>" +
  "   <ns2:EffectiveEndDate>1969-12-31-08:00</ns2:EffectiveEndDate>" +
  "   <ns2:EffectiveSequence>0</ns2:EffectiveSequence>" +
  "   <ns2:ObjectType>GRC_CONTROL</ns2:ObjectType>" +
  "   <ns2:DraftActivityType>GRC_CONTROLSCREATEPG</ns2:DraftActivityType>" +
  "   <ns2:ManageActivityType>GRC_CTRLATAGLANCEPG</ns2:ManageActivityType>" +
  "   <ns2:ModuleCode>GRC_CM</ns2:ModuleCode>" +
  "   <ns2:SubjectInstance>java created2</ns2:SubjectInstance>" +
  "   <ns2:CreatedByUser>istone</ns2:CreatedByUser>" +
  "   <ns2:EntityName>oracle.apps.grc.framework.controls.model.entity.ControlsEO</ns2:EntityName>" +
  "   <ns2:EntityAltKeyName>AltKeyBPEL</ns2:EntityAltKeyName>" +
  "   <ns2:FinalState>ACTIVE</ns2:FinalState>" +
  "   <ns2:UserAction>SAVE</ns2:UserAction>" +
  "   <ns2:SubjectType/>" +
  "   <ns2:Owners/>" +
  "   <ns2:OwnerCode>OWNER</ns2:OwnerCode>" +
  "   <ns2:StateCode>NEW</ns2:StateCode>" +
  "   <ns2:LastStateCode>NEW</ns2:LastStateCode>" +
  "   <ns2:Submitter/>" +
  "   <ns2:DelegationRuntimeId>0</ns2:DelegationRuntimeId>" +
  "   <ns2:ReturnStatus/>" +
  "</ns2:Delegation>";
  
  
  public static void main(String[] args) throws Exception
  {
    ServiceUnitTestHelper suth = new ServiceUnitTestHelper();    
    suth.setVerbose(false);
    
    System.out.println("WSDL:" + WSDL_LOC);
    
    try
    {
      XMLElement x = null;
     
      boolean ok = suth.isServiceUp(WSDL_LOC);
      System.out.println("Service is " + (ok?"":"not ") + "up");
      
      ok = suth.validateServicePayload(WSDL_LOC, SERVICE_REQUEST);
      if (ok)
      {        
        ok = suth.isServiceNameOK(WSDL_LOC, SERVICE_NAME);
        System.out.println("Service name is " + (ok?"":"NOT ") + "OK");
        ok = suth.isPortNameOK(WSDL_LOC, SERVICE_NAME, PORT);
        System.out.println("Port name is " + (ok?"":"NOT ") + "OK");
        
        long before = System.currentTimeMillis();
        String operation = "initiate";
        String httpPort = "2345";
        try
        {
          x = suth.invokeASync2WayServiceWithTimeout(SERVICE_ENDPOINT, 
                                                     WSDL_LOC, 
                                                     SERVICE_NS_URI, 
                                                     SERVICE_REQUEST,  
                                                     SERVICE_NAME, 
                                                     PORT,
                                                     operation,    // Operation
                                                     httpPort,     // HTTP Port for the tiny server 
                                                     1000L);       // Timeout
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
        catch (ServiceUnitTestHelper.TookTooLongException ttle)
        {
          System.out.println(ttle.toString());
        }
      }
      else
        System.out.println("Invalid payload");
      
      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();      
    }
  }  
}
