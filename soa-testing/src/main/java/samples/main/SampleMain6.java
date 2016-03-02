package samples.main;

import oracle.xml.parser.v2.XMLElement;
import testhelper.ServiceUnitTestHelper;

/**
 * Tests the JAX-WS Client interface
 * Synchronous, with Security (WRONG CLASS BEING LOADED WITH UTF)
 */
public class SampleMain6
{

  // WSDL: http://adc60016fems.us.oracle.com:6027/pjcTransactions/ProjectExpenditureItemService?WSDL
  // For the synchronous service
  private static final String SERVICE_NAME = "ProjectExpenditureItemService";
  private static final String PORT = "ProjectExpenditureItemService";

  private static final String SERVICE_ENDPOINT =
    "http://adc60016fems.us.oracle.com:6027/pjcTransactions/ProjectExpenditureItemService";
  private static final String WSDL_LOC = SERVICE_ENDPOINT + "?WSDL";
  private static final String SERVICE_NS_URI =
    "http://xmlns.oracle.com/apps/projects/costing/transactions/publicService/";

  private static final String SERVICE_REQUEST =
    "<ns1:findProjectExpenditureItem xmlns:ns1=\"http://xmlns.oracle.com/apps/projects/costing/transactions/publicService/types/\">\n" + 
    "    <ns1:bindProjectNumber>IMP-TMPP-D7-992</ns1:bindProjectNumber>\n" + 
    "    <ns1:bindSourceName>Oracle Projects</ns1:bindSourceName>\n" + 
    "    <ns1:bindDocumentName>Usage Expenditure</ns1:bindDocumentName>\n" + 
    "    <ns1:bindBusinessUnitName>Vision Operations</ns1:bindBusinessUnitName>\n" + 
    "    <ns1:findControl xmlns:ns3=\"http://xmlns.oracle.com/adf/svc/types/\">\n" + 
    "        <ns3:retrieveAllTranslations/>\n" + 
    "    </ns1:findControl>\n" + 
    "</ns1:findProjectExpenditureItem>";
  
  private final static String USERNAME = "PROJECT_ACCOUNTANT_VISION_OPERATIONS";
  private final static String PASSWORD = "Welcome1";
  
  public static void main(String[] args)
    throws Exception
  {
    try
    {
      XMLElement x = null;

      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setPolicySecurity(true);
      suth.setUsernamePasswordSecurity(true);
      suth.setUsername(USERNAME);  
      suth.setPassword(PASSWORD);
//    suth.setHandlerResolver(new CustomHandlerResolver());
      
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
