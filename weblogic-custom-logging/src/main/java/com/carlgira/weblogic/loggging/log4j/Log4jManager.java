package com.carlgira.weblogic.loggging.log4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import weblogic.logging.LogFileFormatter;
import weblogic.logging.WLLogRecord;

/**
 * This class controls if a logRecord must be filtered.
 * 
 * @author carlgira
 *
 */
public abstract class Log4jManager
{
	protected Logger logger;

	protected String loggerName;

	protected Pattern loggerPattern;

	protected Log4jManager(String loggerName, String loggerPatternRegex)
	{
		logger = Logger.getLogger(loggerName);
		this.loggerPattern = Pattern.compile(loggerPatternRegex);
	}

	/**
	 * Method to log a message
	 * 
	 * @param priority
	 * @param wlsRecord
	 */
	public void log(Priority priority, WLLogRecord wlsRecord)
	{
		logger.log(priority, new LogFileFormatter().format(wlsRecord));
	}

	public void setLoggerPatternRegex(String loggerPatternRegex)
	{
		this.loggerPattern = Pattern.compile(loggerPatternRegex);
	}

	/**
	 * Method to get loggerName. This method can be replaced so the loggerName
	 * can be obtained from the logRecord
	 * 
	 * @param logRecord
	 * @return
	 */
	public String getLoggerName(WLLogRecord logRecord)
	{
		return this.loggerName;
	}

	/**
	 * Checks the logRecord against the loggerPattern Regex
	 * 
	 * @param logRecord
	 * @return
	 */
	public boolean isALogRecord(WLLogRecord logRecord)
	{
		Matcher matcherFilter = loggerPattern.matcher(logRecord.getMessage());
		return matcherFilter.find();
	}
}
