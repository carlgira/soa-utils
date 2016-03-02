package testserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Node;

import samples.main.handlers.CustomHandlerResolver;

import testhelper.Context;
import testhelper.ServiceUnitTestHelper;

import testing.util.GnlUtilities;

import unittests.patterns.event.RaiseEvent;
import unittests.patterns.hwf.HumanWorkFlowInteraction;

import unittests.util.Utilities;

import util.soautil.ProcessUtil;

import util.xmlutil.XMLUtilities;

/**
 * This is an RMI Server to be used with OpenScript
 * 
 * @author olivier.lediouris@oracle.com
 */
public class TestServerImplementation
  extends UnicastRemoteObject
  implements TestServerInterface
{
  @SuppressWarnings("compatibility:2018450507925409635")
  private final static long serialVersionUID = 1L;
  
  private transient DOMParser parser = new DOMParser();
  private TestServerImplementation instance = this;

  private transient Registry registry     = null;
  private int registryPort                = 1099; // Default value
  
  private transient Thread serverThread = null;
  
  public TestServerImplementation(int i, 
                                  RMIClientSocketFactory rmiClientSocketFactory,
                                  RMIServerSocketFactory rmiServerSocketFactory)
    throws RemoteException
  {
    super(i, rmiClientSocketFactory, rmiServerSocketFactory);
  }

  public TestServerImplementation(int i)
    throws RemoteException
  {
    super(i);
  }

  public TestServerImplementation()
    throws RemoteException
  {
    super();
  }

  public String invokeSynchronousService(String wsdlURL, 
                                         String serviceName, 
                                         String servicePort, 
                                         String serviceOperation,
                                         String serviceNSUri, 
                                         String serviceInputPayload, 
                                         boolean moveonIfPayloadInvalid)
    throws RemoteException, Exception
  {
    XMLElement x = null;
    ServiceUnitTestHelper suth = null;
    try
    {
      String endPointURL = "";
      suth = new ServiceUnitTestHelper();
      try
      {
        endPointURL = suth.getEndpointURL(wsdlURL);
      }
      catch (Exception ex)
      {
        throw new RemoteException("Getting EndPoint URL", ex);
      }
      boolean ok = suth.isServiceUp(wsdlURL);
      if (!ok)
        throw new RemoteException("Service is down");
      ok = suth.validateServicePayload(wsdlURL, serviceInputPayload);
      if (!ok)
      {
        if (!moveonIfPayloadInvalid)
          throw new RemoteException("Invalid payload");
      }
      ok = suth.isServiceNameOK(wsdlURL, serviceName);
      if (!ok)
        throw new RemoteException("Invalid service name");
      ok = suth.isPortNameOK(wsdlURL, serviceName, servicePort);
      if (!ok)
        throw new RemoteException("Invalid port name");
      ok = suth.isOperationNameOK(wsdlURL, serviceName, servicePort, serviceOperation);
      if (!ok)
        throw new RemoteException("Invalid operation name");

      suth.setHandlerResolver(new CustomHandlerResolver());
      x = suth.invokeSyncService(endPointURL, wsdlURL, serviceNSUri, serviceInputPayload, serviceName, servicePort);
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return XMLUtilities.xmlToString(x);
  }
  
  public String invokeSynchronousService(String projectDirectory,
                                         String propertiesFilename,
                                         boolean verbose)
    throws RemoteException, Exception
  {
    XMLElement responsePayload = null;
    XMLElement x = null;
    ServiceUnitTestHelper suth = null;
    Properties props = readTestProperties(projectDirectory, propertiesFilename, verbose);
    
    try
    {
      // Context setup
      suth = new ServiceUnitTestHelper();
      suth.setVerbose(verbose);
      
      // Is there security to apply?
      String userName = props.getProperty("username", null);
      String password = props.getProperty("password", null);
      if (userName != null || password != null)
      {
        if (password == null || userName == null)
          fail("you must provide username and password, or none.");
        else
        {
          suth.setPolicySecurity(true);
          suth.setUsername(userName);
          suth.setPassword(password);
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
            {
              System.out.println("oracle.security.jps.config = " + jpsConfig);
              try
              {
                File f = new File(projectDirectory, jpsConfig);
                if (f.exists())
                {
                  System.out.println("jps-config: found.");
                  System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
                }
                else
                  System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
              }
              catch (Exception ex)
              {
                System.out.println("Security:" + ex.toString());
                onError(ex);
              }
            }
          }
        }
      }

      String wsdlURL = props.getProperty("wsdl.url");
//    wsdlURL = GnlUtilities.replaceString(wsdlURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
//    wsdlURL = GnlUtilities.replaceString(wsdlURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));
//    wsdlURL = GnlUtilities.substituteVariable(wsdlURL, System.getProperties());
            
      String serviceEndPoint = null;
      try 
      { 
        serviceEndPoint = suth.getEndpointURL(wsdlURL); 
      }
      catch (Exception ex)
      {
        System.out.println("Problem getting the ServiceEndPoint URL. Is the service or server up?");
        onError(ex);
      //      fail(ex.toString());
      }
      assertTrue("ServiceEndpoint from ${wsdl.url} returned null", serviceEndPoint != null);
      
      String serviceRequest = "";
      String payloadFileName = props.getProperty("service.input.payload.file", null);
      String payloadAsString = props.getProperty("service.input.payload.as.string", null);
      if (payloadFileName == null && payloadAsString == null)
        fail("Need an input payload (service.input.payload.file or service.input.payload.as.string)");
      
      if (payloadFileName != null)
      {
        BufferedReader br = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        { 
          line = br.readLine(); 
          if (line != null)
            serviceRequest += line;
          else
            keepReading = false;
        }
        br.close();
      }
      else
        serviceRequest = payloadAsString;

      // Beginning the test
      boolean ok = suth.isServiceUp(wsdlURL);
      assertTrue("Service is down", ok);

      boolean goThrough = "true".equals(props.getProperty("move.on.if.payload.is.invalid", "false"));
      ok = suth.validateServicePayload(wsdlURL, serviceRequest);
      if (!ok)
      {
        if (!goThrough)
          fail("Invalid payload:\n" + serviceRequest);
        else
        {
          System.out.println("Payload invalid, but moving on anyway.");
          ok = true;
        }
      }
      if (ok)
      {
        ok = suth.isServiceNameOK(wsdlURL, 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name", ok);
        ok = suth.isPortNameOK(wsdlURL, 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid port name", ok);
        
        try
        {
          String timeout = props.getProperty("service.response.timeout", null);
          try
          {
            if (timeout == null)
              x = suth.invokeSyncService(serviceEndPoint, 
                                         wsdlURL, 
                                         props.getProperty("service.ns.uri"), 
                                         serviceRequest,
                                         props.getProperty("service.name"), 
                                         props.getProperty("service.port"));
            else
            {
              long timeoutValue = 0L;
              try { timeoutValue = Long.parseLong(timeout); }
              catch (NumberFormatException nfe)
              {
                fail("Unparsable timeout value [" + timeout + "]");
              }
              x = suth.invokeSyncServiceWithTimeout(serviceEndPoint, 
                                                    wsdlURL, 
                                                    props.getProperty("service.ns.uri"), 
                                                    serviceRequest,
                                                    props.getProperty("service.name"), 
                                                    props.getProperty("service.port"),
                                                    timeoutValue);
            }
          }
          catch (Exception ex)
          {
            if (ex instanceof SOAPFaultException)
            {
              SOAPFaultException sfe = (SOAPFaultException)ex;
              SOAPFault sf = sfe.getFault();
              if (sf instanceof XMLElement)
                x = (XMLElement)sf;
              else
              {
                String str = sf.toString();
                DOMParser parser = Context.getInstance().getParser();
                synchronized (parser)
                {
                  parser.parse(new StringReader(str));
                  XMLDocument doc = parser.getDocument();
                  x = (XMLElement)doc.getDocumentElement();
                }                
              }
            }
            else
              throw ex;
          }
          if (x != null)
          {
            StringWriter sw = new StringWriter();
            x.print(sw);
            String returnedPayload = sw.toString();
            if (verbose)
              System.out.println("Sync response:\n" + returnedPayload);
          }
          else
          {
            assertTrue("No response...", (x != null));
          }
          responsePayload = x;
        }
        catch (Exception ex)
        {
          onError(ex);
      //  fail(ex.toString());
        }
      }
      if (verbose)
        System.out.println("Done.");      
    }
    catch (Exception ex)
    {
      onError(ex);
    }
    return XMLUtilities.xmlToString(responsePayload);
  }
    
  public String invokeASynchronousTwoWayService(String projectDirectory,
                                                String propertiesFilename,
                                                boolean verbose)
    throws RemoteException, Exception
  {
    XMLElement responsePayload = null;
    XMLElement x = null;
    ServiceUnitTestHelper suth = null;
    Properties props = readTestProperties(projectDirectory, propertiesFilename, verbose);

    try
    {
      // Context setup
      suth = new ServiceUnitTestHelper();
      suth.setVerbose(verbose);
      // Is there security to apply?
      String userName = props.getProperty("username", null);
      String password = props.getProperty("password", null);
      if (userName != null || password != null)
      {
        if (password == null || userName == null)
          throw new RemoteException("you must provide username and password, or none.");
        else
        {
          suth.setPolicySecurity(true);
          suth.setUsername(userName);
          suth.setPassword(password);
          // Check if System variables are set
          // oracle.security.jps.config set to ./security/config/jps-config.xml
          String jpsConfig  = System.getProperty("oracle.security.jps.config", null);
          if (jpsConfig == null)
          {
            throw new RemoteException("You need to provide oracle.security.jps.config as System variable.");
          }
          else
          {
            if (verbose)
            {
              System.out.println("oracle.security.jps.config = " + jpsConfig);
              try
              {
                File f = new File(projectDirectory, jpsConfig);
                if (f.exists())
                {
                  System.out.println("jps-config: found in " + f.getAbsolutePath());
                  System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
                }
                else
                  System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
              }
              catch (Exception ex)
              {
                System.out.println("Security:" + ex.toString());
                throw new RemoteException("Security", ex);
              }
            }
          }
        }
      }

  //      String wsdlURL = props.getProperty("wsdl.url");
  //      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
  //      wsdlURL = GnlUtilities.replaceString(wsdlURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));
  //      wsdlURL = GnlUtilities.substituteVariable(wsdlURL, System.getProperties());
  //      props.setProperty("wsdl.url", wsdlURL);

      String serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url"));
      String serviceRequest = "";
      String payloadFileName = props.getProperty("service.input.payload.file", null);
      String payloadAsString = props.getProperty("service.input.payload.as.string", null);
      if (payloadFileName == null && payloadAsString == null)
        throw new RemoteException("Need an input payload (service.input.payload.file or service.input.payload.as.string)");
      
      if (payloadFileName != null)
      {
        BufferedReader br = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        { 
          line = br.readLine(); 
          if (line != null)
            serviceRequest += line;
          else
            keepReading = false;
        }
        br.close();
      }
      else
        serviceRequest = payloadAsString;

      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      if (!ok)
        throw new RemoteException("Service is down");

      boolean goThrough = "true".equals(props.getProperty("move.on.if.payload.is.invalid", "false"));
      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), 
                                       serviceRequest);
      if (!ok)
      {
        if (!goThrough)
          throw new RemoteException("Invalid payload:\n" + serviceRequest);
        else
        {
          System.out.println("Payload invalid, but moving on anyway.");
          ok = true;
        }
      }
      if (ok)
      {
        ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), 
                                  props.getProperty("service.name"));
        assertTrue("Invalid Service Name [" + props.getProperty("service.name") + "]", ok);
        ok = suth.isPortNameOK(props.getProperty("wsdl.url"), 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid Port Name [" + props.getProperty("service.port")+ "]", ok);

        ok = suth.isOperationNameOK(props.getProperty("wsdl.url"), 
                                    props.getProperty("service.name"),
                                    props.getProperty("service.port"),
                                    props.getProperty("service.operation"));
        assertTrue("Invalid Operation Name [" + props.getProperty("service.operation") + "]", ok);

        try
        {
          String resp = "";
          boolean soapExceptionPath = false;
          long before = 0L, after = 0L;
          String timeout = props.getProperty("service.response.timeout", null);
          try
          {
            if (timeout == null)
            {
              before = System.currentTimeMillis();
              x = suth.invokeASync2WayService(serviceEndPoint, 
                                              props.getProperty("wsdl.url"), 
                                              props.getProperty("service.ns.uri"), 
                                              serviceRequest,
                                              props.getProperty("service.name"), 
                                              props.getProperty("service.port"),
                                              props.getProperty("service.operation"), 
                                              props.getProperty("client.reply.to.port"));
            }
            else
            {
              long timeoutValue = 0L;
              try { timeoutValue = Long.parseLong(timeout); }
              catch (NumberFormatException nfe)
              {
                fail("Unparsable timeout value [" + timeout + "]");
              }
              before = System.currentTimeMillis();
              x = suth.invokeASync2WayServiceWithTimeout(serviceEndPoint, 
                                                         props.getProperty("wsdl.url"), 
                                                         props.getProperty("service.ns.uri"), 
                                                         serviceRequest,
                                                         props.getProperty("service.name"), 
                                                         props.getProperty("service.port"),
                                                         props.getProperty("service.operation"), 
                                                         props.getProperty("client.reply.to.port"),
                                                         timeoutValue);
            }
          }
          catch (Exception ex)
          {
            if (ex instanceof SOAPFaultException)
            {
              SOAPFaultException sfe = (SOAPFaultException)ex;
              SOAPFault sf = sfe.getFault();
              resp = sf.toString();
              soapExceptionPath = true;
            }
            else
              throw ex;
          }
          
          if (x != null && verbose)
            x.print(System.out);
          if (!soapExceptionPath)
            resp = ServiceUnitTestHelper.getAsyncResponse();          
          if (verbose) 
            System.out.println(resp);            
          
          if (!soapExceptionPath && "true".equals(props.getProperty("decrypt.async.response", "false")))
          {
            try 
            { 
              String decrypted = ServiceUnitTestHelper.decryptAsyncResponseBody(resp);
              // Swap the encrypted / decrypted in the original Envelope
              parser.parse(new StringReader(decrypted));
              XMLDocument xmlDecrypted = parser.getDocument();
              parser.parse(new StringReader(resp));
              XMLDocument soapEnvelope = parser.getDocument();
              XMLElement decryptedBody = (XMLElement)xmlDecrypted.getDocumentElement();
              XMLElement originalBody = null;
              NSResolver soapResolver = new NSResolver()
                  {
                    public String resolveNamespacePrefix(String prefix)
                    {
                      return "http://schemas.xmlsoap.org/soap/envelope/";
                    }
                  };
              originalBody = (XMLElement)soapEnvelope.selectNodes("//env:Body", soapResolver).item(0);
              Node parentNode = originalBody.getParentNode();
              parentNode.removeChild(originalBody);
              Node adoptedBody = soapEnvelope.adoptNode(decryptedBody);
  //            soapEnvelope.selectNodes("//env:Envelope", soapResolver).item(0).appendChild(adoptedBody);
              parentNode.appendChild(adoptedBody);
              StringWriter sw = new StringWriter();
              soapEnvelope.print(sw);
              resp = sw.toString();
            }
            catch (Exception ex)
            {
              onError(ex);
  //            fail("Swapping Bodies:" + ex.toString());
            }
          }
          if (verbose)
            System.out.println("Decrypted:\n" + resp);
          
          try
          {
            parser.parse(new StringReader(resp));          
            responsePayload = parser.getDocument();
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          after = System.currentTimeMillis();
          if (verbose) 
            System.out.println("Done in " + Long.toString(after - before) + " ms.");
        }
        catch (Exception ex)
        {
          System.out.println("Cought a " + ex.getClass().getName());
          System.out.println("Make sure all threads are dead...");
          onError(ex);
  //      fail(ex.toString());
        }
      }
      if (verbose)
        System.out.println("Done.");
    }
    catch (Exception ex)
    {
      onError(ex);
  //    ex.printStackTrace();
    }
    return XMLUtilities.xmlToString(responsePayload);
  }
  
  public String invokeASynchronousOneWayService(String projectDirectory,
                                                String propertiesFilename,
                                                boolean verbose)
    throws RemoteException, Exception
  {
    ServiceUnitTestHelper suth = null;
    Properties props = readTestProperties(projectDirectory, propertiesFilename, verbose);
    try
    {
      // Context setup
      suth = new ServiceUnitTestHelper();
      suth.setVerbose(verbose);
      // Is there security to apply?
      String userName = props.getProperty("username", null);
      String password = props.getProperty("password", null);
      if (userName != null || password != null)
      {
        if (password == null || userName == null)
          fail("you must provide username and password, or none.");
        else
        {
          suth.setPolicySecurity(true);
          suth.setUsername(userName);
          suth.setPassword(password);
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
            {
              System.out.println("oracle.security.jps.config = " + jpsConfig);
              try
              {
                File f = new File(projectDirectory, jpsConfig);
                if (f.exists())
                {
                  System.out.println("jps-config: found.");
                  System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
                }
                else
                  System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
              }
              catch (Exception ex)
              {
                System.out.println("Security:" + ex.toString());
                onError(ex);
              }
            }
          }
        }
      }
  
  //  String wsdlURL = props.getProperty("wsdl.url");
  //  wsdlURL = GnlUtilities.replaceString(wsdlURL, "${proxy.name}", masterProps.getProperty("proxy.name", "localhost"));
  //  wsdlURL = GnlUtilities.replaceString(wsdlURL, "${soa.port.number}", masterProps.getProperty("soa.port.number","8001"));
  //  props.setProperty("wsdl.url", wsdlURL);
      
      String serviceEndPoint = null;
      try 
      { 
        serviceEndPoint = suth.getEndpointURL(props.getProperty("wsdl.url")); 
      }
      catch (Exception ex)
      {
        System.out.println("Problem getting the ServiceEndPoint URL. Is the service or server up?");
        onError(ex);
  //    fail(ex.toString());
      }
      assertTrue("ServiceEndpoint from ${wsdl.url} returned null", serviceEndPoint != null);
      
      String serviceRequest = "";
      String payloadFileName = props.getProperty("service.input.payload.file", null);
      String payloadAsString = props.getProperty("service.input.payload.as.string", null);
      if (payloadFileName == null && payloadAsString == null)
        fail("Need an input payload (service.input.payload.file or service.input.payload.as.string)");
      
      if (payloadFileName != null)
      {
        BufferedReader br = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
        String line = "";
        boolean keepReading = true;
        while (keepReading)
        { 
          line = br.readLine(); 
          if (line != null)
            serviceRequest += line;
          else
            keepReading = false;
        }
        br.close();
      }
      else
        serviceRequest = payloadAsString;
  
      // Now we're testing...        
      boolean ok = suth.isServiceUp(props.getProperty("wsdl.url"));
      assertTrue("Service is down", ok);
  
      boolean goThrough = "true".equals(props.getProperty("move.on.if.payload.is.invalid", "false"));
      ok = suth.validateServicePayload(props.getProperty("wsdl.url"), 
                                       serviceRequest);
      if (!ok)
      {
        if (!goThrough)
          fail("Invalid payload:\n" + serviceRequest);
        else
        {
          System.out.println("Payload invalid, but moving on anyway.");
          ok = true;
        }
      }
      if (ok)
      {
        ok = suth.isServiceNameOK(props.getProperty("wsdl.url"), 
                                  props.getProperty("service.name"));
        assertTrue("Invalid service name:[" + props.getProperty("service.name") + "]", ok);
        ok = suth.isPortNameOK(props.getProperty("wsdl.url"), 
                               props.getProperty("service.name"), 
                               props.getProperty("service.port"));
        assertTrue("Invalid Port Name [" + props.getProperty("service.port") + "]", ok);
  
        try
        {
          suth.invokeASync1WayService(serviceEndPoint, 
                                      props.getProperty("wsdl.url"), 
                                      props.getProperty("service.ns.uri"), 
                                      serviceRequest,                                                     
                                      props.getProperty("service.name"), 
                                      props.getProperty("service.port"));
        }
        catch (Exception ex)
        {
          onError(ex);
      //        fail(ex.toString());
        }
      }
    }
    catch (Exception ex)
    {
      onError(ex);
    }
    finally
    {
      if (verbose)
        System.out.println("Test is finished.");
    }
    
    return "";
  }

  // TaskList interaction
  private transient HumanWorkFlowInteraction hwfi = null;
  
  public void initTaskList(String projectDirectory, String propertiesFile, boolean verbose)
    throws RemoteException, Exception
  {
    hwfi = new HumanWorkFlowInteraction(projectDirectory, propertiesFile, verbose);
  }
  
  public String[] getTaskList()
    throws RemoteException, Exception
  {
    if (hwfi == null)
      throw new Exception("HumanWorkFlowInteraction is null");
    else
    {
      List<String> list = hwfi.getTaskIDList();
      String[] sa = list.toArray(new String[list.size()]);
      return sa;
    }
  }
  
  public String updateTaskOutcome(String taskID, String outcome)
    throws RemoteException, Exception
  {
    if (hwfi == null)
      throw new Exception("HumanWorkFlowInteraction is null");
    else
      return hwfi.getTaskOutcomeByTaskID(taskID, outcome);
  }
  
  public void resetHumanWorkFlowInteraction()
    throws RemoteException, Exception
  {
    hwfi = null;
  }
  
  public void raiseBusinessEvent(String projectDirectory, String propertiesFilename, boolean verbose)
    throws RemoteException, Exception
  {
    RaiseEvent re = new RaiseEvent()
    {
      @Override
      public String beforeRaisingEvent(String payload)
      {
        return payload;
      }
      
      @Override
      public void afterRaisingEvent()
      {
        System.out.println("Done with Business Event");
      }
    };
    re.raiseBusinessEvent(projectDirectory, propertiesFilename, verbose);
  }
  
  private ProcessUtil processUtil = null;
  
  public void createProcessUtil(boolean verb, String serverPropFileName)
    throws RemoteException, Exception
  {
    processUtil =  new ProcessUtil(verb);
    processUtil.init(serverPropFileName);
  }

  public String[] getProcessInstancesIDs(String compositeName)
    throws RemoteException, Exception
  {
    if (processUtil == null)
      throw new RemoteException("Initialize ProcessUtil first");
    
    return processUtil.countProcesses(compositeName);
  }

  public String getAuditTrail(String bpelName, String instanceID)
    throws RemoteException, Exception
  {
    if (processUtil == null)
      throw new RemoteException("Initialize ProcessUtil first");
  
    return processUtil.getAuditTrail(bpelName, instanceID);
  }
  
  public void resetProcessUtil()
    throws RemoteException, Exception
  {
    processUtil = null;
  }
  
  public void stopTestServer() throws RemoteException
  {
    try
    {
      System.out.println("Stoping RMI Server " + name);
      Naming.unbind(name);  
      System.out.println("Stopped.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    synchronized (instance) { instance.notify(); }
  }
  
  public void start()
  {
    try
    {
      registry = LocateRegistry.createRegistry(registryPort);
      System.out.println("Registry started");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    Thread t = new Thread()
      {
        public void run()
        {
          try
          {
            System.out.println("...TestServer Waiting");
            serverThread = this;
            synchronized (instance) { instance.wait(); }
            System.out.println("TestServer is done waiting.");
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
          finally
          {
            System.out.println("Done with ServerThread.");
            System.exit(0);
          }
        }
      };
    t.start();    
  }
  private static String name = "";
  
  private static Properties readTestProperties(String projectDirectory, 
                                               String propertiesFilename,
                                               boolean verbose) throws RemoteException, Exception
  {
    Properties props = new Properties();
    try
    {
      final Properties masterProps = Utilities.readMasterProperties(projectDirectory);

      if (propertiesFilename == null)
        throw new RemoteException("No propertiesFileName associated with this test.");
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
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return props;
  }
  
  private static void assertTrue(String mess, boolean cond) throws RemoteException
  {
    if (!cond)
      throw new RemoteException(mess);
  }
  
  private static void fail(String mess) throws RemoteException
  {
    throw new RemoteException(mess);
  }
  
  private void onError(Exception ex) throws RemoteException
  {
    throw new RemoteException("onError", ex);
  }
  
  /**
   * Use -Dserver.name=rmi://111.222.33.44:1099/TestRmiServer
   * This system variable overrides the ip possibly given as a parameter, like in
   * 
   *   java testserver.TestServerImplementation 111.222.33.44
   * 
   * @param args ip address of the server. Optional.
   */
  public static void main(String[] args)
  {
    String hostname = "localhost";
    String ip       = "127.0.0.1";
    
    if (args.length > 0)
      ip = args[0];
    else
    {    
      try 
      { 
        InetAddress addr = InetAddress.getLocalHost(); // Get IP Address 
        byte[] ipAddr = addr.getAddress();             // Get hostname 
        ip = "";
        for (int i=0; i<ipAddr.length; i++) 
        { 
          if (i > 0)
            ip += "."; 
          ip += ipAddr[i]&0xFF;      
        }
        hostname = addr.getHostName(); 
      } 
      catch (UnknownHostException e) 
      {
        e.printStackTrace();
      }
    }

    try
    {
      // Ugly force:
//    ip = "130.35.95.19";
      TestServerImplementation server = new TestServerImplementation();
//    name = System.getProperty("server.name", "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":" + Integer.toString(server.registryPort) + "/TestRmiServer");  
      name = System.getProperty("server.name", "rmi://" + ip + ":" + Integer.toString(server.registryPort) + "/TestRmiServer");  
      System.out.println("Server: server.name=" + name);
      
      server.start();
      System.out.println("Now binding.");
      
      Naming.rebind(name, server);      
      System.out.println(name + " is ready for duty.");
  //  server.start();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    System.out.println("Testing server is on its own.");
  }

  public void setRegistryPort(int registryPort)
  {
    this.registryPort = registryPort;
  }
}
