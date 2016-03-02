package util.httputil.proxyimpl;

import java.io.IOException;

import java.io.StringReader;

import java.io.StringWriter;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import util.httputil.proxy.Proxy;


public class TinyProxySample
  extends Proxy
{
//private int verbose = 1;
  private DOMParser parser = new DOMParser();

  @Override  
  public byte[] onResponse(byte[] response)
  {
    byte[] reply = cloneByteArray(response);
    
    try
    {
      String line = new String(reply);
      
      boolean textXml = false;
      String headers = "";
      String payload = "";
      
      if (line.indexOf(HEADER_SEPARATOR) > -1)
      {
        headers = line.substring(0, line.indexOf(HEADER_SEPARATOR));
        if (verbose > Proxy.HUSH_HUSH) System.out.println("Headers [" + headers + "]");
        String[] headerArray = headers.split("\r\n");
        
        if (verbose > Proxy.HUSH_HUSH) System.out.println("--- Response Headers ---");
        for (int i=0; i<headerArray.length; i++)
        {
          if (verbose > Proxy.HUSH_HUSH) System.out.println(headerArray[i]);
          if (headerArray[i].startsWith("Content-Type: "))
          {
            String mType = headerArray[i].substring("Content-Type: ".length());
            textXml = (mType.indexOf("text/xml") > -1);
          }
        }
      }
      // Condition to rework the payload
      if (textXml && (line.indexOf("Bonjour") > -1))
      {
        if (verbose > Proxy.SHUT_UP) System.out.println("Reworking reply");
        payload = line.substring(line.indexOf(HEADER_SEPARATOR) + HEADER_SEPARATOR.length());
  //    String strPayloadLength = payload.substring(0, payload.indexOf("\r\n"));
  //    System.out.println("Length:" + strPayloadLength);
  //    int payloadLength = Integer.parseInt(strPayloadLength, 16);
  //    System.out.println("Length as int:" + payloadLength);
  
        payload = payload.substring(payload.indexOf("\r\n") + "\r\n".length()); // After the lenght in hexa
        if (payload.indexOf(PAYLOAD_TERMINATOR) > -1)
        {
          payload = payload.substring(0, payload.indexOf(PAYLOAD_TERMINATOR));
        }
        if (verbose > Proxy.HUSH_HUSH) System.out.println("Payload [" + payload + "]");
  
        // Here is the Skill, reverse the result!        
        try
        {
          XMLDocument doc = null;
          synchronized (parser)
          {
            parser.parse(new StringReader(payload));
            doc = parser.getDocument();
          }
          XMLElement loc = (XMLElement)doc.selectNodes("//return").item(0);
          String returnValue = loc.getFirstChild().getNodeValue();
//        System.out.println("Found [" + returnValue + "]");
          byte[] newba = new byte[returnValue.length()];
          byte[] oldba = returnValue.getBytes();
          // Reverse
          for (int i=0; i<oldba.length; i++)
            newba[oldba.length - (i+1)] = oldba[i];
          String newValue = new String(newba);
//        System.out.println("New value [" + newValue + "]");
          loc.getFirstChild().setNodeValue(returnValue + " - " + newValue);
          StringWriter sw = new StringWriter();
          doc.print(sw);
          payload = sw.getBuffer().toString();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();        
        }         
        // Done reworking, now recompose
        int newLength = payload.length();
        String strLen = Integer.toHexString(newLength).toLowerCase();
        while (strLen.length() < 4) strLen = "0" + strLen; // LPad
        line = headers + HEADER_SEPARATOR +
               strLen.toLowerCase() + "\r\n" +
               payload + PAYLOAD_TERMINATOR;
        
        reply = line.getBytes();
        if (verbose > Proxy.SHUT_UP)
        {
  //      System.out.println("After: bytes_read=" + reply.length);
          System.out.println("New reworked reply:");
          System.out.println("---------------------");
          System.out.println(line);
          System.out.println("---------------------");
        }
        else
          System.out.print(".");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return reply;
  }

  @Override
  protected boolean sendRequest(byte[] request)
  {
    boolean decision = true; // Default
    String req = new String(request);
//  System.out.println("Asking for [" + req + "]");
    // Manage behavior here
    if (req.startsWith("POST /greetings/GreetingPort HTTP/1.1") && req.indexOf("Oliv") > -1 && req.indexOf("FR") > -1)
    {
//    System.out.println("Asking for [" + req + "]");

      decision = false; // Hook!
      String altRespHeaders = 
        "HTTP/1.1 200 OK\r\n" + 
        "Date: Tue, 04 Aug 2009 15:19:57 GMT\r\n" + 
        "Transfer-Encoding: chunked\r\n" + 
        "Content-Type: text/xml;charset=\"utf-8\"\r\n" + 
        "X-ORACLE-DMS-ECID: 0000IB_zgjZBLA5xrOECSY1ASWsa0000Si\r\n" + 
        "X-Powered-By: Servlet/2.5 JSP/2.1";
      
      String altRespPayload =
        "<?xml version = '1.0' encoding = 'UTF-8'?>\n" + 
        "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + 
        "   <S:Body>\n" + 
        "      <ns2:sayHelloResponse xmlns:ns2=\"http://greetingsservice/\">\n" + 
        "         <return>Non mais t'as vu ta gueule?</return>\n" + 
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
    else if (req.startsWith("GET /merdalor HTTP/1.1"))
    {
//    System.out.println("Asking for [" + req + "]");

      decision = false; // Hook!
      String altRespHeaders = 
        "HTTP/1.1 200 OK\r\n" + 
        "Date: Tue, 04 Aug 2009 15:19:57 GMT\r\n" + 
        "Transfer-Encoding: chunked\r\n" + 
        "Content-Type: text/html;charset=\"utf-8\"\r\n" + 
        "X-ORACLE-DMS-ECID: 0000IB_zgjZBLA5xrOECSY1ASWsa0000Si\r\n" + 
        "X-Powered-By: Servlet/2.5 JSP/2.1";
      
      String altRespPayload =
        "<html>" + 
          "<body>" + 
            "<h1>Lost on the net?</h1>" +
            "<p>" +
            " No panic, we can help..." +
            "<br>" +
            " You are here --> +" +
            "</p>" +
          "</body>" + 
        "</html>";
      
      int newLength = altRespPayload.length();
      String strLen = Integer.toHexString(newLength).toLowerCase();
      while (strLen.length() < 4) strLen = "0" + strLen; // LPad
      String altResp = altRespHeaders + HEADER_SEPARATOR +
                       strLen.toLowerCase() + "\r\n" +
                       altRespPayload + PAYLOAD_TERMINATOR;
      
      setAlternateResponse(altResp);
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
      String remoteHost = args[0];
      int remoteport    = Integer.parseInt(args[1]);
      int localport     = Integer.parseInt(args[2]);
      // Print a start-up message
      System.out.println("Starting proxy for " + remoteHost + ":" + remoteport + " on local port " + localport);
      System.out.println("To stop the proxy, send it this GET request: http://localhost:" + localport + "/please-stop-proxy");
      // And start running the server
      TinyProxySample tps = new TinyProxySample();
      tps.setVerbose(Proxy.CHATTERBOX);
      tps.run(localport, remoteHost, remoteport, 3000); 
    }
    catch (Exception e)
    {
//    System.err.println(e);
      e.printStackTrace();
      System.err.println("Usage: java TinyProxySample <remotehost> <remoteport> <localport>");
    }
  }  
}
