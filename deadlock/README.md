### DeadLock Detector

Two utilities to detect a deadlock inside a JVM. <br/>

The application and the service only return a boolean.
- "true" means a deadlock is occurring
- "false" means a deadlock is not occurring

#### deadlock-detection 

A remote deadlock detector. In connects to a JVM using JMX to detect if a deadlock is ocurring. It needs the host of the JVM and the JMX port. 
```sh
  $ cd deadlock-detection
  $ mvn clean package
  $ java -jar deadlock-detection-1.0-SNAPSHOT.jar localhos:3333
```
#### deadlock-detector-service

A Weblogic Rest service to detect if is ocurring a deadlock in the server. It can deployed on Weblogic or tested with spring-boot<br/>

- Test with Spring-boot
```sh
  $ cd deadlock-detector-service
  $ mvn clean package
  $ java -jar target/deadlock-detector-service-1.0.0.war
  $ curl -X GET http://localhost:8080/deadlock
```
- Weblogic 
```sh
  $ cd deadlock-detector-service
  $ mvn clean package
  $ Deploy to Weblogic 
  $ curl -X GET http://localhost:7001/deadlock-detector-service-1.0.0/deadlock
```

