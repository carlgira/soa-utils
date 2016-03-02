package unittests.patterns.hwf;

import java.util.List;

public class HumanWorkFlowInteractionSampleClient
{
  public static void main(String[] args) throws Exception
  {
    HumanWorkFlowInteraction hwfi = new HumanWorkFlowInteraction(".", "hwf.ut.properties", false);
    try
    {
      List<String> tl = hwfi.getTaskIDList();
      System.out.println("Returned " + tl.size() + " task(s)");
      if (tl.size() > 0)
      {
        String id = tl.get(0);
        System.out.println("Result:" + hwfi.getTaskOutcomeByTaskID(id, "WHO_CARES"));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
