package util.httputil.proxyimpl;

import java.io.IOException;

import java.net.BindException;

import util.httputil.proxy.Proxy;

/*
 * Shows how to trap some requests.
 */
public class NeutralProxy
  extends Proxy
{
  private static Proxy instance = null;
  
  public NeutralProxy()
  {
    super();
    instance = this;
  }
  
  @Override
  protected boolean sendRequest(byte[] request)
  {
    boolean decision = true; // Default
    String req = new String(request);
    if (req.startsWith("GET /are-you-up"))
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
      setAlternateResponse(altResp);   // Trap & Substitute response
    }
    else if (req.startsWith("GET /please-stop-proxy"))
    {
      System.out.println("Stop requested.");
      decision = false;
      String altRespHeaders = 
        "HTTP/1.1 200 OK\r\n" + 
        "Content-Type: text/html;charset=\"utf-8\"\r\n" + 
        "X-Powered-By: The A-Team";
      
      String altRespPayload =
      "<html>" + 
        "<head><title>Provided by the A-Team</title></head>" +
        "<body>" + 
          "<h1>Bye-bye</h1>" +
        "</body>" + 
      "</html>";
      
      String altResp = altRespHeaders + HEADER_SEPARATOR + altRespPayload;
      
      setAlternateResponse(altResp);   // Trap & Substitute response
      this.setKeepListening(false);    // Done listening. Stop.
    }
    else
    {
      System.out.println("Forwarding request [\n" + req + "\n] to " + this.getHostName() + ":" + this.getHostPort());
    }
    return decision;
  }

  public static void main(String[] args)
    throws IOException
  {
    try
    {
      // Check the number of arguments
      if (args.length != 3)
        throw new IllegalArgumentException("Wrong number of arguments.");

      // Get the command-line arguments: the host and port we are proxy for
      // and the local port that we listen for connections on
      String remoteHost = args[0];
      int remoteport = Integer.parseInt(args[1]);
      int localport = Integer.parseInt(args[2]);
      // Print a start-up message
      // And start running the server
      NeutralProxy tps = new NeutralProxy();
      tps.setVerbose(Proxy.CHATTERBOX);
      boolean keepTrying = true;
      while (keepTrying)
      {
        try
        {
          System.out.println("Starting proxy for " + remoteHost + ":" + remoteport + " on port " + localport);
          System.out.println("To stop the proxy, send it this GET request: /please-stop-proxy");
          tps.run(localport, remoteHost, remoteport, 3000);
          keepTrying = false;
        }
        catch (BindException be)
        {
          localport++;
        }
      }
    }
    catch (Exception e)
    {
      //    System.err.println(e);
      e.printStackTrace();
      System.err.println("Usage: java NeutralProxy <remotehost> <remoteport> <localport>");
    }
  }
}
