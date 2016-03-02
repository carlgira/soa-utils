package util.httputil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.Date;

/**
 * <p>
 * Dedicated HTTP Server.<br>
 * This is <b><i>NOT</i></b> J2EE Compliant, not even CGI.
 * </p>
 * <p>
 * Runs the communication between an HTTP client and the
 * features of the Data server to be displayed remotely
 * </p>
 *
 * @author olivier.lediouris@oracle.com
 */
public class TinyHTTPServer
{
  private String postContent = "", getContent = "";
  private ServerSocket ss = null;
  private boolean verbose = false;
  private boolean exitAfterFirstRequest = false;
  private HTTPRequestHandler hrh = null;
  private String port = "";
  
  /**
   * <p>
   * Default port is 6666<br>
   * Default behavior is <b>not</b> to exit after the first request<br>
   * Default machineName is localhost
   * </p>
   * Port and Server name can be overridden by using system properties:
   * <ul>
   *   <li>http.port</li>
   *   <li>http.host</li>
   * </ul>
   */
  public TinyHTTPServer()
    throws BindException, Exception
  {
    this("6666", false, false);
  }

  public TinyHTTPServer(String port, 
                        boolean exitAfterFirstRequest,
                        boolean verbose)
    throws BindException, Exception
  {
    this(port, null, exitAfterFirstRequest, verbose);
  }

  public TinyHTTPServer(String port, 
                        boolean exitAfterFirstRequest)
    throws BindException, Exception
  {
    this(port, exitAfterFirstRequest, false);
  }

  public TinyHTTPServer(String port, 
                        HTTPRequestHandler hrh,
                        boolean exitAfterFirstRequest, 
                        boolean verbose)
    throws BindException, Exception
  {
    this(port, hrh, exitAfterFirstRequest, verbose, true);
  }
  
