package util.httputil.proxyimpl;

import java.io.IOException;

import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

public class StopProxy
{
  public static void main(String[] args) throws Exception
  {
    try
    {
      // Check the number of arguments
      if (args.length !=1)
        throw new IllegalArgumentException("Wrong number of arguments.");

      // Get the command-line arguments: the host and port we are proxy for
      // and the local port that we listen for connections on
      int localport = Integer.parseInt(args[0]);
      // Print a start-up message
      System.out.println("Stopping proxy on port " + localport);
      String httpRequest = "http://localhost:" + localport + "/please-stop-proxy";
      URL url = new URL(httpRequest);
      URLConnection connection = url.openConnection();
      connection.connect();
      Object o = connection.getContent();
    }
    catch (IllegalArgumentException e)
    {
      System.err.println("Usage: java StopProxy <localport>");
    }
    catch (ConnectException ex)
    {
      System.out.println("Proxy is down.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
