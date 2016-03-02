package testing.util.http;

import java.io.InputStreamReader;

import java.io.Reader;

import java.net.BindException;
import java.net.URL;
import java.net.URLConnection;

import util.httputil.TinyHTTPServer;

public class WaitAndKill
{
  private Worker worker = null;
  private String httpPort = "6666";  

  private HttpServerThread httpServer = null;
  
  private boolean verbose = true;
  private final JobFlag jf = new JobFlag(false);
  
  private final Thread parent = Thread.currentThread();
  
  public WaitAndKill(Worker worker, String port)
  {
    this.worker = worker;
    this.httpPort = port;
  }
  
  public void startWorking() throws Exception
  {  
    try
    {
      httpServer = new HttpServerThread(parent, this.httpPort);
      httpServer.start(); // HTTP Server now ready for duty
    }
    catch (Exception e)
    {
      throw e;
    }        
    
    synchronized (parent) 
    { 
      final Worker localWorker = worker;
      // Start worker in a thread
      Thread workerThread = new Thread()
        {
          public void run()
          {
            localWorker.proceed();
            if (!jf.getValue())
            {
              synchronized (parent)
              {
                try 
                { 
                  if (httpServer != null && httpServer.isAlive())
                  {
                    System.out.println("Notifying the HTTP Thread.");
                    httpServer.interrupt();
                  }
                }
                catch (Exception ie) 
                { 
                  System.err.println("HTTP Thread Interruption:");              
                  ie.printStackTrace();
                }
              }
            }
            jf.setValue(true);
          }
        };
      workerThread.start();
      parent.wait(); 
      System.out.println("Parent resuming");
      if (jf.getValue())
      {
        System.out.println("Worker has finished his work (not to be killed)");
      }
      else
      {
        System.out.println("Worker to be interrupted");
        worker.interrupt();
      }
    }
  }

  public void killThemAll()
  {
    if (!jf.getValue())
    {
      worker.interrupt();
      synchronized (parent)
      {
        try 
        { 
          if (httpServer != null && httpServer.isAlive())
          {
            System.out.println("Notifying the HTTP Thread.");
            httpServer.interrupt();
          }
        }
        catch (Exception ie) 
        { 
          System.err.println("HTTP Thread Interruption:");              
          ie.printStackTrace();
        }
      }
    }
    
  }
  
  public boolean isDoneWorking()
  {
    return jf.getValue();
  }

  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }

  private class JobFlag
  {
    boolean b = false;
    public JobFlag(boolean b)
    { this.b = b; }
    public void setValue(boolean b)
    { this.b = b; }
    public boolean getValue() { return this.b; }
  }

  private class HttpServerThread extends Thread  
  {
    private boolean before = true;
    private String httpServerPort = "6666";
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
        ths = new TinyHTTPServer(httpServerPort, 
                                 null, 
                                 terminateAfterFirstRequest, 
                                 verbose, 
                                 startServerNow);
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
        ths.runServer();
        before = false;
        if (verbose) System.out.println("HTTP Server: Got notified");
        if (parent != null)
        {
          if (verbose) System.out.println("HTTP Server: Notifying parent thread.");
          synchronized (parent) { parent.notify(); }
        }
        else
          System.out.println("Parent Thread is null");
      }
      catch (Exception ex)
      {              
        System.out.println("From HTTP Server thread:" + ex.toString());
  //    System.exit(1);
      }
      finally
      {
        if (verbose) System.out.println("HTTP Thread terminated.");
      }
    } 
    
    public void interrupt()
    {
      super.interrupt();
      if (verbose) System.out.println("HTTP Thread (port " + httpServerPort + ") interrupted");
      // Release the thread
      if (before)
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
          System.out.println("Failed to release the http server:" + ex.toString());
        }
      }
    }
  }
}
