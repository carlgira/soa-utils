package unittests.patterns.adfbc;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Properties;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLAttr;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import unittests.util.Utilities;

public class ADFbcUtils
{
  private static DOMParser parser = new DOMParser();
  
  private final static String XPATH_PROP_RADICAL = "xpath.to.patch.output.";
  private final static String XPATH_PROP_PATCH   = "value.to.patch.output.";
  private final static String NODE_PROP_PATCH    = "node.to.patch.output.";

  public static String patchResponse(String response, boolean verbose, Properties props)
  {
    final int TEXT_PATCH = 0;
    final int NODE_PATCH = 1;
    
    int patchOption = -1;
    XMLElement nodeToPatch = null;
    
    String newResponse = response;
    try
    {
      XMLDocument doc = null;
      synchronized (parser)
      {
        parser.parse(new StringReader(newResponse));
        doc = parser.getDocument();
      }
      boolean keepLooping = true;
      int index = 1;
      while (keepLooping)
      {
        String xpathPropName      = XPATH_PROP_RADICAL + Integer.toString(index);
        String patchValuePropName = XPATH_PROP_PATCH   + Integer.toString(index);
        String nodeValuePropName  = NODE_PROP_PATCH    + Integer.toString(index);
        
        if (verbose)
          System.out.println("Looking for " + xpathPropName);
        
        String xPathProp = props.getProperty(xpathPropName, null);
        if (xPathProp == null)
        {
          keepLooping = false;
          if (verbose)
            System.out.println("Patch process interrupted at loop #" + Integer.toString(index));
        }
        else
        {
          String patchValue = props.getProperty(patchValuePropName, null);
          if (patchValue == null) // Then it may be a node replacement
          {
            String nodeProp = props.getProperty(nodeValuePropName, null);
            if (nodeProp != null)
            {
              patchOption = NODE_PATCH;
              // Get the node to patch the document with
              synchronized (parser)
              {
                try
                {
                  parser.parse(new StringReader(nodeProp));
                  XMLDocument nodeDoc = parser.getDocument();
                  nodeToPatch = (XMLElement)nodeDoc.getDocumentElement();
                }
                catch (Exception ex)
                {
                  throw new RuntimeException("Error parsing " + nodeProp, ex);
                }
              }
            }            
          }
          else
            patchOption = TEXT_PATCH;
          HashMap<String, String> map = Utilities.prepareNSHashMap(xPathProp);
          String newXPath = Utilities.patchXPath(xPathProp, map);
          Utilities.PatchNSResolver resolver = new Utilities.PatchNSResolver(map);
          
          if (verbose)
          {
            System.out.println("Searching [" + newXPath + "]");
            System.out.println("Replacing with [" + (patchOption == TEXT_PATCH?patchValue:"<some new node>") + "]");
          }
          
          NodeList nl = doc.selectNodes(newXPath, resolver);
          for (int i=0; i<nl.getLength(); i++)
          {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
              XMLElement xelm = (XMLElement)nl.item(i); // Found the element to patch
              if (patchOption == TEXT_PATCH)
              {
                try { xelm.getFirstChild().setTextContent(patchValue); } catch (Exception ignore) {}
              }
              else if (patchOption == NODE_PATCH)
              {
                Node node = xelm.getParentNode();
                node.removeChild(xelm);              
                node.appendChild(doc.adoptNode(nodeToPatch));
              }
              else
                throw new RuntimeException("Patching #" + Integer.toString(index) + ": no " + patchValuePropName + " and no " + nodeValuePropName + ".");
            }
            else if (nl.item(i).getNodeType() == Node.ATTRIBUTE_NODE)
            {
              if (patchOption == TEXT_PATCH)
                ((XMLAttr)nl.item(i)).setNodeValue(patchValue);
              else
                throw new RuntimeException("Cannot insert a Node into an Attribute!");
            }
          }
          index++;
        }
      }
      StringWriter sw = new StringWriter();
      doc.print(sw);
      newResponse = sw.toString();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return newResponse;
  }
}
