package util.xmlutil;
/* $Header: fatools/opensource/utilities/programmingutilities/TestingGenericUtilities/src/util/xmlutil/SchemaSucker.java /main/4 2009/08/12 16:22:35 olediour Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    olediour    10/24/08 - Schema Sucker
    olediour    10/24/08 - Creation
 */

/**
 *  @version $Header: fatools/opensource/utilities/programmingutilities/TestingGenericUtilities/src/util/xmlutil/SchemaSucker.java /main/4 2009/08/12 16:22:35 olediour Exp $
 *  @author  olediour
 *  @since   release specific (what release of product did this appear in)
 */

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.net.URLConnection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.HashMap;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;

import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

/**
 * This utility has been created to be able to resolve all the dependencies an
 * XML Schema can have, even when working off the network, or from JDev, which
 * does not like the schema URLs usingthe http protocol...
 * 
 * Run this class, with the schema location to start from as parameter (this schema
 * needs to be on the file system, for now.
 * 
 * The utility will create <b>all files</b> in the directory it running from,
 * the root one will be duplicated (named closed_[originalName]).
 * All the subsequent schemas will be named 0000000.xsd, 0000001.xsd, etc.
 * All schemas refering to another remote schema will be patched to referer to
 * the local copy made by the utility, so the closed_[originalName] has all its
 * remote dependencies now local.
 * 
 * The idea here was to KISS (Keep It Small and Stupid).
 * 
 * @author olivier.lediouris@oracle.com
 */
public class SchemaSucker
{
  private final static String DEFAULT_SCHEMA = "AssignLaunch.xsd"; // For tests.
  private final static String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
  private final static String WSDL_URI   = "http://schemas.xmlsoap.org/wsdl/";
  
  private static HashMap<String, String> namesMap = new HashMap<String, String>();
  
  private static DOMParser parser = new DOMParser();
  
  private static int nbDocument = 0;
  private final static NumberFormat nf = new DecimalFormat("0000000");
  
