package unittests.patterns.hwf;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import oracle.bpel.services.workflow.client.IWorkflowServiceClient;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;
import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.repos.Column;
import oracle.bpel.services.workflow.repos.Ordering;
import oracle.bpel.services.workflow.repos.Predicate;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.ITaskAssignee;
import oracle.bpel.services.workflow.task.ITaskService;
import oracle.bpel.services.workflow.task.impl.TaskAssignee;
import oracle.bpel.services.workflow.task.model.IdentityTypeImpl;
import oracle.bpel.services.workflow.task.model.Task;
import oracle.bpel.services.workflow.verification.IWorkflowContext;

import testing.util.GnlUtilities;
import unittests.util.Utilities;

public class HumanWorkFlowInteraction
{
  protected IWorkflowServiceClient wfsvcClient = null;
  protected IWorkflowContext ctx = null;
  protected ITaskQueryService querySvc = null;

  protected Properties props = new Properties();
  protected final Properties masterProps = new Properties();  
  
  private String propertiesFileName = null;
  protected boolean verbose = false;
  private String projectDirectory = ".";

  private String predicateStr = "";  

  public HumanWorkFlowInteraction(String projectDirectory, String propertiesFile, boolean verbose) throws Exception
  {
    this.propertiesFileName = propertiesFile;
    this.projectDirectory   = projectDirectory;
    this.verbose = verbose;
    try
    {
      init();
    }
    catch (Exception ex)
    {
      throw ex;
    }
  }

  public List<String> getTaskIDList() throws Exception
  {
    List<String> list = null;
    List<Task> taskList = fetchTaskList();
    if (taskList != null)
    {
      list = new ArrayList<String>(taskList.size());
      for (Task t : taskList)
      {
        list.add(t.getSystemAttributes().getTaskId());
      }
    }    
    return list;                                                  
  }
  
