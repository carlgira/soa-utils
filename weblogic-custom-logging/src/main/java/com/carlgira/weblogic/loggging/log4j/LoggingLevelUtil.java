package com.carlgira.weblogic.loggging.log4j;

import java.util.HashMap;

import org.apache.log4j.Priority;
import java.util.logging.Level;
/**
 * This class maintains the equivalences between Weblogic log level and Log4j log level
 * @author carlgira
 *
 */
public class LoggingLevelUtil
{
	/**
	 * Map to maintain equivalences between log levels
	 */
	private HashMap<Level, Priority> loggingLevelMap;
	
	/**
	 * LoggingLevelUtil Constructor
	 */
	public LoggingLevelUtil()
	{
		this.loggingLevelMap = new HashMap<Level, Priority>();
		
		this.loggingLevelMap.put(Level.SEVERE, Priority.FATAL);
		this.loggingLevelMap.put(Level.WARNING, Priority.WARN);
		this.loggingLevelMap.put(Level.CONFIG, Priority.INFO);
		this.loggingLevelMap.put(Level.INFO, Priority.INFO);
		this.loggingLevelMap.put(Level.FINE, Priority.INFO);
		this.loggingLevelMap.put(Level.FINER, Priority.DEBUG); // FIX You can fix this in superior version of log4j using TRACE Level
		this.loggingLevelMap.put(Level.FINEST, Priority.DEBUG); // FIX You can fix this in superior version of log4j using TRACE Level
	}

	/**
	 * Get the Log4j log level from Weblogic log level
	 * @param level
	 * @return
	 */
	public Priority getLevel(Level level)
	{
		return loggingLevelMap.get(level);
	}
}
