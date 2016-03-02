package testing.util.http;

import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;
import java.net.URLConnection;

public class Terminator
{
  private String httpServerPort = "6666";
  private static boolean verbose = false;
  
  public void interrupt()
  {
    try
    {
      // Write some content to release it smoothly...
      URL url = new URL("http://localhost:" + httpServerPort + "/exit");
      URLConnection conn = url.openConnection();
      Reader r = new InputStreamReader(conn.getInputStream());
      r.read();
    }
    catch (Exception ex)
    {
      if (verbose)
        System.out.println("Failed to release the http server:" + ex.toString());
    }
  }
  
  public static void main(String[] args)
  {
    Terminator terminator = new Terminator();
    try
    {
      for (int i=0; i<args.length; i++)
      {
        if ("-port".equals(args[i]))
          terminator.httpServerPort = args[i+1];
        else if ("-verbose".equals(args[i]))
          verbose = ("true".equals(args[i+1]) || "yes".equals(args[i+1]) || "on".equals(args[i+1]));
      }
    }
    catch (Exception ex)
    {
      System.out.println("Something sux:" + ex.toString());  
    }
    terminator.interrupt();
  }
}
