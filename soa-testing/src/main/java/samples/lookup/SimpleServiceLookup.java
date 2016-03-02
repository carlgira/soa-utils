package samples.lookup;


import java.io.StringWriter;

import java.util.HashMap;

import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import testhelper.ServiceUnitTestHelper;

public class SimpleServiceLookup 
{
    private final static boolean verbose = false;
    
    private final static String SERVICE_NAME     = "DepartmentAMService";
    private final static String PORT             = SERVICE_NAME + "SoapHttpPort";
    
    private final static String SERVICE_ENDPOINT = "http://fpp-ta04:8888/ADFServiceApp-Model-context-root/" + SERVICE_NAME;
    private final static String WSDL_LOC         = SERVICE_ENDPOINT + "?WSDL";
    private final static String SERVICE_NS_URI   = "/oracle/ateam/serviceapp/model/common/";
    
//    private final static String SERVICE_REQUEST = 
//      "<ns1:getDepartments xmlns:ns1=\"/oracle/ateam/serviceapp/model/common/types/\">\n" + 
//      "  <ns1:departmentId>10</ns1:departmentId>\n" + 
//      "</ns1:getDepartments>";
//    
    
    private String wsdlLoc      = "";
    private String serviceName  = "";
    private String srvTns       = "";
    private String portName     = "";
    private String svcEndpoint  = "";
    private String pathIn       = "";
    private String pathOut      = "";
    private String nsIn         = "";
    private String nsOut        = "";
    private String defaultValue = "";
    
    ServiceUnitTestHelper suth = null;

    /**
     *
     * @param wsdlLoc URL of the WSDL Of the service, as a String
     * @param serviceNSURI Service Target Namespace URI
     * @param serviceName Service Name
     * @param portName Port Name
     * @param payloadNSURIin A string containing the namespaces and associated prefixes used in the next variable (pathToDataIn), 
     *                       comma separated, like "ns0:http://domain.machine/path/ns,ns1:urn:some-other-stuff"
     * @param pathToDataIn The path to the data, used to build the outgoing document. Refers to the namespaces mentioned above, 
     *                     like "ns0:root-element/ns1:data-container"
     * @param payloadNSURIout Equivalent of payloadNSURIin for the data to fetch in the returned payload.
     * @param pathToDataOut XPath expression, involving the prefixes mentioned above, to get to the expected result
     *                      in the returned payload. Supports whatever XPath supports.
     * @param defaultReturnedValue returned in case no lookup value comes back                      
     */
    public SimpleServiceLookup(String wsdlLoc,
                               String serviceNSURI,
                               String serviceName,
                               String portName,
                               String payloadNSURIin,
                               String pathToDataIn,
                               String payloadNSURIout,
                               String pathToDataOut, 
                               String defaultReturnedValue) 
    {
      if (verbose) System.out.println("Entering Constructor");
      this.suth = new ServiceUnitTestHelper();  
      
      this.wsdlLoc = wsdlLoc;
      this.srvTns = serviceNSURI;
      this.serviceName = serviceName;
      this.portName = portName;
      this.pathIn = pathToDataIn;
      this.pathOut = pathToDataOut;
      this.nsIn = payloadNSURIin;
      this.nsOut = payloadNSURIout;
      this.defaultValue = defaultReturnedValue;
      this.svcEndpoint = suth.getEndpointURL(wsdlLoc);
      if (verbose) System.out.println("Exiting Constructor");
    }
    
    public static void main(String[] args) 
    {
      SimpleServiceLookup ssl = new SimpleServiceLookup(WSDL_LOC, 
                                                        SERVICE_NS_URI, 
                                                        SERVICE_NAME, 
                                                        PORT, 
                                                        "ns1:/oracle/ateam/serviceapp/model/common/types/", 
                                                        "ns1:getDepartments/ns1:departmentId", 
                                                        "ns0:/oracle/ateam/serviceapp/model/common/types/,ns1:/oracle/ateam/serviceapp/model/common/", 
                                                        "//ns0:getDepartmentsResponse/ns0:result[./ns1:DepartmentId = '20']/ns1:DepartmentName",
                                                        "NotFound");
      
      String result = ssl.lookup("20");
      System.out.println("Result: [" + result + "]");
    }
    
