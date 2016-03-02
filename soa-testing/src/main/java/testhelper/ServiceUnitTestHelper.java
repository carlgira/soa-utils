package testhelper;

import com.sun.xml.internal.messaging.saaj.soap.impl.ElementImpl;
import com.sun.xml.internal.messaging.saaj.soap.ver1_1.Message1_1Impl;

//import com.sun.xml.ws.client.dispatch.SOAPMessageDispatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.BindException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.crypto.spec.SecretKeySpec;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
//import javax.xml.soap.MimeHeader;
//import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;

import oracle.j2ee.ws.client.jaxws.OracleDispatchImpl;

import oracle.j2ee.ws.common.jaxws.ServiceDelegateImpl;
//import oracle.j2ee.ws.saaj.soap.AttachmentPartImpl;

import oracle.security.xmlsec.enc.XEEncryptedKey;
import oracle.security.xmlsec.enc.XEEncryptionMethod;
import oracle.security.xmlsec.enc.XEException;
import oracle.security.xmlsec.enc.XEncUtils;

import oracle.webservices.ClientConstants;
import oracle.webservices.OracleService;
import oracle.webservices.SOAPUtil;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParseException;
import oracle.xml.parser.v2.XSLException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import testhelper.gui.SOAPTrafficFrame;

import util.httputil.HTTPRequestHandler;
import util.httputil.TinyHTTPServer;

import util.xmlutil.XMLUtilities;


/**
 * @author olivier.lediouris@oracle.com
 * @author andre.correa@oracle.com
 *
 */
public class ServiceUnitTestHelper
{
  public  final static String WSA_NS = "http://www.w3.org/2005/08/addressing";
//public final static String WSA_NS  = "http://schemas.xmlsoap.org/ws/2004/03/addressing";
  public final static String WSA_ANONYMOUS = "http://www.w3.org/2005/08/addressing/anonymous";
  private final static String WSS_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  private final static String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

  private final static String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
  private final static String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
  private final static String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";

  private static final String JKS_KEYSTORE_NAME = "default-keystore.jks";
  private static final String JKS_ORAKEY_ALIAS = "orakey";
  private static final String JKS_PASSWORD = "welcome1";
  private static final String JKS_ORAKEY_ALIAS_PASSWORD = "welcome1";
  private static final String KEYSTORE_TYPE = "JKS";
  private static final String ORACLE_SECURITY_JPS_CONFIG_ENV_PROP = "oracle.security.jps.config";
  private static final String DATA_ENCRYPTION_ALGORITHM = "AES";
  //private static final String SOAP_ENV_URI              = "http://schemas.xmlsoap.org/soap/envelope/";
  private static final String SOAP_ENV_PREFIX = "soap-env";
  private static final String WS_XENC_URI = "http://www.w3.org/2001/04/xmlenc#";
  private static final String WS_XENC_PREFIX = "ws-xenc";
  private static final String WSSE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  private static final String WSSE_PREFIX = "wsse";

  private final static int SYNCHRONOUS                     = 1;
  private final static int ASYNCHRONOUS_ONE_WAY            = 2;
  private final static int ASYNCHRONOUS_TWO_WAYS           = 3;
  private final static int SYNC_THEN_ASYNCHRONOUS_TWO_WAYS = 4; // More like an exercice...

  private static boolean verbose = false;
  private String serviceResponse = "";
  private XMLElement xmlServiceResponse = null;

  private Iterator attachmentIterator = null;

  private static String asyncResponse = "";

  private HTTPRequestHandler hrh = null;
  private boolean usernamePasswordSecurity = false;
  private boolean policySecurity = false;
  private URL policyDocumentLocation = null;

  private String username = null;
  private String password = null;

  private HandlerResolver handlerResolver = null;
  
  private static SOAPTrafficFrame trafficFrame = null;

  public void setVerbose(boolean b)
  {
    verbose = b;
  }

  public static void dumpMessage(String name, XMLElement message)
    throws IOException
  {
    System.out.println(name + " message is:");
    message.print(System.out);
  }

  private static void dumpMessage(String name, SOAPMessage message)
    throws IOException, SOAPException, SAXException
  {
    System.out.println(name + " message is:");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    message.writeTo(baos);
    DOMParser parser = Context.getInstance().getParser();
    parser.parse(new StringReader(baos.toString()));
    XMLDocument doc = parser.getDocument();
    doc.print(System.out);
  }

