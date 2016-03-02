package samples.main;

import javax.xml.soap.SOAPConstants;
import testhelper.ServiceUnitTestHelper;

import testhelper.ServiceUnitTestHelper.TookTooLongException;

public class TestUnit
{
  private final static boolean one   = true, 
                               two   = true, 
                               three = true, 
                               four  = true, 
                               five  = true;

  public static void main(String[] args)
    throws Exception
  {
    TestUnit tu = new TestUnit();
    
    if (one)
    {
      try { tu.one(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    if (two)
    {
      try { tu.two(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    if (three)
    {
      try { tu.three(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    if (four)
    {
      try { tu.four(); } catch (Exception e) { e.printStackTrace(); }
    }
    
    if (five)
    {
      try { tu.five(); } catch (Exception e) { e.printStackTrace(); }
    }
  }
  
  public void one() throws Exception
  {
    String endPointURL = "";
    String payload = "";
    String response = "";
    ServiceUnitTestHelper helper = new ServiceUnitTestHelper();

    System.out.println("-----------");
    System.out.println("!   ONE   !");
    System.out.println("-----------");
    // HelloWorld default process
    endPointURL = "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/HelloWorld!1.0*2007-08-24_22-10-25_602/client";    
    payload =
    "<ns1:HelloWorldProcessRequest xmlns:ns1=\"http://xmlns.oracle.com/HelloWorldApp/HelloWorld/HelloWorld\">\n" + 
    "  <ns1:input>SOA UnitTesting</ns1:input>\n" + 
    "</ns1:HelloWorldProcessRequest>\n";
    response = helper.dynamicallyCallService(endPointURL, 
                                             payload, 
                                             SOAPConstants.SOAP_1_2_PROTOCOL);
    System.out.println("Response:\n" + response);        
  }
  
  public void two() throws Exception
  {
    String endPointURL = "";
    String payload = "";
    String response = "";
    boolean ok = true;

    ServiceUnitTestHelper helper = new ServiceUnitTestHelper();

    System.out.println("-----------");
    System.out.println("!   TWO   !");
    System.out.println("-----------");
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
    
    ok = helper.validateServicePayload("http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AssignService!1.0*2008-07-23_14-37-53_767/client?WSDL", 
                                   //  "http://www.oracle.apps.scm.doo.decomp.org", 
                                       payload);
    if (ok)
    {
      System.out.println("BPEL payload is valid.");
      response = helper.dynamicallyCallService(endPointURL, 
                                                 payload, 
                                                 SOAPConstants.SOAP_1_1_PROTOCOL);
      System.out.println("Response:\n" + response);    
    }
    else
      System.out.println("BPEL Payload is Invalid! Not calling the service.");    
  }
  
  public void three() throws Exception
  {
    String endPointURL = "";
    String payload = "";
    String payloadURI = "";
    String WSDLUrl = "";
    String response = "";
    boolean ok = true;

    ServiceUnitTestHelper helper = new ServiceUnitTestHelper();

    System.out.println("-----------");
    System.out.println("!  THREE  !");
    System.out.println("-----------");    
    // ADFbc AM Service
    endPointURL = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService";
    payloadURI = "/oracle/ateam/serviceapp/model/common/types/";
    payload = 
      "<ns1:getDepartments xmlns:ns1=\"" + payloadURI + "\">\n" + 
      "  <ns1:departmentId>40</ns1:departmentId>\n" + 
      "</ns1:getDepartments>";
    
    // 1 - Validating Payload
    WSDLUrl = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService?WSDL";
    ok = helper.validateServicePayload(WSDLUrl, /* payloadURI, */ payload);
    if (ok)
    {    
      System.out.println("Payload is valid");
      // 2 - Send the payload, get the response
      response = helper.dynamicallyCallService(endPointURL, 
                                                 payload, 
                                                 SOAPConstants.SOAP_1_1_PROTOCOL);
      System.out.println("Response:\n" + response);    
    }
    else
      System.out.println("Payload id INVALID! Skipping the call.");
  }
  
  public void four() throws Exception
  {
    String endPointURL = "";
    String payload = "";
    String payloadURI = "";
    String WSDLUrl = "";
    String response = "";
    boolean ok = true;

    ServiceUnitTestHelper helper = new ServiceUnitTestHelper();

    System.out.println("-----------");
    System.out.println("!  FOUR   !");
    System.out.println("-----------");    
    
    endPointURL = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService";
    payloadURI = "/oracle/ateam/serviceapp/model/common/types/";
    payload = 
      "<ns1:getDepartments xmlns:ns1=\"" + payloadURI + "\">\n" + 
      "  <ns1:departmentId>40</ns1:departmentId>\n" + 
      "</ns1:getDepartments>";
    
    // 1 - Validating Payload
    WSDLUrl = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/DepartmentAMService?WSDL";

    ok = helper.validateServicePayload(WSDLUrl, /* payloadURI, */ payload);
    if (ok)
    {    
      System.out.println("--- Payload is valid ---");
      // 2 - Is service up
      if (helper.isServiceUp(WSDLUrl))
      {
        try
        {
          response = helper.invokeServiceFromWSDLwithTimeout(WSDLUrl, payload, SOAPConstants.SOAP_1_1_PROTOCOL, 5000L);
          System.out.println("Response:\n" + response);
        }
        catch (ServiceUnitTestHelper.TookTooLongException tle)
        {
          System.err.println(tle.getMessage());
    //          tle.printStackTrace();
        }
      }
      else
        System.out.println("Service is down...");
    }
    else
      System.out.println("Payload id INVALID! Skipping the call.");    
  }
  
  public void five() throws Exception
  {
    String payload = "";
    String payloadURI = "";
    String WSDLUrl = "";
    String response = "";
    boolean ok = true;

    ServiceUnitTestHelper helper = new ServiceUnitTestHelper();

    System.out.println("-----------");
    System.out.println("!  FIVE   !");
    System.out.println("-----------");    
    
    WSDLUrl = "http://adc60044fems.us.oracle.com:8880/SetTransform/SetTransformService?WSDL";
    payloadURI = "http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/";
    
    // That one is bad.
    payload = 
    "<simpleLookup xmlns=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\">\n" + 
    "   <docSDO xmlns:ns2=\"http://xmlns.oracle.com/apps/orderCapture/demo/priceDoc\">\n" + 
    "      <ns2:header>\n" + 
    "         <ns2:headerId>1</ns2:headerId>\n" + 
    "         <ns2:currencyCode>USD</ns2:currencyCode>\n" + 
    "         <ns2:pricingDate>2007-06-16T00:00:00</ns2:pricingDate>\n" + 
    "         <ns2:totalPrice>0</ns2:totalPrice>\n" + 
    "         <ns2:discountPercent>10</ns2:discountPercent>\n" + 
    "      </ns2:header>\n" + 
    "      <ns2:item>\n" + 
    "         <ns2:lineId>11</ns2:lineId>\n" + 
    "         <ns2:headerId>1</ns2:headerId>\n" + 
    "         <ns2:inventoryItemId>9100</ns2:inventoryItemId>\n" + 
    "         <ns2:lineNumber>1001</ns2:lineNumber>\n" + 
    "         <ns2:quantity>2</ns2:quantity>\n" + 
    "         <ns2:priceListId>9000</ns2:priceListId>\n" + 
    "         <ns2:discountPercent>20</ns2:discountPercent>\n" + 
    "      </ns2:item>\n" + 
    "   </docSDO>\n" + 
    "   <ns0:simpleLookupParamSDO xmlns=\"http://xmlns.oracle.com/apps/orderCapture/core/setTransform/data\"\n" + 
    "                             xmlns:ns3=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\"\n" + 
    "                             xmlns:ns0=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\">\n" + 
    "      <uniqueKey>PriceListItemLookup</uniqueKey>\n" + 
    "      <name>PriceListItemLookup</name>\n" + 
    "      <dataSchema>priceDoc1.xml</dataSchema>\n" + 
    "      <primarySet>\n" + 
    "         <alias>row</alias>\n" + 
    "         <name>item</name>\n" + 
    "      </primarySet>\n" + 
    "      <referenceSet>\n" + 
    "         <alias>context</alias>\n" + 
    "         <name>header</name>\n" + 
    "         <cardinality>1</cardinality>\n" + 
    "         <joinSpec>\n" + 
    "            <referenceSetField>headerId</referenceSetField>\n" + 
    "            <primarySetField>headerId</primarySetField>\n" + 
    "         </joinSpec>\n" + 
    "      </referenceSet>\n" + 
    "      <appModule>oracle.apps.qocPricingDemo.priceList.model.AppModulePriceList</appModule>\n" + 
    "      <appModuleConfig>AppModulePriceListShared</appModuleConfig>\n" + 
    "      <viewObject>PriceListItem1</viewObject>\n" + 
    "      <viewCriteria>PriceListIdProdIdCriteria</viewCriteria>\n" + 
    "      <inMemoryFilteringCriteria>row.priceListId != null</inMemoryFilteringCriteria>\n" + 
    "      <cacheCriteria>group1</cacheCriteria>\n" + 
    "      <sortCriteria/>\n" + 
    "      <processCondition>row.priceListId != null</processCondition>\n" + 
    "      <result>\n" + 
    "         <onFirstMatch>\n" + 
    "            <action>row.listPrice = match.listPrice</action>\n" + 
    "            <action>context.totalPrice = 0.0</action>\n" + 
    "         </onFirstMatch>\n" + 
    "      </result>\n" + 
    "      <viewCriteriaBindVariables>\n" + 
    "         <bindVariable>\n" + 
    "            <name>PriceLstId</name>\n" + 
    "            <bindValue>row.priceListId</bindValue>\n" + 
    "         </bindVariable>\n" + 
    "         <bindVariable>\n" + 
    "            <name>ProdId</name>\n" + 
    "            <bindValue>row.inventoryItemId</bindValue>\n" + 
    "         </bindVariable>\n" + 
    "      </viewCriteriaBindVariables>\n" + 
    "   </ns0:simpleLookupParamSDO>\n" + 
    "</simpleLookup>";
    
    if (true) // overriding?
    {
      // This one is good
      payload = 
      "<simpleLookup xmlns=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\">\n" + 
      "   <docSDO xmlns:ns2=\"http://xmlns.oracle.com/apps/orderCapture/demo/priceDoc\">\n" + 
      "      <ns2:header>\n" + 
      "         <ns2:headerId>1</ns2:headerId>\n" + 
      "         <ns2:currencyCode>USD</ns2:currencyCode>\n" + 
      "         <ns2:discountPercent>10</ns2:discountPercent>\n" + 
      "      </ns2:header>\n" + 
      "      <ns2:item>\n" + 
      "         <ns2:lineId>11</ns2:lineId>\n" + 
      "         <ns2:headerId>1</ns2:headerId>\n" + 
      "         <ns2:inventoryItemId>9100</ns2:inventoryItemId>\n" + 
      "         <ns2:lineNumber>1001</ns2:lineNumber>\n" + 
      "         <ns2:quantity>8</ns2:quantity>\n" + 
      "         <ns2:priceListId>9000</ns2:priceListId>\n" + 
      "         <ns2:discountPercent>20</ns2:discountPercent>\n" + 
      "      </ns2:item>\n" + 
      "   </docSDO>\n" + 
      "   <ns0:paramSDO xmlns=\"http://xmlns.oracle.com/apps/orderCapture/core/setTransform/data\"\n" + 
      "                 xmlns:ns3=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\"\n" + 
      "                 xmlns:ns0=\"http://xmlns.oracle.com/oracle/apps/orderCapture/core/setTransform/types/\">\n" + 
      "      <uniqueKey>PriceListItemLookup</uniqueKey>\n" + 
      "      <name>PriceListItemLookup</name>\n" + 
      "      <dataSchema>priceDoc1.xml</dataSchema>\n" + 
      "      <primarySet>\n" + 
      "         <alias>row</alias>\n" + 
      "         <name>item</name>\n" + 
      "      </primarySet>\n" + 
      "      <referenceSet>\n" + 
      "         <alias>context</alias>\n" + 
      "         <name>header</name>\n" + 
      "         <cardinality>1</cardinality>\n" + 
      "         <joinSpec>\n" + 
      "            <referenceSetField>headerId</referenceSetField>\n" + 
      "            <primarySetField>headerId</primarySetField>\n" + 
      "         </joinSpec>\n" + 
      "      </referenceSet>\n" + 
      "      <!-- Added by Oliv -->\n" + 
      "      <contextName/>\n" + 
      "      <rowSetName/>\n" + 
      "      <!-- End Oliv -->\n" + 
      "      <appModule>oracle.apps.qocPricingDemo.priceList.model.AppModulePriceList</appModule>\n" + 
      "      <appModuleConfig>AppModulePriceListShared</appModuleConfig>\n" + 
      "      <viewObject>PriceListItem1</viewObject>\n" + 
      "      <viewCriteria>PriceListIdProdIdCriteria</viewCriteria>\n" + 
      "      <inMemoryFilteringCriteria>row.priceListId != null</inMemoryFilteringCriteria>\n" + 
      "      <cacheCriteria>group1</cacheCriteria>\n" + 
      "      <sortCriteria/>\n" + 
      "      <processCondition>row.priceListId != null</processCondition>\n" + 
      "      <result>\n" + 
      "         <onFirstMatch>\n" + 
      "            <action>row.listPrice = match.listPrice</action>\n" + 
      "            <action>context.totalPrice = 0.0</action>\n" + 
      "         </onFirstMatch>\n" + 
      "      </result>\n" + 
      "      <viewCriteriaBindVariables>\n" + 
      "         <bindVariable>\n" + 
      "            <name>PriceLstId</name>\n" + 
      "            <bindValue>row.priceListId</bindValue>\n" + 
      "         </bindVariable>\n" + 
      "         <bindVariable>\n" + 
      "            <name>ProdId</name>\n" + 
      "            <bindValue>row.inventoryItemId</bindValue>\n" + 
      "         </bindVariable>\n" + 
      "      </viewCriteriaBindVariables>\n" + 
      "   </ns0:paramSDO>\n" + 
      "</simpleLookup>";
    }
    ok = helper.validateServicePayload(WSDLUrl, /* payloadURI, */ payload);
    if (ok)
    {    
      System.out.println("--- Payload is valid ---");
      // 2 - Is service up
      if (helper.isServiceUp(WSDLUrl))
      {
        long before = System.currentTimeMillis();
        try
        {
          response = helper.invokeServiceFromWSDLwithTimeout(WSDLUrl, payload, SOAPConstants.SOAP_1_1_PROTOCOL, 1000L);
          System.out.println("Response:\n" + response);
        }
        catch (ServiceUnitTestHelper.TookTooLongException tle)
        {
          System.err.println(tle.getMessage());
    //          tle.printStackTrace();
        }
        finally
        {
          long after = System.currentTimeMillis();
          System.out.println("Executed in " + Long.toString(after - before) + " ms.");
        }
      }
      else
        System.out.println("Service is down...");
    }
    else
      System.out.println("Payload id INVALID! Skipping the call.");
  }
}
