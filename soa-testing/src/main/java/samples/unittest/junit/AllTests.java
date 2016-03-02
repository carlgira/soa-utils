package samples.unittest.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ Synchronous_JUnitTest.class, 
                      AsyncTwoWays_JUnitTest.class, 
                      AsyncTwoWaysWithTimeout_JUnitTest.class,
                      AsyncOneWay_JUnitTest.class })
public class AllTests
{
}
