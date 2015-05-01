package com.carlgira.weblogic.loggging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import weblogic.logging.WLLevel;

import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.carlgira.weblogic.loggging.log4j.Log4jManager;
import com.carlgira.weblogic.loggging.log4j.LoggingLevelUtil;

import weblogic.logging.LoggingHelper;
import weblogic.logging.WLLogRecord;
/**
 * A LogHandler class that manages a list of Log4jManager. It runs the list of managers to filter the logRecords
 * @author carlgira
 *
 */
public class LogHandler extends Handler
{
	private List<Log4jManager> logManagers;
	
	/**
	 * 
	 * @param logManagers List of active Log4jManagers
	 */
	public LogHandler(List<Log4jManager> logManagers)
	{
		this.logManagers = logManagers;
		setErrorManager(new ErrorManager()
		{
			public void error(String msg, Exception ex, int code)
			{
				try
				{
					LoggingHelper.getServerLogger().removeHandler(LogHandler.this);
				}
				catch (Exception error)
				{
				}
			}
		});
	}
	

	@Override
	/**
	 * This method runs the logRecord against all the LogManagers to know if it belongs to one of them.
	 * @param logRecord LogRecord to filter
	 */
	public void publish(LogRecord logRecord)
	{
		WLLogRecord wlsRecord = (WLLogRecord) logRecord;
			
		LoggingLevelUtil loggingLevelUtil = new LoggingLevelUtil();
		
		for(Log4jManager log4jManager : logManagers)
		{
			if(log4jManager.isALogRecord(wlsRecord))
			{
				log4jManager.log(loggingLevelUtil.getLevel(wlsRecord.getLevel()), wlsRecord);
			}
		}
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void close() throws SecurityException
	{
		// TODO Auto-generated method stub
		
	}
}
