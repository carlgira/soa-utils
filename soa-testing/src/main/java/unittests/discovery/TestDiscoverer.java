package unittests.discovery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import testing.util.GnlUtilities;

import unittests.patterns.event.RaiseEvent;
import unittests.patterns.services.AsynchronousOneWayUnitTest;
import unittests.patterns.services.AsynchronousTwoWayUnitTest;
import unittests.patterns.services.SynchronousServiceUnitTest;

import unittests.util.VirtualClassWizard;

import util.javautil.InFlightCompilation;

/**
 * Will run the Unit Tests defined in a project
 * The definition of those test are defined in a properties file
 * named test.definition.properties, that must exist at
 * the project's root for it to be found.
 *
 * See in the current project the test.suite.definition.properties file
 * for an example.
 *
 * There is a main, its argument must be the location of the project to run the tests on,
 * like /scratch/olediour/view_storage/olediour_fatools/fatools/opensource/testutilities/soatesthelper/ServiceUnitTest.jpr
 *
 * A FileNotFoundException is raised if the project is not found.
 */

public class TestDiscoverer 
{
  private static String testDefinitionPropertiesFileName = "test.suite.definition.properties";  
  private static File   pf = null;
  
  protected static Class<? extends TestCase>[] testSuite = null;

  private final static boolean DEBUG = false;
  
  private static TestDiscoverer instance = null;
  
  private static ClassLoader getTestDiscovererClassLoader()
  {
    return getInstance().getClass().getClassLoader();
  }
  
  private synchronized static TestDiscoverer getInstance()
  {
    if (instance == null)
      instance = new TestDiscoverer();
    return instance;    
  }
  
