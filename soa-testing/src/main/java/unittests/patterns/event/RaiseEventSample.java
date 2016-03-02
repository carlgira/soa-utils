package unittests.patterns.event;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.junit.Test;
//import static org.junit.Assert.*;

public class RaiseEventSample extends RaiseEvent
{
  @Override
  public void afterRaisingEvent()
  {
    System.out.println("Now I am doing some stuff");
    
    System.out.println("Bonus: some.system.variable=" + System.getProperty("some.system.variable", "Not Set"));
    
    Date now = new Date();
    if (false && now.getTime() % 2 == 0)
      fail("Bad time...");
    else
      System.out.println("Test finished at " + now.getTime());
  }

  /**
   * That is for tests
   * @param args
   */
  public static void main(String[] args)
  {
    RaiseEvent re = new RaiseEventSample();
//  System.setProperty("edn.debug.event-connection", "true");
    System.setProperty("verbose", "true");
    System.setProperty("oracle.security.jps.config", "./security/config/jps-config.xml");
    System.setProperty("properties.file.name", "event.sophia.properties");
//  System.setProperty("properties.file.name", "event.generic.properties");
    
    System.out.println("Running:" + re.toString());
    
    re.testRaiseBusinessEvent();
  }

  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd 'at' HH:mm:ss");
  
  @Override
  public String beforeRaisingEvent(String payload)
  {
    String newPayload = payload.replaceAll("\\$\\{current.date\\}", sdf.format(new Date()));
    return newPayload;
  }
}
