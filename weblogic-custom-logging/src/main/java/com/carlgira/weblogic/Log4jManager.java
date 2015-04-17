package com.carlgira.weblogic;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class Log4jManager
{
    private Logger logger;
    
    public Log4jManager() 
    { 
        logger = Logger.getRootLogger();
	}
    
    public void log(Priority priority, String message)
    {
    	logger.log(priority, message);
    }
}
