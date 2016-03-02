package util.httputil.proxy;

import java.net.*;

import java.io.*;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import testhelper.ServiceUnitTestHelper;

import util.httputil.TinyHTTPServer;

public abstract class Proxy
{
  protected DOMParser parser      = new DOMParser();

  protected static final String HEADER_SEPARATOR = "\r\n\r\n";
  protected static final String PAYLOAD_TERMINATOR = "0000\r\n\r\n";
  public final static String RESPONSE_FOR_ASYNC_REQUEST =
    "HTTP/1.1 200 \r\n" +
    "Content-Type: text/xml\r\n" +
    "\r\n";

  public static final String usageArgs = " <localport> <host> <port> <timeout_ms>";
  static int clientCount;

  private String alternateResponse = null;
  protected static int verbose = 0;
  
  public final static int SHUT_UP      = 0;
  public final static int HUSH_HUSH    = 1;
  public final static int NORMAL       = 2;
  public final static int VERBOSE      = 3;
  public final static int VERY_VERBOSE = 4;
  public final static int CHATTERBOX   = 5;
    
  public final static int SYNCHRONOUS_RESPONSE_REQUIRED     = 0;  
  public final static int ASYNCHRONOUS_RESPONSE_REQUIRED    = 1;  
  public final static int NO_ASYNCHRONOUS_RESPONSE_REQUIRED = 2;  // Just absorb

  protected int conversationMode = SYNCHRONOUS_RESPONSE_REQUIRED; // Default
  protected String requestPayload = null;
  protected String responsePayloadContent = null;
  
  private String hostName = "";
  private int    hostPort = 0;
  private int    localPort = 0;
  
  /**
   *
   * @param request the incoming request
   * @return true if the actual service is to be invoked, false to bypass the regular service behavior
   */
  protected boolean sendRequest(byte[] request)
  {
    return true;
  }
  
  protected byte[] onResponse(byte[] response)
  {
    return response;
  }  
  
