package util.httputil.proxyimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.net.BindException;

import java.util.HashMap;
import java.util.Properties;

import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import testhelper.ServiceUnitTestHelper;

import testing.util.http.Worker;

import util.httputil.proxy.Proxy;

/*
 * Shows how to trap some requests, GET & POST, to bypass their standard behavior
 * 
 * All the requests to the real service (wsdl, xsd, etc) are done to the real service.
 * Only the request with a given syntax (POST on a given URL) are trapped and by passed.
 * 
 * Start this proxy like this:
 * Prompt> java util.httputil.proxyimpl.ReferenceProxy <server-name> 7001 7101
 * 
 * 7001 is the SOA Port on the server <server-name>.
 * 7101 is the port of the proxy.
 * 
 * Service should be invoked on localhost:7101
 */
public class ReferenceProxy extends Proxy
{
  private Properties properties = null;
  
  private final static String REQUEST_STARTS_WITH_RADICAL     = "request.starts.with.";
  private final static String REQUEST_COND_XPATH_RADICAL      = "request.condition.xpath.";
  private final static String REQUEST_COND_VALUE_RADICAL      = "request.condition.value.";
  private final static String RESPONSE_PAYLOAD_FILE_RADICAL   = "response.payload.file.";
  private final static String RESPONSE_PAYLOAD_STRING_RADICAL = "response.payload.string.";
   
//  private int getPropertiesIndex(String httpRequest)
//  {
//    return getPropertiesIndex(httpRequest, 1);
//  }
  
  private int getPropertiesIndex(String httpRequest, int startAt)
  {
    int index = 0;
    if (properties != null)
    {
      int idx = startAt;
      boolean keepLooping = true;
      while (keepLooping)
      {
        String requestStart = properties.getProperty(REQUEST_STARTS_WITH_RADICAL + Integer.toString(idx));
        if (requestStart != null)
        {
          if (httpRequest.startsWith(requestStart))
          {
            keepLooping = false;
            index = idx;
          }
          idx++;
        }
        else
          keepLooping = false;
      }
    }    
    return index;
  }
  
