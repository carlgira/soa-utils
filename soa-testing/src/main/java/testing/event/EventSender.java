package testing.event;

import java.io.File;

import java.io.IOException;
import java.io.Reader;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;

import java.security.PrivilegedAction;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import java.util.Set;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.naming.NameNotFoundException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContext;

import javax.sql.DataSource;

import javax.transaction.UserTransaction;

import javax.xml.namespace.QName;

import oracle.adf.share.security.authentication.JAASAuthenticationService;

import oracle.fabric.blocks.event.BusinessEventConnection;
import oracle.fabric.blocks.event.BusinessEventConnectionFactory;

import oracle.fabric.common.BusinessEvent;

import oracle.integration.platform.blocks.event.BusinessEventBuilder;
import oracle.integration.platform.blocks.event.saq.SAQRemoteBusinessEventConnectionFactory;

import oracle.security.jps.JpsContext;
import oracle.security.jps.JpsContextFactory;

import oracle.security.jps.service.login.LoginService;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import testhelper.ServiceUnitTestHelper;

/**
 * Important
 * ---------
 *
 * To be able to send event programatically with the code below,
 * a property must be set at the server level.
 * In $MW_HOME/user_projects/domains/<your domain>/bin/setDomainEnv.sh
 * (or setDomainEnv.cmd on Windows)
 * Use the folloing line:
 *
 * WLS_JDBC_REMOTE_ENABLED="-Dweblogic.jdbc.remoteEnabled=true"
 *
 * WLS Servers (including SOA server) must be bounced after modifying that one.
 *
 * In case security is required, system property "oracle.security.jps.config" must be
 * set and represent the path to jps-config.xml
 *
 */
public class EventSender
{
  private boolean verbose = false;
  private static boolean dumpAll = false;
  
  private DOMParser parser = new DOMParser();
  
  public EventSender() 
  {    
  }

  public void setVerbose(boolean b)
  { this.verbose = b; }
  
  private static void dumpSysProps()
  {
    // Dump, after setting
    if (dumpAll)
    {
      Properties sysProps = System.getProperties();
      Set<Object> keys = sysProps.keySet();
      SortedSet sortedKeys = new TreeSet();
      sortedKeys.addAll(keys);
      Iterator keyIterator = sortedKeys.iterator();
      while (keyIterator.hasNext())
      {
        String key = (String)keyIterator.next();
        System.out.println("[" + key + "] = [" + (String)sysProps.get(key) + "]");
      }
    }
    else // Only mines
    {
      String pName = "java.naming.factory.initial";
      System.out.println("[" + pName + "] = [" + System.getProperty(pName) + "]");
      pName = "java.naming.provider.url";
      System.out.println("[" + pName + "] = [" + System.getProperty(pName) + "]");
      pName = "java.naming.security.principal";
      System.out.println("[" + pName + "] = [" + System.getProperty(pName) + "]");
      pName = "java.naming.security.credentials";
      System.out.println("[" + pName + "] = [" + System.getProperty(pName) + "]");
    }    
  }

  public void connectAndSend(Properties props, 
                             String eventName, 
                             String ns, 
                             Reader eventPayload) throws Exception
  {
    connectAndSend(props, eventName, ns, eventPayload, null, null);
  }
  
  public void connectAndSend(final Properties props, 
                             final String eventName, 
                             final String ns, 
                             final Reader eventPayload,
                             String username,
                             String password) throws Exception
  {
    Subject subject = null;
    try
    {
      if (username != null || password != null)
      {
        try
        {
          if (username == null || password == null)
          {
            throw new RuntimeException("Please provide both username AND password");
          }
          String jpsConfig = System.getProperty("oracle.security.jps.config", null);
          if (jpsConfig == null)
          {
            throw new RuntimeException("Please set the property \"oracle.security.jps.config\" to the path to jps-config.xml");
          }
//        System.out.println(jpsConfig);
          if (verbose)
          {
            System.out.println("Authenticating as [" + username + "] / [" + password + "]");
          }
          
          String jpsContext = ServiceUnitTestHelper.getContext(jpsConfig);
            
          PolicyContext.setContextID(jpsContext);
          if (false)
          {
            JAASAuthenticationService jaasAuthService = new JAASAuthenticationService();
            jaasAuthService.login(username, password);
          }
          else
          {
            JpsContextFactory factory = JpsContextFactory.getContextFactory();
            JpsContext context = factory.getContext(jpsContext);
            LoginService loginService = context.getServiceInstance(LoginService.class);
            CallbackHandler cb = new MyCallbackHandler(username, password.toCharArray());
            LoginContext logctx = null;
            try
            {              
              logctx = loginService.getLoginContext(null, cb);
              logctx.login();
              subject = logctx.getSubject();
              if (verbose)
              {
                System.out.println("Subject:\n" + subject.toString());
                Set<Principal> principals = subject.getPrincipals();
                Iterator<Principal> it = principals.iterator();
                while (it.hasNext()) 
                  System.out.println("Principal: " + it.next().toString());
              }
            }
            catch (Exception e)
            {
              e.printStackTrace();
              throw new RuntimeException("Authenticating:", e);
            }
            catch (Throwable tr)
            {
              tr.printStackTrace();
              throw new RuntimeException("LoginContext", tr);
            }
          }
        }
        catch (Exception ex)
        {
          throw new RuntimeException("Issue in the security block (of EventSender) when raising a business event", ex);
       // System.out.println("Security issue when raising a business event:" + ex.toString());
        }
      }
      actualEventRaise(props, eventName, ns, eventPayload, subject);
    }
    catch (Exception ex)
    {
       throw ex;      
    }
  }
    
