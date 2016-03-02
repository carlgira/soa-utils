package util.javautil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import java.net.URL;

import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import testing.util.GnlUtilities;

public class JavaUtilities 
{
  private static FileFilter fnf = null;
  
  public static ArrayList<String> searchInDir(String startFrom, String theClassWeLookFor) throws Exception
  {
    String dir = startFrom;
//  System.out.println("Starting from " + dir);
    File top = new File(dir);
    File[] files = top.listFiles(fnf = new FileFilter()
      {
        public boolean accept(File file) 
        {
            return file.isDirectory() || 
                   file.getName().toUpperCase().endsWith(".JAR") || 
                   file.getName().toUpperCase().endsWith(".ZIP");
        }
      });
    return drillDown(files, theClassWeLookFor);
  }
  
  public static ArrayList<String> searchAlongClasspath(String theClassWeLookFor) throws Exception
  {
    ArrayList<String> al = null;
    String classpath = System.getProperty("java.class.path");
//  System.out.println("Classpath:[" + classpath + "]");
    StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
    while (st.hasMoreTokens() && al == null)
    {
      String pathElement = st.nextToken();
//    System.out.println("-> " + pathElement);
      File pe = new File(pathElement);
      al = drillDown(new File[] { pe }, theClassWeLookFor);
    }
    return al;
  }
  
  private static ArrayList<String> drillDown(File[] f, String resource) throws Exception
  {
    ArrayList<String> al = null;
    for (int i=0; i<f.length; i++)
    {
      if (f[i].isDirectory())
      {
        File[] files = f[i].listFiles(fnf);
        drillDown(files, resource);
      }
      else
      {
//      System.out.println(f[i].getAbsolutePath());
        ZipFile zf = null;
        try { zf = new ZipFile(f[i]); }
        catch (Exception ex)
        {
          System.err.println("Opening " + f[i].getAbsolutePath());
          System.err.println(ex.getLocalizedMessage());
          continue;
        }
        Enumeration e = zf.entries();
        while (e.hasMoreElements())
        {
          ZipEntry ze = (ZipEntry)e.nextElement();
          if (ze.toString().indexOf(resource) > -1)
          {
            String resourceURL = "jar:" + f[i].toURI().toURL().toString() + "!/" + ze.toString();
//          System.out.println(resource + " (" + ze.toString() + ") found in " + f[i].getAbsolutePath());
//          System.out.println("URL:[" + resourceURL + "]");
            al = new ArrayList<String>(3);
            al.add(resourceURL);
            al.add(ze.toString());
            al.add(f[i].getAbsolutePath());
            break;
          }
        }
      }
    }
    return al;
  }
  
  public static void searchReplace(String fromFilePath,
                                   String toFilePath,
                                   String search,
                                   String replace) throws Exception
  {
    BufferedReader br = new BufferedReader(new FileReader(fromFilePath));
    BufferedWriter bw = new BufferedWriter(new FileWriter(toFilePath));
    String line = "";
    boolean keepGoing = true;
    while (keepGoing)
    {
      line = br.readLine();
      if (line == null)
        keepGoing = false;
      else
      {
        line = GnlUtilities.replaceString(line, search, replace);
        bw.write(line + "\n");
      }
    }
    br.close();
    bw.close();
  }
  
  public static void main(String[] args) throws Exception
  {
    if (args.length != 4)
      throw new IllegalArgumentException("Need 4 parameters: from to search replace");
    
    System.out.println("Patching " + args[0] + " into " + args[1]);
    System.out.println("Replacing " + args[2] + " with " + args[3]);
    searchReplace(args[0], args[1], args[2], args[3]);
  }
}