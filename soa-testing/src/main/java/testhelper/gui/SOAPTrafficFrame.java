package testhelper.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.Font;

import java.awt.event.WindowAdapter;

import java.awt.event.WindowEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import jsyntaxpane.DefaultSyntaxKit;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import testhelper.ServiceUnitTestHelper;

public class SOAPTrafficFrame
  extends JFrame
{
  @SuppressWarnings("compatibility:9021159981053973814")
  private static final long serialVersionUID = 1L;
  
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane = new JTabbedPane();
  private JScrollPane editorScrollPaneOut = new JScrollPane();
  private JEditorPane textEditorPaneOut   = new JEditorPane();
  private JScrollPane editorScrollPaneIn  = new JScrollPane();
  private JEditorPane textEditorPaneIn    = new JEditorPane();

  private transient ServiceUnitTestHelper parent;
  
  public SOAPTrafficFrame(ServiceUnitTestHelper parent)
  {
    if (false)
    {
      // Who called me
      Throwable t = new Throwable("From " + this.getClass().getName() + " constructor.");
      StackTraceElement[] elements = t.getStackTrace(); 
      System.out.println("----------------------------------");
      for (StackTraceElement ste : elements)
        System.out.println(ste.toString());
      System.out.println("----------------------------------");    
    }
    this.parent = parent;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.getContentPane().setLayout(borderLayout1);
    DefaultSyntaxKit.initKit(); // XML Syntax highlighting
    this.setSize( new Dimension(400, 300) );
    this.setTitle( "SOAP Traffic" );
    this.getContentPane().add(jTabbedPane, BorderLayout.CENTER);
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() 
                            {
                               public void windowClosing(WindowEvent we) 
                               {
                                 synchronized (parent.getGUIThread()) { parent.getGUIThread().notify(); }
                               }
                            });              

    editorScrollPaneOut.getViewport().add(textEditorPaneOut, null);
    textEditorPaneOut.setFont(new Font("Courier New", 0, 11));

    editorScrollPaneIn.getViewport().add(textEditorPaneIn, null);
    textEditorPaneIn.setFont(new Font("Courier New", 0, 11));
    
    textEditorPaneOut.setContentType("text/xml"); // For the syntax highlighter
    textEditorPaneIn.setContentType("text/xml"); // For the syntax highlighter

    jTabbedPane.add("Request", editorScrollPaneOut);
    jTabbedPane.add("Response", editorScrollPaneIn);
  }
  
  public void setMessageOut(String text)
  {
    text = prettyPrint(text);
    textEditorPaneOut.setText(text);
  }

  public void setMessageIn(String text)
  {
    text = prettyPrint(text);
    textEditorPaneIn.setText(text);
  }

  /**
   * Pretty print a XML String (payload).
   *
   * @param xml The string containing XML that you want to pretty print.
   */
  private static String prettyPrint(String xml)
  {
    try
    {
      Document doc = DocumentHelper.parseText(xml);
      StringWriter sw = new StringWriter();
      OutputFormat format = OutputFormat.createPrettyPrint();
      XMLWriter xw = new XMLWriter(sw, format);
      xw.write(doc);
      return sw.toString();
    }
    catch (Exception e)
    {
      return null;
    }
  }
}
