package samples.main;

import java.io.File;

import java.io.StringReader;

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
public class SampleMain5
{
  private static final String WSDL_URL = "http://130.35.95.84:7101/B7aFusionWebServices-Model-context-root/MyAppModuleService?WSDL";
  //private final static String WSDL_URL = "http://130.35.95.59:7101/AndreAsyncWS-Model-context-root/AsyncAppModuleService?WSDL";
  private static final String PAYLOAD = "<ns1:sayHello xmlns:ns1=\"http://xmlns.oracle.com/oracle/apps/types/\">\n" +
    "   <ns1:name>Pouic</ns1:name>\n" +
    "</ns1:sayHello>";

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

    {
      XMLElement x =
        suthws.invokeSyncService(suthws.getEndpointURL(WSDL_URL), 
                                 WSDL_URL, 
                                 "http://xmlns.oracle.com/oracle/apps/types/", 
                                 new StringReader(PAYLOAD), 
                                 "MyAppModuleService", 
                                 "portName",
                                 null,
                                 "sayHello");
      x.print(System.out);
    }
  }
}
