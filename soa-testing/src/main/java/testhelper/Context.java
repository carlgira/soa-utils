package testhelper;

import oracle.xml.parser.v2.DOMParser;

/**
 * A singleton, to hold whatever is going to be used
 * during the test session.
 */
public class Context
{
  private static Context instance = null;  
  private static DOMParser parser = null;
  
  private Context()
  {
    parser = new DOMParser();
  }
  
  public synchronized static Context getInstance()
  {
    if (instance == null)
      instance = new Context();
    return instance;    
  }
  
  public DOMParser getParser()
  {
    return parser;
  }
}
