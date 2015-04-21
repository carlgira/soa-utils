package com.carlgira.weblogic.loggging.log4j.managers;

import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Priority;

import weblogic.logging.LogFileFormatter;
import weblogic.logging.WLLogRecord;

import com.carlgira.weblogic.loggging.log4j.Log4jManager;

public class IntroScopeLogManager extends Log4jManager
{
	private static String instroScopeloggerName = "IntroScopeLogger";
	
	private static String instroScopeLogPattern = "IntroscopeAgent";
	
	public IntroScopeLogManager()
	{
		super(instroScopeloggerName,instroScopeLogPattern );
	}
	
    public void log(Priority priority, WLLogRecord wlsRecord)
    {
    	logger.log(priority, wlsRecord.getMessage());
    }
}