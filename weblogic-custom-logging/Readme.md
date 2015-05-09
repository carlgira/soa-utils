#Weblogic Custom logging

Tool to filter and redirect log records from the Weblogic log. The tool brings logManagers to filter OSB (Oracle Service BUS) proxy log messages 

This tool adds a custom handler to the Weblogic Server logger, that handler manages a list of Log4jManagers that filters the logRecords. I add two LogManagers to the lib; one that filters all the OSB messages and redirect them to a file, and another logManager that it's able to write a log for every Proxy Service configured.(You can also add your own custom logManagers to filter other applications)

To configure it, it's necessary to copy the library to the domain/lib, create and configure a startupClass in Weblogic, and finally add a log4j file with the configuration of the appenders and loggers.

Follow the instructions on this post http://carlgira.blogspot.com.es/2015/05/weblogic-filter-logs.html