  public static TestSuite suite(String customPropertiesFile)
  {
    System.out.println("suite() method:" + customPropertiesFile);
    
    testDefinitionPropertiesFileName = customPropertiesFile;
    pf = new File(testDefinitionPropertiesFileName);
    if (!pf.exists())
    {
      String projDir = System.getProperty("project.directory", ".");
      System.out.println("Looking for " + testDefinitionPropertiesFileName + " into [" + projDir + "] ...");
      pf = new File(projDir, testDefinitionPropertiesFileName);
      if (!pf.exists())
      {
        System.out.println("Looking for " + testDefinitionPropertiesFileName + " along Classpath:" + System.getProperty("java.class.path") + " ...");
        String alternateLocation = GnlUtilities.searchAlongClasspath(testDefinitionPropertiesFileName);
        if (alternateLocation != null)
          pf = new File(alternateLocation);
        if (alternateLocation == null || !pf.exists())
        {
          System.out.println("Looking for " + testDefinitionPropertiesFileName + " as a resource ...");
          alternateLocation = GnlUtilities.findResourceAsFile(getInstance(), testDefinitionPropertiesFileName);
          if (alternateLocation != null)
            pf = new File(alternateLocation);
          if (alternateLocation == null || !pf.exists())
          {
            String message = "[" + testDefinitionPropertiesFileName + "] not found, along:\n" +
                             "user.dir:" + System.getProperty("user.dir", ".") + "\n" +
                             "project.directory:" + System.getProperty("project.directory", ".") + "\n" +
                             "classpath:" + System.getProperty("java.class.path").replace(File.pathSeparatorChar, '\n');
            System.err.println(message);
            throw new RuntimeException(message);
          }
          else
            projectToTest = alternateLocation.substring(0, alternateLocation.lastIndexOf(File.separator));
        }
        else
          projectToTest = alternateLocation.substring(0, alternateLocation.lastIndexOf(File.separator));
      }
      else
      {
        String projLoc = pf.getAbsolutePath();
        projectToTest = projLoc.substring(0, projLoc.lastIndexOf(File.separator));
      }
    }
    
    if (DEBUG)
    {
      System.out.println("project.directory=" + projectToTest);
      System.out.println("properties.file.name=" + testDefinitionPropertiesFileName);
    }
    
    TestSuite suite = null;
    try
    {
      suite = new TestSuite("unittests.discovery.TestDiscoverer");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    discover();
    for (Class<? extends TestCase> cl : testSuite)
      suite.addTestSuite(cl);
    return suite;
  }
  
  public static TestSuite suite()
  {
    testDefinitionPropertiesFileName = System.getProperty("test.definition.file.name", testDefinitionPropertiesFileName);
    return suite(testDefinitionPropertiesFileName);
  }
  
  public TestDiscoverer()
  {
    testDefinitionPropertiesFileName = System.getProperty("test.definition.file.name", testDefinitionPropertiesFileName);
    System.out.println("-- (TestDiscoverer constructor invoked.)");
    System.out.println("Test Definition Properties:" + testDefinitionPropertiesFileName);
  }
  
//  public TestDiscoverer()
//  {    
//    pf = new File(testDefinitionPropertiesFileName);
//    if (!pf.exists())
//    {
//      String alternateLocation = GnlUtilities.searchAlongClasspath(testDefinitionPropertiesFileName);
//      pf = new File(alternateLocation);
//      if (!pf.exists())
//        throw new RuntimeException(testDefinitionPropertiesFileName + " not found.");
//      else
//        projectToTest = alternateLocation.substring(0, alternateLocation.lastIndexOf(File.separator));
//    }
//    discover();
//  }
  
//  public TestDiscoverer(String propFileName)
//  {
//    System.out.println("Running with prop file [" + propFileName + "]");
//    testDefinitionPropertiesFileName = propFileName;
//    pf = new File(testDefinitionPropertiesFileName);
//    if (!pf.exists())
//    {
//      String alternateLocation = GnlUtilities.searchAlongClasspath(testDefinitionPropertiesFileName);
//      pf = new File(alternateLocation);
//      if (!pf.exists())
//        throw new RuntimeException(testDefinitionPropertiesFileName + " not found.");
//      else
//        projectToTest = alternateLocation.substring(0, alternateLocation.lastIndexOf(File.separator));
//    }
//    discover();
//  }
  
  public static File findPropertiesFile(String projectFullPath) throws Exception
  {
    if (pf == null)
    {
      if (projectFullPath.trim().length() == 0)
        projectFullPath = ".";

      File prj = new File(projectFullPath);
      if (!prj.exists())
      {
        throw new FileNotFoundException("Project [" + projectFullPath + "] not found.");
      }
      // else...
      File prjRoot = null;
      if (!prj.isDirectory()) 
        prjRoot = prj.getParentFile();
      else
        prjRoot = prj;
      pf = new File(prjRoot, testDefinitionPropertiesFileName);
    }
    return pf;
  }
  
  public static Properties loadTestDefinition(File pf) throws Exception
  {
    Properties testProps = new Properties();
    try
    {
      testProps.load(new FileInputStream(pf));
    }
    catch (Exception ex)
    {
      throw ex;
    }
    
    return testProps;
  }
  
  private static String projectToTest = ".";

  private static boolean useASM = true;      // Depends on a system property
  
  private final static String TRANSITION_TO_FILE_RADICAL      = "transition.to.file.";
  private final static String TRANSITION_FROM_XPATH_RADICAL   = "transition.from.xpath.";
  private final static String TRANSITION_TO_XPATH_RADICAL     = "transition.to.xpath.";
  private final static String TRANSITION_FROM_LITERAL_RADICAL = "transition.from.literal.";
  
  public static void discover()
  {
    // Display jar date
    if (true)
    {
      try
      {
        TestDiscoverer td = getInstance();
        String className = td.getClass().getName();
        className = className.substring(className.lastIndexOf(".") + 1) + ".class";
        URL me = td.getClass().getResource(className);
        String strURL = me.toString();
//      System.out.println("Resource:" + strURL);
        String resource = null;
        String jarIdentifier = ".jar!/";
        if (strURL.indexOf(jarIdentifier) > -1) // It's a jar
        {
          String jarFileURL = strURL.substring(0, strURL.indexOf(jarIdentifier) + jarIdentifier.length());
          URL jarURL = new URL(jarFileURL);
          String jarFileName = jarURL.getFile();          
          resource = jarFileName.substring("file:".length());
          if (resource.endsWith("!/"))
            resource = resource.substring(0, resource.length() - "!/".length());
          resource = resource.replace('/', File.separatorChar);
        }
        else // It's a class
        {
          String classFileName = me.getFile();
          resource = classFileName;
        }
        File f = new File(resource);
        Date d = new Date(f.lastModified());
        System.out.println("Resource:" + resource + " [" + d.toString() + "]");
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    
    ArrayList<Class> ts = new ArrayList<Class>();
    int testIndex = 0;
    int nbSkip = 0, nbTest2Generate = 0;
    
    try
    {
      File pf = findPropertiesFile(projectToTest);
      if (pf != null && pf.exists())
      {
        System.out.println("...Found the properties file [" + testDefinitionPropertiesFileName + "]");
        System.out.println("Running from " + System.getProperty("user.dir"));
        
        Properties tp = loadTestDefinition(pf);
        useASM = "true".equals(tp.getProperty("use.asm", "false")); 
        
        boolean keepTesting = true;
        // classDir useless if use.asm=true
        // same for delete.generated.class
        final String classesDir = tp.getProperty("classes.directory"); // , "extended" + File.separator + "classes");
        while (keepTesting)
        {
          final String testName = tp.getProperty("test.name." + Integer.toString(testIndex), null);
          final String skip = tp.getProperty("skip." + Integer.toString(testIndex), "false");
          if (testIndex == 0 || testName != null)
          {
            if (testName != null)
              System.out.println("Test : " + testName);
            else
              System.out.println("Initialization");
            if (testIndex > 0 && "true".equals(skip))
            {
              System.out.println("Skipping " + testName);    
              nbSkip++;
            }
            else
            {
              String testClass = null;
              try
              {
                String verbose = tp.getProperty("verbose."  + Integer.toString(testIndex), "false");                       
                if (testIndex > 0)
                  testClass = tp.getProperty("test.class." + Integer.toString(testIndex)).trim();
                else 
                {
                  if ("true".equals(verbose))
                    System.out.println("Test Index=0 => generating EmptyTest");
                  testClass = "unittests.patterns.empty.EmptyTest";
                }
                Class tClass = Class.forName(testClass);
                if ("true".equals(verbose))
                  System.out.println("** Class [" + testClass + "] loaded successfully.");
                Object testObject = tClass.newInstance();
                
                if (testObject instanceof TestCase)
                {
                  int nbTest = 0;
                  // System.out.println("Scanning " + testClass + "...");
                  Method[] method = tClass.getMethods(); // All methods, not only declared ones
                  for (int i=0; i<method.length; i++)
                  {
                    // System.out.println("Method:" +  method[i].getName());
                    Annotation[] aa = method[i].getDeclaredAnnotations();
                    for (int j=0; j<aa.length; j++)
                    {
                      // System.out.println("  Annotation:" + aa[j].toString());
                      Class cl = aa[j].getClass();
                      Class[] interfaces = cl.getInterfaces();
                      for (int k=0; k<interfaces.length; k++)
                      {
                        // System.out.println("Interface [" + interfaces[k].getName() + "]");
                        if ("org.junit.Test".equals(interfaces[k].getName()))
                        {
                          System.out.println("- Method " + method[i].getName() + " of " + testClass + " is a JUnit @Test");
                          nbTest++;
                        }
                      }
                    }
                  }
                  if (nbTest == 0)
                    System.out.println("Warning! : " + testClass + " contains no annotated JUnit Test");
                  else
                    System.out.println("- " + testClass + " contains " + Integer.toString(nbTest) + " JUnit Test(s).");
                }
    
                // Just FYI...
                System.out.print("...Just FYI : ");
                if (testObject instanceof SynchronousServiceUnitTest)
                  System.out.println("Synchronous service");
                else if (testObject instanceof AsynchronousOneWayUnitTest)
                  System.out.println("Async one way");
                else if (testObject instanceof AsynchronousTwoWayUnitTest)
                  System.out.println("Async two ways");
                else if (testObject instanceof RaiseEvent)
                  System.out.println("Event unit test");
        //      else if (testObject instanceof RulesUnitTest)
        //        System.out.println("Rules unit test");
        //      else if (...)              
                else
                  System.out.println("Custom TestCase"); // Maybe...
    
                String testPropFileName = tp.getProperty("properties.for.test." + Integer.toString(testIndex));
                String jpsconfig = tp.getProperty("jps.config.file.location." + Integer.toString(testIndex), "");
                
                int nbTransition = 1;
                
                String transitionToFile      = tp.getProperty(TRANSITION_TO_FILE_RADICAL + Integer.toString(testIndex), null);
                String transitionFromPath    = tp.getProperty(TRANSITION_FROM_XPATH_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                String transitionFromLiteral = tp.getProperty(TRANSITION_FROM_LITERAL_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                String transitionToPath      = tp.getProperty(TRANSITION_TO_XPATH_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                
                if (false)
                {
                  File testPropFile = new File(projectToTest, testPropFileName);
                  testPropFileName = testPropFile.getAbsolutePath();
  
                  if (jpsconfig != null)
                  {
                    File file = new File(projectToTest, jpsconfig);
                    jpsconfig = file.getAbsolutePath();
                  }
                }
                 
                if (transitionToFile != null)
                {
                  File file = new File(projectToTest, transitionToFile);
                  transitionToFile = file.getAbsolutePath();
                }
                boolean addThisTest = true;
                if (testIndex == 0 && transitionToFile == null)
                  addThisTest = false;
                else
                  nbTest2Generate++;
                if (addThisTest)
                {
                  // Code generation begins here
                  try
                  {
                    //    ------------->                                                               For Unicity...
                    String newClassName = tClass.getName() + "_" + Integer.toString(testIndex) + "_" + Long.toString(new Date().getTime());
                    
                    String packageName = newClassName.substring(0, newClassName.lastIndexOf("."));
                    String className   = newClassName.substring(newClassName.lastIndexOf(".") + 1);
                    if (useASM)
                    {
                      try
                      {
                        VirtualClassWizard vcw = VirtualClassWizard.getInstance(getTestDiscovererClassLoader());
                        Class extended = vcw.generateAndLoad(packageName,  
                                                             className, 
                                                             tClass.getName(), 
                                                             "true".equals(verbose), 
                                                             projectToTest,
                                                             testPropFileName, 
                                                             jpsconfig, 
                                                             testIndex,
                                                             tp,
                                                             transitionToFile, 
                                                             transitionFromPath, 
                                                             transitionFromLiteral, 
                                                             transitionToPath);
                        if (extended != null)
                        {
                          if ("true".equals(verbose))
                            System.out.println("Class " + className + " added to the TestSuite");
                          ts.add(extended);
                        }
                        else
                          System.out.println(className + " NOT added in the suite (null)");
                      }
                      catch (Exception ex)
                      {
                        System.err.println("ASM Class generation and load...");
                        ex.printStackTrace();
                      }
                    }
                    else // Compile from String
                    {
                      String newCode = 
                        "package " + packageName + ";\n\n" +
                        "public class " + className + " extends " + tClass.getName() + "\n" +
                        "{\n";
                      if (testIndex > 0)
                        newCode +=
                        "  @Override\n" +  
                        "  protected void setUp() throws Exception\n" + // overriding the setUp method
                        "  {\n" +
                        "    super.setUp();\n" +
                        "    // System variables for this test\n" +
                        "    System.setProperty(\"verbose\", \"" + verbose + "\");\n" + 
                        "    System.setProperty(\"project.directory\", \"" + projectToTest + "\");\n" + 
                        "    System.setProperty(\"properties.file.name\", \"" + escapeBackSlash(testPropFileName) + "\");\n" + 
                        "    System.setProperty(\"oracle.security.jps.config\", \"" + escapeBackSlash(jpsconfig) + "\");\n" +
                        "    if (" + Boolean.toString("true".equals(verbose)) + ")\n" +
                        "      System.out.println(\"System variables are set.\");\n" +
                        "  }\n";
                        // Transition ?
                        if (transitionToFile != null && (transitionFromPath != null || transitionFromLiteral != null) && transitionToPath != null)                
                        {
                          if (transitionFromPath != null && transitionFromLiteral != null)
                            throw new RuntimeException("Ambiguous transition definition:" + 
                                                       TRANSITION_FROM_XPATH_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition) + 
                                                       " OR " + TRANSITION_FROM_LITERAL_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition));
                          if (testIndex == 0 && transitionFromLiteral == null)  
                            throw new RuntimeException("For transition.0, only literal value are accepted.");
                          newCode += 
                            "  @Override\n" +  
                            "  protected void tearDown() throws Exception\n" +
                            "  {\n" +
                            "    super.tearDown();\n" +
                            "    oracle.xml.parser.v2.XMLElement fromXML = null;\n";
                          if (testIndex > 0)
                          {
                            newCode +=
                            "    fromXML = this.getResponsePayload();\n" +
                            "    if (" + Boolean.toString("true".equals(verbose)) + ")\n" +
                            "    {\n" + 
                            "      System.out.println(\"=== Previous Response Payload: ===\");\n" + 
                            "      fromXML.print(System.out);\n" +
                            "      System.out.println(\"==================================\");\n" + 
                            "    }\n";
                          }
                          newCode +=
                            "    oracle.xml.parser.v2.XMLElement toXML   = unittests.util.Utilities.fileToXML(\"" + escapeBackSlash(transitionToFile) + "\");\n";
                          boolean keepTransitioning = true;
                          while (keepTransitioning)
                          {
                            if (transitionFromPath != null)
                            {
                              newCode +=
                                "    toXML = unittests.util.Utilities.patchXML(fromXML,\n" +
                                "                                              toXML,\n" +
                                "                                             \"" + transitionFromPath + "\",\n" + 
                                "                                             \"" + transitionToPath + "\");\n";
                            }
                            else if (transitionFromLiteral != null)
                            {
                              newCode +=  
                              "    String literalValue = \"" + transitionFromLiteral + "\";\n" +
                              "    toXML = unittests.util.Utilities.patchXML(literalValue,\n" +
                              "                                              toXML,\n" +
                              "                                             \"" + transitionToPath + "\");\n";
                            }
                            nbTransition++;
                            transitionFromPath = tp.getProperty(TRANSITION_FROM_XPATH_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                            transitionFromLiteral = tp.getProperty(TRANSITION_FROM_LITERAL_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                            transitionToPath   = tp.getProperty(TRANSITION_TO_XPATH_RADICAL + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
                            keepTransitioning = (transitionFromPath != null || transitionFromLiteral != null) && transitionToPath != null;
                          }
                          newCode += "    unittests.util.Utilities.spitXMLtoFile(toXML, \"" + escapeBackSlash(transitionToFile) + "\");\n";
                          newCode += "  }\n";
                        }
                        newCode += "}";
                      if (/* testIndex == 0 || */ "true".equals(verbose))
                        System.out.println("Code to Compile:\n" + newCode);       
                      // Secret property: delete.generated.class=true|false (true is the default)
                      boolean deleteGeneratedClass = "true".equals(tp.getProperty("delete.generated.class", "true"));
                      Class extended = null;
                      try
                      {
                        extended = InFlightCompilation.compileFromStringAndLoad(packageName, 
                                                                                className, 
                                                                                classesDir, 
                                                                                newCode, 
                                                                                getTestDiscovererClassLoader(),
                                                                                deleteGeneratedClass); // default true
                      }
                      catch (Exception ce)
                      {
                        ce.printStackTrace();
                        System.err.println("...Aborting.");
                        return;
                      }
                      if (extended != null)
                      {
                        if ("true".equals(verbose))
                          System.out.println("Class " + className + " added to the TestSuite");
                        ts.add(extended);
                      }
                      else
                        System.out.println(className + " NOT added in the suite (null)");
                    }
                  }
                  catch (Exception ex)
                  {
                    System.err.println("Test Exception - 1 !");
                    ex.printStackTrace();
                  }
                }
              }
              catch (ClassNotFoundException cnfe)
              {
                System.err.println("Class [" + testClass + "] not found ?");
                cnfe.printStackTrace();
              }
              catch (Exception ex)
              {
                System.err.println("Test Exception - 2 !");
                ex.printStackTrace();
              }
              // End of code generation
            }
            testIndex++;
          }
          else
            keepTesting = false;
        }
      }
      else
        System.out.println("No test file found.");
    }
    catch (Exception ex)
    {
      System.err.println("Exception at the test definition level:");
      ex.printStackTrace();
    }
    
    System.out.println("This is a suite of " + ts.size() + "/" + nbTest2Generate + " Test(s). (" + nbSkip + " skipped)");
    testSuite = ts.toArray(new Class/*<? extends TestCase>*/[ts.size()]);
  }

  private static String escapeBackSlash(String s)
  {
    String str = "";
    str = s.replace("\\", "\\\\");
    
    return str;
  }
  
  /*
   * For tests
   */
  public static void main2(String[] args)
  {
    if (args.length > 0)
      projectToTest = args[0];
    
 /* TestDiscoverer me = */ new TestDiscoverer();        
  }
  
  public static void main1(String[] args)
  {
    /* TestDiscoverer td = */ new TestDiscoverer(); // Default value for properties file: test.suite.definition.properties
  }
  
  public static void main0(String[] args)
  {
    System.out.println(escapeBackSlash("C:\\akeu\\coucou.txt"));
  }
}
