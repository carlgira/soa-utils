package com.carlgira.rules.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import oracle.rules.sdk2.dictionary.RuleDictionary;
import oracle.rules.sdk2.exception.SDKException;
import oracle.rules.sdk2.exception.SDKWarning;

/**
 * 
 * @author carlgira
 *
 */
public class DictionaryUtil
{
	public static void checkDictionary(RuleDictionary ruleDictionary) throws Exception
	{
		if(ruleDictionary == null)
        {
        	System.out.println("Null Dictionary");
        	return ;
        }
		
		List<SDKWarning> warnings = new ArrayList<SDKWarning>();
		ruleDictionary.update(warnings);

        if (warnings.size() > 0)
        {
          System.out.println("Validation warnings:\n" + warnings);
          throw new Exception("Problem with dictionary, check for warnings");
        }
        else
        {
        	System.out.println("No Warnings");
        }
	}
	
	public static RuleDictionary loadDictionary(String pathToDictionary, ListRuleFinder memoryRuleFinder) throws SDKException, IOException
	{
		Reader reader = new FileReader(new File(pathToDictionary));
		RuleDictionary dict = RuleDictionary.readDictionary(reader, memoryRuleFinder );		
        return dict;
	}
}
