package com.carlgira.soapui.projectgen.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.IOUtils;

/**
 * Class of file utilities to the SoapUIProjectGenerator
 * @author Carlos Giraldo. carlgira@gmail.com 
 */
public class SoapUIFileUtility
{
    /**
     * Utility to find all the WSDLs of a directory. Returns a list of Strings
     *
     * @param file Directory to look up for WSLDs
     * @return Returns a List of
     */
    public Set<String> findWSDLs(File file) 
    {
        Set<String> result = new HashSet<String>();

        if (file.isDirectory()) 
        {
            File[] list = file.listFiles();
            Arrays.sort(list);
            if (list != null) 
            {
                for (File fil : list) 
                {
                    if (getFileExtension(fil).equalsIgnoreCase(".wsdl")) 
                    {
                        result.add(fil.getAbsolutePath());
                    }
                }
            }
        } 
        else if (file.isFile() && getFileExtension(file).equalsIgnoreCase(".wsdl")) 
        {
            result.add(file.getAbsolutePath());
        }
        return result;
    }

    /**
     * Util to the extension of a File class
     *
     * @param file
     * @return
     */
    public String getFileExtension(File file) 
    {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
       
        if (lastIndexOf == -1) 
        {
            return "";
        }
        return name.substring(lastIndexOf);
    }
    
    /**
     * Load the content of a file into a String
     *
     * @param fileName FileName of the file in the resources folder
     * @return Content of a file
     * @throws IOException
     */
    public String loadFileToString(String fileName) {
 
	String result = "";
 
	ClassLoader classLoader = getClass().getClassLoader();
	try 
        {
	    result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
	} 
        catch (IOException e) 
        {
		e.printStackTrace();
	}
 
	return result;
  }
}
