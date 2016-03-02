package unittests.patterns.event;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.junit.Test;
//import static org.junit.Assert.*;

public class HosunEventSample extends RaiseEvent
{
  @Override
  public void afterRaisingEvent()
  {
    System.out.println("Test finished.");
  }

  /**
   * That is for tests
   * @param args
   */
  public static void main1(String[] args)
  {
    RaiseEvent re = new HosunEventSample();
//  System.setProperty("edn.debug.event-connection", "true");
    System.setProperty("verbose", "true");
    System.setProperty("oracle.security.jps.config", "./security/config/jps-config.xml");
    System.setProperty("properties.file.name", "event.sophia.properties");
//  System.setProperty("properties.file.name", "event.generic.properties");
    
    System.out.println("Running:" + re.toString());
    
    re.testRaiseBusinessEvent();
  }
}