  public TinyHTTPServer(String port, 
                        HTTPRequestHandler hrh,
                        boolean exitAfterFirstRequest, 
                        boolean verbose,
                        boolean start)
    throws BindException, Exception
  {
    // Bind the server
    String machineName = "localhost";

    machineName = System.getProperty("http.host", machineName);
    port = System.getProperty("http.port", port);
    
    this.port = port;
    this.hrh = hrh;
    this.exitAfterFirstRequest = exitAfterFirstRequest;
    this.verbose = verbose;

    if (verbose)
    {
      System.out.println("HTTP Host:" + machineName);
      System.out.println("HTTP Port:" + port);
    }

    int _port = 0;
    try
    {
      _port = Integer.parseInt(port);
    }
    catch (NumberFormatException nfe)
    {
      throw nfe;
    }

    try
    {
      ss = new ServerSocket(_port);
      if (start)
        this.runServer();
    }
    catch (BindException be)
    {
      throw be;
    }
    catch (Exception e)
    {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  public void runServer() throws Exception
  {
    // Reading loop
    try
    {
      boolean keepLooping = true;
      while (keepLooping)
      {
        if (verbose)
          System.out.println("HTTP Server about to accept...");
        Socket client = ss.accept();
        if (verbose)
          System.out.println("Client socket accepted (port " + port + ").");
        InputStream is = client.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

        out.print("HTTP/1.1 202 \r\n"); // Ack, even for async!
        out.print("Content-Type: text/xml\r\n");
//      out.print("Connection: close\r\n");
        out.print("\r\n"); // End Of Header
        out.flush();

        String line;
        boolean post = false;
        int contentLength = -1;
        if (verbose) 
          System.out.println("============= HTTP traffic begins ===================");
        while ((line = in.readLine()) != null)
        {
          if (verbose)
            System.out.println(/*"[" +*/ line /*+ "]"*/);
          if (line.toUpperCase().startsWith("CONTENT-LENGTH:")) // oc4j "Content-length:", wls "Content-Length:"
          {
            contentLength = Integer.parseInt(line.substring("Content-length:".length() + 1));
//          if (verbose)
//            System.out.println("Content Length:" + contentLength);
          }
          if (line.length() == 0)
          {
    //      System.out.println("Read []");
            break;
          }
          else if (line.startsWith("POST /exit") ||
                   line.startsWith("GET /exit"))
          {
            if (verbose)
              System.out.println("Received an exit signal");
            keepLooping = false;
            out.println("Bye now");
          }
          else if (line.startsWith("POST /help") ||
                   line.startsWith("GET /help"))
          {
            if (verbose)
              System.out.println("Received an help request");
    //          help = true;
          }
          else
          {
    //          System.out.println("Read:[" + line + "]");
            if (line.startsWith("POST "))
            {
//            if (verbose)
//              System.out.println("This is a POST request.");
              post = true;
            }
            else if (line.startsWith("GET "))
            {
//            if (verbose)
//              System.out.println("This is a GET request.");   
              getContent = line.substring("GET ".length());
            }
          }
        }

        // POST ?
        if (post)
        {
//        System.out.println("Trying to read POST Data");
          if (/*false &&*/ contentLength != -1) 
          {
            char[] buff = new char[contentLength];
            try
            {
              if (verbose) System.out.println("Available:" + is.available());
              int offset = 0;
              int num = 0;
              boolean keepReading = true;
              postContent = "";
              while (offset < contentLength && keepReading)
              {
                num = in.read(buff);
                postContent += new String(buff, 0, num);
                if (verbose) 
                  System.out.println("HTTP Thread got a response:\n" + postContent + "\n" + Integer.toString(num) + " byte(s) read.");
                offset += num;
              }
            }
            catch (SocketException se)
            {
              System.out.println(">> " + se.toString());
            }
          }
          else
          {
            StringBuffer sb = new StringBuffer();
            String returnedLine;
            boolean ok = true;
            long before = 0L, after = 0L;
            while (ok)
            {
              try
              {
                before = new Date().getTime();
                returnedLine = in.readLine();
                after = new Date().getTime();
                if (returnedLine == null)
                {
                  ok = false;
                  if (false && verbose)
                    System.out.println("Done reading");
                }
                else
                {
                  sb.append(returnedLine);
                  if (false && verbose)
                  {
                    System.out.println("ReturnedLine [" + returnedLine + "]");
                    System.out.println("So far:[" + sb.toString() + "]");
                  }
                  if (verbose && (after - before) > 0L)
                  {
                    System.out.println("Line read in " + Long.toString(after - before) + " ms. (now " + sb.length() + " byte(s) long)");
                  }
                }
              }
              catch (SocketException se)
              {
                System.out.println("Socket Closed...");
                ok = false;
              }
              catch (Exception ex)
              {
                System.out.println(ex.toString());
                ok = false;
              }
            }
            postContent = sb.toString();
            if (verbose)
              System.out.println(postContent);
          }
          if (hrh != null)
            hrh.onMessage(postContent);
        }
        else // GET
        {
          if (hrh != null)
            hrh.onMessage(getContent);          
        }
        if (verbose) 
          System.out.println("============== HTTP traffic ends ====================");
        try { out.close(); } catch (Exception ignore) {}
        try { in.close(); } catch (Exception ignore) {}
        try { client.close(); } catch (Exception ignore) {}

        if (exitAfterFirstRequest)
          keepLooping = false;
      }
      ss.close();
    }
    catch (BindException be)
    {
      throw be;
    }
    catch (Exception e)
    {
      System.err.println(e.toString());
      e.printStackTrace();
    }
    finally
    {
      if (verbose)
        System.out.println("Exiting HTTP Server");
    }    
  }

  public String getPostContent()
  {
    return postContent;
  }

  public static void postContent(String machine,
                                 String port,
                                 String content) throws Exception
  {
    try
    {
      Socket socket = new Socket(machine, Integer.parseInt(port));
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
      bw.write(content);
      bw.flush();
      socket.close();
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
  /**
   * For tests
   *
   * @param args see usage
   */
  public static void main(String[] args)
    throws Exception
  {
    new TinyHTTPServer();
  }
}
