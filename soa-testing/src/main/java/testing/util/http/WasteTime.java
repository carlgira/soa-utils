package testing.util.http;

public class WasteTime
{
  private static long wait = 1000L;
  
  public static void main(String[] args)
  {
    try
    {
      for (int i=0; i<args.length; i++)
      {
        if ("-wait".equals(args[i]))
          wait = Long.parseLong(args[i+1]);
      }
    }
    catch (Exception ex)
    {
      System.out.println("Something sux:" + ex.toString());  
    }
    try { Thread.sleep(wait); } catch (InterruptedException ie) {}
  }
}
