package testing.util.http;

public class SampleMain
{
  public SampleMain()
  {
    super();
  }

  private WaitAndKill wak        = null;
  private static boolean verbose = false;
  private static boolean timeout = false;
  private static int nbLoops     = 10;

  public void launchIt(final String port)
  {
    try
    {
      final Worker worker = new Worker()
      {
        private boolean b = true;
        public void proceed()
        {
          for (int i=0; b && i<nbLoops; i++)
          {
            System.out.println("Working...");
            try { Thread.sleep(1000L); } catch (Exception tex) {}
          }
          System.out.println("Worker has completed its job (" + (b?"not ":"") + "interrupted)");
        }
        
        public void interrupt()
        {
          System.out.println("Work interruption was requested");
          b = false;
        }
      };
      
      Thread delegate = new Thread()
        {
          public void run()
          {
            try 
            { 
              wak = new WaitAndKill(worker, port); 
              wak.setVerbose(verbose);
              wak.startWorking();
            } 
            catch (Exception ex) { ex.printStackTrace(); }
          }
        };
      delegate.start();

      /*
       * Timeout implementation, example
       */
      Thread monitor = new Thread()
        {
          public void run()
          {
            try { Thread.sleep(3000L); } catch (InterruptedException ie) {}
            System.out.println("Monitor wakes up");
            if (wak != null)
            {
              System.out.println("WaK not null...");
              if (!wak.isDoneWorking())
              {
                System.out.println("Terminating whatever's moving.");
                wak.killThemAll();
              }
              else
                System.out.println("WaK was done working...");
            }
            System.out.println("Monitor ending");
          }
        };
      if (timeout)
        monitor.start();

      System.out.println("Main Completed. Everyone's working.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static void main(String[] args)
  {
    SampleMain sm = new SampleMain();        
    String port = "2345";
    try
    {
      for (int i=0; i<args.length; i++)
      {
        if ("-port".equals(args[i]))
          port = args[i+1];
        else if ("-verbose".equals(args[i]))
          verbose = ("true".equals(args[i+1]) || "yes".equals(args[i+1]) || "on".equals(args[i+1]));
        else if ("-timeout".equals(args[i]))
          timeout = ("true".equals(args[i+1]) || "yes".equals(args[i+1]) || "on".equals(args[i+1]));
        else if ("-nbloop".equals(args[i]))
          nbLoops = Integer.parseInt(args[i+1]);
      }
    }
    catch (Exception ex)
    {
      System.out.println("Something sux:" + ex.toString());  
    }
    sm.launchIt(port);
  }
}