  private void init() throws Exception
  {
    File propertiesFile = new File(projectDirectory, propertiesFileName);
    if (!propertiesFile.exists())
    {
      String cpFileName = GnlUtilities.searchAlongClasspath(propertiesFileName);
      if (cpFileName.trim().length() == 0)
        throw new Exception("\"" + propertiesFileName + "\" not found from \"" + System.getProperty("user.dir") + "\", nor along your classpath");
      else
      {
        propertiesFileName = cpFileName;
        propertiesFile = new File(cpFileName);
      }
    }
    else
      propertiesFileName = propertiesFile.getAbsolutePath();

    props = new Properties();
    try { props.load(new FileInputStream(propertiesFileName)); } 
    catch (Exception fnfe)
    {
      throw fnfe;
    }
    ArrayList<Properties> pa = new ArrayList<Properties>(2);
    pa.add(masterProps);
    pa.add(System.getProperties());
    props = Utilities.patchProperties(props, pa, true);
    
    if (verbose)
      System.out.println("Loading test from " + propertiesFileName);

    try
    {
      if (verbose)
        System.out.println("Connecting on [" + props.getProperty("hwf.url") + "] as [" + props.getProperty("admin.username") + "/" + props.getProperty("admin.password") + "]");
      
      Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties = new HashMap<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String>();
      properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.MODE, IWorkflowServiceClientConstants.MODE_DYNAMIC);
      
      properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, props.getProperty("hwf.url"));
      properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL, props.getProperty("admin.username"));
      properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS, props.getProperty("admin.password"));
      wfsvcClient = WorkflowServiceClientFactory.getWorkflowServiceClient(WorkflowServiceClientFactory.REMOTE_CLIENT,
                                                                          properties,
                                                                          null);
      querySvc = wfsvcClient.getTaskQueryService();
      if (verbose)
        System.out.println("Init Completed");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private List<Task> getTasklist()
  {
    String user  = props.getProperty("user.to.check.name");
    String pswd  = props.getProperty("user.to.check.password");
    String realm = props.getProperty("user.to.check.realm");

    List<Task> returnList = null;
    try
    {
      ITaskQueryService querySvc = wfsvcClient.getTaskQueryService();
//    String defaultContext = ISConfiguration.getDefaultRealmName();
//    System.out.println("Default Context:" + defaultContext);
      
      if (realm != null && realm.trim().length() > 0)
        ctx = querySvc.authenticate(user, pswd.toCharArray(), realm);

      List queryColumns = new ArrayList<String>();
      queryColumns.add(TableConstants.WFTASK_TASKID_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_TITLE_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_TASKDEFINITIONNAME_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_OUTCOME_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_ASSIGNEES_COLUMN.getName());
      queryColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());

      Predicate predicate = buildPredicate();
      
      Ordering ordering = new Ordering(TableConstants.WFTASK_TASKNUMBER_COLUMN, true, false);

      List<Task> tasks = querySvc.queryTasks(ctx, 
                                             queryColumns, 
                                             null,  // emptyList,
                                             ITaskQueryService.AssignmentFilter.ALL,
                                             null, 
                                             predicate, 
                                             ordering, 
                                             0, 
                                             0);

      returnList = tasks;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return returnList;
  }
  
  private static final String PREDICATE_COLUMN_RADICAL   = "predicate.column.";
  private static final String PREDICATE_OPERATOR_RADICAL = "predicate.operator.";
  private static final String PREDICATE_VALUE_RADICAL    = "predicate.value.";

  private String translateOp(int op)
  {
    String str = "";
    switch (op)
    {
//    case Predicate.OP_AFTER:
//      str = " after ";
//      break;
//    case Predicate.OP_BEFORE:
//      str = " before ";
//      break;
      case Predicate.OP_BEGINS:
        str = " begins with ";
        break;
      case Predicate.OP_CONTAINS:
        str = " contains ";
        break;
      case Predicate.OP_ENDS:
        str = " ends with ";
        break;
      case Predicate.OP_EQ:
        str = " = ";
        break;
      case Predicate.OP_GT:
        str = " > ";
        break;
      case Predicate.OP_GTE:
        str = " >= ";
        break;
      case Predicate.OP_IN:
        str = " in ";
        break;
      case Predicate.OP_IS_NOT_NULL:
        str = " is not null ";
        break;
      case Predicate.OP_IS_NULL:
        str = " is null ";
        break;
      case Predicate.OP_LAST_N_DAYS:
        str = " last N days ";
        break;
      case Predicate.OP_LIKE:
        str = " like ";
        break;
      case Predicate.OP_LT:
        str = " < ";
        break;
      case Predicate.OP_LTE:
        str = " <= ";
        break;
      case Predicate.OP_NEQ:
        str = " != ";
        break;
      case Predicate.OP_NEXT_N_DAYS:
        str = " next N days ";
        break;
      case Predicate.OP_NOT_BEGINS:
        str = " not begins ";
        break;
      case Predicate.OP_NOT_CONTAINS:
        str = " not contains ";
        break;
      case Predicate.OP_NOT_ENDS:
        str = " not ends ";
        break;
      case Predicate.OP_NOT_IN:
        str = " not in ";
        break;
      case Predicate.OP_NOT_LIKE:
        str = " not like ";
        break;
      case Predicate.OP_ON:
        str = " on ";
        break;
      default:
        str = "<unknown>";
    }    
    return str;
  }
  
  protected Predicate buildPredicate() throws Exception
  {
    Predicate predicate = null;
    int i = 1;
    boolean keepPredicating = true;
    predicateStr = "";
    while (keepPredicating)
    {
      String queryColumn = props.getProperty(PREDICATE_COLUMN_RADICAL + Integer.toString(i), null);
      if (queryColumn == null)
        keepPredicating = false;
      else
      {
        Class c = Class.forName("oracle.bpel.services.workflow.repos.TableConstants");
        Field column = c.getField(queryColumn);
        Type type = column.getGenericType();
        if (!type.toString().endsWith("oracle.bpel.services.workflow.repos.Column"))
          throw new RuntimeException(PREDICATE_COLUMN_RADICAL + Integer.toString(i) + " is a " + type.toString() + ", expecting a oracle.bpel.services.workflow.repos.Column");
        else
        {
          Column queryCol = (Column)column.get(new Object());
          // Now, operator
          String operator = props.getProperty(PREDICATE_OPERATOR_RADICAL + Integer.toString(i), null);
          if (operator == null)
            throw new RuntimeException(PREDICATE_OPERATOR_RADICAL + Integer.toString(i) + " not found.");
          else
          {
            Class op = Class.forName("oracle.bpel.services.workflow.repos.Predicate");
            Field oper = op.getField(operator);
            Type opType = oper.getGenericType();
            if (!opType.toString().endsWith("int"))
              throw new RuntimeException(PREDICATE_OPERATOR_RADICAL + Integer.toString(i) + " is a " + opType.toString() + ", expecting an int.");
            else
            {
              int queryOperator = oper.getInt(new Object());
              // Now, value
              String value = props.getProperty(PREDICATE_VALUE_RADICAL + Integer.toString(i), null);
              if (value == null && queryOperator != Predicate.OP_IS_NULL && queryOperator != Predicate.OP_IS_NOT_NULL)
                throw new RuntimeException(PREDICATE_VALUE_RADICAL + Integer.toString(i) + " is expected.");
              else
              {
                String predicateMessage = "(Column [" + queryCol.toString() + "] Operator [" + translateOp(queryOperator) + "] Value [" + value + "])";
                predicateStr += ((predicateStr.length() > 0?" and \n":"") + predicateMessage);
                if (verbose)
                  System.out.println(predicateMessage);
                
                Predicate predicateElement = new Predicate(queryCol, queryOperator, value);
                if (predicate == null)
                  predicate = predicateElement;
                else
                  predicate = new Predicate(predicate, Predicate.AND, predicateElement);
              }
            }
          }
        }        
        i++;
      }
    }    
    return predicate;
  }
  
  private List<Task> fetchTaskList()
  {
    List<Task> list = null;

    int nbLoop = 0, maxLoop = 20;
    long sleepTime = 1000L;
    try { maxLoop = Integer.parseInt(props.getProperty("fetch.max.loops", "20")); } 
    catch (NumberFormatException nfe)
    {
      System.out.println("INFO: fetch.max.loop:" + nfe.toString());
    }
    try { sleepTime = 1000L * Integer.parseInt(props.getProperty("between.loops", "1")); } 
    catch (NumberFormatException nfe)
    {
      System.out.println("INFO: between.loops:" + nfe.toString());
    }
            
    boolean found = false;
    while (!found && nbLoop < maxLoop)
    {
      nbLoop++;
      if (verbose)
        System.out.println("== Getting task list, loop #" + Integer.toString(nbLoop) + " ==");

      list = getTasklist();
      if (list != null && list.size() > 0)
      {
        found = true;
      }
      else
        try { Thread.sleep(sleepTime); } catch (Exception ignore) {}
    }
    
    return list;
  }
  
  protected String setTaskOutcome(Task task, String outcome)
  {
    String completed = "Ok";

    if (task != null)
    {
      if (this.wfsvcClient != null)
      {
        ITaskService taskSvc = this.wfsvcClient.getTaskService();

        try
        {
          task = querySvc.getTaskDetailsById(ctx, task.getSystemAttributes().getTaskId());

          if (outcome.equals("REQUEST_INFORMATION"))
          {
            String firstAssignee = "";
            List<IdentityTypeImpl> assignees = task.getSystemAttributes().getAssignees();
            if (assignees != null)
            {
              Iterator<IdentityTypeImpl> assigneesIterator = assignees.iterator();
              while (assigneesIterator.hasNext())
              {
                IdentityTypeImpl ident = assigneesIterator.next();
                firstAssignee = ident.getId();
              }
            }
            ITaskAssignee ta = new TaskAssignee(firstAssignee, false);
            taskSvc.requestInfoForTask(this.ctx, task, ta); // To the owner (current) of the task
          }
          else if (outcome.equals("ESCALATE"))
          {
            taskSvc.escalateTask(this.ctx, task);
          }
          else if (outcome.equals("WITHDRAW"))
          {
            taskSvc.withdrawTask(this.ctx, task);
          }
          else if (outcome.equals("SUSPEND"))
          {
            taskSvc.suspendTask(this.ctx, task);
          }
          else if (outcome.equals("RESUME"))
          {
            taskSvc.resumeTask(this.ctx, task);
          }
          else // User outcome
          {
            // Update task outcome
            if (verbose)
              System.out.println("Updating task : " + task.getSystemAttributes().getTaskId() + " to " + outcome);
            taskSvc.updateTaskOutcome(this.ctx, task.getSystemAttributes().getTaskId(), outcome);
          }
        }
        catch (Exception ex)
        {
          StringWriter sw = new StringWriter();
          ex.printStackTrace(new PrintWriter(sw));
          completed = sw.toString();
        }
      }
    }
    if (verbose)
      System.out.println("Set outcome to " + completed);
    return completed;
  }    

  public String getTaskOutcomeByTaskID(String taskID, String outcome)
  {
    String completion = "";
    // Find the Task
    String user  = props.getProperty("user.to.check.name");
    String pswd  = props.getProperty("user.to.check.password");
    String realm = props.getProperty("user.to.check.realm");

    List<Task> returnList = null;
    try
    {
      ITaskQueryService querySvc = wfsvcClient.getTaskQueryService();
    //    String defaultContext = ISConfiguration.getDefaultRealmName();
    //    System.out.println("Default Context:" + defaultContext);
      
      if (realm != null && realm.trim().length() > 0)
        ctx = querySvc.authenticate(user, pswd.toCharArray(), realm);

      List queryColumns = new ArrayList<String>();
      queryColumns.add(TableConstants.WFTASK_TASKID_COLUMN.getName());

      Predicate predicate = new Predicate(TableConstants.WFTASK_TASKID_COLUMN, Predicate.OP_EQ, taskID);
      
      Ordering ordering = new Ordering(TableConstants.WFTASK_TASKID_COLUMN, true, false);

      List<Task> tasks = querySvc.queryTasks(ctx, 
                                             queryColumns, 
                                             null,  // emptyList,
                                             ITaskQueryService.AssignmentFilter.ALL,
                                             null, 
                                             predicate, 
                                             ordering, 
                                             0, 
                                             0);

      returnList = tasks;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    if (returnList != null && returnList.size() == 1)
    {
      // Update
      Task t = returnList.get(0);
      completion = setTaskOutcome(t, outcome);
    }
    else
      System.out.println("A problem: list size is " + returnList.size());
    
    return completion;
  }
}