  @Override
  protected synchronized boolean sendRequest(byte[] request)
  {
    boolean decision = true; // Default returned value
    String req = new String(request);
    if (verbose == Proxy.CHATTERBOX)
    {
      System.out.println("\n============== TOP ===============");
      System.out.println("Managing request [" + req + "]");
      System.out.println("============ BOTTOM ==============");
    }
    /* 
     * Manage behavior here
     * We look for a match in the http request and a property like request.starts.with.X
     */
    boolean loop = true;
    int startPropertyScanAt = 1;
    while (loop)
    {
      loop = false;
      int propertiesIndex = getPropertiesIndex(req, startPropertyScanAt);
      if (propertiesIndex > 0)
      {
        boolean textXml = false;
        String headers = "";
        String payload = "";
        
        if (req.indexOf(HEADER_SEPARATOR) > -1)
        {
          headers = req.substring(0, req.indexOf(HEADER_SEPARATOR));
          if (verbose > Proxy.HUSH_HUSH) 
            System.out.println("Headers [" + headers + "]");
          String[] headerArray = headers.split("\r\n");
          
          if (verbose > Proxy.HUSH_HUSH) 
            System.out.println("--- Response Headers ---");
          for (int i=0; i<headerArray.length; i++)
          {
            if (verbose > Proxy.HUSH_HUSH) 
              System.out.println("[Header " + (i+1) + "]" + headerArray[i]);
            if (headerArray[i].startsWith("Content-Type: ") || // Only with POST requests...
                headerArray[i].startsWith("Content-type: "))
            {
              String mType = headerArray[i].substring("Content-Type: ".length());
              textXml = (mType.indexOf("text/xml") > -1);
            }
          }
        }
        // Condition to rework the payload
        if (textXml)
        {
          payload = req.substring(req.indexOf(HEADER_SEPARATOR) + HEADER_SEPARATOR.length());
          if (verbose > Proxy.HUSH_HUSH) 
            System.out.println("------ Payload to parse ------\n" + 
                                payload + 
                             "\n--- End of payload to parse --");  
          // Here is the Skill
          try
          {
            XMLDocument doc = null;
            synchronized (parser)
            {
              parser.parse(new StringReader(payload));
              doc = parser.getDocument();
            }
            String xPath = properties.getProperty(REQUEST_COND_XPATH_RADICAL + Integer.toString(propertiesIndex));
            boolean conditionEvaluation = false;
            String condValue = "";
            if (xPath == null)              
            {
              conditionEvaluation = true; // Nothing to evaluate, go ahead.
        //    throw new RuntimeException("Property " + REQUEST_COND_XPATH_RADICAL + Integer.toString(propertiesIndex) + " missing.");
            }
            else
            {
              NSResolver nsr = generateNSR(xPath);
              String reworkedXPath = reworkXPath(xPath);
              
              NodeList nl = doc.selectNodes(reworkedXPath, nsr);
              if (nl.getLength() == 0)
              {
                String errMess = "XPath [" + xPath + "] return nothing when applied to \n" + payload;
                throw new RuntimeException(errMess);
              }
              XMLElement name = (XMLElement)nl.item(0);
              condValue = properties.getProperty(REQUEST_COND_VALUE_RADICAL + Integer.toString(propertiesIndex));
              if (condValue == null)
                throw new RuntimeException("Property " + REQUEST_COND_VALUE_RADICAL + Integer.toString(propertiesIndex) + " missing.");
              conditionEvaluation = name.getFirstChild().getNodeValue().equals(condValue);
            }
            if (conditionEvaluation)
            {
              System.out.println("Found the test to patch. Bypassing the service invocation (" + condValue + ").");
              // Find the type of response (Sync, Asyn 2 ways, Async 1 way).
              String replyToAddress = null;
              try
              {
                replyToAddress = doc.selectNodes("//wsa:ReplyTo/wsa:Address", new NSResolver()
                  {
                    public String resolveNamespacePrefix(String string)
                    {
                      return ServiceUnitTestHelper.WSA_NS;
                    }
                  }).item(0).getFirstChild().getNodeValue();
              }
              catch (NullPointerException npe)
              {
                System.out.println("No Replyto in Envelope");
              }
              if (replyToAddress != null && replyToAddress.trim().length() > 0 && !ServiceUnitTestHelper.WSA_ANONYMOUS.equals(replyToAddress))
              {
                conversationMode = Proxy.ASYNCHRONOUS_RESPONSE_REQUIRED;     
                requestPayload = payload;
              }
              
              String altRespHeaders = 
                "HTTP/1.1 200 OK\r\n" + 
                "Date: Tue, 04 Aug 2009 15:19:57 GMT\r\n" + 
                "Transfer-Encoding: chunked\r\n" + 
                "Content-Type: text/xml;charset=\"utf-8\"\r\n" + 
                "X-ORACLE-DMS-ECID: 0000IB_zgjZBLA5xrOECSY1ASWsa0000Si\r\n" + 
                "X-Powered-By: Servlet/2.5 JSP/2.1";
  
              decision = false; // Hook!
              
              String payloadContent =  properties.getProperty(RESPONSE_PAYLOAD_STRING_RADICAL + Integer.toString(propertiesIndex));
              if (payloadContent == null) // Try with a file
              {
                String fileName = properties.getProperty(RESPONSE_PAYLOAD_FILE_RADICAL + Integer.toString(propertiesIndex));
                if (fileName == null)
                {
                  throw new RuntimeException("Both " + RESPONSE_PAYLOAD_STRING_RADICAL + Integer.toString(propertiesIndex) + " and " +
                                             RESPONSE_PAYLOAD_FILE_RADICAL + Integer.toString(propertiesIndex) + " are missing.");
                }
                else
                {
                  File f = new File(fileName);
                  if (!f.exists())
                    throw new RuntimeException("File " + fileName + " not found.");
                  else
                  {
                    StringBuffer sb = new StringBuffer();
                    String line = "";
                    boolean keepReading = true;
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    while (keepReading)
                    {
                      line = br.readLine();
                      if (line == null)
                        keepReading = false;
                      else
                        sb.append(line);
                    }
                    br.close();
                    payloadContent = sb.toString();
                  }
                }
              }
              responsePayloadContent = payloadContent;
              
              String altRespPayload =
                "<?xml version = '1.0' encoding = 'UTF-8'?>\n" + 
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <S:Body>\n" +
                // --- Patched payload here ---
                payloadContent +
                // --- End of the patched payload ---
                "   </S:Body>\n" + 
                "</S:Envelope>\n";
              
              int newLength = altRespPayload.length();
              String strLen = Integer.toHexString(newLength).toLowerCase();
              while (strLen.length() < 4) strLen = "0" + strLen; // LPad
              String altResp = altRespHeaders + HEADER_SEPARATOR +
                               strLen.toLowerCase() + "\r\n" +
                               altRespPayload + PAYLOAD_TERMINATOR;
              
              setAlternateResponse(altResp);
            }      
            else // Try again, in case another set matches.
            {
              loop = true;
              startPropertyScanAt++;
            }
          }
          catch (Exception ex)
          {          
            System.err.println("For payload:\n");
            System.err.println(payload);
            System.err.println();
            ex.printStackTrace();
          }
        }      
        else // GET HTTP mode? Pure Socket mode?
        {
//        System.out.println("***** Request:" + req);
          decision = false;    
          boolean conditionEvaluation = false;

          XMLDocument doc = null;
          synchronized (parser)
          {
            try
            {
              parser.parse(new StringReader(req));
              doc = parser.getDocument();
            }
            catch (Exception ex)
            {
              conditionEvaluation = true;
            }
          }
          String xPath = properties.getProperty(REQUEST_COND_XPATH_RADICAL + Integer.toString(propertiesIndex));
          String condValue = "";
          if (xPath == null)              
          {
            conditionEvaluation = true; // Nothing to evaluate, go ahead.
      //    throw new RuntimeException("Property " + REQUEST_COND_XPATH_RADICAL + Integer.toString(propertiesIndex) + " missing.");
          }
          else
          {
            NSResolver nsr = generateNSR(xPath);
            String reworkedXPath = reworkXPath(xPath);            
            try
            {
              NodeList nl = doc.selectNodes(reworkedXPath, nsr);
              if (nl.getLength() == 0)
              {
                String errMess = "XPath [" + xPath + "] return nothing when applied to \n" + payload;
                throw new RuntimeException(errMess);
              }
              XMLElement name = (XMLElement)nl.item(0);
              condValue = properties.getProperty(REQUEST_COND_VALUE_RADICAL + Integer.toString(propertiesIndex));
              if (condValue == null)
                throw new RuntimeException("Property " + REQUEST_COND_VALUE_RADICAL + Integer.toString(propertiesIndex) + " missing.");
              conditionEvaluation = name.getFirstChild().getNodeValue().equals(condValue);
              if (verbose > Proxy.NORMAL)
              {
                System.out.println("Comparing [" + condValue + 
                   "] (properties)\nWith      [" + name.getFirstChild().getNodeValue() + 
                   "] (actual)\nConclusion:" + Boolean.toString(conditionEvaluation));
              }
            }
            catch (Exception ex)
            {
              if (verbose > Proxy.NORMAL)
                System.err.println(ex.toString());
            }
          }
          if (conditionEvaluation)
          {
            System.out.println("Found the test to patch. Bypassing the service invocation (" + condValue + ").");
            // TODO Find the type of response (Sync, Asyn 2 ways, Async 1 way).

          }
          else
            continue; // TODO Redirect to the original Adapter? Possible?
          
          String payloadContent =  properties.getProperty(RESPONSE_PAYLOAD_STRING_RADICAL + Integer.toString(propertiesIndex));
          if (payloadContent == null) // Try with a file
          {
            String fileName = properties.getProperty(RESPONSE_PAYLOAD_FILE_RADICAL + Integer.toString(propertiesIndex));
            if (fileName == null)
            {
              throw new RuntimeException("Both " + RESPONSE_PAYLOAD_STRING_RADICAL + Integer.toString(propertiesIndex) + 
                                         " and " + RESPONSE_PAYLOAD_FILE_RADICAL + Integer.toString(propertiesIndex) + " are missing.");
            }
            else
            {
              File f = new File(fileName);
              if (!f.exists())
                throw new RuntimeException("File " + fileName + " not found.");
              else
              {
                StringBuffer sb = new StringBuffer();
                String line = "";
                boolean keepReading = true;
                try
                {
                  BufferedReader br = new BufferedReader(new FileReader(f));
                  while (keepReading)
                  {
                    line = br.readLine();
                    if (line == null)
                      keepReading = false;
                    else
                      sb.append(line);
                  }
                  br.close();
                }
                catch (Exception ex)
                {
                  ex.printStackTrace();
                }
                payloadContent = sb.toString();
              }
            }
          }
          responsePayloadContent = payloadContent;
          
          if (verbose > Proxy.NORMAL)
            System.out.println("For request [" + req + "]\nReturning\n" + payloadContent);
          
          String altRespPayload = payloadContent;
          
          String altRespHeaders = 
            "HTTP/1.1 200 OK\r\n" + 
            "Content-Type: text/xml;charset=\"utf-8\"\r\n" + 
            "X-Powered-By: The A-Team";
          
          // GET HTTP, or pure Socket  
          String altResp = (req.startsWith("GET")?(altRespHeaders + HEADER_SEPARATOR):"") + altRespPayload;
          
          setAlternateResponse(altResp);
        }
      }
      else if (req.startsWith("GET /are-you-up"))
      {
        decision = false; // Hook!
        String altRespHeaders = 
          "HTTP/1.1 200 OK\r\n" + 
          "Content-Type: text/html;charset=\"utf-8\"\r\n" + 
          "X-Powered-By: The A-Team";
        
        String altRespPayload =
        "<html>" + 
          "<head><title>Provided by the A-Team</title></head>" +
          "<body>" + 
            "<h1>Yes!</h1>" +
            "Proxy is up on port " + getLocalPort() + "<br>" +
            "Managing requests for " + getHostName() + ":" + getHostPort() + "<br>" +
          "</body>" + 
        "</html>";
        
        String altResp = altRespHeaders + HEADER_SEPARATOR + altRespPayload;
        
        setAlternateResponse(altResp);   
      }
      else if (req.startsWith("GET /please-stop-proxy"))
      {
        decision = false;
        String altRespHeaders = 
          "HTTP/1.1 200 OK\r\n" + 
          "Content-Type: text/html;charset=\"utf-8\"\r\n" + 
          "X-Powered-By: The A-Team";
        
        String altRespPayload = // That one might not reach its destination (server down...)
        "<html>" + 
          "<head><title>Provided by the A-Team</title></head>" +
          "<body>" + 
            "<h1>Bye-bye</h1>" +
          "</body>" + 
        "</html>";
        
        String altResp = altRespHeaders + HEADER_SEPARATOR + altRespPayload;
        
        setAlternateResponse(altResp);   
        this.setKeepListening(false);
      }
      else
      {
        if (verbose > Proxy.NORMAL)
        {
          System.out.println("+++ Sending request to the server (dummy proxy).");
        }
      }
    }
//  System.out.println("sendRequest returns " + decision);
    return decision;
  }
  
