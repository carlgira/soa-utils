package unittests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;
import java.util.Properties;

import java.util.Set;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import testhelper.Context;

import testing.util.GnlUtilities;

// import utils.system;

public class Utilities
{
  private final static DOMParser parser = Context.getInstance().getParser(); // new DOMParser();
  
  public static HashMap<String, String> prepareNSHashMap(String xPathExpression)
  {
    HashMap<String, String> map = new HashMap<String, String>();
    
    ArrayList<String> aluri = new ArrayList<String>();
    boolean keepGoing = true;
    int idx = 0;
    while (keepGoing)
    {
      int next = xPathExpression.indexOf("{", idx);
      if (next < 0)
        keepGoing = false;
      else
      {
        int closing = xPathExpression.indexOf("}", next);
        if (closing < 0)
        {
          System.err.println("That is baaaaaaaad!!!"); 
          throw new RuntimeException("Missing closing curly brace in " + xPathExpression);
        }
        else
        {
          String uri = xPathExpression.substring(next + 1, closing);
          if (!aluri.contains(uri))
            aluri.add(uri);
        }
        idx = closing;
      }
    }
    // Feeding the map
    idx = 1;
    for (String s:aluri)
      map.put("ns" + Integer.toString(idx++), s);
    
    return map;
  }
  
  public static String patchXPath(String xPathProp, HashMap<String, String> map)
  {
    String newExpression = xPathProp;
    Iterator<String> keys = map.keySet().iterator();
    while (keys.hasNext())
    {
      String prefix = keys.next();
      String nsuri  = map.get(prefix);
      newExpression = newExpression.replaceAll("\\{" + nsuri + "\\}", prefix + ":");
    }
    
    return newExpression;
  }
  
  public static XMLElement patchXML(String fromLiteral,
                                    XMLElement toElement,
                                    String toPath) throws Exception
  {
    if (toPath.startsWith("${"))
    {
      // Assume the target is a System variable like ${variable.name}
      String varName = toPath.substring("${".length(), toPath.trim().length() - 1);
      System.out.println("*** Setting content [" + fromLiteral + "] to variable [" + varName + "]");
      System.setProperty(varName, fromLiteral);          
    }
    else
    {
      HashMap<String, String> mapto = Utilities.prepareNSHashMap(toPath);
      String newToXPath = Utilities.patchXPath(toPath, mapto);
      Utilities.PatchNSResolver resolverTo = new Utilities.PatchNSResolver(mapto);
      
      System.out.println("Searching [" + newToXPath + "]");
      
      NodeList nl = toElement.selectNodes(newToXPath, resolverTo);
      if (nl.getLength() != 1)
      {   
        System.out.println("--------------------");
        System.out.println(toPath + " returns " + nl.getLength() + " node(s). MUST be 1.");
        System.out.println("Xpath expression [" + toPath + "] was applied on:");
        toElement.print(System.out);
        System.out.println("--------------------");
        throw new RuntimeException(toPath + " returns " + nl.getLength() + " node(s). MUST be exactly 1.");
        }      
      for (int i=0; i<nl.getLength(); i++)
      {
        XMLElement xelm = (XMLElement)nl.item(i);
        try { xelm.getFirstChild().setTextContent(fromLiteral); } catch (Exception ignore) {}
      }
    }
    return toElement;
  }
  
  public static XMLElement patchXML(XMLElement fromElement,
                                    XMLElement toElement,
                                    String fromPath,
                                    String toPath) throws Exception
  {
    HashMap<String, String> mapfrom = Utilities.prepareNSHashMap(fromPath);
    String newFromXPath = Utilities.patchXPath(fromPath, mapfrom);
    Utilities.PatchNSResolver resolverFrom = new Utilities.PatchNSResolver(mapfrom);
    NodeList nl = null;
    
    System.out.println("Searching [" + newFromXPath + "]");
    String patchValue = "";
    
    if (fromPath.startsWith("${"))
    {
      // Assume the value is a System variable like ${variable.name}
      String varName = fromPath.substring("${".length(), fromPath.trim().length() - 1);
      patchValue = System.getProperty(varName);
      System.out.println("*** Taking content from variable [" + varName + "] : [" + patchValue + "]");
    }
    else
    {
      nl = fromElement.selectNodes(newFromXPath, resolverFrom);
      if (nl.getLength() != 1)
      {   
        System.out.println("--------------------");
        System.out.println(fromPath + " returns " + nl.getLength() + " node(s). MUST be 1.");
        System.out.println("Xpath expression [" + fromPath + "] was applied on:");
        fromElement.print(System.out);
        System.out.println("--------------------");
        throw new RuntimeException(fromPath + " returns " + nl.getLength() + " node(s). MUST be exactly 1.");
      }
      patchValue = nl.item(0).getTextContent();
    }
    return patchXML(patchValue, toElement, toPath);
  }
  
  public static XMLElement fileToXML(String fName) throws Exception
  {
    XMLElement xelm = null;
    synchronized (parser)
    {
      parser.parse(new File(fName).toURI().toURL());
      xelm = (XMLElement)parser.getDocument().getDocumentElement();
    }
    return xelm;
  }
  
