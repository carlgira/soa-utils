package unittests.util;

import java.util.Properties;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class VirtualClassWizard
  extends ClassLoader
  implements Opcodes
{
  private static VirtualClassWizard instance = null;
  
  private VirtualClassWizard()
  {
    super();    
  }

  private VirtualClassWizard(ClassLoader classLoader)
  {
    super(classLoader);
  }
  
  public static synchronized VirtualClassWizard getInstance(ClassLoader cl)
  {
    if (instance == null)
      instance = new VirtualClassWizard(cl);
    return instance;
  }
  
  public static synchronized VirtualClassWizard getInstance()
  {
    if (instance == null)
      instance = new VirtualClassWizard();
    return instance;
  }
  
  public Class generateAndLoad(String packageName,
                               String className,
                               String baseClassName,
                               boolean verbose,
                               String prjDirectory,
                               String testPropFileName,
                               String jpsconfig,
                               int testIndex,
                               Properties testProp,
                               String transitionToFile,
                               String transitionFromPath,
                               String transitionFromLiteral,
                               String transitionToPath)
    throws Exception
  {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    String fullyQualifiedClassName = packageName.replace('.', '/') + "/" + className;
    cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, fullyQualifiedClassName, null, baseClassName.replace('.', '/'), null);
    // Implicit constructor
    MethodVisitor mw = null;
    mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    // Pushes the this variable
    mw.visitVarInsn(Opcodes.ALOAD, 0);
    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, baseClassName.replace('.', '/'), "<init>", "()V"); // Super constructor
    mw.visitInsn(Opcodes.RETURN);
    mw.visitMaxs(1, 1);
    mw.visitEnd();
    
    if (testIndex > 0)
    {
      // setUp()
      mw = cw.visitMethod(Opcodes.ACC_PROTECTED, "setUp", "()V", null, null);
      // super.setUp()
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, baseClassName.replace('.', '/'), "setUp", "()V");
      // verbose property
      mw.visitLdcInsn("verbose");
      mw.visitLdcInsn(verbose?"true":"false");
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
      mw.visitInsn(Opcodes.POP);
      // project.directory property
      mw.visitLdcInsn("project.directory");
      mw.visitLdcInsn(prjDirectory);
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
      mw.visitInsn(Opcodes.POP);
      // properties.file.name property
      mw.visitLdcInsn("properties.file.name");
      mw.visitLdcInsn(testPropFileName);
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
      mw.visitInsn(Opcodes.POP);
      // oracle.security.jps.config property
      mw.visitLdcInsn("oracle.security.jps.config");
      mw.visitLdcInsn(jpsconfig);
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
      mw.visitInsn(Opcodes.POP);
      
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(6, 6);
      mw.visitEnd();
    }
    // tearDown() ?
    if (transitionToFile != null && (transitionFromPath != null || transitionFromLiteral != null) && transitionToPath != null)
    {
      if (transitionFromPath != null && transitionFromLiteral != null)
        throw new RuntimeException("Ambiguous transition definition: transition.from.xpath OR transition.from.literal, not both!");
      if (testIndex == 0 && transitionFromLiteral == null)  
        throw new RuntimeException("For transition.0, only literal value are accepted.");

      mw = cw.visitMethod(Opcodes.ACC_PROTECTED, "tearDown", "()V", null, null);
      // super.tearDown()
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, baseClassName.replace('.', '/'), "tearDown", "()V");
      mw.visitInsn(Opcodes.ACONST_NULL);
      mw.visitVarInsn(Opcodes.ASTORE, 1);
      
      // Loop on the transformations
      if (testIndex > 0)
      {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, baseClassName.replace('.', '/'), "getResponsePayload", "()Loracle/xml/parser/v2/XMLElement;");
        mw.visitVarInsn(Opcodes.ASTORE, 1);
      }
      mw.visitLdcInsn(transitionToFile);
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "unittests/util/Utilities", "fileToXML", "(Ljava/lang/String;)Loracle/xml/parser/v2/XMLElement;");
      mw.visitVarInsn(Opcodes.ASTORE, 2);

      // Actual loop goes here
      boolean keepTransitioning = true;
      int nbTransition = 1;
      while (keepTransitioning)
      {
        if (transitionFromPath != null)
        {
          mw.visitVarInsn(Opcodes.ALOAD, 1);
          mw.visitVarInsn(Opcodes.ALOAD, 2);
          mw.visitLdcInsn(transitionFromPath);
          mw.visitLdcInsn(transitionToPath);
          mw.visitMethodInsn(Opcodes.INVOKESTATIC, "unittests/util/Utilities", "patchXML", "(Loracle/xml/parser/v2/XMLElement;Loracle/xml/parser/v2/XMLElement;Ljava/lang/String;Ljava/lang/String;)Loracle/xml/parser/v2/XMLElement;");
          mw.visitVarInsn(Opcodes.ASTORE, 2);
        }
        else if (transitionFromLiteral != null)
        {
          mw.visitLdcInsn(transitionFromLiteral);
          mw.visitVarInsn(Opcodes.ASTORE, 3);
          mw.visitVarInsn(Opcodes.ALOAD, 3);
          mw.visitVarInsn(Opcodes.ALOAD, 2);
          mw.visitLdcInsn(transitionToPath);
          mw.visitMethodInsn(Opcodes.INVOKESTATIC, "unittests/util/Utilities", "patchXML", "(Ljava/lang/String;Loracle/xml/parser/v2/XMLElement;Ljava/lang/String;)Loracle/xml/parser/v2/XMLElement;");
          mw.visitVarInsn(Opcodes.ASTORE, 2);
        }
        nbTransition++;
        transitionFromPath    = testProp.getProperty("transition.from.xpath." + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
        transitionFromLiteral = testProp.getProperty("transition.from.literal." + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
        transitionToPath      = testProp.getProperty("transition.to.xpath." + Integer.toString(testIndex) + "." + Integer.toString(nbTransition), null);
        keepTransitioning = (transitionFromPath != null || transitionFromLiteral != null) && transitionToPath != null;
      }          
      // Spit out file
      mw.visitVarInsn(Opcodes.ALOAD, 2);
      mw.visitLdcInsn(transitionToFile);
      mw.visitMethodInsn(Opcodes.INVOKESTATIC, "unittests/util/Utilities", "spitXMLtoFile", "(Loracle/xml/parser/v2/XMLElement;Ljava/lang/String;)V");
            
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(6, 6);
      mw.visitEnd();      
    }
    
    // Now load
    cw.visitEnd();
    byte[] code = cw.toByteArray();
    
    // Want to write it down here ?
    
    if (verbose)
      System.out.println("Loading class " + fullyQualifiedClassName + " (as " + fullyQualifiedClassName.replace('/', '.') + "), byteCode length is " + Integer.toString(code.length) +  " ...");
    Class generatedClass = null;
    try { generatedClass = instance.defineClass(fullyQualifiedClassName.replace('/', '.'), code, 0, code.length); }
    catch (Throwable ex) 
    {       
      throw new Exception(ex); 
    }
    if (verbose)
      System.out.println("Class " + fullyQualifiedClassName + " loaded from the blue...");
    return generatedClass;
  }
}
