package util.javautil.sample;

import java.io.File;

import util.xmlutil.XMLUtilities;

public class TXTest
{
  public static void main(String[] args) throws Exception
  {
    String str = XMLUtilities.xslTransform("counting.xml", "process.xsl", "output.xml");
    System.out.println("Produced:" + str);
  }  
}
