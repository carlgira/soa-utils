package com.carlgira.weblogic.loggging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.log4j.xml.DOMConfigurator;

import com.carlgira.weblogic.loggging.log4j.Log4jManager;

import weblogic.logging.LoggingHelper;

public class WeblogicCustomLogging
{
	public static void main(String args[])
	{
		Logger logger = LoggingHelper.getServerLogger();
		try
		{
			String log4jFileLocation = args[0];
			long delay = Long.parseLong(args[1]);
			
			if(new File(log4jFileLocation).exists())
			{
				if(delay == 0)
				{
					DOMConfigurator.configureAndWatch(log4jFileLocation, delay);
				}
				else
				{
					DOMConfigurator.configure(log4jFileLocation);
				}
			}
			else
			{
				throw new Exception("Log4j.xml file does not exist - FileLocation: " + log4jFileLocation);
			}
			
			List<Log4jManager> logManagers = new ArrayList<Log4jManager>();
			for(int i=2;i<args.length;i++)
			{
				String logManagerClassName = args[i];
				Class<Log4jManager> logManagerClass = (Class<Log4jManager>) Class.forName(logManagerClassName);
				System.out.println("CLASS "+ logManagerClass.getName());
				Log4jManager logManager = (Log4jManager)logManagerClass.newInstance();
				logManagers.add(logManager);
			}
			Handler handler = new LogHandler(logManagers);
			logger.addHandler(handler);
		}
		catch(Exception e)
		{
			logger.severe("weblogic-custom-logging, Problem starting");
			logger.severe(e.getLocalizedMessage());
			return;
		}
		logger.info("weblogic-custom-logging, Started");
	}
}