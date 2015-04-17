package com.carlgira.weblogic.util;


import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class RegexFilter extends Filter
{
	private String regex;
	
	private String action;

	@Override
	public int decide(LoggingEvent event)
	{
		String msg = event.getRenderedMessage();
		
		if(msg != null && regex != null && action != null)
		{
			if(msg.matches(regex))
			{
				 if(action.equals("ACCEPT"))
				 {
					return Filter.ACCEPT; 
				 }
				 else if(action.equals("DENY"))
				 {
					 return Filter.DENY;
				 }
			}
			else
			{
				return Filter.DENY;
			}
		}
	
		return Filter.NEUTRAL;
	}
	
	
	

	public String getRegex()
	{
		return regex;
	}

	public void setRegex(String regex)
	{
		this.regex = regex;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}


}