  public static void main(String[] args) throws Exception
  {
//  String firstWSDL = "http://fpp-ta05.us.oracle.com:7501/HcmCore-HcmPeopleRolesUserDetailsService-context-root/UserDetailsService?wsdl";
    String firstWSDL = "http://fpp-ta05.us.oracle.com:7501/HcmCore-HcmUsersUserService-context-root/UserService?WSDL";
//  String firstWSDL = "http://fpp-ta05.us.oracle.com:7501/HcmCore-HcmUsersLdapRequestService-context-root/LdapRequestService?WSDL";
    String directoryName = "./akeu/coucou";

    if (args.length > 0)
    {
      if (args.length != 2)
      {
        System.out.println("You have entered " + args.length + " parameters:");
        for (int i=0; i<args.length; i++)
          System.out.println("- " + args[i]);
        System.out.println();
        System.out.println("Parameters are:");
        System.out.println("\tDirectoryName");
        System.out.println("\tURL (or location) of the first WSDL");
        throw new IllegalArgumentException("Bad number of parameters");
//      System.exit(0);
      }
      directoryName = args[0];
      firstWSDL = args[1];      
    }
    
    File dir = null;
    try
    {
      dir = new File(directoryName);
      if (dir.exists())
      {
        if (!dir.isDirectory())
        {
          System.out.println(directoryName + " is not a directory...");
          System.exit(1);
        }
      }
      else
      {
        if (!dir.mkdirs())
        {
          System.out.println("Cannot create directory [" + directoryName + "]");
          System.exit(1);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }
    
    long before = System.currentTimeMillis();
    try
    {
      URL wsdlURL = null;
      if (firstWSDL.startsWith("http://"))
        wsdlURL = new URL(firstWSDL);
      else
        wsdlURL = new File(firstWSDL).toURI().toURL();
      
      parser.parse(wsdlURL);
      XMLDocument doc = parser.getDocument();
      fromWSDL(doc, dir, "NEW-ROOT-WSDL.wsdl");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    System.out.println("Done in " + Long.toString(after - before) + " ms.");
  }
  
  public static void main2(String[] args)
  {
    String firstSchema = "";
    if (args.length > 0)
      firstSchema = args[0];
    else
      firstSchema = DEFAULT_SCHEMA;
    
    long before = System.currentTimeMillis();
    try
    {
      parser.parse(new File(firstSchema).toURI().toURL());
      XMLDocument doc = parser.getDocument();
      fromSchema(doc, new File("."), "cloned_" + firstSchema);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    long after = System.currentTimeMillis();
    System.out.println("Done in " + Long.toString(after - before) + " ms.");
  }
  
  private static void fromSchema(XMLDocument doc, File dir, String schemaFileName) throws Exception
  {
//  System.out.println("Managing Imports for " + schemaFileName);
    NodeList imports = doc.selectNodes("//xsd:*[./@schemaLocation]", new NSResolver()
                                       {
                                         public String resolveNamespacePrefix(String string)
                                         {
                                           return SCHEMA_URI;
                                         }
                                       });
//  System.out.println("Found " + imports.getLength() + " schema(s) to import.");
    for (int i=0; i<imports.getLength(); i++)
    {
      XMLElement imp = (XMLElement)imports.item(i);
      String schemaName = imp.getAttribute("schemaLocation");
      if (schemaName.toLowerCase().startsWith("http://"))
      {
        // 0 - Is it is the namesMap yet?
        String fName = namesMap.get(schemaName);
        if (fName != null && fName.trim().length() > 0)
        {
          System.out.println("Schema " + schemaName + " already there as [" + fName + "]");
        }
        else
        {
          // 1 - Download
          URL url = new URL(schemaName);
          System.out.print("Downloading " + schemaName + "...");
          URLConnection newURLConn = url.openConnection();
          InputStream is = newURLConn.getInputStream();
  //      int cl = newURLConn.getContentLength();
  //      System.out.println("Content Length:" + Integer.toString(cl) + " byte(s)");
          
          fName = nf.format(nbDocument++) + ".xsd";
          FileOutputStream fos = new FileOutputStream(new File(dir, fName));
          copy(is, fos);
          fos.close();
          System.out.println(" Saved as " + fName);
          namesMap.put(schemaName, fName);
        }
        // 2 - Patch
//      System.out.println("Changing [" + schemaName + "] into [" + fName + "] for [" + schemaFileName + "]");
        imp.setAttribute("schemaLocation", fName);
        // 3 - Parse
        parser.parse(new File(dir, fName).toURI().toURL());
        XMLDocument subSchema = parser.getDocument();
        // 4 - Go!
        fromSchema(subSchema, dir, fName);
      }
    }
    // Spit out the modified one
    OutputStream os = new FileOutputStream(new File(dir, schemaFileName));
    doc.print(os);
    os.close();
  }
  
  private static void fromWSDL(XMLDocument doc, File dir, String documentFileName) throws Exception
  {
  //  System.out.println("Managing Imports for " + schemaFileName);

    // Part One: WSDL Imports
    NodeList imports = doc.selectNodes("//wsdl:import[./@location]", new NSResolver()
                                       {
                                         public String resolveNamespacePrefix(String string)
                                         {
                                           return WSDL_URI;
                                         }
                                       });
  //  System.out.println("Found " + imports.getLength() + " schema(s) to import.");
    for (int i=0; i<imports.getLength(); i++)
    {
      XMLElement imp = (XMLElement)imports.item(i);
      String documentName = imp.getAttribute("location");
      if (documentName.toLowerCase().startsWith("http://"))
      {
        // 0 - Is it is the namesMap yet?
        String fName = namesMap.get(documentName);
        if (fName != null && fName.trim().length() > 0)
        {
          System.out.println("WSDL " + documentName + " already there as [" + fName + "]");
        }
        else
        {
          // 1 - Download
          URL url = new URL(documentName);
          System.out.print("Downloading " + documentName + "...");
          URLConnection newURLConn = url.openConnection();
          InputStream is = newURLConn.getInputStream();
  //      int cl = newURLConn.getContentLength();
  //      System.out.println("Content Length:" + Integer.toString(cl) + " byte(s)");
          
          fName = nf.format(nbDocument++) + ".wsdl";
          FileOutputStream fos = new FileOutputStream(new File(dir, fName));
          copy(is, fos);
          fos.close();
          System.out.println(" Saved as " + fName);
          namesMap.put(documentName, fName);
        }
        // 2 - Patch
  //      System.out.println("Changing [" + schemaName + "] into [" + fName + "] for [" + schemaFileName + "]");
        imp.setAttribute("location", fName);
        // 3 - Parse
        parser.parse(new File(dir, fName).toURI().toURL());
        XMLDocument subSchema = parser.getDocument();
        // 4 - Go!
        fromWSDL(subSchema, dir, fName);
      }
    }
    
    // Part Two: Schema Imports
    imports = doc.selectNodes("//xsd:*[./@schemaLocation]", new NSResolver()
                                       {
                                         public String resolveNamespacePrefix(String string)
                                         {
                                           return SCHEMA_URI;
                                         }
                                       });
    //  System.out.println("Found " + imports.getLength() + " schema(s) to import.");
    for (int i=0; i<imports.getLength(); i++)
    {
      XMLElement imp = (XMLElement)imports.item(i);
      String documentName = imp.getAttribute("schemaLocation");
      if (documentName.toLowerCase().startsWith("http://"))
      {
        // 0 - Is it is the namesMap yet?
        String fName = namesMap.get(documentName);
        if (fName != null && fName.trim().length() > 0)
        {
          System.out.println("Schema " + documentName + " already there as [" + fName + "]");
        }
        else
        {
          // 1 - Download
          URL url = new URL(documentName);
          System.out.print("Downloading " + documentName + "...");
          URLConnection newURLConn = url.openConnection();
          InputStream is = newURLConn.getInputStream();
    //      int cl = newURLConn.getContentLength();
    //      System.out.println("Content Length:" + Integer.toString(cl) + " byte(s)");
          
          fName = nf.format(nbDocument++) + ".xsd";
          FileOutputStream fos = new FileOutputStream(new File(dir, fName));
          copy(is, fos);
          fos.close();
          System.out.println(" Saved as " + fName);
          namesMap.put(documentName, fName);
        }
        // 2 - Patch
    //      System.out.println("Changing [" + schemaName + "] into [" + fName + "] for [" + schemaFileName + "]");
        imp.setAttribute("schemaLocation", fName);
        // 3 - Parse
        parser.parse(new File(dir, fName).toURI().toURL());
        XMLDocument subSchema = parser.getDocument();
        // 4 - Go!
        fromSchema(subSchema, dir, fName);
      }
    }
    
    // Spit out the modified one
    OutputStream os = new FileOutputStream(new File(dir, documentFileName));
    doc.print(os);
    os.close();
  }
  
  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    synchronized(in)
    {
      synchronized(out)
      {
        byte buffer[] = new byte[256];
        while (true)
        {
          int bytesRead = in.read(buffer);
          if(bytesRead == -1)
            break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }  
}
