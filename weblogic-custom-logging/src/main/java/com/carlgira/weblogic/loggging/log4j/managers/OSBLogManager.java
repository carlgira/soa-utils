package com.carlgira.weblogic.loggging.log4j.managers;

import java.util.logging.LogRecord;

import weblogic.logging.WLLogRecord;

import com.carlgira.weblogic.loggging.log4j.Log4jManager;

public class OSBLogManager extends Log4jManager
{
	private static String osbloggerName = "OSBServerLogger";
	
	private static String osbLogPattern = "\\[OSB\\sTracing\\]";
	
	public OSBLogManager()
	{
		super(osbloggerName, osbLogPattern);
	}
}