  /**
   *
   * @param serviceEndPoint
   * @param payload
   * @param protocol
   * @return
   * @throws Exception
   *
   * @deprecated
   */
  @Deprecated
  public String dynamicallyCallService(String serviceEndPoint, String payload, String protocol)
    throws Exception
  {
    String response = "";
    try
    {
      response = dynInvoke(serviceEndPoint, payload, protocol);
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return response;
  }

  /**
   * Uses the WSDL of a service to validate a payload.
   *
   * @param WSDLUrl
   * @param payload XML Payload, as a String containing the XML document
   * @return true: OK, false: KO
   * @throws Exception
   */
  public boolean validateServicePayload(String WSDLUrl, String payload)
    throws Exception
  {
    return validateServicePayload(WSDLUrl, new StringReader(payload));
  }

  /**
   *
   * @param WSDLUrl
   * @param payload a Reader (StringReader, InputStreamReader, FileReader) from the payload.
   * @return true: OK, false: KO
   * @throws Exception
   */
  public boolean validateServicePayload(String WSDLUrl, Reader payload)
    throws Exception
  {
    boolean ok = false;
    URL wsdlURL = new URL(WSDLUrl);
    DOMParser parser = Context.getInstance().getParser(); // new DOMParser();
    parser.parse(wsdlURL);
    XMLDocument wsdl = parser.getDocument();
    CustomNamespaceResolver nsr = new CustomNamespaceResolver();
    nsr.setPrefix("xsd", SCHEMA_NS);
    nsr.setPrefix("wsdl", WSDL_NS);
    NodeList nl = wsdl.selectNodes("//wsdl:types/xsd:schema", nsr);
    XMLElement schemaNode = null;
    XMLDocument doc = null;
    if (nl.getLength() > 0)
    {
      for (int i = 0; !ok && i < nl.getLength(); i++)
      {
        schemaNode = (XMLElement) nl.item(i);
        doc = new XMLDocument();
        doc.appendChild(doc.adoptNode(schemaNode));
        if (verbose)
        {
          System.out.println("Validating against:");
          doc.print(System.out);
        }
        payload.reset();
        ok = XMLUtilities.validate(doc, payload, wsdlURL);
      }
    }
    else
    {
      nl = wsdl.selectNodes("//wsdl:import", nsr);
      if (nl.getLength() == 0)
      {
        throw new RuntimeException("No type, No imported schema were found.");
      }
      if (nl.getLength() > 1)
      {
        // Restrict to the namespace of the payload
      }
      String schemaLoc = ((XMLElement) nl.item(0)).getAttribute("location");
      parser.parse(new URL(schemaLoc));
      doc = parser.getDocument();
      XMLElement _root = (XMLElement) doc.getDocumentElement();
      String rootNS = _root.getNamespaceURI();
      if (rootNS.equals(WSDL_NS))
      {
        return validateServicePayload(schemaLoc, payload);
      }
      ok = XMLUtilities.validate(doc, payload, wsdlURL);
    }

    return ok;
  }

  /**
   * @param wsdl WSDL URL as a String, like "http://machine:port/.../servicePort?WSDL"
   * @return the endpoint url, found in the WSDL document.
   */
  public final String getEndpointURL(String wsdl)
  {
    String ep = "";
    try
    {
      URL wsdlurl = new URL(wsdl);
      DOMParser parser = Context.getInstance().getParser();
      parser.parse(wsdlurl);
      CustomNamespaceResolver nsr = new CustomNamespaceResolver();
      nsr.setPrefix("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
      nsr.setPrefix("soap12", "http://schemas.xmlsoap.org/wsdl/soap12/");
      nsr.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
      nsr.setPrefix("wsdl", "http://schemas.xmlsoap.org/wsdl/");
      XMLDocument doc = parser.getDocument();
      if (doc == null)
      {
        throw new RuntimeException("getEndPointURL: parsing returned null document for [" + wsdl + "]");
      }
      //    doc.print(System.out);
      NodeList nl = doc.selectNodes("//soap:address", nsr);
      //    System.out.println("Returned " + nl.getLength() + " node(s)");
      if (nl.getLength() == 0)
      {
        throw new RuntimeException("getEndPointURL: //soap:address not found in [" + wsdl + "]");
      }
      ep = ((XMLElement) nl.item(0)).getAttribute("location");
    }
    catch (Exception ex)
    {
      throw new RuntimeException("getEndPointURL:" + ex.toString());
    }
    return ep;
  }

  /**
   * @param wsdlurl
   * @return true if service has been reached successfully
   * @throws RuntimeException
   */
  public boolean isServiceUp(String wsdlurl) // throws RuntimeException
  {
    boolean soWhat = false;
    try
    {
      URL wsdl = new URL(wsdlurl);
      URLConnection connection = wsdl.openConnection();
      connection.connect();
      soWhat = true;
      // Go to endpoint
      DOMParser parser = Context.getInstance().getParser();
      parser.parse(wsdlurl);
      CustomNamespaceResolver nsr = new CustomNamespaceResolver();
      nsr.setPrefix("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
      nsr.setPrefix("soap12", "http://schemas.xmlsoap.org/wsdl/soap12/");
      nsr.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
      nsr.setPrefix("wsdl", "http://schemas.xmlsoap.org/wsdl/");
      XMLDocument doc = parser.getDocument();
      NodeList nl = doc.selectNodes("//soap:address", nsr);
      //    System.out.println("Returned " + nl.getLength() + " node(s)");
      String ep = ((XMLElement) nl.item(0)).getAttribute("location");
      // Make sure the endpoint is up.
      URL epurl = new URL(ep);
      connection = epurl.openConnection();
      connection.connect();
      soWhat = true;
    }
    catch (Exception ex)
    {
      soWhat = false;
      //    throw new RuntimeException(ex.toString());
    }
    return soWhat;
  }

  /**
   * @deprecated Uses JAX-RPC
   *
   * @param wsdl
   * @param payload
   * @param protocol
   * @return
   * @throws Exception
   */
  @Deprecated
  public String invokeServiceFromWSDL(String wsdl, String payload, String protocol)
    throws Exception
  {
    String response = dynamicallyCallService(getEndpointURL(wsdl), payload, protocol);
    return response;
  }

  /**
   * @deprecated Uses JAX-RPC
   *
   * @param WSDLUrl
   * @param payload
   * @param protocol
   * @param timeout
   * @return
   * @throws TookTooLongException
   */
  @Deprecated
  public String invokeServiceFromWSDLwithTimeout(String WSDLUrl, String payload, String protocol, long timeout)
    throws TookTooLongException
  {
    String response = "";
    final Thread waiter = Thread.currentThread();

    ServiceThread serviceThread = new ServiceThread(WSDLUrl, payload, waiter, protocol, this);
    serviceThread.start();

    synchronized (waiter)
    {
      try
      {
        long before = System.currentTimeMillis();
        waiter.wait(timeout);
        long after = System.currentTimeMillis();
        if (verbose)
          System.out.println("- Done waiting (" + Long.toString(after - before) + " vs " + Long.toString(timeout) + ")");
        if (serviceThread.isAlive())
        {
          serviceThread.interrupt();
          //        System.out.println("Interrupt Notification");
          throw new TookTooLongException("Timeout exceeded (" + Long.toString(timeout) + " ms).");
        }
        else
        {
          // Get response
          response = serviceResponse;
        }
      }
      catch (InterruptedException ie)
      {
        System.out.println("Waiter Interrupted! (before end of wait)");
      }
    }
    return response;
  }

  private synchronized void setServiceResponse(String str)
  {
    this.serviceResponse = str;
  }

  /**
   * Uses JAX-WS
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @return
   * @throws Exception
   */
  public XMLElement invokeSyncService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, String payload,
                                      String serviceName, String portName)
    throws Exception
  {
    return invokeSyncService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                             portName);
  }

  public XMLElement invokeSyncService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                      String serviceName, String portName)
    throws Exception
  {
    return invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, null, SYNCHRONOUS,
                         null, null, null);
  }

  public XMLElement invokeSyncService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, String payload,
                                      String serviceName, String portName, String bindingProtocol)
    throws Exception
  {
    return invokeSyncService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                             portName, bindingProtocol);
  }

  public XMLElement invokeSyncService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                      String serviceName, String portName, String bindingProtocol)
    throws Exception
  {
    return invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, bindingProtocol,
                         SYNCHRONOUS, null, null, null);
  }

  public XMLElement invokeSyncService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                      String serviceName, String portName, String bindingProtocol, String operation)
    throws Exception
  {
    return invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, bindingProtocol,
                         SYNCHRONOUS, operation, null, null);
  }

  /**
   * Uses JAX-WS
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @param timeout
   * @return
   * @throws Exception
   */
  public XMLElement invokeSyncServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                 String payload, String serviceName, String portName, long timeout)
    throws Exception
  {
    return invokeSyncServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                        null, timeout);
  }

  public XMLElement invokeSyncServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                 Reader payload, String serviceName, String portName, long timeout)
    throws Exception
  {
    return invokeSyncServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                        null, timeout);
  }

  public XMLElement invokeSyncServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                 String payload, String serviceName, String portName, String bindingProtocol,
                                                 long timeout)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                    bindingProtocol, SYNCHRONOUS, null, null, timeout, null);
  }

  public XMLElement invokeSyncServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                 Reader payload, String serviceName, String portName, String bindingProtocol,
                                                 long timeout)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                    bindingProtocol, SYNCHRONOUS, null, null, timeout, null);
  }

  /**
   * Uses JAX-WS
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @throws Exception
   */
  public void invokeASync1WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, String payload,
                                     String serviceName, String portName)
    throws Exception
  {
    invokeASync1WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                           portName, null);
  }

  public void invokeASync1WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                     String serviceName, String portName, String protocolBinding)
    throws Exception
  {
    invokeASync1WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, protocolBinding,
                           null);
  }

  public void invokeASync1WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                     String serviceName, String portName, String protocolBinding, String operation)
    throws Exception
  {
    invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, protocolBinding,
                  ASYNCHRONOUS_ONE_WAY, operation, null, null);
  }

  /**
   * Uses JAX-WS
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @param op
   * @param callbackHttpPort
   * @return
   * @throws Exception
   */
  public XMLElement invokeASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                           Reader payload, String serviceName, String portName, String op,
                                           String callbackHttpPort)
    throws Exception
  {
    return invokeASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, null, op,
                                  callbackHttpPort);
  }

  public XMLElement invokeASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                           String payload, String serviceName, String portName, String op,
                                           String callbackHttpPort)
    throws Exception
  {
    return invokeASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                                  portName, op, callbackHttpPort);
  }

  public XMLElement invokeASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                           String payload, String serviceName, String portName, String protocolBinding,
                                           String op, String callbackHttpPort)
    throws Exception
  {
    return invokeASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                                  portName, protocolBinding, op, callbackHttpPort);
  }

  public XMLElement invokeASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                           Reader payload, String serviceName, String portName, String protocolBinding,
                                           String op, String callbackHttpPort)
    throws Exception
  {
    return invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, protocolBinding,
                         ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, null);
  }

  /**
   * Uses JAX-WS
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @param protocolBinding
   * @param op
   * @param callbackHttpPort
   * @param timeout
   * @return
   * @throws Exception
   */
  public XMLElement invokeASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                      String payload, String serviceName, String portName,
                                                      String protocolBinding, String op, String callbackHttpPort, long timeout)
    throws Exception
  {
    return invokeASync2WayServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload),
                                             serviceName, portName, protocolBinding, op, callbackHttpPort, timeout);
  }

  public XMLElement invokeASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                      Reader payload, String serviceName, String portName,
                                                      String protocolBinding, String op, String callbackHttpPort, long timeout)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                    protocolBinding, ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, timeout, null);
  }

  public XMLElement invokeASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                                      String payload, String serviceName, String portName, String op,
                                                      String callbackHttpPort, long timeout)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, null,
                                    ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, timeout, null);
  }

  public XMLElement invokeSyncASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                               Reader payload, String serviceName, String portName, String op,
                                               String callbackHttpPort, SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeSyncASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, null,
                                      op, callbackHttpPort, srlsnr);
  }

  public XMLElement invokeSyncASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                               String payload, String serviceName, String portName, String op,
                                               String callbackHttpPort, SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeSyncASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload),
                                      serviceName, portName, op, callbackHttpPort, srlsnr);
  }

  public XMLElement invokeSyncASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                               String payload, String serviceName, String portName, String protocolBinding,
                                               String op, String callbackHttpPort, SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeSyncASync2WayService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload),
                                      serviceName, portName, protocolBinding, op, callbackHttpPort, srlsnr);
  }

  public XMLElement invokeSyncASync2WayService(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                               Reader payload, String serviceName, String portName, String protocolBinding,
                                               String op, String callbackHttpPort, SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, protocolBinding,
                         SYNC_THEN_ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, srlsnr);
  }

  public XMLElement invokSyncASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation,
                                                         String serviceNamespaceURI, String payload, String serviceName,
                                                         String portName, String protocolBinding, String op,
                                                         String callbackHttpPort, long timeout,
                                                         SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeSyncASync2WayServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload),
                                                 serviceName, portName, protocolBinding, op, callbackHttpPort, timeout,
                                                 srlsnr);
  }

  public XMLElement invokeSyncASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation,
                                                          String serviceNamespaceURI, Reader payload, String serviceName,
                                                          String portName, String protocolBinding, String op,
                                                          String callbackHttpPort, long timeout,
                                                          SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                    protocolBinding, SYNC_THEN_ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, timeout, srlsnr);
  }

  public XMLElement invokeSyncASync2WayServiceWithTimeout(String serviceEndpoint, String wsdlLocation,
                                                          String serviceNamespaceURI, String payload, String serviceName,
                                                          String portName, String op, String callbackHttpPort, long timeout,
                                                          SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, null,
                                    SYNC_THEN_ASYNCHRONOUS_TWO_WAYS, op, callbackHttpPort, timeout, srlsnr);
  }

  private HttpServerThread httpServer = null;

  /**
   * Generic JAX-WS Service Invocation
   * All the calls above are re-directed here.
   *
   * In the case of an ASYNC-2-WAY service call, WS-Assdressing is used
   * to set the ReplyTo address to the current machine (the one this class is run from)
   * on a port chosen by the user (6666 is the default).
   *
   * Then a Thread running a tiny http server is started, to which the
   * reply-to will be made.
   * When the HTTP Server receives the reply-to, it wakes up the main thread.
   * This way, no polling is necessary.
   *
   * @param serviceEndpoint
   * @param wsdlLocation
   * @param serviceNamespaceURI
   * @param payload
   * @param serviceName
   * @param portName
   * @param soapBinding
   * @param mode
   * @param operation
   * @param httpServerPort
   * @return
   * @throws Exception
   */
  private XMLElement invokeService(String serviceEndpoint, 
                                   String wsdlLocation, 
                                   String serviceNamespaceURI, 
                                   Reader payload,
                                   String serviceName, 
                                   String portName, 
                                   String soapBinding, 
                                   int mode, 
                                   String operation,
                                   final String httpServerPort, 
                                   SynchronousResponseListener srlsnr)
    throws BindException, Exception
  {
    boolean actionInserted = false;
    String replyToURL = "";
    XMLElement serviceResponse = null;
    XMLElement policyElement = null;

    URL wsdlUrl = new URL(wsdlLocation);
    //  Service service = Service.create(new QName(serviceNamespaceURI, serviceName));
    // For Bug# 9314765
    //  Service service = Service.create(wsdlUrl,
    //                                   new QName(serviceNamespaceURI, serviceName));
    ServiceDelegateImpl service =
      new ServiceDelegateImpl(wsdlUrl, new QName(serviceNamespaceURI, serviceName), OracleService.class);

    QName port = new QName(serviceNamespaceURI, portName);

    Iterator<QName> ports = service.getPorts();
    boolean found = false;
    while (ports.hasNext())
    {
      QName p = ports.next();
      if (p.getNamespaceURI().equals(serviceNamespaceURI) && p.getLocalPart().equals(portName))
      {
        if (verbose)
          System.out.println("Found Port [" + p.getNamespaceURI() + ":" + p.getLocalPart() + "]");
        found = true;
        break;
      }
    }

    if (!found)
    {
      try
      {
        service.addPort(port, SOAPBinding.SOAP11HTTP_BINDING, serviceEndpoint);
      }
      catch (Exception ex)
      {
        System.out.println("=====================");
        System.out.println(ex.toString());
        System.out.println("=====================");
      }
    }
    // Now building the SOAP Message
    MessageFactory messageFactory = null;
    if (soapBinding == null)
      messageFactory = MessageFactory.newInstance(); // Protocol?
    else
      messageFactory = MessageFactory.newInstance(soapBinding);
    //  MimeHeaders mimeHeaders = new MimeHeaders();
    //  mimeHeaders.addHeader("Content-type", "text/xml");

    //  SOAPMessage soapMessage = messageFactory.createMessage(mimeHeaders, new ByteArrayInputStream(payload.getBytes()));
    SOAPMessage soapMessage = messageFactory.createMessage();

    //  soapMessage.getSOAPHeader().detachNode(); // not using SOAP headers
    SOAPBody body = soapMessage.getSOAPBody();
    SOAPElement input = getSOAPElementFromSource(payload);
    body.addChildElement(input);

    // SOAPAction
    if (operation != null)
    {
      if (verbose)
        System.out.println("operation:" + operation);
      soapMessage.getMimeHeaders().addHeader("SOAPAction", operation);
    }
    final Thread parent = Thread.currentThread();

    // WS-Security Option, Username Password, in the signature of the method.
    /*
    <wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
       <wsse:UsernameToken wsu:Id="UsernameToken-31312581" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
          <wsse:Username>pardha</wsse:Username>
          <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">welcome1</wsse:Password>
       </wsse:UsernameToken>
    </wsse:Security>
    */
    if (usernamePasswordSecurity)
    {
      SOAPHeader soapHeader = soapMessage.getSOAPHeader();
      // 1 - Security
      QName wss = new QName(WSS_NS, "Security", "wsse");
      SOAPElement wssElement = soapHeader.addChildElement(wss);

      wssElement.addAttribute(new QName(SOAP_NS, "mustUnderstand", "soapenv"), "1");
      // 2 - UsernameToken
      QName unt = new QName(WSS_NS, "UsernameToken", "wsse");
      SOAPElement untElement = wssElement.addChildElement(unt);
      untElement.addAttribute(new QName(WSU_NS, "Id", "wsu"), "UsernameToken-31312581");
      // 3 - Username
      QName un = new QName(WSS_NS, "Username", "wsse");
      SOAPElement unElement = untElement.addChildElement(un);
      unElement.addTextNode(username);
      // 4 - Password
      QName pw = new QName(WSS_NS, "Password", "wsse");
      SOAPElement pwElement = untElement.addChildElement(pw);
      pwElement.setAttribute("Type",
                             "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
      pwElement.addTextNode(password);
    }

    if (mode == SYNCHRONOUS || mode == ASYNCHRONOUS_ONE_WAY)
    {
      // 1 - Action
      // Doing this because sync ADFbc web services (seen in CRM - Peter Moore/Dhevendra) are misteriously complaining about the absence of Action SOAP header
      SOAPHeader soapHeader = soapMessage.getSOAPHeader();
      QName action = new QName(WSA_NS, "Action", "wsa");
      SOAPElement soapAction = soapHeader.addChildElement(action);
      // SOAPElement soapAction = soapHeader.addChildElement(ADDR.getActionQName());
      soapAction.addTextNode(operation);
      actionInserted = true;
      // ADDED BY SMIKOLAI FOR ESS - 2010-05-19
      QName messageID = new QName(WSA_NS, "MessageID", "wsa");
      SOAPElement soapMessageID = soapHeader.addChildElement(messageID);
      // Generate something, kind of unique...
      // soapMessageID.addTextNode("urn:service-unit-test-helper:" + Long.toString(System.currentTimeMillis()));
      soapMessageID.addTextNode("uuid:" + UUID.randomUUID().toString());
      // ADDED BY SMIKOLAI FOR ESS
    }

    if (verbose)
      System.out.println("1. policySecurity:" + policySecurity);
    if (policySecurity)
    {
      DOMParser parser = Context.getInstance().getParser();
      synchronized (parser)
      {
        if (policyDocumentLocation == null)
          policyDocumentLocation = this.getClass().getResource("Policy.xml"); // Default location
        if (verbose)
          System.out.println("Policy.xml URL:" + policyDocumentLocation.toExternalForm());
        parser.parse(policyDocumentLocation);
        policyElement = (XMLElement) parser.getDocument().getDocumentElement();
      }
    }

    if (mode == ASYNCHRONOUS_TWO_WAYS || mode == SYNC_THEN_ASYNCHRONOUS_TWO_WAYS) // replyTo, WS-Addressing headers
    {
      String httpPort = (httpServerPort == null? "6666": httpServerPort);
      String ipAddress = "";
      try
      {
        InetAddress addr = InetAddress.getLocalHost();
        // Get IP Address, safer than the machine name.
        byte[] ipAddr = addr.getAddress();
        for (int i = 0; i < ipAddr.length; i++)
        {
          int dot = (int) ipAddr[i];
          if (dot < 0)
            dot += 256;
          ipAddress += ((ipAddress.length() > 0? ".": "") + Integer.toString(dot));
        }
        // Get hostname
        //      String hostname = addr.getHostName();
      }
      catch (UnknownHostException e)
      {
        e.printStackTrace();
      }
      replyToURL = "http://" + ipAddress + ":" + httpPort + "/";

      /*
       * Build headers for WS-A
       */
      SOAPHeader soapHeader = soapMessage.getSOAPHeader();
      // 1 - Action
      if (!actionInserted)
      {
        QName action = new QName(WSA_NS, "Action", "wsa");
        SOAPElement soapAction = soapHeader.addChildElement(action);
        //   SOAPElement soapAction = soapHeader.addChildElement(ADDR.getActionQName());
        soapAction.addTextNode(operation);
        actionInserted = true;
      }
      // 2 - To
      QName to = new QName(WSA_NS, "To", "wsa");
      SOAPElement soapTo = soapHeader.addChildElement(to);
      soapTo.addTextNode(serviceEndpoint); // was WSA_ANONYMOUS
      // 3 - MessageID
      QName messageID = new QName(WSA_NS, "MessageID", "wsa");
      SOAPElement soapMessageID = soapHeader.addChildElement(messageID);
      // Generate something, kind of unique...
      //    soapMessageID.addTextNode("urn:service-unit-test-helper:" + Long.toString(System.currentTimeMillis()));
      soapMessageID.addTextNode("uuid:" + UUID.randomUUID().toString());
      // 4 - ReplyTo
      QName replyTo = new QName(WSA_NS, "ReplyTo", "wsa");
      SOAPElement soapReplyTo = soapHeader.addChildElement(replyTo);
      // 4.1 - Address
      QName address = new QName(WSA_NS, "Address", "wsa");
      SOAPElement soapAddress = soapReplyTo.addChildElement(address);
      soapAddress.addTextNode(replyToURL);
      //    soapAddress.addTextNode("http://fpp-ta04.us.oracle.com:8888/r2/ReplyToServiceSoapHttpPort"); // was WSA_ANONYMOUS
      // 4.2 - ReferenceParameters
      //    QName refParam = new QName(WSA_NS, "ReferenceParameters", "wsa");
      //    SOAPElement soapRefParam = soapReplyTo.addChildElement(refParam);
      // 4.2.1 - CustomerKey
      //    QName customerKey = new QName(serviceNamespaceURI, "CustomerKey", "customer");
      //    SOAPElement soapCustomerKey = soapRefParam.addChildElement(customerKey);
      //    soapCustomerKey.addTextNode("Key#123456789");
      // 5 - FaultTo
      QName faultTo = new QName(WSA_NS, "FaultTo", "wsa");
      SOAPElement soapFaultTo = soapHeader.addChildElement(faultTo);
      // 4.1 - Address
      QName address2 = new QName(WSA_NS, "Address", "wsa");
      SOAPElement soapAddress2 = soapFaultTo.addChildElement(address2);

      try
      {
        httpServer = new HttpServerThread(parent, httpServerPort);
        httpServer.start(); // HTTP Server now ready for duty
      }
      catch (Exception e)
      {
        throw e;
      }

      if (verbose)
        System.out.println("Reply To [" + replyToURL + "]");

      soapAddress2.addTextNode(replyToURL);
      /*
      // 4.2 - ReferenceParameters
      QName refParam2 = new QName(WSA_NS, "ReferenceParameters", "wsa");
      SOAPElement soapRefParam2 = soapFaultTo.addChildElement(refParam2);
      // 4.2.1 - CustomerKey
      QName customerKey2= new QName(serviceNamespaceURI, "CustomerKey", "customer");
      SOAPElement soapCustomerKey2 = soapRefParam2.addChildElement(customerKey);
      soapCustomerKey2.addTextNode("Fault#123456789");
      */
    }
    // GUI ? Request
    if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
    {
      if (!trafficFrame.isVisible())
        trafficFrame.setVisible(true);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      soapMessage.writeTo(baos);
      trafficFrame.setMessageOut(baos.toString());
    }
    
    if (verbose)
    {
      soapMessage.writeTo(System.out);
      System.out.println();
    }
    // Handlers?
    if (handlerResolver != null)
      service.setHandlerResolver(handlerResolver);

    // Note: The addressing feature is disabled!
    Dispatch<SOAPMessage> dispatcher =
      service.createDispatch(port, SOAPMessage.class, Service.Mode.MESSAGE, new AddressingFeature(false));
    // service.createDispatch(port, SOAPMessage.class, Service.Mode.MESSAGE);
    if (verbose)
      System.out.println("2. policySecurity:" + policySecurity);
    if (policySecurity)
    {
      try
      {
        if (dispatcher instanceof OracleDispatchImpl)
        {
          OracleDispatchImpl odi = (OracleDispatchImpl) dispatcher;
          odi.getRequestContext().put(ClientConstants.CLIENT_CONFIG, policyElement);
          odi.getRequestContext().put(OracleDispatchImpl.USERNAME_PROPERTY, this.getUsername());
          odi.getRequestContext().put(OracleDispatchImpl.PASSWORD_PROPERTY, this.getPassword());
          //odi.getRequestContext().put(SecurityConstants.ClientConstants.WSS_CSF_KEY,"system1-key");
        }
        //        else if (dispatcher instanceof SOAPMessageDispatch)
        //        {
        //          SOAPMessageDispatch smd = (SOAPMessageDispatch)dispatcher;
        //          smd.getRequestContext().put(ClientConstants.CLIENT_CONFIG, policyElement);
        //          smd.getRequestContext().put(OracleDispatchImpl.USERNAME_PROPERTY, this.getUsername());
        //          smd.getRequestContext().put(OracleDispatchImpl.PASSWORD_PROPERTY, this.getPassword());
        //        }
        else
        {
          String errorMessage =
            "Unsupported Dispatcher type: " + dispatcher.getClass().getName() + ".\nSupported type is " + OracleDispatchImpl.class.getName();
          System.out.println(errorMessage);
          errorMessage += ("\nYour Classpath is:\n" +
              System.getProperty("java.class.path").replaceAll(File.pathSeparator, "\n"));
          //        System.out.println("=============== java.class.path ====================");
          //        System.out.println(System.getProperty("java.class.path").replaceAll(File.pathSeparator, "\n"));
          //        System.out.println("====================================================");
          throw new RuntimeException(errorMessage);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    // The code below duplicates the headers if the addressing feature is enabled.
    //  WSBindingProvider wsbp = (WSBindingProvider)dispatcher;
    //
    //  WSEndpointReference replyTo = new WSEndpointReference(replyToURL, AddressingVersion.W3C);
    //  String uuid = "uuid:" + UUID.randomUUID().toString();
    //  EndpointReference er = wsbp.getEndpointReference();
    //
    //  wsbp.setOutboundHeaders(new StringHeader(AddressingVersion.W3C.messageIDTag, uuid),
    //                          replyTo.createHeader(AddressingVersion.W3C.replyToTag));

    if (verbose)
    {
      Map<String, Object> reqctx = dispatcher.getRequestContext();
      for (String k: reqctx.keySet())
      {
        Object o = reqctx.get(k);
        System.out.println(".. Key [" + k + "] Obj [" + (o != null? o.toString(): "null") + "]");
      }
    }
    SOAPMessage response = null;
    long before = 0L, after = 0L;
    switch (mode)
    {
      case SYNCHRONOUS:
        response = dispatcher.invoke(soapMessage);
        if (response != null)
          attachmentIterator = response.getAttachments();
        break;
      case ASYNCHRONOUS_ONE_WAY:
        if (verbose)
          System.out.println(".. invoking invokeOneWay method");
        try
        {
          dispatcher.invokeOneWay(soapMessage);
        }
        catch (Exception ex)
        {
          System.out.println(ex.getLocalizedMessage());
          if (verbose)
            ex.printStackTrace();
        }
        if (verbose)
          System.out.println(".. done with invokeOneWay method");
        break;
      case ASYNCHRONOUS_TWO_WAYS:
        synchronized (parent)
        {
          dispatcher.invokeOneWay(soapMessage);
          before = System.currentTimeMillis();
          parent.wait();
        }
        after = System.currentTimeMillis();
        if (verbose)
          System.out.println("Ready in " + Long.toString(after - before) + " ms (request)");
        break;
      case SYNC_THEN_ASYNCHRONOUS_TWO_WAYS:
        // Handle sync call asynchronously.
        //      AsyncResponseHandler arh = new AsyncResponseHandler();
        Response<SOAPMessage> resp = dispatcher.invokeAsync(soapMessage);
        //      Future<?> asyncServiceResponse = dispatcher.invokeAsync(soapMessage, arh);
        Object o = resp.get();
        if (verbose)
          System.out.println(" -- (in AsyncResponseHandler, Object is " + (o == null? "null": "a " + o.getClass().getName()) +
                             ")");

        if (o instanceof Message1_1Impl)
        {
          Message1_1Impl ri = (Message1_1Impl) o;
          SOAPElement se = SOAPUtil.toSOAPElement(ri.getSOAPPart().getEnvelope());
          //        System.out.println("Full SOAPMessage:" + se.toString());
          DOMParser parser = Context.getInstance().getParser();
          parser.parse(new StringReader(se.toString()));
          XMLDocument doc = parser.getDocument();
          if (verbose)
          {
            System.out.println("Got the response:");
            doc.print(System.out);
          }
          //        replyMessage = (SOAPMessage)o;
          if (srlsnr != null)
            srlsnr.onSynchronousResponse((XMLElement) doc.getDocumentElement());
        }
        else
        {
          SOAPMessage reply = resp.get();
//        replyMessage = reply; // TODO Tell the Listener (done above though)
        }

        //    response = dispatcher.invoke(soapMessage);
        before = System.currentTimeMillis();
        synchronized (parent)
        {
          parent.wait();
        }
        after = System.currentTimeMillis();
        if (verbose)
          System.out.println("Ready in " + Long.toString(after - before) + " ms (request)");
        break;
      default:
        break;
    }
    // GUI ? Response
    if (response != null && "yes".equals(System.getProperty("display.traffic.gui", "no")))
    {
      if (!trafficFrame.isVisible())
        trafficFrame.setVisible(true);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      response.writeTo(baos);
      trafficFrame.setMessageIn(baos.toString());
    }
    if ((mode == SYNCHRONOUS || mode == SYNC_THEN_ASYNCHRONOUS_TWO_WAYS) && response != null)
    {
      if (verbose)
      {
        System.out.println("====== Full Response =====");
        response.writeTo(System.out);
        System.out.println("\n===== End Of Response ====");
      }
      SOAPBody responseBody = response.getSOAPBody();

      // Alternate, tentative for xop attachments
      if (false)
      {
        SOAPPart soapPart = response.getSOAPPart();
        Element elmt = soapPart.getDocumentElement();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (elmt instanceof XMLElement)
        {
          if (verbose)
            ((XMLElement) elmt).print(System.out);
          ((XMLElement) elmt).print(baos);
        }
        // response.writeTo(baos);
        System.out.println("SOAPEnv:\n" +
            baos.toString());
        DOMParser parser = Context.getInstance().getParser();
        XMLDocument doc = null;
        synchronized (parser)
        {
          parser.parse(new StringReader(baos.toString()));
          doc = parser.getDocument();
        }
        NodeList nl = doc.selectNodes("//env:Body", new NSResolver()
          {
            public String resolveNamespacePrefix(String prefix)
            {
              return "http://schemas.xmlsoap.org/soap/envelope/";
            }
          });

        XMLElement _body = (XMLElement) nl.item(0);
        _body.print(System.out);
      }

      if (verbose && responseBody instanceof XMLElement)
      {
        System.out.println("=== Response Body ===");
        ((XMLElement) responseBody).print(System.out);
        System.out.println("===[Response Body]===");
      }
      
      NodeList childNodes = responseBody.getChildNodes();
      Object resultObj = null;
      /*
       * This is done to skip the case where there is a TextNode bofre the body.
       * This can happen where the payload is reworked, like by the Proxy. (see that class)
       */
      for (int n=0; n<childNodes.getLength(); n++) 
      {
        Node node = childNodes.item(n);
        if (node instanceof Element)
        {
          resultObj = node;
          break;
        }
      }
      if (resultObj instanceof Element)
      {
        Element result = (Element)resultObj;
        if (result instanceof XMLElement)
        {
          serviceResponse = (XMLElement) result;
          if (srlsnr != null)
            srlsnr.onSynchronousResponse(serviceResponse);
          if (verbose)
          {
            System.out.println("==  Body  ==");
            serviceResponse.print(System.out);
            System.out.println("============");
          }
        }
        else if (result instanceof ElementImpl)
        {
          SOAPElement se = SOAPUtil.toSOAPElement(result);
          DOMParser parser = Context.getInstance().getParser();
          XMLDocument doc = null;
          synchronized (parser)
          {
            parser.parse(new StringReader(se.toString()));
            doc = parser.getDocument();
          }
          serviceResponse = (XMLElement) doc.getDocumentElement();
          if (srlsnr != null)
            srlsnr.onSynchronousResponse(serviceResponse);
        }
        else if (result instanceof Node)
        {
          Node ei = (Node) result;
        //      SOAPElement se = SOAPUtil.toSOAPElement(result);

          XMLDocument doc = new XMLDocument();
          Node n = ei.cloneNode(true);
          System.out.println("The node is a " + n.getClass().getName());
          System.out.println("Node Value:" + n.getNodeValue());
          doc.appendChild(n);
          doc.print(System.out);
          serviceResponse = (XMLElement) doc.getDocumentElement();
          if (srlsnr != null)
            srlsnr.onSynchronousResponse(serviceResponse);
        }
        else
          System.out.println("Result is a " + result.getClass().getName());
      }
      else if (resultObj instanceof Node)
      {
        Node ei = (Node) resultObj;
//      SOAPElement se = SOAPUtil.toSOAPElement(result);

        XMLDocument doc = new XMLDocument();
        Node n = ei.cloneNode(true);
        System.out.println("The node is a " + n.getClass().getName());
        System.out.println("Node Value:" + n.getNodeValue());
        doc.appendChild(n);
        doc.print(System.out);
        serviceResponse = (XMLElement) doc.getDocumentElement();
        if (srlsnr != null)
          srlsnr.onSynchronousResponse(serviceResponse);
      }
      else
        System.out.println("Result is a " + resultObj.getClass().getName());
    }
    if (verbose)
      System.out.println(".. Exiting invokeService method");
    return serviceResponse;
  }

  private XMLElement invokeServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                              String payload, String serviceName, String portName, String protocolBinding,
                                              int mode, String operation, final String httpServerPort, long timeout,
                                              SynchronousResponseListener srlsnr)
    throws Exception
  {
    return invokeServiceWithTimeout(serviceEndpoint, wsdlLocation, serviceNamespaceURI, new StringReader(payload), serviceName,
                                    portName, protocolBinding, mode, operation, httpServerPort, timeout, srlsnr);
  }

  private XMLElement invokeServiceWithTimeout(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI,
                                              Reader payload, String serviceName, String portName, String protocolBinding,
                                              int mode, String operation, final String httpServerPort, long timeout,
                                              SynchronousResponseListener srlsnr)
    throws Exception
  {
    XMLElement response = null;
    final Thread waiter = Thread.currentThread();

    ServiceInvocationThread serviceThread =
      new ServiceInvocationThread(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName,
                                  protocolBinding, mode, operation, httpServerPort, waiter, this, srlsnr);
    serviceThread.start();

    synchronized (waiter)
    {
      try
      {
        long before = System.currentTimeMillis();
        waiter.wait(timeout);
        long after = System.currentTimeMillis();
        if (verbose)
          System.out.println("- Done waiting (" + Long.toString(after - before) + " vs " + Long.toString(timeout) + ")");
        if (httpServer != null && httpServer.isAlive())
        {
          httpServer.interrupt();
        }
        if (serviceThread.isAlive())
        {
          serviceThread.interrupt();
          //        System.out.println("Interrupt Notification");
          if (httpServer == null || (httpServer != null && !httpServer.responseCameInTime()))
          {
            throw new TookTooLongException("Timeout exceeded (" + Long.toString(timeout) + " ms).");
          }
          else
            response = xmlServiceResponse;
        }
        else
        {
          // Get response
          response = xmlServiceResponse;
        }
      }
      catch (InterruptedException ie)
      {
        System.out.println("Waiter Interrupted! (before end of wait)");
      }
    }
    return response;
  }

  private static SOAPElement getSOAPElementFromSource(String xmlStream)
  {
    SOAPElement res = null;
    try
    {
      DOMParser builder = Context.getInstance().getParser(); // new DOMParser();
      builder.setPreserveWhitespace(false);
      builder.parse(new StringReader(xmlStream));
      res = SOAPUtil.toSOAPElement((Element) builder.getDocument().getDocumentElement());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return res;
  }

  private static SOAPElement getSOAPElementFromSource(Reader xmlStream)
  {
    SOAPElement res = null;
    try
    {
      DOMParser builder = Context.getInstance().getParser(); // new DOMParser();
      builder.setPreserveWhitespace(false);
      builder.parse(xmlStream);
      res = SOAPUtil.toSOAPElement((Element) builder.getDocument().getDocumentElement());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return res;
  }

  private static SOAPElement getSOAPElementFromSource(URL xmlStream)
  {
    SOAPElement res = null;
    try
    {
      DOMParser builder = Context.getInstance().getParser(); // new DOMParser();
      builder.setPreserveWhitespace(false);
      builder.parse(xmlStream);
      res = SOAPUtil.toSOAPElement((Element) builder.getDocument().getDocumentElement());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return res;
  }

  private static SOAPElement getSOAPElementFromSource(Element xmlStream)
  {
    SOAPElement res = null;
    try
    {
      res = SOAPUtil.toSOAPElement(xmlStream);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return res;
  }

  public static void setAsyncResponse(String asr)
  {
    // GUI ? Response
    if (asr != null && "yes".equals(System.getProperty("display.traffic.gui", "no")))
    {
      if (!trafficFrame.isVisible())
        trafficFrame.setVisible(true);
      trafficFrame.setMessageIn(asr);
    }
    asyncResponse = asr;
  }

  public static String getAsyncResponse()
  {
    return asyncResponse;
  }

  /**
   * JAX-RPC Service Invocation
   *
   * @param endpoint
   * @param payload
   * @param protocol
   * @return
   * @throws Exception
   *
   * @deprecated Uses JAX-RPC
   */
  @Deprecated
  public String dynInvoke(String endpoint, String payload, String protocol)
    throws Exception
  {
    MessageFactory mf = MessageFactory.newInstance(protocol);
    SOAPMessage request = mf.createMessage();

    request.getSOAPHeader().detachNode(); // not using SOAP headers
    SOAPBody body = request.getSOAPBody();
    // Specify that the SOAP encoding style is being used.
    //  SOAPFactory soapFactory = SOAPFactory.newInstance();
    //  Name name =  soapFactory.createName("encodingStyle", "SOAP-ENV", SOAP_ENVELOPE_NS);
    //  body.addAttribute(name, SOAP_ENCODING_NS);

    SOAPElement input = getSOAPElementFromSource(payload);
    body.addChildElement(input);

    if (verbose)
      dumpMessage("Request", request); // for debugging

    // Make the call.
    SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
    SOAPConnection connection = scf.createConnection();
    SOAPMessage response = connection.call(request, new URL(endpoint));
    connection.close();
    body = response.getSOAPBody();
    //  dumpMessage("response",
    //              (XMLElement) body.getFirstChild()); // for debugging
    StringWriter sw = new StringWriter();
    XMLElement bodyContent = (XMLElement) body.getFirstChild();
    bodyContent.print(sw);
    return sw.getBuffer().toString();
  }

  public void setXmlServiceResponse(XMLElement xmlServiceResponse)
  {
    this.xmlServiceResponse = xmlServiceResponse;
  }

  public XMLElement getXmlServiceResponse()
  {
    return xmlServiceResponse;
  }

  public boolean isServiceNameOK(String wsdlURL, String serviceName)
    throws Exception
  {
    boolean ok = true;
    DOMParser parser = Context.getInstance().getParser();
    try
    {
      URL wsdl = new URL(wsdlURL);
      parser.parse(wsdl);
      XMLDocument wsdlDoc = parser.getDocument();
      CustomNamespaceResolver nsr = new CustomNamespaceResolver();
      nsr.setPrefix("wsdl", WSDL_NS);
      NodeList nl = wsdlDoc.selectNodes("//wsdl:service[@name = '" + serviceName + "']", nsr);
      if (nl.getLength() == 0)
        ok = false;
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return ok;
  }

  public boolean isPortNameOK(String wsdlURL, String serviceName, String portType)
    throws Exception
  {
    boolean ok = true;
    DOMParser parser = Context.getInstance().getParser();
    try
    {
      URL wsdl = new URL(wsdlURL);
      parser.parse(wsdl);
      XMLDocument wsdlDoc = parser.getDocument();
      CustomNamespaceResolver nsr = new CustomNamespaceResolver();
      nsr.setPrefix("wsdl", WSDL_NS);
      NodeList nl = wsdlDoc.selectNodes("//wsdl:service['" + serviceName + "']/wsdl:port[@name = '" + portType + "']", nsr);
      if (nl.getLength() == 0)
        ok = false;
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return ok;
  }

  public boolean isOperationNameOK(String wsdlURL, String serviceName, String portType, String operation)
    throws Exception
  {
    boolean ok = false;
    ok = isPortNameOK(wsdlURL, serviceName, portType);
    if (ok)
    {
      DOMParser parser = Context.getInstance().getParser();
      try
      {
        URL wsdl = new URL(wsdlURL);
        parser.parse(wsdl);
        XMLDocument wsdlDoc = parser.getDocument();
        CustomNamespaceResolver nsr = new CustomNamespaceResolver();
        nsr.setPrefix("wsdl", WSDL_NS);
        NodeList nl =
          wsdlDoc.selectNodes("//wsdl:portType['" + portType + "']/wsdl:operation[@name = '" + operation + "']", nsr);
        if (nl.getLength() == 0)
          ok = false;
      }
      catch (Exception ex)
      {
        throw ex;
      }
    }
    return ok;
  }

  public void setHrh(HTTPRequestHandler hrh)
  {
    this.hrh = hrh;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return this.username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    return this.password;
  }

  public static String decryptAsyncResponseBody(String response)
    throws XMLParseException, SAXException, IOException, XSLException, Exception
  {
    DOMParser parser = Context.getInstance().getParser();
    XMLDocument doc = null;
    synchronized (parser)
    {
      parser.parse(new StringReader(response));
      doc = parser.getDocument();
    }

    if (verbose)
    {
      doc.print(System.out);
    }

    CustomNamespaceResolver customNsr = new CustomNamespaceResolver();
    customNsr.setPrefix(SOAP_ENV_PREFIX, SOAP_NS);
    customNsr.setPrefix(WS_XENC_PREFIX, WS_XENC_URI);
    customNsr.setPrefix(WSSE_PREFIX, WSSE_URI);

    String encryptedDataXpath = "//" + SOAP_ENV_PREFIX + ":Body/" + WS_XENC_PREFIX + ":EncryptedData";
    String encryptedKeyXpath =
      "//" + SOAP_ENV_PREFIX + ":Header/" + WSSE_PREFIX + ":Security/" + WS_XENC_PREFIX + ":EncryptedKey";
    String encryptionMethodXpath = encryptedKeyXpath + "/" + WS_XENC_PREFIX + ":EncryptionMethod";

    NodeList nl1 = doc.selectNodes(encryptedDataXpath, customNsr);
    NodeList nl2 = doc.selectNodes(encryptedKeyXpath, customNsr);
    NodeList nl3 = doc.selectNodes(encryptionMethodXpath, customNsr);

    Element encryptedDataElement = (Element) nl1.item(0);
    Element encryptedKeyElement = (Element) nl2.item(0);
    Element encryptionMethodElement = (Element) nl3.item(0);

    return decrypt(encryptedDataElement, encryptedKeyElement, encryptionMethodElement);
  }

  public static String decrypt(Element encryptedDataElement, Element encryptedKeyElement, Element encryptionMethodElement)
    throws Exception
  {
    KeyStore keyStore = null;
    String jksLocation = null;
    String jpsConfigLocation = null;
    char[] jksPassword = JKS_PASSWORD.toCharArray();
    char[] keyPassword = JKS_ORAKEY_ALIAS_PASSWORD.toCharArray();
    Element plainElement = null;
    try
    {
      keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

      // Getting the key store input stream. In my case, it is in the same folder as the jps-config.xml file, which is pointed by the system property "oracle.security.jps.config"
      jpsConfigLocation =
          System.getProperty(ORACLE_SECURITY_JPS_CONFIG_ENV_PROP).substring(0, System.getProperty(ORACLE_SECURITY_JPS_CONFIG_ENV_PROP).lastIndexOf(System.getProperty("file.separator")));
      jksLocation =
          jpsConfigLocation + System.getProperty("file.separator") + getKeyStoreFileLocation(System.getProperty(ORACLE_SECURITY_JPS_CONFIG_ENV_PROP));
      keyStore.load(new FileInputStream(jksLocation), jksPassword);
      // Key store loaded.
      // Now exposing some key store contents
      Enumeration aliases = keyStore.aliases();
      String orakeyAlias = null;
      String currentAlias = null;
      if (verbose)
      {
        System.out.println("[ Aliases found within : " + jksLocation + "]");
      }
      while (aliases.hasMoreElements())
      {
        currentAlias = aliases.nextElement().toString();
        if (currentAlias.equals(JKS_ORAKEY_ALIAS))
        {
          orakeyAlias = currentAlias;
        }
        if (verbose)
        {
          System.out.println(currentAlias);
        }
      }

      if (orakeyAlias == null)
      {
        throw new Exception(JKS_ORAKEY_ALIAS + " not found within " + jksLocation);
      }
      Key key = keyStore.getKey(orakeyAlias, keyPassword);

      if (verbose)
      {
        System.out.println("[ Key Algorithm: " + key.getAlgorithm() + "]");
        System.out.println("[ Key Format: " + key.getFormat() + "]");
        Provider[] secProviders = Security.getProviders();
        for (int i = 0; i < secProviders.length; i++)
        {
          System.out.println(secProviders[i].getName());
          System.out.println(secProviders[i].getInfo());
          /*
                    Enumeration e = secProviders[i].elements();
                    while (e.hasMoreElements()) {
                        System.out.println("-- " + e.nextElement().toString());
                    }
  */
        }
      }
      XEEncryptedKey encryptedKey = (XEEncryptedKey) XEEncryptedKey.getInstance(encryptedKeyElement);
      byte[] secretKeyBytes = encryptedKey.decrypt(new XEEncryptionMethod(encryptionMethodElement), key);

      SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, DATA_ENCRYPTION_ALGORITHM);

      plainElement = XEncUtils.decryptElement(encryptedDataElement, secretKeySpec);
    }
    catch (KeyStoreException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
    catch (CertificateException e)
    {
      e.printStackTrace();
    }
    catch (UnrecoverableKeyException e)
    {
      e.printStackTrace();
    }
    catch (XEException e)
    {
      e.printStackTrace();
    }
    catch (DOMException e)
    {
      e.printStackTrace();
    }
    //  return plainElement.getTextContent();
    String plainData = "";
    if (plainElement instanceof XMLElement)
    {
      XMLElement xml = (XMLElement) plainElement;
      StringWriter sw = new StringWriter();
      xml.print(sw);
      plainData = sw.toString();
    }
    return plainData;
  }

  private void addSecurityHeaders(Dispatch<SOAPMessage> dispatcher, Service service, QName port, boolean addressingFeature,
                                  boolean policySecurity)
    throws XMLParseException, SAXException, IOException
  {
    if (policySecurity)
    {
      DOMParser parser = Context.getInstance().getParser();
      XMLElement policyElement = null;
      synchronized (parser)
      {
        parser.parse(this.getClass().getResource("Policy.xml"));
        policyElement = (XMLElement) parser.getDocument().getDocumentElement();
      }

      try
      {
        dispatcher =
            service.createDispatch(port, SOAPMessage.class, Service.Mode.MESSAGE, new AddressingFeature(addressingFeature));
        OracleDispatchImpl odi = (OracleDispatchImpl) dispatcher;
        odi.getRequestContext().put(ClientConstants.CLIENT_CONFIG, policyElement);
        odi.getRequestContext().put(OracleDispatchImpl.USERNAME_PROPERTY, this.getUsername());
        odi.getRequestContext().put(OracleDispatchImpl.PASSWORD_PROPERTY, this.getPassword());
        //odi.getRequestContext().put(SecurityConstants.ClientConstants.WSS_CSF_KEY,"system1-key");
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public String getServiceResponse()
  {
    return serviceResponse;
  }

  public void setPolicySecurity(boolean policySecurity)
  {
    this.policySecurity = policySecurity;
    if (verbose)
      System.out.println("Setting policySecurity to " + policySecurity);
  }

  public void setPolicyDocumentLocation(URL policyDocumentLocation)
  {
    this.policyDocumentLocation = policyDocumentLocation;
  }

  public void setUsernamePasswordSecurity(boolean usernamePasswordSecurity)
  {
    this.usernamePasswordSecurity = usernamePasswordSecurity;
  }

  public void setHandlerResolver(HandlerResolver handlerResolver)
  {
    this.handlerResolver = handlerResolver;
  }

  public Iterator getAttachmentIterator()
  {
    return attachmentIterator;
  }

  private static XMLDocument parseJpsConfig(String jpsConfig)
  {
    DOMParser parser = Context.getInstance().getParser(); // new DOMParser();
    try
    {
      parser.parse(new FileInputStream(jpsConfig));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (XMLParseException e)
    {
      e.printStackTrace();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
    return parser.getDocument();
  }

  private static XMLElement getDefaultContext(String jpsConfig)
    throws Exception
  {
    XMLElement defaultJpsContext = null;
    try
    {
      CustomNamespaceResolver cnr = new CustomNamespaceResolver();
      cnr.setPrefix("jps", "http://xmlns.oracle.com/oracleas/schema/11/jps-config-11_1.xsd");
      NodeList jpsContextsList = parseJpsConfig(jpsConfig).selectNodes("/jps:jpsConfig/jps:jpsContexts", cnr);

      System.out.println("---> JpsConfig in getDefaultContext: " + jpsConfig);

      if (jpsContextsList.getLength() == 0)
      {
        throw new Exception("No jpsContexts element defined! This is not supported! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      else if (jpsContextsList.getLength() > 1)
      {
        throw new Exception("More than 1 jpsContexts element defined! This is not supported! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      NodeList jpsContextNodeList =
        ((XMLElement) jpsContextsList.item(0)).selectNodes("./jps:jpsContext[@name='" + ((XMLElement) jpsContextsList.item(0)).getAttribute("default") +
                                                           "']", cnr);

      if (jpsContextNodeList.getLength() == 0)
      {
        throw new Exception("No jpsContext element defined for \"" +
                            ((XMLElement) jpsContextsList.item(0)).getAttribute("default") +
                            "\" context! This is not supported! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      else if (jpsContextNodeList.getLength() > 1)
      {
        throw new Exception("More than 1 jpsContext element defined for " +
                            ((XMLElement) jpsContextNodeList.item(0)).getAttribute("default") +
                            " context! This is not supported! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      defaultJpsContext = ((XMLElement) jpsContextNodeList.item(0));
    }
    catch (XSLException e)
    {
      e.printStackTrace();
    }
    return defaultJpsContext;
  }

  public static String getKeyStoreFileLocation(String jpsConfig)
    throws Exception
  {
    String location = null;
    try
    {
      XMLElement defaultContext = getDefaultContext(jpsConfig);

      CustomNamespaceResolver cnr = new CustomNamespaceResolver();
      cnr.setPrefix("jps", "http://xmlns.oracle.com/oracleas/schema/11/jps-config-11_1.xsd");
      NodeList keystoreRefList = defaultContext.selectNodes("./jps:serviceInstanceRef[@ref='keystore']", cnr);

      if (keystoreRefList.getLength() == 0)
      {
        throw new Exception("No keystore service reference defined in default jpsContext! Unable to decrypt secured async message! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      else if (keystoreRefList.getLength() > 1)
      {
        throw new Exception("More than 1 keystore service reference defined in default jpsContext! Unable to decrypt secured async message! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }

      NodeList serviceInstanceList =
        parseJpsConfig(jpsConfig).selectNodes("/jps:jpsConfig/jps:serviceInstances/jps:serviceInstance[@name='" +
                                              ((XMLElement) keystoreRefList.item(0)).getAttribute("ref") + "']", cnr);

      if (serviceInstanceList.getLength() == 0)
      {
        throw new Exception("No keystore service instance defined! Unable to decrypt secured async message! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      else if (serviceInstanceList.getLength() > 1)
      {
        throw new Exception("More than 1 keystore service instance defined! Unable to decrypt secured async message! Check https://kix.oraclecorp.com/KIX/display.php?labelId=3525&articleId=256113 out!");
      }
      location = ((XMLElement) serviceInstanceList.item(0)).getAttribute("location");
    }
    catch (XSLException e)
    {
      e.printStackTrace();
    }
    return location;
  }

  public static String getContext(String jpsConfig)
    throws Exception
  {
    return getDefaultContext(jpsConfig).getAttribute("name");
  }
  
  private Thread guiThread = null;
  public void startGUIThread()
  {
    final ServiceUnitTestHelper instance = this;
    final Thread main = Thread.currentThread();
    guiThread = new Thread()
      {
        public void run()
        {
          if (trafficFrame == null)
            trafficFrame = new SOAPTrafficFrame(instance);
          if (!trafficFrame.isVisible())
            trafficFrame.setVisible(true);
          try
          {
            synchronized (main) { main.notify(); }
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          synchronized (this)
          {
            try
            {
              this.wait();
              System.out.println("GUIThread terminated.");
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
        }
      };
    guiThread.start();
    try
    {
      synchronized (main) { main.wait(); } // Wait for the GUI to be up
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    System.out.println("GUIThread on its way");
  }
  
  public Thread getGUIThread()
  {
    return guiThread;
  }

  private static class CustomNamespaceResolver
    implements NSResolver
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


  private class HttpServerThread
    extends Thread
  {
    private boolean before = true;
    private String httpServerPort = "6666";
    private final SpecialBoolean httpServerGotItFirst = new SpecialBoolean(false);
    private Thread parent = null;
    private TinyHTTPServer ths = null;
    private boolean terminateAfterFirstRequest = true;

    public HttpServerThread(Thread parent, String port)
    {
      this.httpServerPort = port;
      this.parent = parent;
      try
      {
        boolean startServerNow = false; // Allows to trap the BindException before starting the server.
        ths = new TinyHTTPServer(httpServerPort, hrh, terminateAfterFirstRequest, verbose, startServerNow);
      }
      catch (BindException be)
      {
        //      System.err.println("Address already in use, try another one.");
        throw new RuntimeException(be);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    public void run()
    {
      try
      {
        before = true;
//      ths = new TinyHTTPServer(httpServerPort, null, hrh, terminateAfterFirstRequest, verbose);
        ths.runServer();
        before = false;
        if (verbose)
          System.out.println("HTTP Server: Got the response");
        httpServerGotItFirst.setValue(true);
        String response = ths.getPostContent(); // TASK What about Multipart?
        if (verbose)
          System.out.println("Got the async reponse");
        setAsyncResponse(response);
        if (parent != null)
        {
          if (verbose)
            System.out.println("HTTP Server: Notifying parent thread.");
          synchronized (parent)
          {
            parent.notify();
          }
        }
        else
          System.out.println("Parent Thread is null");
      }
      catch (Exception ex)
      {
        System.out.println("From HTTP Server thread:" + ex.toString());
        //      System.exit(1);
      }
      finally
      {
        if (verbose)
          System.out.println("HTTP Thread terminated.");
      }
    }

    public void interrupt()
    {
      super.interrupt();
      if (verbose)
        System.out.println("HTTP Thread interrupted");
      // Release the thread
      if (before)
      {
        try
        {
          // Write some content to release it smoothly...
          URL url = new URL("http://localhost:" + httpServerPort);
          URLConnection conn = url.openConnection();
          conn.setDoOutput(true);
          OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
          wr.write("");
          wr.flush();
        }
        catch (Exception ex)
        {
          System.out.println("Failed to release the http server:" + ex.toString());
        }
      }
    }

    public boolean responseCameInTime()
    {
      return httpServerGotItFirst.isTrue();
    }
  }

  /**
   * The one to use with JAX-WS
   */
  private class ServiceInvocationThread
    extends Thread
  {
    private Thread waiter = null;
    private ServiceUnitTestHelper parent = null;

    private String serviceEndpoint = "";
    private String wsdlLocation = "";
    private String serviceNamespaceURI = "";
    private Reader payload = null;
    private String serviceName = "";
    private String portName = "";
    private String protocolBinding = "";
    private int mode = -1;
    private String operation = "";
    private String httpServerPort = "";
    private SynchronousResponseListener srlsnr = null;

    public ServiceInvocationThread(String serviceEndpoint, String wsdlLocation, String serviceNamespaceURI, Reader payload,
                                   String serviceName, String portName, String protocolBinding, int mode, String operation,
                                   final String httpServerPort, Thread t, ServiceUnitTestHelper caller,
                                   SynchronousResponseListener srlsnr)
    {
      super();
      this.waiter = t;
      this.parent = caller;

      this.serviceEndpoint = serviceEndpoint;
      this.wsdlLocation = wsdlLocation;
      this.serviceNamespaceURI = serviceNamespaceURI;
      this.payload = payload;
      this.serviceName = serviceName;
      this.portName = portName;
      this.protocolBinding = protocolBinding;
      this.mode = mode;
      this.operation = operation;
      this.httpServerPort = httpServerPort;
      this.srlsnr = srlsnr;
      this.setDaemon(true);
    }

    public void run()
    {
      try
      {
        XMLElement x =
          invokeService(serviceEndpoint, wsdlLocation, serviceNamespaceURI, payload, serviceName, portName, protocolBinding,
                        mode, operation, httpServerPort, srlsnr);
        parent.setXmlServiceResponse(x);
        synchronized (waiter)
        {
//        System.out.println("Notifying waiter (Done).");
          waiter.notify();
        }
      }
      catch (InterruptedException e)
      {
        System.out.println("InterruptedException in ServiceInvocationThread!");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        if (verbose)
          System.out.println("Exiting ServiceInvocationThread");
      }
      if (verbose)
        System.out.println("ServiceInvocationThread: Done.");
    }

    public void interrupt()
    {
      super.interrupt();
      //    System.out.println("ServiceInvocationThread interrupted!");
    }
  }

  /**
   * @deprecated - Written for the JAX-RPC part.
   */
  @Deprecated
  private class ServiceThread
    extends Thread
  {
    private String wsdlUrl = "";
    private String payload = "";
    private Thread waiter = null;
    private String protocol = "";
    private ServiceUnitTestHelper parent = null;

    public ServiceThread(String wsdl, String pl, Thread t, String ptcl, ServiceUnitTestHelper caller)
    {
      super();
      wsdlUrl = wsdl;
      payload = pl;
      waiter = t;
      protocol = ptcl;
      parent = caller;
//    this.setDaemon(true);
    }

    public void run()
    {
      try
      {
        String resp = invokeServiceFromWSDL(wsdlUrl, payload, protocol);
        parent.setServiceResponse(resp);
        synchronized (waiter)
        {
          //        System.out.println("Notifying waiter (Done).");
          waiter.notify();
        }
      }
      catch (InterruptedException e)
      {
        System.out.println("InterruptedException in ServiceThread!");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        if (verbose)
          System.out.println("Exiting ServiceThread");
      }
      if (verbose)
        System.out.println("ServiceThread: Done.");
    }

    public void interrupt()
    {
      super.interrupt();
      //    System.out.println("Service Thread interrupted!");
    }
  }

  public static class AsyncResponseHandler
    implements AsyncHandler<SOAPMessage>
  {
    SOAPMessage replyMessage = null;

    public void handleResponse(Response<SOAPMessage> response)
    {
      try
      {
        assert response.getClass().getName().equals("oracle.j2ee.ws.common.jaxws.JAXWSResponseImpl");
        Object o = response.get();
        if (verbose)
          System.out.println(" -- (in AsyncResponseHandler, Object is " + (o == null? "null": "a " + o.getClass().getName()) +
                             ")");

        if (o instanceof Message1_1Impl)
        {
          Message1_1Impl resp = (Message1_1Impl) o;
          if (verbose)
          {
            System.out.println("Got the AsyncHandler response...");
            SOAPElement se = SOAPUtil.toSOAPElement(resp.getSOAPPart().getEnvelope());
            //          System.out.println("Full SOAPMessage:" + se.toString());
            DOMParser parser = Context.getInstance().getParser();
            parser.parse(new StringReader(se.toString()));
            XMLDocument doc = parser.getDocument();
            doc.print(System.out);
          }
          replyMessage = (SOAPMessage) o;
        }
        else
        {
          SOAPMessage reply = response.get();
          replyMessage = reply;
        }
        //      replyBuffer = reply.getSOAPBody().getTextContent();
      }
      catch (ExecutionException ee)
      {
        System.out.println("ServiceUnitTestHelper: ExecutionException " + ee.getCause().toString());
        ee.printStackTrace();
      }
      catch (InterruptedException ie)
      {
        System.out.println("Interrupted.");
        ie.printStackTrace();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    public SOAPMessage getReplyMessage()
    {
      return replyMessage;
    }
  }

  @SuppressWarnings("serial")
  public static class TookTooLongException
    extends Exception
  {
    public TookTooLongException(String str)
    {
      super(str);
    }
  }

  public static class SpecialBoolean
  {
    private boolean value = false;

    public SpecialBoolean(boolean b)
    {
      this.value = b;
    }

    public void setValue(boolean b)
    {
      this.value = b;
    }

    public boolean isTrue()
    {
      return this.value;
    }
  }
}
