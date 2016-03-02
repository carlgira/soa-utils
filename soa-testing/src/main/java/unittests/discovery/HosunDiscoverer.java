package unittests.discovery;

import junit.framework.TestSuite;

/*
 * Sample for Hosun Yoo
 */
public class HosunDiscoverer
  extends TestDiscoverer
{
  public static TestSuite suite()
  {
    return TestDiscoverer.suite("test.suite.definition.hosun.properties");
  }
}
