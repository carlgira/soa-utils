package samples.main;

import java.net.URL;
import java.net.URLConnection;

import java.util.HashMap;

import javax.xml.soap.SOAPConstants;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import testhelper.Context;
import testhelper.ServiceUnitTestHelper;

public class GreetingServiceClient
{
  private static DOMParser parser = Context.getInstance().getParser();

  public GreetingServiceClient()
  {
  }

  private final static String getEndpointURL(String wsdl)
  {
    String ep = "";
    try
    {
      URL wsdlurl = new URL(wsdl);
      parser.parse(wsdlurl);
      LocalNSResolver nsr = new LocalNSResolver();
      nsr.setPrefix("soap",   "http://schemas.xmlsoap.org/wsdl/soap/");
      nsr.setPrefix("soap12", "http://schemas.xmlsoap.org/wsdl/soap12/");
      nsr.setPrefix("xsd",    "http://www.w3.org/2001/XMLSchema");
      nsr.setPrefix("wsdl",   "http://schemas.xmlsoap.org/wsdl/");
      XMLDocument doc = parser.getDocument();
      NodeList nl = doc.selectNodes("//soap:address", nsr);
      System.out.println("Returned " + nl.getLength() + " node(s)");
      ep = ((XMLElement)nl.item(0)).getAttribute("location");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex.toString());
    }
    return ep;
  }
  
  public static boolean isServiceUp(String wsdlurl) throws RuntimeException
  {
    boolean soWhat = false;
    try
    {
      URL wsdl = new URL(wsdlurl);
      URLConnection connection = wsdl.openConnection();
      connection.connect();
      soWhat = true;
    }
    catch (Exception ex)
    {
      soWhat = false;
      throw new RuntimeException(ex.toString());
    }
    return soWhat;
  }
  
  public static String invokeService(String wsdl, String payload, String protocol) throws Exception
  {
    String response = new ServiceUnitTestHelper().dynamicallyCallService(getEndpointURL(wsdl), 
                                                                   payload, 
                                                                   protocol);
    return response;
  }
  
  public static void main(String[] args) throws Exception
  {
    String serviceWSDLURL = "http://fpp-ta04:8888/greeting-ws/GreetringServiceSoapHttpPort?WSDL";
    // 1 - Is service available?
    boolean b = isServiceUp(serviceWSDLURL);
    if (b)
    {
        // 2 - Does it return what's expected
      String s = getEndpointURL(serviceWSDLURL);
      System.out.println("Endpoint:" + s);
      String payload = 
      "<ns1:sayHelloElement xmlns:ns1=\"http://serviceandmethodtests/types/\">\n" + 
      "  <ns1:who>Oliv</ns1:who>\n" + 
      "  <ns1:country>FR</ns1:country>\n" + 
      "</ns1:sayHelloElement>";
      String resp = invokeService(serviceWSDLURL, payload, SOAPConstants.SOAP_1_1_PROTOCOL);
      System.out.println(resp);
    }
  }
  
  public static class LocalNSResolver implements NSResolver
  {
    HashMap<String, String> map = new HashMap<String, String>();
    
    public void setPrefix(String prefix, String uri)
    {
      map.put(prefix, uri);
    }
    
    public String resolveNamespacePrefix(String prefix)
    {
      return map.get(prefix);
    }
  }
}
