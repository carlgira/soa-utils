package com.carlgira.weblogic.loggging.log4j.managers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import weblogic.logging.LogFileFormatter;
import weblogic.logging.WLLogRecord;

public class OSBProxyLogManager extends OSBLogManager
{
	private static Pattern patternServiceRef = Pattern.compile("Service\\sRef");
	private static Pattern patternProxyName = Pattern.compile("(Service\\sRef\\s=\\s)(.*?)\\n");

	public OSBProxyLogManager()
	{
		super();
		setLoggerPatternRegex(OSBLogManager.osbLogPattern);
	}

	@Override
	/**
	 * Gets the ProxyName from the logRecord. It uses regex to find it.
	 * It saves the loggerName temporarily in the MDC so the other logRecords could use it. 
	 */
	public String getLoggerName(WLLogRecord logRecord)
	{
		String message = logRecord.getMessage();
		Matcher matcherServiceRef = patternServiceRef.matcher(message);
		String cid = logRecord.getTransactionId();
		loggerName = OSBLogManager.osbloggerName;

		if (matcherServiceRef.find())
		{
			try
			{
				if (MDC.get(cid) == null)
				{
					// Must replace the / because log4j can't recognize that
					// character
					loggerName = parseProxyName(message).replaceAll("/", "-");
					MDC.put(cid, loggerName);
				}
			}
			catch (Exception e)
			{
			}
		}

		if (MDC.get(cid) != null)
		{
			loggerName = MDC.get(cid).toString();
		}
		return loggerName;
	}

	/**
	 * Parse the name of a ProxyService from the logMessage. If cant find the ProxyName it throws an Exception
	 * @param message The logMessage
	 * @return Name of ProxyService
	 * @throws Exception Throws an exception if cant find the ProxyServiceName
	 */
	private String parseProxyName(String message) throws Exception
	{
		String response = "";
		Matcher matcher = patternProxyName.matcher(message);

		if (matcher.find())
		{
			response = matcher.group(2);
			if (response != null && response.trim().length() > 0)
			{
				return response;
			}
		}
		throw new Exception();
	}

	@Override
	/**
	 * It look for the logger first and then write the message
	 */
	public void log(Priority priority, WLLogRecord wlsRecord)
	{
		logger = Logger.getLogger(getLoggerName(wlsRecord));
		logger.log(priority, new LogFileFormatter().format(wlsRecord));
	}
}
