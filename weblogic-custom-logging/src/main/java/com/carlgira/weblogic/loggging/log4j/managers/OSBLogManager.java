package com.carlgira.weblogic.loggging.log4j.managers;

import com.carlgira.weblogic.loggging.log4j.Log4jManager;

public class OSBLogManager extends Log4jManager
{
	protected static String osbloggerName = "OSBServerLogger";

	protected static String osbLogPattern = "(\\[OSB\\sTracing\\]|\\[Rastreo\\sde\\sOSB\\])";

	public OSBLogManager()
	{
		super(osbloggerName, osbLogPattern);
	}
}