    /**
     * This function has been designed to be called from XSL.
     * All parameters are Strings, as well as the returned value.
     * It's meant to be just a lookup function on an SDO (or ADFBC) service.
     * Pass it an atomic value (not a node) and it will rerturn another value (not a node).
     *
     * @param dataIn The data used to build the outgoing document payload.
     * @return The value returned by the XPath expression pathToDataOut.
     * 
     * Example:
     * A call like 
     * SimpleServiceLookup ssl = new SimpleServiceLookup(WSDL_LOC, 
     *                                                   SERVICE_NS_URI, 
     *                                                   SERVICE_NAME, 
     *                                                   PORT, 
     *                                                   "ns1:/oracle/ateam/serviceapp/model/common/types/", 
     *                                                   "ns1:getDepartments/ns1:departmentId", 
     *                                                   "ns0:/oracle/ateam/serviceapp/model/common/types/,ns1:/oracle/ateam/serviceapp/model/common/", 
     *                                                   "//ns0:getDepartmentsResponse/ns0:result[./ns1:DepartmentId = '20']/ns1:DepartmentName",
     *                                                   "[NotFound]");
     *                                     
     * String result = ssl.lookup("20");
     * 
     * would generate a request like
     * <ns1:getDepartments xmlns:ns1="/oracle/ateam/serviceapp/model/common/types/">
     *   <ns1:departmentId>20</ns1:departmentId>
     * </ns1:getDepartments>
     *
     * which would itself generate a response as 
     * <ns0:getDepartmentsResponse xmlns:ns0="/oracle/ateam/serviceapp/model/common/types/">
     *   <ns0:result xsi:type="ns2:DepartmentsVOSDO" xmlns:ns2="/oracle/ateam/serviceapp/model/common/" xmlns:ns0="/oracle/ateam/serviceapp/model/common/types/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *      <ns2:DepartmentId>20</ns2:DepartmentId>
     *      <ns2:DepartmentName>Marketing</ns2:DepartmentName>
     *      <ns2:ManagerId>201</ns2:ManagerId>
     *      <ns2:LocationId>1800</ns2:LocationId>
     *   </ns0:result>
     * </ns0:getDepartmentsResponse>
     * 
     * The XPath expression in pathToDataOut applied on this payload would isolate the 
     * string "Marketing", which is the expected result.
     */
    public String lookup(String dataIn)
    {
      String retVal = defaultValue;
      if (verbose)
      {
        System.out.println("WSDL: " + wsdlLoc);
        System.out.println("serviceName: " + serviceName);
        System.out.println("PortName   : " + portName);
      }
      try
      {
        HashMap<String, String> nsuriIn = new HashMap<String, String>(1);
        String[] nsArray = nsIn.split(",");
        for (int i=0; i<nsArray.length; i++) 
        {
          String prefix = nsArray[i].substring(0, nsArray[i].indexOf(":"));
          String nsuri  = nsArray[i].substring(nsArray[i].indexOf(":") + 1);
          nsuriIn.put(prefix, nsuri);  
        }
        
          
        String sr = "";  
        String[] elementHierarchy = pathIn.split("/");
        XMLDocument doc = new XMLDocument();
        XMLElement parent = null, child = null;
        String prefix = elementHierarchy[0].substring(0, elementHierarchy[0].indexOf(":"));
        parent = (XMLElement)doc.createElementNS(nsuriIn.get(prefix), elementHierarchy[0]);
        doc.appendChild(parent);
        for (int i=1; i<elementHierarchy.length; i++) 
        {
          prefix = elementHierarchy[i].substring(0, elementHierarchy[i].indexOf(":"));
          child = (XMLElement)doc.createElementNS(nsuriIn.get(prefix), elementHierarchy[i]);
          parent.appendChild(child);
          if (i == (elementHierarchy.length - 1)) // Last one
          {
            Text txt = doc.createTextNode("text#");
            txt.setNodeValue(dataIn);
            child.appendChild(txt);
          }
          parent = child;
        }
        StringWriter sw = new StringWriter();
        doc.print(sw);
        sr = sw.toString();          

        if (verbose) System.out.println(sr);
          
        XMLElement x = suth.invokeSyncService(svcEndpoint, 
                                              wsdlLoc, 
                                              srvTns, 
                                              sr, 
                                              serviceName, 
                                              portName);
        if (verbose)
        {
          System.out.println("Synchronous, response:");      
          x.print(System.out);
          System.out.println();
        }
        
        // Build XPath expression
        HashMap<String, String> nsuriOut = new HashMap<String, String>(1);
        nsArray = nsOut.split(",");
        for (int i=0; i<nsArray.length; i++) 
        {
          prefix = nsArray[i].substring(0, nsArray[i].indexOf(":"));
          String nsuri  = nsArray[i].substring(nsArray[i].indexOf(":") + 1);
//        System.out.println(prefix + "->" + nsuri);
          nsuriOut.put(prefix, nsuri);  
        }

        SpecialNSResolver nsr = new SpecialNSResolver();
        nsr.setPrefixMap(nsuriOut);
        
        if (verbose) System.out.println("Applying : [" + pathOut + "]");
        NodeList nl = x.selectNodes(pathOut, nsr);
        if (nl.getLength() != 1)
        {
          if (verbose) System.out.println("Wierd cardinality... " + Integer.toString(nl.getLength()));
        }
        else
          retVal = nl.item(0).getFirstChild().getNodeValue();
      }
      catch (Exception e) 
      {
        e.printStackTrace();    
      }      
      return retVal;
    }
    
    static class SpecialNSResolver implements NSResolver 
    {
      HashMap<String, String> nsmap = new HashMap<String, String>();
      
      public void setPrefixMap(HashMap<String, String> hm) { nsmap = hm; }

      public String resolveNamespacePrefix(String prefix) 
      {
        String uri = nsmap.get(prefix);
//      System.out.println("[" + prefix + "] returns [" + uri + "]");
        return uri;
      }
    }
}
