package testing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GnlUtilities
{
  public static void resolveClassPath() throws Exception
  {
    ArrayList<String> missingBits = new ArrayList<String>();
    String classpath = System.getProperty("java.class.path");
    String[] cpElements = classpath.split(File.pathSeparator);
    for (int i=0; i<cpElements.length; i++)
    {
      File f = new File(cpElements[i]);
      if (!f.exists())
      {
        System.out.println("Missing " + cpElements[i]);
        missingBits.add(cpElements[i]);
      }
      else
        System.out.println("Found " + cpElements[i]);
    }
    if (missingBits.size() > 0)
    {
      String errorMessage = "Missing Elements:\n";
      for (String s:missingBits)
      {
        errorMessage += (s + "\n");
      }
      throw new Exception(errorMessage);
    }
  }
  
  public final static String searchAlongClasspath(String fileName)
  {
    String fullPath = "";
    String classpath = System.getProperty("java.class.path");
    String[] cpElements = classpath.split(File.pathSeparator);
    for (int i=0; i<cpElements.length; i++)
    {    
      String dir = cpElements[i];
      File directory = new File(dir);
      if (directory.isDirectory())
      {
//s      System.out.println("Searching " + fileName + " in " + dir);
        File tentative = new File(directory, fileName);
        if (tentative.exists())
        {
          return tentative.getAbsolutePath();
        }
      }
    }
    return fullPath;
  }
  
  public final static String findResource(Object parent, String resource)
  {
    String found = "";
    
    URL url = parent.getClass().getClassLoader().getResource(resource);
    if (url != null)
      found = url.toExternalForm();
    
    return found;
  }
  
  public final static String findResourceAsFile(Object parent, String resource)
  {
    String found = findResource(parent, resource);
    if (found.startsWith("file:"))
    {
      found = found.substring("file:".length());
      found = found.replace('/', File.separatorChar);
    }
    else
      found = null;
    
    return found;
  }
  
  public static String replaceString(String orig, 
                                     String oldStr,
                                     String newStr)
  {
    String ret = orig;
    int indx = 0;
    for (boolean go = true; go; )
    {
      indx = ret.indexOf(oldStr, indx);
      if (indx < 0)
      {
        go = false;
      }
      else
      {
        ret =
            ret.substring(0, indx) + newStr + ret.substring(indx + oldStr.length());
        indx += 1 + oldStr.length();
      }
    }
    return ret;
  }
  
  public static void dumpFileContent(String filename) 
  {
    try 
    {
      File f = new File(filename);
      if (!f.exists())
        System.out.println(filename + " not found...");
      else
      {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = "";
        boolean keepDumping = true;
        while (keepDumping)
        {
          line = br.readLine();
          if (line == null)
            keepDumping = false;
          else
          {
            System.out.println(line);
          }
        }
        br.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public static String substituteVariable(String originalString, Properties props)
  {
    return substituteVariable(originalString, props, false);
  }
  
  public static String substituteVariable(String originalString, Properties props, boolean checkEnvVars)
  {
    String patched = originalString;

    boolean keepLooping = true;    
    int fromIdx = 0;
    while (keepLooping)
    {
      int idx = patched.indexOf("${", fromIdx);
      if (idx == -1)
        keepLooping = false;
      else
      {    
        int closingBracketIdx = patched.indexOf("}", idx);
        if (closingBracketIdx == -1)
          throw new RuntimeException("Missing closing brace in " + originalString);
        else
        {
          String matchingString = patched.substring(idx, closingBracketIdx + 1);
          String variableName = matchingString.substring(2, matchingString.length() - 1);
          String variableValue = null;
          if (props != null)
            variableValue = props.getProperty(variableName, null);
          if (variableValue != null)
            patched = replaceString(patched, matchingString, variableValue);
          else
          {
            if (checkEnvVars)
            {
              Map<String, String> env = System.getenv();
              variableValue = env.get(variableName);
              if (variableValue != null)
                patched = replaceString(patched, matchingString, variableValue);
              else              
              fromIdx = closingBracketIdx;
            }
            else
              fromIdx = closingBracketIdx;
          }
        }
      }
    }
    return patched;
  }
  
  public static void main(String[] args)
  {
    try
    {
//    resolveClassPath();
      System.setProperty("coucou", "Oliv");
      String s = substituteVariable("Akeu ${coucou} ${tiens.fume} Larigou", System.getProperties());
      System.out.println("Replaced:" + s);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    System.out.println("Returned:" + searchAlongClasspath("Policy.xml"));
    System.out.println("Returned:" + findResource(new GnlUtilities(), "Policy.xml"));
    System.out.println("Returned:" + findResourceAsFile(new GnlUtilities(), "Policy.xml"));
  }

}
