package unittests.discovery;

import junit.framework.TestSuite;

/*
 * Sample for HumanTask
 * Run with , -Dtest.definition.file.name=test.suite.definition.hwf.properties
 */
public class HWFTestDiscoverer
  extends TestDiscoverer
{
  public static TestSuite suite()
  {
    return TestDiscoverer.suite(System.getProperty("test.definition.file.name")); 
//  return TestDiscoverer.suite("test.suite.definition.hwf.properties");
  }
}
