package testing.util;

import java.io.File;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Hashtable;

import java.util.Iterator;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;


public class ClasspathTask extends Task
{
  private boolean verbose = false;
  private String failureproperty = "";
  private String haltonfailure = "yes";
  
  private Path path = null;
  
  private boolean stopOnFailure = true;
  
  public ClasspathTask()
  {
    super();
  }

  private PrintStream out = System.out;
  
  public void addPath(Path path)
  {
    this.path = path;
  }

  @Override
  public void execute() throws BuildException
  {
    super.execute();
    
    stopOnFailure = !("no".equals(haltonfailure));
    
    if (verbose)
      out.println("Initializing Failure Property:" + failureproperty);
    
    // Failure set to false means success
    String fp = this.getProject().getProperty(failureproperty);
    if (fp == null || fp.trim().length() == 0)
    {
      if (verbose) out.println(failureproperty + " does not exist yet.");
      this.getProject().setNewProperty(failureproperty, Boolean.valueOf(false).toString());
    }
    else
    {
      if (verbose) out.println(failureproperty + " exists already:" + fp);
      this.getProject().setProperty(failureproperty, Boolean.valueOf(false).toString());
    }
    
    // Test Core
    try
    {
      if (this.path == null)
        throw new BuildException("Classpath is null");
      else
      {
        ArrayList<String> missingBits = new ArrayList<String>();
        Iterator iterator = this.path.iterator();
        while (iterator.hasNext())
        {
          Object o = iterator.next();
          if (o instanceof FileResource)
          {
            FileResource fr = (FileResource)o;
            File f = fr.getFile();
            if (!f.exists())
            {
//            System.out.println(f.getAbsolutePath() + " not found!");      
              missingBits.add(f.getAbsolutePath());
            }
          }
          else
            System.out.println("Object is a :" + o.getClass().getName());
        }
        if (missingBits.size() > 0)
        {
          String errorMessage = Integer.toString(missingBits.size()) + " Missing Elements (not found) :\n";
          errorMessage += "---------------------------------\n";
          int i = 0;
          for (String s : missingBits)
            errorMessage += (Integer.toString(++i) + " - " + s + "\n");
          if (stopOnFailure)
            throw new BuildException(errorMessage);
          else
            System.err.println(errorMessage);
        }
        else
          System.out.println("Classpath is 100% resolved.");
      }
    }
    catch (Exception ex)
    {
      throw new BuildException(ex);
    }
  }

  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }

  public boolean isVerbose()
  {
    return verbose;
  }

  public void setFailureproperty(String failureproperty)
  {
    this.failureproperty = failureproperty;
  }

  public String getFailureproperty()
  {
    return failureproperty;
  }

  public void setHaltonfailure(String haltonfailure)
  {
    this.haltonfailure = haltonfailure;
  }

  public String getHaltonfailure()
  {
    return haltonfailure;
  }
}
