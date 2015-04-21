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

public class LogHandler extends Handler
{
	private List<Log4jManager> logManagers;
	
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