  private final static String NAMESPACE_PREFIX_RADICAL = "ns";
  
  public static NSResolver generateNSR(String xpath)
  {
    DynamicNSResolver nsr = new DynamicNSResolver();
    boolean keepWorking = true;
    int nbNs = 1;
    String progress = xpath;
    while (keepWorking)
    {
      int openIndex = progress.indexOf("{"); 
      if (openIndex > -1)
      {
        int closeIndex = progress.indexOf("}", openIndex + 1);
        if (closeIndex > -1)
        {
          String uri = progress.substring(openIndex + 1, closeIndex);
          nsr.addNamespace(NAMESPACE_PREFIX_RADICAL + Integer.toString(nbNs), uri);
          progress = progress.substring(closeIndex + 1);
          nbNs++;
        }
        else
          throw new RuntimeException("Unbalanced parenthesis in XPath expression [" + xpath + "]");
      }
      else
        keepWorking = false;
    }    
    return nsr;
  }
  
  public static String reworkXPath(String xpath)
  {
    String newXPath = "";
    boolean keepWorking = true;
    int nbNs = 1;
    String progress = xpath;
    while (keepWorking)
    {
      int openIndex = progress.indexOf("{"); 
      if (openIndex > -1)
      {
        if (newXPath.length() == 0 && openIndex > 0)
          newXPath = progress.substring(0, openIndex);
        int closeIndex = progress.indexOf("}", openIndex + 1);
        if (closeIndex > -1)
        {
          newXPath += (NAMESPACE_PREFIX_RADICAL + Integer.toString(nbNs) + ":");
          int nextOpen = progress.indexOf("{", closeIndex);
          if (nextOpen > -1)
          newXPath += (progress.substring(closeIndex + 1, nextOpen));
          else
            newXPath += (progress.substring(closeIndex + 1));
          progress = progress.substring(closeIndex + 1);
          nbNs++;
        }
        else
          throw new RuntimeException("Unbalanced parenthesis in XPath expression [" + xpath + "]");
      }
      else
        keepWorking = false;
    }        
    return newXPath;
  }
  
