package com.carlgira.weblogic.loggging.log4j;

import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Priority;

import weblogic.logging.LogFileFormatter;
import weblogic.logging.WLLogRecord;
import weblogic.servlet.logging.LogFormat;

public abstract class Log4jManager
{
	protected Logger logger;
	
	protected String loggerName;
	
	protected Pattern loggerPatternPattern;
    
    protected Log4jManager()
    { 
        logger = Logger.getRootLogger();
	}
    
    protected Log4jManager(String loggerName, String loggerPatternRegex) 
    { 
        logger = Logger.getLogger(loggerName);
        this.loggerPatternPattern = Pattern.compile(loggerPatternRegex);
	}
    
    public void log(Priority priority, WLLogRecord wlsRecord)
    {
    	logger.log(priority, new LogFileFormatter().format(wlsRecord) );
    }
    
	public void setLoggerPatternRegex(String loggerPatternRegex)
	{
		this.loggerPatternPattern = Pattern.compile(loggerPatternRegex);
	}
	
	public String getLoggerName(WLLogRecord logRecord)
	{
		return this.loggerName;
	}
	
	public boolean isALogRecord(WLLogRecord logRecord)
	{
		Matcher matcherFilter = loggerPatternPattern.matcher(logRecord.getMessage());
		return matcherFilter.find();
	}
}
