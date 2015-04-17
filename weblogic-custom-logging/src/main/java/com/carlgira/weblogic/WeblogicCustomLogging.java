package com.carlgira.weblogic;

import java.util.logging.Handler;
import java.util.logging.Logger;
import weblogic.logging.LoggingHelper;

public class WeblogicCustomLogging
{
	public static void main(String args[])
	{
		try
		{
			Logger logger = LoggingHelper.getServerLogger();
			Handler handler = new LogHandler();
			logger.addHandler(handler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
