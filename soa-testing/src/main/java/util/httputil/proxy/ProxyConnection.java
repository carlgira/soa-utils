package util.httputil.proxy;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;

public class ProxyConnection extends Thread
{
  Socket fromClient;
  String host;
  int port;
  long timeout;

  Proxy parent = null;
  boolean bypassRequest = false;

  ProxyConnection(Proxy proxy, Socket s, String host, int port, long timeout)
  {
    this.parent = proxy;
    fromClient = s;
    this.host = host;
    this.port = port;
    this.timeout = timeout;
  }

  public void run()
  {
    InputStream clientIn = null;
    OutputStream clientOut = null;
    InputStream serverIn = null;
    OutputStream serverOut = null;
    Socket toServer = null;
    int r0 = -1, r1 = -1, ch = -1, i = -1;
    long time0 = System.currentTimeMillis();
    long time1 = time0;
    try
    {
      toServer = new Socket(host, port);
      if (parent.getVerbose() > Proxy.SHUT_UP) Proxy.display("open connection to:" + toServer + "(timeout=" + timeout + " ms)");
      
      clientIn = fromClient.getInputStream();
      clientOut = new BufferedOutputStream(fromClient.getOutputStream());
      
      serverIn  = toServer.getInputStream();
      serverOut = new BufferedOutputStream(toServer.getOutputStream());
      
      while (!bypassRequest && (r0 != 0 || r1 != 0 || (time1 - time0) <= timeout))
      {
        ByteArrayOutputStream clientRequestBAOS = new ByteArrayOutputStream();
        while ((r0 = clientIn.available()) > 0)
        {
          if (parent.getVerbose() > Proxy.NORMAL)
          {
            Proxy.println("");
            Proxy.println("<<<" + r0 + " bytes from client");
          }
          for (i = 0; i < r0; i++)
          {
            ch = clientIn.read();
            if (ch != -1)
            {
              clientRequestBAOS.write(ch);
//            serverOut.write(ch);
              if (parent.getVerbose() > Proxy.VERBOSE) 
                Proxy.print(ch);
            }
            else
            {
              if (parent.getVerbose() > Proxy.HUSH_HUSH) 
                Proxy.display("client stream closed");
            }
          }
          time0 = System.currentTimeMillis();
          // Bypass ?
          byte[] request = clientRequestBAOS.toByteArray();
          if (parent.sendRequest(request))
          {
            // Regular behavior
            bypassRequest = false;
            serverOut.write(request);
            serverOut.flush();
          }
          else
          {
            // Manage Alternate Response
            bypassRequest = true;
            String altResponse = parent.getAlternateResponse();
            if (altResponse != null && parent.getConversationMode() == Proxy.SYNCHRONOUS_RESPONSE_REQUIRED) // Synchronous
            {
              // In case of a Synchronous reply, the altResponse contains already everything (that's the default)
              // There is no need to rework anything. Send it back now
              clientOut.write(altResponse.getBytes());
              clientOut.flush();
            }
            else if (altResponse != null && parent.getConversationMode() == Proxy.ASYNCHRONOUS_RESPONSE_REQUIRED) // ASynchronous two ways
            {
              // Ack Client Request (HTTP 200)           
              clientOut.write(Proxy.RESPONSE_FOR_ASYNC_REQUEST.getBytes());
              clientOut.flush();
              
              // Callback. Assuming the operation is the name of the response element. Questionable...
              String operation = "";
              try
              {
                synchronized (parent.parser)
                {
                  parent.parser.parse(new StringReader(parent.getResponsePayloadContent()));
                  operation = parent.parser.getDocument().getDocumentElement().getNodeName();
                }
              }
              catch (Exception ex)
              {
                ex.printStackTrace();
              }
              // postCallback will rework header and envelope
              parent.postCallback(parent.getRequestPayload(), 
                                  parent.getResponsePayloadContent(), 
                                  operation);
            }
            else // Assuming it is an ASYNC-ONE_WAY. TASK Test this.
            {
              // Ack Client Request (HTTP 200). And absorb.         
              clientOut.write(Proxy.RESPONSE_FOR_ASYNC_REQUEST.getBytes());
              clientOut.flush();
            }
          }
        } // Finished to read the client request
                
        // Read server response if required (ie synchronous)
        ByteArrayOutputStream serverResponseBAOS = new ByteArrayOutputStream();
        while (!bypassRequest && (r1 = serverIn.available()) > 0)
        {
          if (parent.getVerbose() > Proxy.NORMAL)
          {
            Proxy.println("");
            Proxy.println(">>>" + r1 + " bytes from server");
          }
          for (i = 0; i < r1; i++)
          {
            ch = serverIn.read();
            if (ch != -1)
            {
              serverResponseBAOS.write(ch);
//            clientOut.write(ch);
              if (parent.getVerbose() > Proxy.VERBOSE) 
                Proxy.print(ch);
            }
            else
            {
              if (parent.getVerbose() > Proxy.HUSH_HUSH) 
                Proxy.display("server stream closed");
            }
          }
          time0 = System.currentTimeMillis();
          // Rework response here?
          byte[] response = serverResponseBAOS.toByteArray();
          response = parent.onResponse(response);
          
          clientOut.write(response);
          clientOut.flush();
        }
        if (r0 == 0 && r1 == 0)
        {
          time1 = System.currentTimeMillis();
          Thread.sleep(100);
          //Proxy.display("waiting:"+(time1-time0)+" ms");
        }
      }
    }
    catch (Throwable t)
    {
      Proxy.display("i=" + i + " ch=" + ch);
      t.printStackTrace(System.err);
    }
    finally
    {
      try
      {
        clientIn.close();
        clientOut.close();
        serverIn.close();
        serverOut.close();
        fromClient.close();
        toServer.close();
        Proxy.quit(time1 - time0);
      }
      catch (Exception e)
      {
        e.printStackTrace(System.err);
      }
    }
  }
}

