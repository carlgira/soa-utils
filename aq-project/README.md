1. Execute script qdtest.sql, 
	* Creation of user with SYS
	* Creation of queue and procedures with AQ.

2. Configure WildFly
	* Copy oracle driver in modules, "wildfly\modules\system\layers\base\com\oracle\main"
		- module.xml
		- ojdbc7-12.1.0.jar
	* Configure driver and datasource in standalone.xml		
		- standalone.xml

3. Deploy App
4. Test services

	http://localhost:8080/aq-test-1.0.0/enqueue?message=val4

	http://localhost:8080/aq-test-1.0.0/enqueue_direct?message=val3

5. Check new messages in table 

	select * from message_table;