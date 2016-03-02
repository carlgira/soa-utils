package unittests.patterns.incremental;

import java.io.File;
import java.io.FileReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Properties;

import junit.framework.TestCase;

import testing.util.GnlUtilities;

public class TestFederator extends TestCase
{
  public TestFederator()
  {
    super();
  }
  
  private final static String TEST_CLASS_NAME_RADICAL      = "test.class.name.";
  private final static String TEST_PROPERTIES_FILE_RADICAL = "test.properties.name.";
  private final static String TEST_VERBOSE_RADICAL         = "test.verbose.";
  private final static String TEST_JPS_CONFIG_RADICAL      = "oracle.security.jps.config.";
  
  public void testInOrder()
  {
    String mainPropertiesFileName = System.getProperty("properties.file.name", "federated.tests.properties");
    File pf = new File(mainPropertiesFileName);
    String projectToTest = ".";
    if (!pf.exists())
    {
      String projDir = System.getProperty("project.directory", ".");
      pf = new File(projDir, mainPropertiesFileName);
      if (!pf.exists())
      {
        String alternateLocation = GnlUtilities.searchAlongClasspath(mainPropertiesFileName);
        pf = new File(alternateLocation);
        if (!pf.exists())
          throw new RuntimeException(mainPropertiesFileName + " not found.");
        else
          projectToTest = alternateLocation.substring(0, alternateLocation.lastIndexOf(File.separator));
      }
      else
      {
        String projLoc = pf.getAbsolutePath();
        projectToTest = projLoc.substring(0, projLoc.lastIndexOf(File.separator));
      }
    }
        
    int index = 1;
    Properties federationDriverProperties = new Properties();
    try
    {
      federationDriverProperties.load(new FileReader(new File(projectToTest, mainPropertiesFileName)));
      boolean keepLooping = true;
      while (keepLooping)
      {
        String testClassName = federationDriverProperties.getProperty(TEST_CLASS_NAME_RADICAL + Integer.toString(index), null);
        if (testClassName == null)
          keepLooping = false;
        else
        {
          String testPropFile = federationDriverProperties.getProperty(TEST_PROPERTIES_FILE_RADICAL + Integer.toString(index), null);
          boolean verbose     = "true".equals(federationDriverProperties.getProperty(TEST_VERBOSE_RADICAL + Integer.toString(index), null));
          String jpsConfig    = federationDriverProperties.getProperty(TEST_JPS_CONFIG_RADICAL + Integer.toString(index), null);
          loadAndRunOneTestCase(testClassName, testPropFile, projectToTest, jpsConfig, index, verbose);
        }
        index++;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
  }

  private void loadAndRunOneTestCase(String testCaseClassName,
                                     String propertiesFile,
                                     String projectToTest,
                                     String jpsConfig,
                                     int index,
                                     boolean verbose)
  {
    try
    {
      Class testClass = Class.forName(testCaseClassName);
      TestCase testCase = (TestCase)testClass.newInstance();
      System.setProperty("verbose", verbose?"true":"false");
      System.setProperty("properties.file.name", propertiesFile);
      System.setProperty("project.directory", projectToTest);
      if (jpsConfig != null)
        System.setProperty("oracle.security.jps.config", jpsConfig);
      else
        System.setProperty("oracle.security.jps.config", "");
      Method[] method = testClass.getMethods();      
      for (int i=0; i<method.length; i++)
      {
//      System.out.println("--> Test Method [" + method[i] + "]");
        String mts = method[i].toString();
        if (mts.startsWith("public void"))
        {
          if (method[i].getName().startsWith("test")) // TODO Proceed with Annotations
          {
            System.out.println("----> Method " + method[i].getName() + " will be attempted on class " + testClass.getName());
            try
            {
              method[i].invoke(testCase, null);
            }
            catch (junit.framework.AssertionFailedError afe)
            {
              System.out.println("Exception running Method " + method[i].getName() + " on class " + testClass.getName());
              afe.printStackTrace();
              fail("Step " + Integer.toString(index) + ", running " + method[i].getName() + " on class " + testClass.getName() + ":" + afe.toString());
            }
            catch (InvocationTargetException ex)
            {
              System.out.println("Exception running Method " + method[i].getName() + " on class " + testClass.getName());
              ex.printStackTrace();
              fail("Step " + Integer.toString(index) + ", running " + method[i].getName() + " on class " + testClass.getName() + ":" + ex.toString());
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      fail(ex.toString());
    }
    finally
    {
      System.out.println("Done");
    }

  }
}
