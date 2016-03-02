package testing.dummy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.AbstractNodeTester;
import org.custommonkey.xmlunit.CountingNodeTester;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.HTMLDocumentBuilder;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTestException;
import org.custommonkey.xmlunit.TolerantSaxDocumentBuilder;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


public class XMLUnitSample
  extends XMLTestCase
{
  public XMLUnitSample(String name)
  {
    super(name);
  }

  public void testAndreaXML() throws Exception
  {
    String ns12 = "", ns13 = "";
    BufferedReader brns12 = new BufferedReader(new FileReader("ns12.xml"));
    BufferedReader brns13 = new BufferedReader(new FileReader("ns13.xml"));
    String line = "";
    while (line != null)
    {
      line = brns12.readLine();
      if (line != null)
        ns12 += line;
    }
    brns12.close();
    line = "";
    while (line != null)
    {
      line = brns13.readLine();
      if (line != null)
        ns13 += line;
    }
    brns13.close();
    Diff myDiff = new Diff(ns12, ns13);

    assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    System.out.println(ns12 + "\n is identical to\n" + ns13);
  }
  

  public void testForEqualityWithNS()
    throws Exception
  {
    String myControlXML = "<ns1:msg xmlns:ns1=\"urn:dummy\"><ns1:uuid>0x00435A8C</ns1:uuid></ns1:msg>";
    String myTestXML    = "<msg xmlns=\"urn:dummy\"><uuid>0x00435A8C</uuid></msg>";
    Diff myDiff = new Diff(myControlXML, myTestXML);

    assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    System.out.println(myControlXML + "\n is identical to\n" + myTestXML);

    myTestXML = "<ns2:msg xmlns:ns2=\"urn:dummy\"><ns2:uuid>0x00435A8C</ns2:uuid></ns2:msg>";
    myDiff    = new Diff(myControlXML, myTestXML);

    assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    System.out.println(myControlXML + "\n is identical to\n" + myTestXML);

//  assertXMLEqual("comparing test xml to control xml", myControlXML, myTestXML);

//  assertXMLNotEqual("test xml not similar to control xml", myControlXML, myTestXML);
  }

  public void testForEquality()
    throws Exception
  {
    String myControlXML = "<msg><uuid>0x00435A8C</uuid></msg>";
    String myTestXML = "<msg><localId>2376</localId></msg>";
    assertXMLEqual("comparing test xml to control xml", myControlXML, myTestXML);

    assertXMLNotEqual("test xml not similar to control xml", myControlXML, myTestXML);
  }

  public void testIdentical()
    throws Exception
  {
    String myControlXML =
      "<struct><int>3</int><boolean>false</boolean></struct>";
    String myTestXML =
      "<struct><boolean>false</boolean><int>3</int></struct>";
    Diff myDiff = new Diff(myControlXML, myTestXML);
    assertTrue("pieces of XML are similar " + myDiff, myDiff.similar());
    assertTrue("but are they identical? " + myDiff, myDiff.identical());
  }

  public void testAllDifferences()
    throws Exception
  {
    String myControlXML =
      "<news><item id=\"1\">War</item>" + "<item id=\"2\">Plague</item><item id=\"3\">Famine</item></news>";
    String myTestXML =
      "<news><item id=\"1\">Peace</item>" + "<item id=\"2\">Health</item><item id=\"3\">Plenty</item></news>";
    DetailedDiff myDiff =
      new DetailedDiff(compareXML(myControlXML, myTestXML));
    List allDifferences = myDiff.getAllDifferences();
    assertEquals(myDiff.toString(), 0, allDifferences.size());
  }

  public void testCompareToSkeletonXML()
    throws Exception
  {
    String myControlXML =
      "<location><street-address>22 any street</street-address><postcode>XY00 99Z</postcode></location>";
    String myTestXML =
      "<location><street-address>20 east cheap</street-address><postcode>EC3M 1EB</postcode></location>";
    DifferenceListener myDifferenceListener =
      new IgnoreTextAndAttributeValuesDifferenceListener();
    Diff myDiff = new Diff(myControlXML, myTestXML);
    myDiff.overrideDifferenceListener(myDifferenceListener);
    assertTrue("test XML matches control skeleton XML " + myDiff, myDiff.similar());
  }

  public void testRepeatedChildElements()
    throws Exception
  {
    String myControlXML =
      "<suite><test status=\"pass\">FirstTestCase</test><test status=\"pass\">SecondTestCase</test></suite>";
    String myTestXML =
      "<suite><test status=\"pass\">SecondTestCase</test><test status=\"pass\">FirstTestCase</test></suite>";

    assertXMLNotEqual("Repeated child elements in different sequence order are not equal by default",
                      myControlXML, myTestXML);

    Diff myDiff = new Diff(myControlXML, myTestXML);
    myDiff.overrideElementQualifier(new ElementNameAndTextQualifier());
    assertXMLEqual("But they are equal when an ElementQualifier controls which test element is compared with each control element",
                   myDiff, true);
  }

  public void testXSLTransformation()
    throws Exception
  {
    String myInputXML = "...";
    File myStylesheetFile = new File("...");
    Transform myTransform = new Transform(myInputXML, myStylesheetFile);
    String myExpectedOutputXML = "...";
    Diff myDiff = new Diff(myExpectedOutputXML, myTransform);
    assertTrue("XSL transformation worked as expected " + myDiff,
               myDiff.similar());
  }

  public void testAnotherXSLTransformation()
    throws Exception
  {
    File myInputXMLFile = new File("...");
    File myStylesheetFile = new File("...");
    Transform myTransform = new Transform(new StreamSource(myInputXMLFile), new StreamSource(myStylesheetFile));
    Document myExpectedOutputXML =
      XMLUnit.buildDocument(XMLUnit.getControlParser(),
                            new FileReader("..."));
    Diff myDiff =
      new Diff(myExpectedOutputXML, myTransform.getResultDocument());
    assertTrue("XSL transformation worked as expected " + myDiff,
               myDiff.similar());
  }

  public void testValidation()
    throws Exception
  {
    XMLUnit.getTestDocumentBuilderFactory().setValidating(true);
    // As the document is parsed it is validated against its referenced DTD
    Document myTestDocument = XMLUnit.buildTestDocument("...");
    String mySystemId = "...";
    String myDTDUrl = new File("...").toURI().toURL().toExternalForm();
    Validator myValidator =
      new Validator(myTestDocument, mySystemId, myDTDUrl);
    assertTrue("test document validates against unreferenced DTD",
               myValidator.isValid());
  }

  public void testXPaths()
    throws Exception
  {
    String mySolarSystemXML =
      "<solar-system><planet name='Earth' position='3' supportsLife='yes'/>" +
      "<planet name='Venus' position='4'/></solar-system>";
    assertXpathExists("//planet[@name='Earth']", mySolarSystemXML);
    assertXpathNotExists("//star[@name='alpha centauri']",
                         mySolarSystemXML);
    assertXpathsEqual("//planet[@name='Earth']", "//planet[@position='3']",
                      mySolarSystemXML);
    assertXpathsNotEqual("//planet[@name='Venus']",
                         "//planet[@supportsLife='yes']",
                         mySolarSystemXML);
  }

  public void testXPathValues()
    throws Exception
  {
    String myJavaFlavours =
      "<java-flavours><jvm current='some platforms'>1.1.x</jvm>" +
      "<jvm current='no'>1.2.x</jvm><jvm current='yes'>1.3.x</jvm>" +
      "<jvm current='yes' latest='yes'>1.4.x</jvm></java-flavours>";
    assertXpathEvaluatesTo("1.4.x", "//jvm[@latest='yes']",
                           myJavaFlavours);
    assertXpathEvaluatesTo("2", "count(//jvm[@current='yes'])",
                           myJavaFlavours);
    assertXpathValuesEqual("//jvm[4]/@latest", "//jvm[4]/@current",
                           myJavaFlavours);
    assertXpathValuesNotEqual("//jvm[2]/@current", "//jvm[3]/@current",
                              myJavaFlavours);
  }

  public void testXpathsInHTML()
    throws Exception
  {
    String someBadlyFormedHTML =
      "<html><title>Ugh</title><body><h1>Heading<ul><li id='1'>Item One<li id='2'>Item Two";
    TolerantSaxDocumentBuilder tolerantSaxDocumentBuilder =
      new TolerantSaxDocumentBuilder(XMLUnit.getTestParser());
    HTMLDocumentBuilder htmlDocumentBuilder =
      new HTMLDocumentBuilder(tolerantSaxDocumentBuilder);
    Document wellFormedDocument =
      htmlDocumentBuilder.parse(someBadlyFormedHTML);
    assertXpathEvaluatesTo("Item One", "/html/body//li[@id='1']",
                           wellFormedDocument);
  }

  public void testCountingNodeTester()
    throws Exception
  {
    String testXML =
      "<fibonacci><val>1</val><val>2</val><val>3</val>" + "<val>5</val><val>9</val></fibonacci>";
    CountingNodeTester countingNodeTester = new CountingNodeTester(4);
    assertNodeTestPasses(testXML, countingNodeTester, Node.TEXT_NODE);
  }

  public void testCustomNodeTester()
    throws Exception
  {
    String testXML =
      "<fibonacci><val>1</val><val>2</val><val>3</val>" + "<val>5</val><val>9</val></fibonacci>";
    NodeTest nodeTest = new NodeTest(testXML);
    assertNodeTestPasses(nodeTest, new FibonacciNodeTester(), new short[]
        { Node.TEXT_NODE, Node.ELEMENT_NODE }, true);
  }

  private class FibonacciNodeTester
    extends AbstractNodeTester
  {
    private int nextVal = 1, lastVal = 1, priorVal = 0;

    public void testText(Text text)
      throws NodeTestException
    {
      int val = Integer.parseInt(text.getData());
      if (nextVal != val)
      {
        throw new NodeTestException("Incorrect sequence value", text);
      }
      nextVal = val + lastVal;
      priorVal = lastVal;
      lastVal = val;
    }

    public void testElement(Element element)
      throws NodeTestException
    {
      String name = element.getLocalName();
      if ("fibonacci".equals(name) || "val".equals(name))
      {
        return;
      }
      throw new NodeTestException("Unexpected element", element);
    }

    public void noMoreNodes(NodeTest nodeTest)
      throws NodeTestException
    {
    }
  }
}
