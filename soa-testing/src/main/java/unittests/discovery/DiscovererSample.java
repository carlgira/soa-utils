package unittests.discovery;

import junit.framework.TestSuite;

public class DiscovererSample
  extends TestDiscoverer
{
  public static TestSuite suite()
  {
//  return TestDiscoverer.suite("alternate.test.suite.definition.properties");
    return TestDiscoverer.suite("federated.test.suite.properties");
  }
}
