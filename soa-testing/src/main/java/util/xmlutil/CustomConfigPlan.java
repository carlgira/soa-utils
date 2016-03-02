package util.xmlutil;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.StringReader;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.httputil.proxyimpl.ReferenceProxy;

public class CustomConfigPlan
{
  /**
   * Patch for composite.xml
   * For features not (yet) implemented in Config plan.
   * 
   * @param compositeFullPath
   * @param pathToNode
   * @param patchHolder
   * @throws Exception
   */
  public static void replaceNodeInComposite(String compositeFullPath,
                                            String pathToNode,
                                            String patchHolder)
    throws Exception
  {
    DOMParser parser = new DOMParser();
    XMLDocument doc = null;
    try
    {
      parser.parse(new File(compositeFullPath).toURI().toURL());
      doc = parser.getDocument();
      NSResolver nsr = ReferenceProxy.generateNSR(pathToNode);
      String rxp = ReferenceProxy.reworkXPath(pathToNode);
      NodeList nl = doc.selectNodes(rxp, nsr);
      if (nl.getLength() != 1)
      {
        throw new RuntimeException("Bad cardinality (expected 1) for XPath:" + pathToNode + 
                                   " returned " + nl.getLength() + " node(s).");
      }
      
      StringBuffer sb = new StringBuffer();
      BufferedReader br = new BufferedReader(new FileReader(patchHolder));
      
      String line = "";
      boolean loop = true;
      while (loop)
      {
        line = br.readLine();
        if (line == null)
          loop = false;
        else
          sb.append(line + "\n");
      }
      br.close();
      parser.parse(new StringReader(sb.toString()));
      Node node = parser.getDocument().getDocumentElement();
      Node newNode = doc.adoptNode(node);
      
      Node nodeToReplace = nl.item(0);
      Node parent = nodeToReplace.getParentNode();
      parent.replaceChild(newNode, nodeToReplace);
      
      FileOutputStream fos = new FileOutputStream(compositeFullPath);   
      doc.print(fos);
      fos.close();
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }
  
  private static String composite2patch = "";
  private static String xPath = "";
  private static String newContent = "";
  
  private final static String COMPOSITE_LOCATION = "-composite-location";
  private final static String PATH_TO_NODE       = "-path-to-node";
  private final static String NEW_NODE_CONTENT   = "-new-node-content";
  
  private static void parseArgs(String[] args)
  {
    for (int i=0; i<args.length; i++)
    {
      if (COMPOSITE_LOCATION.equals(args[i]))
        composite2patch = args[i+1];
      else if (PATH_TO_NODE.equals(args[i]))
        xPath = args[i+1];
      else if (NEW_NODE_CONTENT.equals(args[i]))
        newContent = args[i+1];
    }
  }
  
  /*
   * Usage:
   *   java CustomConfigPlan -composite-location composite2patch.xml 
   *                         -path-to-node "//{http://xmlns.oracle.com/sca/1.0}reference[@name = 'HRAccess']" 
   *                         -new-node-content newContent.xml
   */
  public static void main(String[] args) throws Exception
  {
    composite2patch  = "composite2patch.xml";
    xPath            = "//{http://xmlns.oracle.com/sca/1.0}reference[@name = 'HRAccess']";
    newContent       = "newContent.xml";
    
    if (args.length == 6)
    {
      parseArgs(args);
    }
    
    try
    {
      // Make a backup of the original...
      
      replaceNodeInComposite(composite2patch, xPath, newContent);
    }
    catch (Exception e)
    {
      throw e;
    }
  }
}
