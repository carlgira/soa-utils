package util.javautil;

import java.lang.instrument.Instrumentation;

public class JavaAgent
{
  private static Instrumentation inst;

  public static Instrumentation getInstrumentation()
  {
    return inst;
  }

  public static void premain(String agentArgs, Instrumentation inst)
  {
//  System.out.println(inst.getClass() + ": " + inst);
    JavaAgent.inst = inst;
  }
}
