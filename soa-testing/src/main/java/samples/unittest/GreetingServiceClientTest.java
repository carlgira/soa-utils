package samples.unittest;

import java.io.StringReader;

import javax.xml.soap.SOAPConstants;

import samples.main.GreetingServiceClient;

import oracle.xml.parser.v2.DOMParser;

import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import static org.junit.Assert.*;
import org.junit.Test;

import org.w3c.dom.NodeList;

import testhelper.Context;
import testhelper.ServiceUnitTestHelper;

import testhelper.ServiceUnitTestHelper.TookTooLongException;

public class GreetingServiceClientTest
{
  private DOMParser parser;
  private ServiceUnitTestHelper helper;

  public GreetingServiceClientTest()
  {
    parser = Context.getInstance().getParser(); // new DOMParser();
    helper = new ServiceUnitTestHelper();
  }

  @Test
  public void testIsServiceUp()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    assertTrue("Service is not up", helper.isServiceUp(url));
  }

  @Test
  public void testInvokeService()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayHelloElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Oliv</ns1:who>\n" +
      "  <ns1:country>FR</ns1:country>\n" +
      "</ns1:sayHelloElement>";
    try
    {
      String resp =
        helper.invokeServiceFromWSDL(url, payload, SOAPConstants.SOAP_1_1_PROTOCOL);
      parser.parse(new StringReader(resp));
      XMLDocument doc1 = parser.getDocument();
      NodeList nl = doc1.selectNodes("//prefix:result", new NSResolver()
          {
            public String resolveNamespacePrefix(String string)
            {
              return "http://serviceandmethodtests/types/";
            }
          });
      assertEquals("Same XML Document", "Bonjour Oliv!",
                   nl.item(0).getFirstChild().getNodeValue());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void testInvokeServiceSayByeMethod()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElement>";
    try
    {
      String resp =
        helper.invokeServiceFromWSDL(url, payload, SOAPConstants.SOAP_1_1_PROTOCOL);
      parser.parse(new StringReader(resp));
      XMLDocument doc1 = parser.getDocument();
      NodeList nl = doc1.selectNodes("//prefix:result", new NSResolver()
          {
            public String resolveNamespacePrefix(String string)
            {
              return "http://serviceandmethodtests/types/";
            }
          });
      assertEquals("Same XML Document", "Good bye Brian!",
                   nl.item(0).getFirstChild().getNodeValue());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void testInvokeServiceWithBadPayload()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElementXX xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElementXX>";
    try
    {
      String resp =
        helper.invokeServiceFromWSDL(url, payload, SOAPConstants.SOAP_1_1_PROTOCOL);
      //  System.out.println("Returned\n" + resp);
      parser.parse(new StringReader(resp));
      XMLDocument doc1 = parser.getDocument();
      NodeList nl = doc1.selectNodes("//prefix:result", new NSResolver()
          {
            public String resolveNamespacePrefix(String string)
            {
              return "http://serviceandmethodtests/types/";
            }
          });
      assertEquals("Same XML Document", "Good bye Brian!",
                   nl.item(0).getFirstChild().getNodeValue());
    }
    catch (Exception ex)
    {
      //    ex.printStackTrace();
      //    fail("Service invocation failed:" + ex.toString());
      assertTrue("Expected Exception", true);
    }
  }

  @Test
  public void testInvokeServiceValidateBadPayload()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElementXX xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElementXX>";
    try
    {
      boolean ok = helper.validateServicePayload(url, payload);
      assertTrue("Bad Payload", ok);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void testInvokeServiceValidatePayload()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElement>";
    try
    {
      boolean ok = helper.validateServicePayload(url, payload);
      assertTrue("Payload Validation", ok);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void testInvokeServiceWithTimeout()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElement>";
    try
    {
      String resp =
        helper.invokeServiceFromWSDLwithTimeout(url, payload, SOAPConstants.SOAP_1_1_PROTOCOL,
                                                5000L);
      //  System.out.println("Returned\n" + resp);
      parser.parse(new StringReader(resp));
      XMLDocument doc1 = parser.getDocument();
      NodeList nl = doc1.selectNodes("//prefix:result", new NSResolver()
          {
            public String resolveNamespacePrefix(String string)
            {
              return "http://serviceandmethodtests/types/";
            }
          });
      assertEquals("Same XML Document", "Good bye Brian!",
                   nl.item(0).getFirstChild().getNodeValue());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void fullTest()
  {
    String url =
      "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    String payload =
      "<ns1:sayByeElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" +
      "  <ns1:who>Brian</ns1:who>\n" +
      "  <ns1:country>US</ns1:country>\n" +
      "</ns1:sayByeElement>";

    try
    {
      boolean ok = helper.validateServicePayload(url, payload);
      assertTrue("Bad Payload", ok);

      if (ok)
      {
        ok = helper.isServiceUp(url);
        assertTrue("Service unreacheable", ok);

        if (ok)
        {
          String resp =
            helper.invokeServiceFromWSDLwithTimeout(url, payload,
                                                    SOAPConstants.SOAP_1_1_PROTOCOL,
                                                    1000L);
          parser.parse(new StringReader(resp));
          XMLDocument doc1 = parser.getDocument();
          NodeList nl =
            doc1.selectNodes("//prefix:result", new NSResolver()
              {
                public String resolveNamespacePrefix(String string)
                {
                  return "http://serviceandmethodtests/types/";
                }
              });
          assertEquals("Same XML Document", "Good bye Brian!",
                       nl.item(0).getFirstChild().getNodeValue());
        }
      }
    }
    catch (ServiceUnitTestHelper.TookTooLongException tle)
    {
      fail(tle.toString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  @Test
  public void jaxWSTest()
  {
    try
    {
      String operation = "initiate";
      String httpPort = "1234";
      String serviceName = "AsyncBPELProcess";
      String port = "AsyncBPELProcess_pt";
      String serviceEndpoint =
        "http://fpp-ta04.us.oracle.com:8888/soa-infra/services/default/AsyncBPEL!1.0*2008-09-25_09-59-26_194/AsyncBPEL_ep";
      String wsdlLocation = serviceEndpoint + "?WSDL";
      String serviceNSURI =
        "http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL";

      String servicePayload =
        "<ns1:AsyncBPELProcessRequest xmlns:ns1=\"http://xmlns.oracle.com/SeveralComposites/AsyncBPEL/AsyncBPEL\">\n" +
        "  <ns1:input>Oliv</ns1:input>\n" +
        "</ns1:AsyncBPELProcessRequest>";
      XMLElement x =
        helper.invokeASync2WayServiceWithTimeout(serviceEndpoint,
                                                 wsdlLocation,
                                                 serviceNSURI,
                                                 servicePayload,
                                                 serviceName, port,
                                                 operation, httpPort,
                                                 5000L);
      //    System.out.println("Async 2 way, response:");
      if (x != null)
        x.print(System.out);
      else
      {
        //      System.out.println("Nothing came back from JAX-WS");
        //      System.out.println("...but...");
        //      System.out.println(XMLUtilities.prettyXMLPrint(ServiceUnitTestHelper.getAsyncResponse()));
        String resp = ServiceUnitTestHelper.getAsyncResponse();
        assertTrue("Response is empty", resp.length() > 0);
        assertTrue("Expected content not found in payload",
                   resp.indexOf("I received:Oliv") > -1);

        //      System.out.println(resp);
        //      System.out.println("...............");
        //      System.out.println("(Length:" + resp.length() + " character(s))");
      }
    }
    catch (ServiceUnitTestHelper.TookTooLongException tle)
    {
      fail(tle.toString());
    }
    catch (Exception ex)
    {
      System.err.println(ex.toString());
      fail(ex.toString());
    }
  }
}
