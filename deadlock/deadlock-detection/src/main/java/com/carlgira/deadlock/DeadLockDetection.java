package com.carlgira.deadlock;

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class DeadLockDetection {
  private MBeanServerConnection server;

  private JMXConnector jmxc;

  public DeadLockDetection(String hostname, int port) {
    String urlPath = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
    connect(urlPath);
  }

  /**
   * Connect to a JMX agent of a given URL.
   */
  private void connect(String urlPath) {
    try {
      JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
      this.jmxc = JMXConnectorFactory.connect(url);
      this.server = jmxc.getMBeanServerConnection();
    } catch (MalformedURLException e) {
      // should not reach here
    } catch (IOException e) {
      System.err.println("\nCommunication error: " + e.getMessage());
      System.exit(1);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      usage();
    }

    String[] arg2 = args[0].split(":");
    if (arg2.length != 2) {
      usage();
    }
    String hostname = arg2[0];
    int port = -1;
    try {
      port = Integer.parseInt(arg2[1]);
    } catch (NumberFormatException x) {
      usage();
    }
    if (port < 0) {
      usage();
    }

    DeadLockDetection ftd = new DeadLockDetection(hostname, port);
    System.out.println(ftd.deadLock());
  }
  
  public boolean deadLock() throws IOException{
      ThreadMonitor monitor = new ThreadMonitor(server);
      return monitor.findDeadlock();
  }

  private static void usage() {
    System.out.println("Usage: java FullThreadDump <hostname>:<port>");
  }
}


class ThreadMonitor {
  private MBeanServerConnection server;

  private ThreadMXBean tmbean;

  private ObjectName objname;

  private String findDeadlocksMethodName = "findDeadlockedThreads";


  public ThreadMonitor(MBeanServerConnection server) throws IOException {
    this.server = server;
    this.tmbean = newPlatformMXBeanProxy(server, THREAD_MXBEAN_NAME, ThreadMXBean.class);
    try {
      objname = new ObjectName(THREAD_MXBEAN_NAME);
    } catch (MalformedObjectNameException e) {
      // should not reach here
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    }
    parseMBeanInfo();
  }


  private static String INDENT = "    ";

  public boolean findDeadlock() {
    long[] tids;
      tids = tmbean.findDeadlockedThreads();
      if (tids == null) {
        return false;
      }
      return tids.length > 0;
    }

  private void parseMBeanInfo() throws IOException {
    try {
      MBeanOperationInfo[] mopis = server.getMBeanInfo(objname).getOperations();

      // look for findDeadlockedThreads operations;
      boolean found = false;
      for (MBeanOperationInfo op : mopis) {
        if (op.getName().equals(findDeadlocksMethodName)) {
          found = true;
          break;
        }
      }
      if (!found) {
        findDeadlocksMethodName = "findMonitorDeadlockedThreads";
      }
    } catch (IntrospectionException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    } catch (InstanceNotFoundException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    } catch (ReflectionException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    }
  }
}