  public static XMLElement stringToXML(String content) throws Exception
  {
    XMLElement xelm = null;
    synchronized (parser)
    {
      parser.parse(new StringReader(content));
      xelm = (XMLElement)parser.getDocument().getDocumentElement();
    }
    return xelm;
  }
  
  public static void spitXMLtoFile(XMLElement xelm, String fName) throws Exception
  {
    xelm.print(new FileWriter(new File(fName)));
  }
  
  public static Properties readMasterProperties() throws Exception
  {
    return readMasterProperties(".");
  }
  
  public static Properties readMasterProperties(String prjDir) throws Exception
  {
    Properties masterProps = new Properties();
    String masterPropName = "master.properties";
    File master = new File(prjDir, masterPropName);
    if (!master.exists())
      System.out.println("\"master.properties\" not found in \"" + prjDir + "\"");
    else      
      masterProps.load(new FileInputStream(master));
    // Patch properties with System props.
    Set<Object> keySet = masterProps.keySet();
    Iterator<Object> iterator = keySet.iterator();
    while (iterator.hasNext())
    {  
      Object obj = iterator.next();
      if (obj instanceof String)
      {
        String key = (String)obj;
        String value = masterProps.getProperty(key);
        value = GnlUtilities.substituteVariable(value, System.getProperties(), true);
        masterProps.setProperty(key, value);
      }
      else
        System.out.println("Weird key:" + obj.getClass().getName());
    }
    return masterProps;
  }
  
  
  public static Properties patchProperties(Properties p2patch, ArrayList<Properties> pa, boolean systemEnvCheck)
  {
    Properties p = p2patch;
    for (Properties prop : pa)
    {
      Set<Object> keySet = p.keySet();
      Iterator<Object> iterator = keySet.iterator();
      while (iterator.hasNext())
      {  
        Object obj = iterator.next();
        if (obj instanceof String)
        {
          String key = (String)obj;
          String value = p.getProperty(key);
          value = GnlUtilities.substituteVariable(value, prop, systemEnvCheck);
          p.setProperty(key, value);
        }
        else
          System.out.println("Weird key:" + obj.getClass().getName());
      }
    }    
    return p;
  }
  
  // For tests
  public static void main1(String[] args) throws Exception
  {
    String from = 
      "<ns:root xmlns:ns=\"urn:akeu\">" +
      "  <ns:one>Akeu</ns:one>" +
      "  <ns:two>Coucou</ns:two>" +
      "</ns:root>";
    String to = 
      "<ns:root-to xmlns:ns=\"urn:coucou\">" +
      "  <ns:one-to>XXX</ns:one-to>" +
      "  <ns:two-to>YYY</ns:two-to>" +
      "</ns:root-to>";
    XMLElement docFrom = stringToXML(from);
    XMLElement docTo   = stringToXML(to);
    docTo = patchXML(docFrom, docTo, "//{urn:akeu}two", "//{urn:coucou}one-to");
    docTo = patchXML(docFrom, docTo, "//{urn:akeu}one", "//{urn:coucou}two-to");
    docTo.print(System.out);
  }

  public static void main(String[] args) throws Exception
  {
    System.setProperty("some.system.value", "*** Substituted Variable (System) ***");
    Properties props = readMasterProperties();
    System.out.println("-- Master Properties --");
    Set<Object> keySet = props.keySet();
    Iterator<Object> iterator = keySet.iterator();
    while (iterator.hasNext())
    {  
      Object obj = iterator.next();
      if (obj instanceof String)
      {
        String key = (String)obj;
        String value = props.getProperty(key);
//      value = GnlUtilities.substituteVariable(value, System.getProperties(), true);
        System.out.println("[" + key + "] = [" + value + "]");
      }
      else
        System.out.println("Weird key:" + obj.getClass().getName());
    }
    
    if (false)
    {
      // Test: Environment Variables
      Map<String, String> env = System.getenv();
      Set<String> keys = env.keySet();
      Iterator<String> it = keys.iterator();
      while (it.hasNext())
      {  
        String s = it.next();
        String val = env.get(s);
        System.out.println("[" + s + "] = [" + val + "]");
      }
    }
    
    Properties propSet = new Properties();
    propSet.setProperty("akeu.coucou.1", "${protocol.4.rmi}://${ADE_VIEW_ROOT}");
    propSet.setProperty("akeu.coucou.2", "${machine.name}:${soa.port.number}");
    
    ArrayList<Properties> pa = new ArrayList<Properties>(2);
    pa.add(props);
    pa.add(System.getProperties());
    propSet = patchProperties(propSet, pa, true);
    Set<Object> k = propSet.keySet();
    Iterator<Object> i = k.iterator();
    while (i.hasNext())
    {  
      Object obj = i.next();
      if (obj instanceof String)
      {
        String key = (String)obj;
        String value = propSet.getProperty(key);
        System.out.println("[" + key + "] = [" + value + "]");
      }
      else
        System.out.println("Weird key:" + obj.getClass().getName());
    }
  }


  public static class PatchNSResolver implements NSResolver
  {
    private HashMap<String, String> map = null;
    
    public PatchNSResolver(HashMap<String, String> map)
    {
      this.map = map;
    }
    
    public String resolveNamespacePrefix(String prefix)
    {
      return map.get(prefix);
    }
  }

}
