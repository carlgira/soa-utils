package util.httputil.proxyimpl;

import java.io.IOException;
import java.io.StringReader;

import java.net.BindException;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import testing.util.http.WaitAndKill;
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
public class ReferenceProxyV1 extends Proxy
{
  private DOMParser parser = new DOMParser();
   
  @Override
  protected synchronized boolean sendRequest(byte[] request)
  {
    boolean decision = true; // Default returned value
    String req = new String(request);
//  if (verbose == Proxy.CHATTERBOX)
    if (verbose == Proxy.CHATTERBOX || true)
    {
      System.out.println("============== TOP ===============");
      System.out.println("Managing request [" + req + "]");
      System.out.println("============ BOTTOM ==============");
    }
    // Manage behavior here
    if (req.startsWith("POST /greetings/GreetingPort HTTP/1.1"))
    {
//    System.out.println("Asking for [" + req + "]");
      boolean textXml = false;
      String headers = "";
      String payload = "";
      
      if (req.indexOf(HEADER_SEPARATOR) > -1)
      {
        headers = req.substring(0, req.indexOf(HEADER_SEPARATOR));
        if (verbose > Proxy.HUSH_HUSH) System.out.println("Headers [" + headers + "]");
        String[] headerArray = headers.split("\r\n");
        
        if (verbose > Proxy.HUSH_HUSH) System.out.println("--- Response Headers ---");
        for (int i=0; i<headerArray.length; i++)
        {
          if (verbose > Proxy.HUSH_HUSH) System.out.println("[Header " + (i+1) + "]" + headerArray[i]);
          if (headerArray[i].startsWith("Content-Type: ") ||
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
          XMLElement name = (XMLElement)doc.selectNodes("//arg1").item(0);
          if (name.getFirstChild().getNodeValue().equals("Faked Test"))
          {
            System.out.println("Found the Faked Test...");
            
            String altRespHeaders = 
              "HTTP/1.1 200 OK\r\n" + 
              "Date: Tue, 04 Aug 2009 15:19:57 GMT\r\n" + 
              "Transfer-Encoding: chunked\r\n" + 
              "Content-Type: text/xml;charset=\"utf-8\"\r\n" + 
              "X-ORACLE-DMS-ECID: 0000IB_zgjZBLA5xrOECSY1ASWsa0000Si\r\n" + 
              "X-Powered-By: Servlet/2.5 JSP/2.1";

            XMLElement language = (XMLElement)doc.selectNodes("//arg0").item(0);
            if (language.getFirstChild().getNodeValue().equals("FR"))
            {
              System.out.println("Found FR");
              decision = false; // Hook!
              String altRespPayload =
                "<?xml version = '1.0' encoding = 'UTF-8'?>\n" + 
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
                "   <S:Body>\n" + 
                "      <ns2:sayHelloResponse xmlns:ns2=\"http://greetingsservice/\">\n" + 
                "         <return>Bonjour tres cher Monsieur.</return>\n" + 
                "      </ns2:sayHelloResponse>\n" + 
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
            else if (language.getFirstChild().getNodeValue().equals("ES"))
            {
              System.out.println("Found ES");
              decision = false; // Hook!
              String altRespPayload =
                "<?xml version = '1.0' encoding = 'UTF-8'?>\n" + 
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
                "   <S:Body>\n" + 
                "      <ns2:sayHelloResponse xmlns:ns2=\"http://greetingsservice/\">\n" + 
                "         <return>Muy Buenos dias Senor.</return>\n" + 
                "      </ns2:sayHelloResponse>\n" + 
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
    }
    else if (req.startsWith("POST /soa-infra/services/default/SOACompositeForInstallationTests/synchronousbpelprocess_client_ep HTTP/1.1"))
    {
      boolean textXml = false;
      String headers = "";
      String payload = "";
      
      if (req.indexOf(HEADER_SEPARATOR) > -1)
      {
        headers = req.substring(0, req.indexOf(HEADER_SEPARATOR));
        if (verbose > Proxy.HUSH_HUSH) System.out.println("Headers [" + headers + "]");
        String[] headerArray = headers.split("\r\n");
        
        if (verbose > Proxy.HUSH_HUSH) System.out.println("--- Response Headers ---");
        for (int i=0; i<headerArray.length; i++)
        {
          if (verbose > Proxy.HUSH_HUSH) System.out.println("[Header " + (i+1) + "]" + headerArray[i]);
          if (headerArray[i].startsWith("Content-Type: ") ||
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
        NSResolver nsr = new NSResolver()
        {
          public String resolveNamespacePrefix(String string)
          {
            return "http://xmlns.oracle.com/soatesthelper/SOACompositeForInstallationTests/SynchronousBPELProcess";
          }
        };
        try
        {
          XMLDocument doc = null;
          synchronized (parser)
          {
            parser.parse(new StringReader(payload));
            doc = parser.getDocument();
          }
          XMLElement name = (XMLElement)doc.selectNodes("//ns:input", nsr).item(0);
          if (name.getFirstChild().getNodeValue().equals("whatever"))
          {
            System.out.println("Found the test to patch...");
            
            String altRespHeaders = 
              "HTTP/1.1 200 OK\r\n" + 
              "Date: Tue, 04 Aug 2009 15:19:57 GMT\r\n" + 
              "Transfer-Encoding: chunked\r\n" + 
              "Content-Type: text/xml;charset=\"utf-8\"\r\n" + 
              "X-ORACLE-DMS-ECID: 0000IB_zgjZBLA5xrOECSY1ASWsa0000Si\r\n" + 
              "X-Powered-By: Servlet/2.5 JSP/2.1";

            decision = false; // Hook!
            String altRespPayload =
              "<?xml version = '1.0' encoding = 'UTF-8'?>\n" + 
              "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
              "   <S:Body>\n" +
              // --- Patched payload here ---
              "     <ns2:processResponse xmlns:ns2=\"http://xmlns.oracle.com/soatesthelper/SOACompositeForInstallationTests/SynchronousBPELProcess\">\n" + 
              "       <ns2:result>PATCHED</ns2:result>\n" + 
              "     </ns2:processResponse>\n" +
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
        }
        catch (Exception ex)
        {          
          System.err.println("For payload:\n");
          System.err.println(payload);
          System.err.println();
          ex.printStackTrace();
        }
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
//  System.out.println("sendRequest returns " + decision);
    return decision;
  }
  
  public static void main(String[] args) throws IOException
  {
    try
    {
      // Check the number of arguments
      if (args.length != 3)
        throw new IllegalArgumentException("Wrong number of arguments.");

      // Get the command-line arguments: the host and port we are proxy for
      // and the local port that we listen for connections on
      final String remoteHost = args[0];
      final int remoteport    = Integer.parseInt(args[1]);
      final int localport     = Integer.parseInt(args[2]);
      final Worker worker = new Worker()
      {
        public void proceed()
        {
          // Print a start-up message
          System.out.println("Starting proxy for " + remoteHost + ":" + remoteport + " on port " + localport);
          System.out.println("To stop the proxy, send it this GET request: /please-stop-proxy");
          // And start running the server
          ReferenceProxyV1 tps = new ReferenceProxyV1();
          tps.setVerbose(Proxy.HUSH_HUSH); // 0: Shut up, 5: Chatterbox
          try
          {
            tps.run(localport, remoteHost, remoteport, 3000);
          }
          catch (BindException e)
          {
            System.err.println(e.toString());
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
      System.err.println("Usage: java ReferenceProxy <remotehost> <remoteport> <localport>");
    }
  }
}