  private void actualEventRaise(Properties props, 
                                String eventName, 
                                String ns, 
                                Reader eventPayload,
                                Subject subject) throws Exception
  {
    try
    {
      Context ctx = null;
      BusinessEventConnectionFactory cf = null;
      
      DataSource localDS   = null, 
                 localTXDS = null;      
      
      try
      {
        try { ctx = new InitialContext(props); }
        catch (Exception ex) { throw new Exception("Initializing Context Failed", ex); }
//      ctx = new InitialContext();
        if (verbose) System.out.println("Context Initialized");
        try
        {
          String jndiName = "jdbc/EDNDataSource";
          Object jndiObject = ctx.lookup(jndiName);
          if (verbose) System.out.println(jndiName + " is a " + jndiObject.getClass().getName() + "\n");     
          localDS = (DataSource)jndiObject;
        }
        catch (NameNotFoundException nnfe)
        {
          System.out.println(nnfe.toString());            
        }
        catch (Exception ex)
        {
          System.out.println(ex.toString());
        }
        
        try
        {
          String jndiName = "jdbc/EDNLocalTxDataSource";
          Object jndiObject = ctx.lookup(jndiName);
          if (verbose) System.out.println(jndiName + " is a " + jndiObject.getClass().getName() + "\n");    
          localTXDS = (DataSource)jndiObject;
        }
        catch (NameNotFoundException nnfe)
        {
          System.out.println(nnfe.toString());            
        }
        catch (Exception ex)
        {
          System.out.println(ex.toString());
        }
        if (verbose) System.out.println("DataSources lookup OK");
        
//      cf = BusinessEventConnectionFactorySupport.findRelevantBusinessEventConnectionFactory(true);
        UserTransaction ut = new UserTransaction()
          {

            public void begin()
            {
            }

            public void commit()
            {
            }

            public void rollback()
            {
            }

            public void setRollbackOnly()
            {
            }

            public int getStatus()
            {
              return 0;
            }

            public void setTransactionTimeout(int i)
            {
            }
          };
        // Drop7 B5 minimum!!
        SAQRemoteBusinessEventConnectionFactory rbecf = new SAQRemoteBusinessEventConnectionFactory(localDS, localTXDS, ut);
        cf = rbecf;          
                     
        if (verbose)
        {
          if (cf != null)
            System.out.println("Good! We're in!");
          else
            System.out.println("...Baaaaaaaaad!");
        }
      }
      catch (Exception ex)
      {
        throw ex;
      }

      if (verbose) System.out.println("Now raising event...");
      if (cf != null)
      {
        final BusinessEventConnection bec = cf.createBusinessEventConnection();
        BusinessEventBuilder builder = BusinessEventBuilder.newInstance();
        /*
         * This part is specific, it depends on the event to raise.
         * 
         * QName parameters:
         * 
         * In your edl:
         *   First  prm: /definitions@targetNamespace
         *   Second prm: /definitions/event-definition@name
         */
        XMLDocument doc = null;
        builder.setEventName(new QName(ns, eventName));
        try
        {
          parser.parse(eventPayload);
          doc = parser.getDocument();        
        }
        catch (Exception ex)
        {
          throw ex;
        }
        if (false && verbose)
        {
          System.out.println("Document:");
          doc.print(System.out);
        }
        
        builder.setBody(doc.getDocumentElement()); // As defined in the event schema definition
        final BusinessEvent event = builder.createEvent();
        
        if (verbose)
        {
          Document eventAsDoc = event.getAsDoc();
          System.out.println("Full Event:");
          if (eventAsDoc instanceof XMLElement)          
            ((XMLElement)eventAsDoc).print(System.out);
          else
            System.out.println("eventAsDoc is a " + eventAsDoc.getClass().getName());
        }
        if (verbose)
        {
          System.out.println("Raising event now!");
//        AccessControlContext acc = AccessController.getContext();
//        Subject _subject = Subject.getSubject(acc);
          System.out.println("Before raising event, Subject:\n" + (subject==null?"-- null --":subject.toString()));
        }
        if (subject == null)
          bec.publishEvent(event, 4);
        else
        {
          Subject.doAs(subject, new PrivilegedAction()
                       {
                         public Object run()
                         {
                           try
                           {
                             if (verbose) System.out.println("Raising event, securely.");
                             bec.publishEvent(event, 4);
                           }
                           catch (Exception ex)
                           {
                             ex.printStackTrace();
                             return ex;
                           }
                           return null;
                         }
                       });
          
        }
      }
      else if (verbose)
        System.out.println("No connection factory...");
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
  
  private void setAsSystemProperties(Properties props)
  {
    Enumeration e = props.keys();        
    while (e.hasMoreElements())
    {
      String s = (String)e.nextElement();
      System.setProperty(s, props.getProperty(s));
    }
  }
  
  protected static class MyCallbackHandler
    implements CallbackHandler
  {

    private String name;
    private char[] password;

    public MyCallbackHandler(String name, char[] password)
    {
      this.name = name;
      this.password = password.clone();
    }

    public void handle(Callback[] callbacks)
      throws IOException, UnsupportedCallbackException
    {
      for (Callback cb: callbacks)
      {
        if (cb instanceof NameCallback)
        {
          ((NameCallback) cb).setName(name);
        }
        else if (cb instanceof PasswordCallback)
        {
          ((PasswordCallback) cb).setPassword(password);
        }
      }
    }
  }
}
