package util.javautil.sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;

import util.javautil.JavaUtilities;

import util.xmlutil.XMLUtilities;

public class EOTest
{
  public static void main(String[] args) throws Exception
  {
    String theClassWeLookFor = "jbo_03_01.xsd";
    if (args.length == 1)
      theClassWeLookFor = args[0];
    
    ArrayList<String> result = JavaUtilities.searchInDir(System.getProperty("user.dir"), theClassWeLookFor);
    if (result == null)
      System.out.println(theClassWeLookFor + " not found from " + System.getProperty("user.dir"));
    else
    {
      System.out.println("Found in " + result.get(0));
    }
    result = JavaUtilities.searchAlongClasspath(theClassWeLookFor);
    if (result == null)
      System.out.println(theClassWeLookFor + " not found in classpath");
    else
    {
      System.out.println("Found in " + result.get(0));
      URL url = new URL(result.get(0));
      if (false) // set to true to see the schema code
      {
        URLConnection conn = url.openConnection();
        InputStreamReader is = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(is);
        String line = "";
        while ((line = br.readLine()) != null)
        {
          System.out.println(line);
        }
        is.close();
      }
      boolean valid = XMLUtilities.validate(url, "InternalPayerEO.xml");
      System.out.println("Validation of InternalPayerEO.xml:" + (valid?"successful":"failed"));
    }
  }  
}
