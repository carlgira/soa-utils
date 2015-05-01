package com.carlgira.weblogic.loggging;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import com.carlgira.weblogic.loggging.LogHandler;
import weblogic.logging.WLLevel;
import weblogic.logging.WLLogRecord;
import com.carlgira.weblogic.loggging.log4j.Log4jManager;
import com.carlgira.weblogic.loggging.log4j.managers.OSBLogManager;
import com.carlgira.weblogic.loggging.log4j.managers.OSBProxyLogManager;
import java.util.ArrayList;
import java.util.List;
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
		WLLogRecord wlsRecord3 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] OSB Filtered Message");
		WLLogRecord wlsRecord4 = new WLLogRecord(WLLevel.WARNING, "IntroscopeAgent Filtered Message");
		
		logger.log(wlsRecord1);
		logger.log(wlsRecord2);
		logger.log(wlsRecord3);
		logger.log(wlsRecord4);
	
		
		List<Log4jManager> logManagers = new ArrayList<Log4jManager>();
		logManagers.add(new OSBLogManager());
		logger.addHandler(new LogHandler(logManagers));
		

		WLLogRecord wlsRecord5 = new WLLogRecord(WLLevel.INFO, "Filtered log");
		WLLogRecord wlsRecord6 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] Log After add LogHandler");
		WLLogRecord wlsRecord7 = new WLLogRecord(WLLevel.WARNING, "[Rastreo de OSB] Log After add LogHandler");
		WLLogRecord wlsRecord8 = new WLLogRecord(WLLevel.WARNING, "IntroscopeAgent Log After add LogHandler" );
		
		logger.log(wlsRecord5);
		logger.log(wlsRecord6);
		logger.log(wlsRecord7);
		logger.log(wlsRecord8);
	}
	
	
	@Test
	public void testProxyLoggger()
	{
		Logger logger = LogManager.getLogManager().getLogger("");
		
		FilterOSB_OHS filter_OSB_OHS = new FilterOSB_OHS();
		
		Handler consoleHandler = logger.getHandlers()[0];
		consoleHandler.setFilter(filter_OSB_OHS);
		
		List<Log4jManager> logManagers = new ArrayList<Log4jManager>();
		logManagers.add(new OSBProxyLogManager());
		logger.addHandler(new LogHandler(logManagers));
	
		WLLogRecord wlsRecord5 = new WLLogRecord(WLLevel.INFO, "Filtered log");
		WLLogRecord wlsRecord6 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] Log After add LogHandler Proxy HelloWorld init\n" +
				"Service Ref = osb-soapui-test/proxy/PS_HelloWordl1\n"
				);
		wlsRecord6.setTransactionId("id1");
		WLLogRecord wlsRecord7 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] Log After add LogHandler Proxy HelloWorld end \n");
		wlsRecord7.setTransactionId("id1");
		
		WLLogRecord wlsRecord8 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] Log After add LogHandler Proxy HiWorld init\n" +
				"Service Ref = osb-soapui-test/proxy/HiWordl\n"
				);
		wlsRecord8.setTransactionId("id2");
		WLLogRecord wlsRecord9 = new WLLogRecord(WLLevel.WARNING, "[OSB Tracing] Log After add LogHandler Proxy HiWorld end \n");
		wlsRecord9.setTransactionId("id2");
			
		logger.log(wlsRecord5);
		logger.log(wlsRecord6);
		logger.log(wlsRecord7);
		logger.log(wlsRecord8);
		logger.log(wlsRecord9);

	}
}

class FilterOSB_OHS implements Filter
{
	public boolean isLoggable(LogRecord record)
	{
		return !(record.getMessage().contains("[OSB Tracing]") || record.getMessage().contains("IntroscopeAgent"));
	}
}