  public void setProperties(String fileName) throws Exception
  {
    properties = new Properties();
    properties.load(new FileReader(fileName));
  }
  
  public static void main__(String[] args)
  {
    String xp = "//{http://xmlns.oracle.com/soatesthelper/SOACompositeForInstallationTests/SynchronousBPELProcess}input/{urn:akeu-coucou}larigou";
    generateNSR(xp);
    String newXPath = reworkXPath(xp);
    System.out.println(xp + "\nbecomes\n" + newXPath);
  }
  
  private final static String REMOTE_HOST = "-remote-host";
  private final static String REMOTE_PORT = "-remote-port";
  private final static String PROXY_PORT  = "-proxy-port";
  private final static String PROP_FILE   = "-prop-file";
  private final static String DEBUG_LEVEL = "-debug-level";
  
  private static String remoteHost = "";
  private static String remotePort = "";
  private static String localPort  = "";
  private static String propFile   = "";
  private static String debugLevel = "0";
  
  private static void scanParameters(String[] prm) throws Exception
  {
    for (int i=0; i<prm.length; i++)
    {
      if (prm[i].equals(REMOTE_HOST))
        remoteHost = prm[i+1];
      else if (prm[i].equals(REMOTE_PORT))
        remotePort = prm[i+1];
      else if (prm[i].equals(PROXY_PORT))
        localPort = prm[i+1];
      else if (prm[i].equals(PROP_FILE))
        propFile = prm[i+1];
      else if (prm[i].equals(DEBUG_LEVEL))
        debugLevel = prm[i+1];
    }
  }
  
