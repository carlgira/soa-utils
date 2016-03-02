package unittests.patterns.adfbc;

import unittests.patterns.services.AsynchronousTwoWayUnitTest;

public class ADFbcAsyncTwoWaysUnitTest
  extends AsynchronousTwoWayUnitTest
{
  public ADFbcAsyncTwoWaysUnitTest()
  {
    super();
  }

  @Override
  public String afterReceive(String response)
  {
    return ADFbcUtils.patchResponse(response, verbose, props);
  }  
}
