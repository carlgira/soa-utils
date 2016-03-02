package samples.main;

import java.io.File;

import java.net.BindException;

import oracle.xml.parser.v2.XMLElement;

import testhelper.ServiceUnitTestHelper;

/**
 * Async-2-ways
 * With Security
 *
 * Must be run with -Doracle.security.jps.config=./security/config/jps-config.xml
 * 
 * For information about security, check out https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=205560
 */
public class SampleMain4
{
  private final static String WSDL_URL = "http://130.35.95.84:7101/AndreAsyncWS-Model-context-root/AsyncAppModuleService?WSDL";
//private final static String WSDL_URL = "http://130.35.95.59:7101/AndreAsyncWS-Model-context-root/AsyncAppModuleService?WSDL";
  private final static String PAYLOAD = 
    "<ns1:processOrderAsync xmlns:ns1=\"http://xmlns.oracle.com/oracle/apps/types/\">\n" +
    "   <ns1:order>1234</ns1:order>\n" +
    "</ns1:processOrderAsync>";

  public static void main(String[] args)
    throws Exception
  {
    ServiceUnitTestHelper suthws = new ServiceUnitTestHelper();
    boolean verbose = true;
    suthws.setVerbose(verbose);    
    
    boolean ok = suthws.isServiceUp(WSDL_URL);
    if (!ok)
    {
      System.out.println("Ah shit! It is down!!");
      System.out.println("Aborting.");
      System.exit(1);
    }
    System.out.println("Service is up...");
    // Security Parameters
    suthws.setUsername("andre");
    suthws.setPassword("welcome1");
    suthws.setPolicySecurity(true);
//  suthws.setPolicyDocumentLocation(new File("Policy.xml").toURI().toURL());
    
    int startFromPort = 12345;
    boolean keepTrying = true;
    while (keepTrying)
    {
      String portStr = Integer.toString(startFromPort);
      System.out.println("Will callback on port:" + portStr);
      try
      {
        XMLElement x = suthws.invokeASync2WayService(suthws.getEndpointURL(WSDL_URL), 
                                                     WSDL_URL, 
                                                     "http://xmlns.oracle.com/oracle/apps/", 
                                                     PAYLOAD,
                                                     "AsyncAppModuleService", 
                                                     "AsyncAppModuleServiceSoapHttpPort", 
                                                     "processOrderAsync", 
                                                     portStr);
        String response = ServiceUnitTestHelper.getAsyncResponse();
        if (verbose)
        {
          System.out.println("(Length:" + response.length() + " character(s))");
          System.out.println("Response: [" + response + "]");
        }
        System.out.println("Async response: [" + ServiceUnitTestHelper.decryptAsyncResponseBody(response) + "]");
        keepTrying = false;
      }
      catch (Exception e)
      {
        Throwable cause = e.getCause();
        if (cause instanceof BindException)
        {
          System.out.println("Incrementing port number...");
          startFromPort++;
        }
        else
        {
          keepTrying = false;
          e.printStackTrace();
        }
      }
    }
  }
}
