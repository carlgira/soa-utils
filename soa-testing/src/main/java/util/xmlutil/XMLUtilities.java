package util.xmlutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.URL;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Set;

import oracle.xml.parser.schema.XSDBuilder;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.DTD;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParseException;
import oracle.xml.parser.v2.XSLProcessor;
import oracle.xml.parser.v2.XSLStylesheet;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class XMLUtilities
{
  private static DOMParser parser = new DOMParser();
  
  /**
   * 
   * @param dataIn     XML File name
   * @param xsl        XSL File name 
   * @param dataOut    Output file name
   * @throws Exception
   * @return The content of the output file, <b>as a String</b>
   */
  public static String xslTransform(String dataIn, String xsl, String dataOut) throws Exception
  {
    return xslTransform(dataIn, xsl, dataOut, null);
  }
  
  /**
   *
   * @param dataIn  XML File name
   * @param xsl     XSL File Name
   * @param dataOut Output File name
   * @param prms    Contains the name/values pairs for the Stylesheet's parameters
   *                Attention: literal values are to be passed between simple quotes, like "'value-one'";
   * @return The content of the output file, <b>as a String</b>
   * @throws Exception
   */
  public static String xslTransform(String dataIn, String xsl, String dataOut, HashMap prms) throws Exception
  {
    URL xslURL = new File(xsl).toURI().toURL();
//  System.out.println("Transforming using " + xslURL.toString()); 
    parser.parse(xslURL);
    XMLDocument xsldoc = parser.getDocument();           
    
    // instantiate a stylesheet
    XSLProcessor processor = new XSLProcessor();
    processor.setBaseURL(xslURL);
    XSLStylesheet xslss = processor.newXSLStylesheet(xsldoc);
    if (prms != null)
    {
      Set keys = prms.keySet();
      Iterator iterator = keys.iterator();
      while (iterator.hasNext())
      {
        String k = (String)iterator.next();
        xslss.setParam(k, (String)prms.get(k)); // TASK Replace with processor.setParam(), takes 3 parameters.
      }
    }
    
    // display any warnings that may occur
    processor.showWarnings(true);
    processor.setErrorStream(System.err);
    
    parser.parse(new File(dataIn).toURI().toURL());
    XMLDocument xml = parser.getDocument();
    // Process XSL    
    
//  
    File tempFile = File.createTempFile("temp", ".tmp");
    tempFile.deleteOnExit();
    PrintWriter pw = new PrintWriter(tempFile);
//  processor.setParam("xmlnx:url", "prm1", "value1");
    processor.processXSL(xslss, xml, pw);    
    pw.close();
    
    BufferedWriter bw = new BufferedWriter(new FileWriter(dataOut));        
    // Prepare output
    StringBuffer tx = new StringBuffer();
    BufferedReader br = new BufferedReader(new FileReader(tempFile));
    String line = "";
    while ((line = br.readLine()) != null)
    {
      bw.write(line + "\n");
      tx.append(line + "\n");
    }
    bw.close();
    br.close();
    return tx.toString();    
  }
  
  public static String xmlToString(XMLElement doc)
  {
    String str = "";
    StringWriter strw = new StringWriter();
    try
    {
      doc.print(strw);
      str = strw.toString();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return str;
  }
  
  public static XMLElement stringToXML(String str) throws Exception
  {
    parser.parse(new StringReader(str));
    return (XMLElement)parser.getDocument().getDocumentElement();
  }
  
  /**
     * 
     * @param str The content of the XML document to print.
     * @return the content of the incoming XML document provided as a String, nicely indented.
     * @throws Exception
     */
  public static String prettyXMLPrint(String str) throws Exception
  {
    return xmlToString(stringToXML(str));
  }
  
  public static boolean validate(String schemaLocation,
                                 String instanceLocation)
  {
    URL validatorStream = new XMLUtilities().getClass().getResource(schemaLocation);
    return validate(validatorStream, instanceLocation);
  }
  
  public static boolean validate(URL schemaURL,
                                 String instanceLocation)
  {
    boolean b = true;
    File source = new File(instanceLocation);
    try
    {
      URL validatorStream = schemaURL;
      if (!source.exists())
      {
        b = false;
        throw new RuntimeException("Instance document not found");
      }
      URL docToValidate = source.toURI().toURL();
      parser.showWarnings(true);
      parser.setErrorStream(System.out);
      parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
      parser.setPreserveWhitespace(true);
      XSDBuilder xsdBuilder = new XSDBuilder();
      java.io.InputStream is = validatorStream.openStream();
      oracle.xml.parser.schema.XMLSchema xmlSchema = xsdBuilder.build(is, validatorStream);
      parser.setXMLSchema(xmlSchema);
      URL doc = docToValidate;
      parser.setDoctype(new DTD());
      parser.parse(doc);
      /* XMLDocument valid = */parser.getDocument();
//    System.out.println(source.getName() + " is valid");
    }
    catch (Exception ex)
    {
      System.out.println(source.getName() + " is invalid...");
      ex.printStackTrace();
      b = false;
    }
    return b;
  }
  
  public static boolean validate(URL schemaURL,
                                 Reader instance)
  {
    boolean b = true;
    try
    {
      URL validatorStream = schemaURL;
      parser.showWarnings(true);
      parser.setErrorStream(System.err);
      parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
      parser.setPreserveWhitespace(true);
      XSDBuilder xsdBuilder = new XSDBuilder();
      java.io.InputStream is = validatorStream.openStream();
      oracle.xml.parser.schema.XMLSchema xmlSchema = xsdBuilder.build(is, validatorStream);
      parser.setXMLSchema(xmlSchema);
      parser.setDoctype(new DTD());
      parser.parse(instance);
      /* XMLDocument valid = */parser.getDocument();
   // System.out.println("Instance is valid");
    }
    catch (Exception ex)
    {
      System.out.println("invalid instance...");
      ex.printStackTrace();
      b = false;
    }
    return b;
  }
  
  public static boolean validate(XMLDocument schemaRoot,
                                 Reader instance,
                                 URL validatorStream)
  {
    boolean b = true;
    try
    {
      parser.showWarnings(true);
      parser.setErrorStream(System.err);
      parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
      parser.setPreserveWhitespace(true);
      XSDBuilder xsdBuilder = new XSDBuilder();
      oracle.xml.parser.schema.XMLSchema xmlSchema = xsdBuilder.build(schemaRoot, validatorStream);
      parser.setXMLSchema(xmlSchema);
      parser.setDoctype(new DTD());
      parser.parse(instance);
      /* XMLDocument valid = */parser.getDocument();
   // System.out.println("Instance is valid");
    }
    catch (Exception ex)
    {
      System.err.println("invalid instance...");
      System.err.println("ValidatorStream: [" + validatorStream.toExternalForm() + "]");
      ex.printStackTrace();
      b = false;
    }
    return b;
  }
  
  /**
   * For tests
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    String xml = "<root><!-- A comment --><elemt>value</elemt></root>";
    try
    {
      parser.parse(new StringReader(xml));
      XMLDocument doc = parser.getDocument();
      System.out.println(xmlToString(doc));
      
      NodeList childs = doc.getDocumentElement().getChildNodes();
      for (int i=0; i<childs.getLength(); i++)
      {
        Node node = childs.item(i);
        short type = node.getNodeType();
        switch (type)
        {
          case Node.COMMENT_NODE:
            System.out.println("There is a comment");
            System.out.println("Value:" + node.getNodeValue());
            break;
          default:
            System.out.println("Type " + type);
            break;
         }
          
      }
      
//      String result = xslTransform("input-document.xml", "ss.xsl", "new-out.xml");
//      System.out.println(result);
    }
    catch (XMLParseException e)
    {
      e.printStackTrace();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
}
