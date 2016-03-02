package testserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestServerInterface
  extends Remote
{
  /**
   * This is for a test, don't use it.
   * @Deprecated
   * 
   * @param wsdlURL
   * @param serviceName
   * @param servicePort
   * @param serviceOperation
   * @param serviceNSUri
   * @param serviceInputPayload
   * @param moveonIfPayloadInvalid
   * @return
   * @throws RemoteException
   * @throws Exception
   */
  public String invokeSynchronousService(String wsdlURL,
                                         String serviceName,
                                         String servicePort,
                                         String serviceOperation,
                                         String serviceNSUri,
                                         String serviceInputPayload,
                                         boolean moveonIfPayloadInvalid) throws RemoteException, Exception;
  /**
   *
   * @param projectDirectory On the SERVER
   * @param propertiesFilename On the SERVER
   * @param verbose
   * @return the response Payload
   * @throws RemoteException
   * @throws Exception
   */
  public String invokeSynchronousService(String projectDirectory,
                                         String propertiesFilename,
                                         boolean verbose) throws RemoteException, Exception;
  /**
   *
   * @param projectDirectory On the SERVER
   * @param propertiesFilename On the SERVER
   * @param verbose
   * @return the response Payload
   * @throws RemoteException
   * @throws Exception
   */
  public String invokeASynchronousTwoWayService(String projectDirectory,
                                                String propertiesFilename,
                                                boolean verbose) throws RemoteException, Exception;
  /**
   *
   * @param projectDirectory On the SERVER
   * @param propertiesFilename On the SERVER
   * @param verbose
   * @return an empty String
   * @throws RemoteException
   * @throws Exception
   */
  public String invokeASynchronousOneWayService(String projectDirectory,
                                                String propertiesFilename,
                                                boolean verbose) throws RemoteException, Exception;
  public void stopTestServer() throws RemoteException;
  // TaskList interactions
  /**
   * To call first
   * 
   * @param projectDirectory On the SERVER
   * @param propertiesFile On the SERVER
   * @param verbose
   * @throws RemoteException
   * @throws Exception
   */
  public void initTaskList(String projectDirectory, 
                           String propertiesFile, 
                           boolean verbose)
    throws RemoteException, Exception;
  public String[] getTaskList()
    throws RemoteException, Exception;
  public String updateTaskOutcome(String taskID, 
                                  String outcome)
    throws RemoteException, Exception;
  /**
   * Close the session, to be called last
   * 
   * @throws RemoteException
   * @throws Exception
   */
  public void resetHumanWorkFlowInteraction()
    throws RemoteException, Exception;

  public void raiseBusinessEvent(String projectDirectory, String propertiesFilename, boolean verbose)
    throws RemoteException, Exception;
  
  public void createProcessUtil(boolean verb, String serverPropFileName)
    throws RemoteException, Exception;
  public String[] getProcessInstancesIDs(String compositeName)
    throws RemoteException, Exception;
  public String getAuditTrail(String bpelName, String instanceID)
    throws RemoteException, Exception;
  public void resetProcessUtil()
    throws RemoteException, Exception;
}