package unittests.patterns.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import java.io.StringReader;

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Properties;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.custommonkey.xmlunit.Diff;

import static org.junit.Assert.*;
import org.junit.Test;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import testhelper.Context;
import testhelper.ServiceUnitTestHelper;

import testing.util.GnlUtilities;

import unittests.util.Utilities;

/**
 * Generic Unit Test for an Async Two way service.
 * A properties file name should be passed as a Systrem property named "properties.file.name"
 * Property "verbose" can be set to true or false
 * File master.properties could be present.
 * We need a property named client.reply.to.port for the reply-to port.
 * if a property named service.response.timeout, its value is in milleseconds
 *
 */
public class AsynchronousTwoWayUnitTest extends TestCase implements ServiceUnitTestInterface
{
  protected DOMParser parser = new DOMParser();
  protected boolean verbose = false;
  protected Properties props = new Properties();
  protected ServiceUnitTestHelper suth = null;
  protected String projectDirectory = ".";
  
  protected XMLElement responsePayload = null;
  
  public AsynchronousTwoWayUnitTest()
  {
  }

  @Test
  public void testASyncTwoWay()
  {
    try
    {
      firstOfAll();
      // Context setup
      suth = new ServiceUnitTestHelper();
      if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
        suth.startGUIThread();
      verbose = "true".equals(System.getProperty("verbose", "false"));
      projectDirectory = System.getProperty("project.directory", ".");
      suth.setVerbose(verbose);
      
      final Properties masterProps = Utilities.readMasterProperties(projectDirectory);

      String propertiesFilename = System.getProperty("properties.file.name", null); // Was "service-test-02.properties"
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
                  System.out.println("jps-config: found in " + f.getAbsolutePath());
                  System.setProperty("oracle.security.jps.config", f.getAbsolutePath());
                }
                else
                  System.out.println("jps-config: NOT found as [" + jpsConfig + "] in [" + projectDirectory + "]");
              }
              catch (Exception ex)
              {
                onError(ex);
                System.out.println("Security:" + ex.toString());
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

      serviceRequest = beforeInvoke(serviceRequest);

      // Real testing part
      XMLElement x = null;

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
          afterInvoke();
          beforeReceive();
          
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
          
          resp = afterReceive(resp);          
          evaluate(resp);
                              
          after = System.currentTimeMillis();
          if (verbose) 
            System.out.println("Done in " + Long.toString(after - before) + " ms.");
        }
        catch (Exception ex)
        {
          System.out.println("Cought a " + ex.getClass().getName());
          System.out.println("Make sure all threads are dead...");
          onError(ex);
//        fail(ex.toString());
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
    finally
    {
      if ("yes".equals(System.getProperty("display.traffic.gui", "no")))
      {
        try
        {
          suth.getGUIThread().join();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }      
    }
  }

  public void firstOfAll()
  {
    if (verbose) System.out.println("** firstOfAll being called");
  }

  public String beforeInvoke(String payload)
  {
    if (verbose) System.out.println("** beforeInvoke being called");
    return payload;
  }

  public void afterInvoke()
  {
    if (verbose) System.out.println("** afterInvoke being called");
  }

  public void beforeReceive()
  {
    if (verbose) System.out.println("** beforeReceive being called");
  }

  public String afterReceive(String payload)
  {
    if (verbose) System.out.println("** afterReceive being called");
    return payload;
  }

  public void evaluate(String payload)
  {
    if (verbose) System.out.println("** evaluate being called");
    
//  String xpath = "/env:Envelope/env:Body/svc:*";
    String xpath = "/env:Envelope/env:Body/*";
    try
    {
      NSResolver nsr = new NSResolver()
        {
          public String resolveNamespacePrefix(String prefix)
          {
            String uri = null;
            if (prefix.equals("env"))
              uri = "http://schemas.xmlsoap.org/soap/envelope/";
            else if (prefix.equals("svc"))
              uri = props.getProperty("service.ns.uri");           
            else if (prefix.equals("orafault"))
              uri = "http://xmlns.oracle.com/oracleas/schema/oracle-fault-11_0";
//          System.out.println(prefix + " : [" + uri + "]");
            return uri;
          }
        };
      parser.parse(new StringReader(payload));
      XMLDocument doc = parser.getDocument();
      
      
      boolean thereIsBody  = false;
      boolean thereIsFault = false;
      
      thereIsBody  = (doc.selectNodes("//env:Body", nsr).getLength() > 0);
      thereIsFault = (doc.selectNodes("//env:Fault", nsr).getLength() > 0);

      XMLElement response = null;
      StringWriter sw = new StringWriter();
      
      if (thereIsBody && !thereIsFault)      
      {
        NodeList nl = doc.selectNodes(xpath, nsr);
        
        assertTrue("Response not found in SOAP Envelope! (probably no SOAP Body)", nl.getLength() == 1);
        response = (XMLElement)nl.item(0);
  //    response.print(System.out);
        response.print(sw);
      }
      else if (thereIsFault)
      {
        doc.print(sw); // The full stuff
      }
      String expectedOutput = "";
      String payloadFileName = props.getProperty("service.output.payload.file", null);
      String payloadAsString = props.getProperty("service.output.payload.as.string", null);
      
      if (payloadAsString == null && payloadFileName == null)
      {
        fail("No payload to refer to (service.output.payload.file, or service.output.payload.as.string");
      }
      if (payloadFileName != null)
      {
        try
        {
          BufferedReader outBr = new BufferedReader(new FileReader(new File(projectDirectory, payloadFileName)));
          boolean keepItUp = true;
          String line = "";
          while (keepItUp)              
          {
            line = outBr.readLine();
            if (line != null)
              expectedOutput += (line + "\n");
            else
              keepItUp = false;
          }
          outBr.close();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      else if (payloadAsString != null)
        expectedOutput = payloadAsString;

      try
      {
        // Use XMLUnit here         
        String controlXML = expectedOutput.trim().replaceAll("\n", "");
        String testXML    = sw.toString().trim().replaceAll("\n", "");
        while (controlXML.indexOf("> ") > 0)
          controlXML = controlXML.replaceAll("> ", ">");
        while (testXML.indexOf("> ") > 0)
          testXML = testXML.replaceAll("> ", ">");
        
        if (verbose) System.out.println("XMLUnit Comparing:\n===================\n" + controlXML + 
                                           "\nwith:\n===================\n" + testXML + 
                                                  "\n===================");
        Diff xmlDiff = new Diff(controlXML, testXML);
        if (verbose)
        {
          System.out.println("Similar:" + xmlDiff.similar());
          System.out.println("Pieces of XML are similar " + xmlDiff);
        }
        assertTrue("Pieces of XML are similar " + xmlDiff, xmlDiff.similar());          
      }
      catch (Exception ex)
      {
        onError(ex);
//      fail(ex.toString());
      }
    }
    catch (Exception ex)
    {
      onError(ex);
//    fail(ex.toString());
    }
  }

  public void onError(Exception ex)
  {
    fail(ex.toString());
  }

  public XMLElement getResponsePayload()
  {
    return responsePayload;
  }

  /**
   * That is for tests
   * @param args
   */
  public static void main(String[] args)
  {
    AsynchronousTwoWayUnitTest atwut = new AsynchronousTwoWayUnitTest();
    System.setProperty("verbose", "true");
//  System.setProperty("properties.file.name", "generic-service-test-async-two-way.properties");
//  System.setProperty("properties.file.name", "adfbc.async.2.second.test.properties");
//  System.setProperty("properties.file.name", "tamil.properties");
    System.setProperty("properties.file.name", "ProjectExpenditureItemService_findProjectExpenditureItemAsync.properties");
    System.setProperty("oracle.security.jps.config", "./security/config/jps-config.xml");
    atwut.testASyncTwoWay();
  }
}
