package com.carlgira.weblogic;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import weblogic.logging.WLLevel;
import weblogic.logging.WLLogRecord;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggingTest
{
	@BeforeClass
	public static void loadPropeties()
	{
		System.setProperty("test.log.dir", "/tmp");
		System.setProperty("log4j.configuration", "file:" + LoggingTest.class.getResource("/log4j.xml").getPath());
	}
	
	@Test
	public void test()
	{
		Logger logger = LogManager.getLogManager().getLogger("");
		
		FilterOSB_OHS filter_OSB_OHS = new FilterOSB_OHS();
		
		Handler consoleHandler = logger.getHandlers()[0];
		consoleHandler.setFilter(filter_OSB_OHS);
		
		
		WLLogRecord wlsRecord1 = new WLLogRecord(WLLevel.INFO, "Log INFO before add Handler");
		WLLogRecord wlsRecord2 = new WLLogRecord(WLLevel.WARNING, "Log WARNING before add Handler");
		WLLogRecord wlsRecord3 = new WLLogRecord(WLLevel.WARNING, "OSB Filtered Message");
		WLLogRecord wlsRecord4 = new WLLogRecord(WLLevel.WARNING, "OHS Filtered Message");
		
		logger.log(wlsRecord1);
		logger.log(wlsRecord2);
		logger.log(wlsRecord3);
		logger.log(wlsRecord4);
	
		logger.addHandler(new LogHandler());
		

		WLLogRecord wlsRecord5 = new WLLogRecord(WLLevel.INFO, "Filtered log");
		WLLogRecord wlsRecord6 = new WLLogRecord(WLLevel.WARNING, "OSB");
		WLLogRecord wlsRecord7 = new WLLogRecord(WLLevel.WARNING, "OHS");
		
		logger.log(wlsRecord5);
		logger.log(wlsRecord6);
		logger.log(wlsRecord7);
	}
}

class FilterOSB_OHS implements Filter
{
	public boolean isLoggable(LogRecord record)
	{
		return !(record.getMessage().contains("OSB") || record.getMessage().contains("OHS"));
	}
}