  public static void main(String[] args) throws IOException
  {
    try
    {
      scanParameters(args);
      if (remoteHost.trim().length() == 0 ||
          remotePort.trim().length() == 0 ||
          localPort.trim().length() == 0 ||
          propFile.trim().length() == 0)
        throw new IllegalArgumentException("Missing argument(s).");
        
      // Get the command-line arguments: the host and port we are proxy for
      // and the local port that we listen for connections on
      final String _remoteHost     = remoteHost;
      final int _remoteport        = Integer.parseInt(remotePort);
      final int _localport         = Integer.parseInt(localPort);
      final String _propertiesFile = propFile;

      final Worker worker = new Worker()
      {
        public void proceed()
        {
          // Print a start-up message
          System.out.println("Starting proxy for " + _remoteHost + ":" + _remoteport + " on port " + _localport);
          System.out.println("To stop the proxy, send it this GET request: localhost:" + _localport + "/please-stop-proxy");
          // And start running the server
          ReferenceProxy proxy = new ReferenceProxy();
          int verboseLevel = Proxy.SHUT_UP; 
          try
          {
            verboseLevel = Integer.parseInt(debugLevel);
            System.out.println("Verbose Level set to " + verboseLevel);
          }
          catch (Exception ex) { System.err.println(ex.toString()); }
          proxy.setVerbose(verboseLevel); // 0: Shut up, 5: Chatterbox
          try
          {
            if (_propertiesFile.trim().length() > 0)
              proxy.setProperties(_propertiesFile.trim());
            proxy.run(_localport, _remoteHost, _remoteport, 3000);
          }
          catch (BindException e)
          {
            System.err.println(e.toString());
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        
        public void interrupt()
        {
          System.out.println("Work interruption was requested");
        }
      };
      
      Thread delegate = new Thread()
        {
          public void run()
          {
            try 
            { 
              worker.proceed();
              System.out.println("Delegate Thread done.");
//            worker.interrupt();
            } 
            catch (Exception ex) { ex.printStackTrace(); }
          }
        };
      delegate.start();
    }
    catch (Exception e)
    {
  //  System.err.println(e);
      e.printStackTrace();
      System.err.println("Usage: java ReferenceProxy " + REMOTE_HOST + " <remotehost> " + 
                                                         REMOTE_PORT + " <remoteport> " + 
                                                         PROXY_PORT + " <localport> " + 
                                                         PROP_FILE + " <properties.file> " +
                                                      "[ " + DEBUG_LEVEL + "[0-5] ]");
    }
  }
  
  public static class DynamicNSResolver implements NSResolver
  {
    private HashMap<String, String> map = new HashMap<String, String>();
    
    public void addNamespace(String prefix, String uri)
    {
      map.put(prefix, uri);
    }
    
    public String resolveNamespacePrefix(String prefix)
    {
      return map.get(prefix);
    }
  }
}
