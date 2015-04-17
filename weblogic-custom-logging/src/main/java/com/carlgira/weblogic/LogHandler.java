package com.carlgira.weblogic;

import java.util.HashMap;
import weblogic.logging.WLLevel;
import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.carlgira.weblogic.util.LoggingLevelUtil;

import weblogic.logging.LoggingHelper;
import weblogic.logging.WLLogRecord;

public class LogHandler extends Handler
{
	
	private Log4jManager logManager;
	
	public LogHandler()
	{
		logManager = new Log4jManager();
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
		logManager.log(loggingLevelUtil.getLevel(wlsRecord.getLevel()), wlsRecord.getMessage());
	}

	@Override
	public void flush()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws SecurityException
	{
		// TODO Auto-generated method stub
		
	}
}