  protected void postCallback(String request, String servicePayload, String action)
  {
    // 1 - Get the reply to address
    try
    {
      XMLDocument doc = null;
      synchronized (parser)
      {
        parser.parse(new StringReader(request));
        doc = parser.getDocument();
      }
      NSResolver nsr = new NSResolver()
        {
          public String resolveNamespacePrefix(String string)
          {
            return ServiceUnitTestHelper.WSA_NS;
          }
        };
      XMLElement replyTo = (XMLElement)doc.selectNodes("//wsa:ReplyTo/wsa:Address", nsr).item(0);
      String replyToAddress = replyTo.getFirstChild().getNodeValue();
      
      if (verbose > Proxy.HUSH_HUSH) 
        System.out.println("*** Will reply to [" + replyToAddress + "]");
      XMLElement relatesTo = (XMLElement)doc.selectNodes("//wsa:MessageID", nsr).item(0);
      String relatesToID = relatesTo.getFirstChild().getNodeValue();
      if (verbose > Proxy.HUSH_HUSH) 
        System.out.println("*** Message ID [" + relatesToID + "]");
      // Do not call the service, but send the asynchronous reply to this address
      String machineName = replyToAddress.substring("http://".length());
      String sep = ":";
      if (machineName.indexOf(sep) == -1)
        sep = "/";
      machineName = machineName.substring(0, machineName.indexOf(sep));
      if (verbose > Proxy.HUSH_HUSH) 
        System.out.println("Machine:[" + machineName + "]");
      String port = "80";
      if (sep.equals(":"))
      {
        port = replyToAddress.substring("http://".length());
        port = port.substring(port.indexOf(":"));
        port = port.substring(1, port.indexOf("/"));            
      }
      if (verbose > Proxy.HUSH_HUSH) 
        System.out.println("Port:[" + port + "]");
      String query = replyToAddress.substring("http://".length());
      query = query.substring(query.indexOf("/"));
      if (verbose > Proxy.HUSH_HUSH) 
        System.out.println("Query:[" + query + "]");
      
      // No alternate reponse in that case.
//    setAlternateResponse(RESPONSE_FOR_ASYNC_REQUEST); // Just acknowledge request
      
      String xmlAlt = 
        "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" + 
        "              xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" + 
        "  <env:Header>\n" + 
        "    <wsa:To>http://" + machineName + ":" + port + "/</wsa:To>\n" + 
        "    <wsa:Action>" + action + "</wsa:Action>\n" + 
        "    <wsa:MessageID>" + relatesToID + "</wsa:MessageID>\n" + 
        "    <wsa:RelatesTo>" + relatesToID + "</wsa:RelatesTo>\n" + 
        "    <wsa:ReplyTo>\n" + 
        "      <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" + 
        "      <wsa:ReferenceParameters>\n" + 
        "        <instra:tracking.conversationId xmlns:instra=\"http://xmlns.oracle.com/sca/tracking/1.0\">" + relatesToID + "</instra:tracking.conversationId>\n" + 
        "        <!--instra:tracking.parentComponentInstanceId xmlns:instra=\"http://xmlns.oracle.com/sca/tracking/1.0\">bpel:40021</instra:tracking.parentComponentInstanceId-->\n" + 
        "      </wsa:ReferenceParameters>\n" + 
        "    </wsa:ReplyTo>\n" + 
        "  </env:Header>\n" + 
        "  <env:Body>\n" + 
        servicePayload +
        "  </env:Body>\n" + 
        "</env:Envelope>";
      final String altPayload = 
        "POST " + query + " HTTP/1.1\r\n" +
        "Host: " + machineName + ":" + port + " \r\n" +
        "SOAPAction: \"" + action + "\"\r\n" + 
        "Content-type: text/xml; charset=UTF-8\r\n" +
        "Content-Length: " + Integer.toString(xmlAlt.length()) + "\r\n" +    
        "\r\n" +
        xmlAlt;
      final String mName = machineName;
      final String portValue = port;
      Thread t = new Thread()
        {
          public void run()
          {
            if (verbose > Proxy.HUSH_HUSH) 
              System.out.println("Posting this payload:[" + altPayload + "]");
            try 
            { 
              TinyHTTPServer.postContent(mName, portValue, altPayload); 
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
            if (verbose > Proxy.HUSH_HUSH) 
              System.out.println("Content posted (async reply)");
          }
        };
      t.start();
//    t.join();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  protected static final byte[] cloneByteArray(byte[] from)
  {
    byte[] to = new byte[from.length];
    System.arraycopy(from, 0, to, 0, from.length);
    return to;
  }

  public static synchronized void print(int i)
  {
    System.out.print((char) i);
  }

  public static synchronized void println(String s)
  {
    System.out.println(s);
  }

  public static synchronized void display(String s)
  {
    System.err.println(s);
  }

  public static synchronized void quit(long t)
  {
    if (verbose > Proxy.NORMAL) 
      display("...Proxy.quit after waiting " + Math.abs(t) + " ms");
    clientCount--;
  }

  private boolean keepListening = true;  

  public void run(int localport, String host, int port, long timeout) throws BindException
  {
    this.localPort = localport;
    this.hostName = host;
    this.hostPort = port;
    
    try
    {
      ServerSocket sSocket = new ServerSocket(localport);
      while (keepListening)
      {
        Socket cSocket = null;
        try
        {
          if (verbose > Proxy.SHUT_UP) 
            display("listening...");
          cSocket = sSocket.accept();
          if (cSocket != null)
          {
            clientCount++;
            if (verbose > Proxy.HUSH_HUSH) 
              display("accepted as #" + clientCount + ":" + cSocket);
            ProxyConnection c = new ProxyConnection(this, cSocket, host, port, timeout);
            c.run();
          }
        }
        catch (Exception e)
        {
          e.printStackTrace(System.err);
        }
        try
        {
          cSocket.close();
        }
        catch (Exception e)
        {
          //fall thru
        }
      }
      System.out.println("Done.");
    }
    catch (BindException be)
    {
      throw be;
    }
    catch (Throwable t)
    {
      t.printStackTrace(System.err);
    }
  }

  public void setAlternateResponse(String alternateResponse)
  {
    this.alternateResponse = alternateResponse;
  }

  public String getAlternateResponse()
  {
    return alternateResponse;
  }

  public void setKeepListening(boolean keepListening)
  {
    this.keepListening = keepListening;
    if (!keepListening)
    {
      System.out.println("Exiting Proxy...");
      try { System.exit(0); } // Brutal... 
      catch (Exception ex)
      {
        System.out.println("Trapped:" + ex.toString());
      }
    }
  }

  public void setVerbose(int verbose)
  {
    this.verbose = verbose;
  }
  
  public int getVerbose()
  {
    return this.verbose;
  }
  
  public DOMParser getParser()
  {
    return this.parser;
  }

  public String getHostName()
  {
    return hostName;
  }

  public int getHostPort()
  {
    return hostPort;
  }

  public int getLocalPort()
  {
    return localPort;
  }

  public int getConversationMode()
  {
    return conversationMode;
}

  public String getRequestPayload()
  {
    return requestPayload;
  }

  public String getResponsePayloadContent()
  {
    return responsePayloadContent;
  }
}
