package unittests.patterns.hwf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Locale;
import java.util.Map;

import oracle.bpel.services.workflow.query.ITaskQueryService;
import oracle.bpel.services.workflow.query.model.TaskSequence;
import oracle.bpel.services.workflow.repos.TableConstants;
import oracle.bpel.services.workflow.task.impl.TaskUtil;
import oracle.bpel.services.workflow.task.model.ActionType;
import oracle.bpel.services.workflow.task.model.IdentityTypeImpl;
import oracle.bpel.services.workflow.task.model.Task;

public class HumanWorkFlowUnitTestImplementationSample
  extends HumanWorkFlowUnitTest
{
  public HumanWorkFlowUnitTestImplementationSample()
  {
    super();
  }

  @Override
  protected void afterFetchingTasks(List<Task> list)
  {
    super.afterFetchingTasks(list); // More than 0 task
    
    if (verbose)
      System.out.println("** We have tasks **");
    int i = 0;
    for (Task t: list)
    {
      String assigneeList = "";
      List<IdentityTypeImpl> assignees = (List<IdentityTypeImpl>) t.getSystemAttributes().getAssignees();
      if (assignees != null)
      {
        for (IdentityTypeImpl ident: assignees)
        {
          String assignee = ident.getId();
          assigneeList += ((assigneeList.trim().length() == 0? "": ", ") + assignee);
        }
      }     
      if (verbose && t != null)
        System.out.println("Task: Number [" + t.getSystemAttributes().getTaskNumber() + "]\n" +
                           "DefinitionName [" + t.getSystemAttributes().getTaskDefinitionName() + "]\n" +
                           "DefinitionID [" + t.getTaskDefinitionId() + "]\n" + 
                           "Title [" + t.getTitle() + "]\n" + 
                           "Owned by [" + t.getOwnerUser() + "]\n" + 
                           "Assigned to [" + assigneeList + "]\n" + 
                           "Created by [" + t.getCreator() + "]\n" + 
                           "State [" + t.getSystemAttributes().getState() + "]");
      if (i==0 && t != null)
      {        
        try { System.setProperty("dyn.task.name", t.getSystemAttributes().getTaskDefinitionName()); }
        catch (Exception ignore) { System.out.println(ignore.toString()); }
      }
      i++;
      
      if (verbose)
      {
        List systemAction = t.getSystemAttributes().getSystemActions();
        for (Object o : systemAction)
        {
//        System.out.println("SystemAction is a :" + o.getClass().getName());
          ActionType at = (ActionType)o;
          System.out.println("System Action:" + at.getAction());
        }
        
        try
        {
  //      Task task = querySvc.getTaskDetailsById(ctx, t.getSystemAttributes().getTaskId());
          List customAction = t.getSystemAttributes().getCustomActions();
          for (Object o : customAction)
          {
            System.out.println("CustomAction is a :" + o.getClass().getName());
            ActionType at = (ActionType)o;
            System.out.println("Custom Action:" + at.getAction());
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
        
        try
        {
          Map<String, String> outcomes = (Map<String, String>)wfsvcClient.getTaskMetadataService().getOutcomes(ctx, t, Locale.getDefault());
          Iterator keys = outcomes.keySet().iterator();
          System.out.println("-- Possible outcomes: --");
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            System.out.println(key + ":" + (String)outcomes.get(key));
          }
          System.out.println("------------------------");
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
        
        List<String> queryColumns = new ArrayList<String>();
        queryColumns.add(TableConstants.WFTASK_TASKID_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_TASKNUMBER_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_TITLE_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_TASKDEFINITIONNAME_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_OUTCOME_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_ASSIGNEES_COLUMN.getName());
        queryColumns.add(TableConstants.WFTASK_STATE_COLUMN.getName());
        
        List<ITaskQueryService.TaskSequenceType> sequenceTypes = new ArrayList<ITaskQueryService.TaskSequenceType>();
        sequenceTypes.add(ITaskQueryService.TaskSequenceType.ALL);
        List<ITaskQueryService.TaskSequenceBuilderContext> taskSequenceBuilderContext = new ArrayList<ITaskQueryService.TaskSequenceBuilderContext>();
        taskSequenceBuilderContext.add(ITaskQueryService.TaskSequenceBuilderContext.WORKFLOW_PATTERN);
                  
        try
        {
          TaskSequence ts = querySvc.getTaskSequence(ctx, t, queryColumns, sequenceTypes, taskSequenceBuilderContext, true);
          System.out.println("Task Sequence is:\n" + TaskUtil.getInstance().toString(ts));
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
//    if (false)
      {
        try
        {
          Task task = t; // this.querySvc.getTaskDetailsById(this.ctx, t.getSystemAttributes().getTaskId());            
          setTaskOutcome(task, "WHO_CARES");
          System.out.println("Task Completed");
        }
        catch (Exception ex)
        {
          System.out.println(ex.toString());
          fail(ex.toString());
        }
      }
    }
    System.out.println("Done");    
  }
}
