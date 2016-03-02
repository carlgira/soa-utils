package util.soautil;

import java.io.FileInputStream;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import oracle.soa.management.facade.ComponentInstance;
import oracle.soa.management.facade.Locator;
import oracle.soa.management.facade.LocatorFactory;
import oracle.soa.management.util.ComponentInstanceFilter;

import testing.util.GnlUtilities;


public class ProcessUtil implements Serializable
{
  @SuppressWarnings("compatibility:1504963717958357498")
  private final static long serialVersionUID = 1L;
  
  private static Locator locator = null;
  private boolean verbose = true;
  
  public ProcessUtil() 
  { }

  public ProcessUtil(boolean verb) 
  { this.verbose = verb; }
  
  public void init(String serverPropFileName) throws Exception
  {
    try
    {
      if (verbose) System.out.println("Creating Locator...");
      Properties jndiProps = readPropFile(verbose, serverPropFileName);
      locator = LocatorFactory.createLocator(jndiProps);
      if (verbose) System.out.println("Locator created.");

//    CompositeDN compositeDN = new CompositeDN("default", COMPOSITE_NAME, "1.0");
//    Composite composite = locator.lookupComposite(compositeDN);
    }
    catch (Exception e)
    {
      System.err.println("Error Initializing Connections:");
      throw e;
    }
  }

  /**
   *  Example:
   *      java.naming.security.principal=weblogic
   *      java.naming.security.credentials=weblogic
   *      java.naming.provider.url=t3://machine.us.oracle.com:8001
   *      java.naming.factory.initial=weblogic.jndi.WLInitialContextFactory
   *
   */
  private static Properties readPropFile(boolean verbose, String serverPropFileName) throws Exception
  {
    final Properties masterProps = new Properties();
    masterProps.load(new FileInputStream("master.properties"));

    Properties props = new java.util.Properties();
    props.load(new FileInputStream(serverPropFileName));    
    // Patch the server URL: java.naming.provider.url=t3://${proxy.name}:${soa.port.number}
    String provider = props.getProperty("java.naming.provider.url");
    provider = GnlUtilities.replaceString(provider, "${proxy.name}",      masterProps.getProperty("proxy.name",      "localhost"));
    provider = GnlUtilities.replaceString(provider, "${soa.port.number}", masterProps.getProperty("soa.port.number", "8001"));
    provider = GnlUtilities.replaceString(provider, "${protocol.4.rmi}",  masterProps.getProperty("protocol.4.rmi",  "t3"));
    provider = GnlUtilities.replaceString(provider, "${machine.name}",    masterProps.getProperty("machine.name",    "localhost"));
    provider = GnlUtilities.replaceString(provider, "${soa.port.number.for.jndi.lookup}", masterProps.getProperty("soa.port.number.for.jndi.lookup","8001"));
    props.setProperty("java.naming.provider.url", provider);
    
    String user = props.getProperty("java.naming.security.principal");
    user = GnlUtilities.replaceString(user, "${admin.user}", masterProps.getProperty("admin.user", "weblogic"));
    props.setProperty("java.naming.security.principal", user);
    String pwd = props.getProperty("java.naming.security.credentials");
    pwd = GnlUtilities.replaceString(pwd, "${admin.password}", masterProps.getProperty("admin.password", "weblogic"));
    props.setProperty("java.naming.security.credentials", pwd);
    
  //  System.out.println("Will send event to " + props.getProperty("java.naming.provider.url"));
    
    // Just to make sure
    if (verbose)
    {
      Enumeration e = props.keys();        
      while (e.hasMoreElements())
      {
        String s = (String)e.nextElement();
        System.out.println("Prop [" + s + "] = [" + props.getProperty(s) + "]");
      }
    }
    return props;
  }
  
  public String[] countProcesses(String compositeName)
  {
    List<String> instances = new ArrayList<String>();
    try
    {
      ComponentInstanceFilter filter = new ComponentInstanceFilter();
      filter.setCompositeName(compositeName);
      List<ComponentInstance> insts = locator.getComponentInstances(filter);
      for (ComponentInstance ci : insts)
        instances.add(ci.getCompositeInstanceId());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return instances.toArray(new String[instances.size()]);
  }

  public String getAuditTrail(String bpelProcessName, String instanceId)
  {
    String auditTrail = null;
    try
    {
      ComponentInstanceFilter filter = new ComponentInstanceFilter();
      filter.setCompositeInstanceId(instanceId);
      List<ComponentInstance> insts = locator.getComponentInstances(filter);

      for (ComponentInstance ci: insts)
      {
        if (ci.getComponentName().equals(bpelProcessName))
        {
          Object o = ci.getAuditTrail();
          if (o instanceof String)
          {
            auditTrail = (String) o;
            break;
          }
        }
        else
          if (verbose) System.out.println("Leaving Component [" + ci.getComponentName() + "] (" + ci.getCompositeInstanceId() + ") alone. Not interested...");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();      
    } 
    return auditTrail;
  }
  
  /**
   * Just for tests
   * @param args
   */
  public static void main(String[] args)
  {
    String compositeName = "EventTriggeredComposite";
    String bpelName      = "EventTriggeredBPELProcess"; // This is the name of the BPEL Process to look for, inside the composite.
    try
    {
      ProcessUtil pu = new ProcessUtil(true);
      pu.init("server.properties");
      String[] sa = pu.countProcesses(compositeName);
      for (String s : sa)
        System.out.println("-> Instance #" + s);
      String at = pu.getAuditTrail(bpelName, "480009");
      System.out.println(at);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
  }
}
