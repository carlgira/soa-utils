package unittests.patterns.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;

import static org.junit.Assert.*;
import org.junit.Test;

import testing.event.EventSender;

import testing.util.GnlUtilities;

import unittests.util.Utilities;

public abstract class RaiseEvent extends TestCase implements RaiseEventInterface
{
  protected boolean verbose = false;
  protected Properties props = new Properties();
  protected EventSender es = null;
  protected String projectDirectory = ".";
  
  protected String userName = null;
  protected String password = null;
    
  @Test
  public void testRaiseBusinessEvent()
  {
    try
    {
      projectDirectory = System.getProperty("project.directory", ".");
      verbose = "true".equals(System.getProperty("verbose", "false"));
      String propertiesFilename = System.getProperty("properties.file.name", null);
      raiseBusinessEvent(projectDirectory, propertiesFilename, verbose);   
      afterRaisingEvent();
    }
    catch (Exception ex)
    {
      fail(ex!=null?ex.toString():"Null Exception");
    }
    finally
    {
      if (verbose) System.out.println("Test is finished.");
    }
  }

  public void raiseBusinessEvent(String projectDirectory, 
                                 String propertiesFilename, 
                                 boolean verbose) throws Exception
  {
    try
    {
      es = new EventSender();
      es.setVerbose(verbose);
      
      final Properties masterProps = Utilities.readMasterProperties(projectDirectory);

      if (propertiesFilename == null)
        fail("System Property \"properties.file.name\" should be set.");
      File propertiesFile = new File(projectDirectory, propertiesFilename);
      if (!propertiesFile.exists())
      {
        String cpFileName = GnlUtilities.searchAlongClasspath(propertiesFilename);
        if (cpFileName.trim().length() == 0)
          fail("\"" + propertiesFilename + "\" not found from \"" + System.getProperty("user.dir") + "\", nor along your classpath");
        else
        {
          propertiesFilename = cpFileName;
          propertiesFile = new File(cpFileName);
        }
      }
      else
        propertiesFilename = propertiesFile.getAbsolutePath();

      if (verbose)
        System.out.println("Loading Test Data from [" + propertiesFilename + "]");
      props.load(new FileInputStream(propertiesFilename));
      
      ArrayList<Properties> pa = new ArrayList<Properties>(2);
      pa.add(masterProps);
      pa.add(System.getProperties());
      props = Utilities.patchProperties(props, pa, true);
            
      // Is there security to apply?
      userName = props.getProperty("security.username", null);
      password = props.getProperty("security.password", null);
      if (userName != null || password != null)
      {
        if (password == null || userName == null)
          fail("you must provide username and password, or none.");
        else
        {
          // Check if System variables are set
          // oracle.security.jps.config set to ./security/config/jps-config.xml
          String jpsConfig  = System.getProperty("oracle.security.jps.config", null);
          if (jpsConfig == null)
          {
            fail("You need to provide oracle.security.jps.config as System variable.");
          }
          else
          {
            if (verbose)
              System.out.println("oracle.security.jps.config = " + jpsConfig);
            try
            {
              File f = new File(projectDirectory, jpsConfig);
              if (f.exists())
              {
                if (verbose)
                  System.out.println("jps-config: found.");
                System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
              }
              else if (verbose)
                System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
            }
            catch (Exception ex)
            {
              System.out.println("Security:" + ex.toString());
            }
          }
        }
      }

      String providerURL = props.getProperty("provider.url");
      
      providerURL = GnlUtilities.replaceString(providerURL, "${protocol.4.rmi}", masterProps.getProperty("protocol.4.rmi", "t3"));
      providerURL = GnlUtilities.replaceString(providerURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
      providerURL = GnlUtilities.replaceString(providerURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));
      props.setProperty("provider.url", providerURL);
            
      String eventPayload = "";
      String payloadFileName = props.getProperty("event.payload.file", null);
      String payloadAsString = props.getProperty("event.payload.as.string", null);
      if (payloadFileName == null && payloadAsString == null)
        throw new Exception("Need an event payload (event.payload.file or event.payload.as.string)");
      
      if (payloadFileName != null)
      {
        BufferedReader br = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        { 
          line = br.readLine(); 
          if (line != null)
            eventPayload += line;
          else
            keepReading = false;
        }
        br.close();
      }
      else
        eventPayload = payloadAsString;
      
      if (verbose) 
        System.out.println("Event Content:\n" + eventPayload);
      try
      {
        // Build jndi props
        Properties jndiProps = new Properties();
        jndiProps.setProperty("java.naming.security.principal",   props.getProperty("admin.user", "weblogic"));
        jndiProps.setProperty("java.naming.security.credentials", props.getProperty("admin.password", "welcome1"));
        jndiProps.setProperty("java.naming.provider.url",         props.getProperty("provider.url"));
        jndiProps.setProperty("java.naming.factory.initial",      "weblogic.jndi.WLInitialContextFactory");
        jndiProps.setProperty("dedicated.connection",             "true");
        
        String eventName = props.getProperty("event.name", null);
        String eventNameSpace = props.getProperty("event.tns", null);
        if (eventName == null)
          throw new Exception("No event.name property provided");    
        
        eventPayload = beforeRaisingEvent(eventPayload);
        
        if (verbose)
        {
          System.out.println("Properties:");
          System.out.println("java.naming.security.principal  : " + jndiProps.getProperty("java.naming.security.principal"));
          System.out.println("java.naming.security.credentials: " + jndiProps.getProperty("java.naming.security.credentials"));
          System.out.println("java.naming.provider.url        : " + jndiProps.getProperty("java.naming.provider.url"));
        }
        // The Core
        es.connectAndSend(jndiProps, 
                          eventName, 
                          eventNameSpace, 
                          new StringReader(eventPayload),
                          userName!=null?userName.trim():userName,
                          password!=null?password.trim():password);    
      }
      catch (Exception ex)
      {
        if (verbose)
        {
          System.err.println("Ooch!");
    //    System.err.println(ex.toString());
          ex.printStackTrace();
        }
        throw ex;
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
  
  public String beforeRaisingEvent(String payload)
  {
    return payload;
  }

  public abstract void afterRaisingEvent();  
}
