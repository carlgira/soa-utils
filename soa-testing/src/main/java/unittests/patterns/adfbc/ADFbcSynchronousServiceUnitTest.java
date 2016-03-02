package unittests.patterns.adfbc;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;

import unittests.patterns.services.SynchronousServiceUnitTest;

import unittests.util.Utilities;

public class ADFbcSynchronousServiceUnitTest
  extends SynchronousServiceUnitTest
{
  public ADFbcSynchronousServiceUnitTest()
  {
    super();
  }
  
  @Override
  public String afterReceive(String response)
  {
    return ADFbcUtils.patchResponse(response, verbose, props);
  }
  
  /**
   *Just for tests.
   * @param args
   */
  public static void main(String[] args)
  {
    try
    {
      ADFbcSynchronousServiceUnitTest thing = new ADFbcSynchronousServiceUnitTest();

      thing.props.setProperty("verbose", "true");
      
      thing.props.setProperty("xpath.to.patch.output.1", "/{http://xmlns.oracle.com/apps/sample/dtsvc/types/}createWorkerResponse//{http://xmlns.oracle.com/apps/sample/dtsvc/}WorkerId");
      thing.props.setProperty("value.to.patch.output.1", "XXX");

      thing.props.setProperty("xpath.to.patch.output.2", "//{http://xmlns.oracle.com/apps/sample/dtsvc/}Age");
//    thing.props.setProperty("value.to.patch.output.2", "YYY");      
      thing.props.setProperty("node.to.patch.output.2", "<ns:NewAge xmlns:ns=\"http://xmlns.oracle.com/apps/sample/dtsvc/\" extra-attr=\"some.value\"/>");

      thing.props.setProperty("xpath.to.patch.output.3", "//{http://xmlns.oracle.com/apps/sample/dtsvc/types/}result/@{http://www.w3.org/2001/XMLSchema-instance}type");
      thing.props.setProperty("value.to.patch.output.3", "no:prefix:fo:me");
      
      if (false)
      {
        String origXPath = "/{urn:one}root/{urn:two}akeu/{urn:three}coucou/{urn:two}again";
        HashMap<String, String> map = Utilities.prepareNSHashMap(origXPath);
        System.out.println("Map completed");
        String newExpr = Utilities.patchXPath(origXPath, map);
        System.out.println("Old:" + origXPath);
        System.out.println("New:" + newExpr);
        System.out.println("-----------------------------------------");
      }
      // For real:
      String original = 
      "<ns0:createWorkerResponse xmlns:ns0=\"http://xmlns.oracle.com/apps/sample/dtsvc/types/\">\n" + 
      "       <ns2:result xmlns:ns2=\"http://xmlns.oracle.com/apps/sample/dtsvc/types/\"\n" + 
      "                   xmlns:ns1=\"http://xmlns.oracle.com/apps/sample/dtsvc/\"\n" + 
      "                   xmlns:ns0=\"http://xmlns.oracle.com/adf/svc/types/\"\n" + 
      "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
      "                   xsi:type=\"ns1:Worker\">\n" + 
      "              <ns1:WorkerId>ABC</ns1:WorkerId>\n" + 
      "              <ns1:Name>xyz</ns1:Name>\n" + 
      "              <ns1:Age>23</ns1:Age>\n" + 
      "              <ns1:Birthday xsi:nil=\"true\"/>\n" + 
      "              <ns1:Salary xsi:nil=\"true\"/>\n" + 
      "              <ns1:Bonus xsi:nil=\"true\"/>\n" + 
      "              <ns1:LastUpdated xsi:nil=\"true\"/>\n" + 
      "       </ns2:result>\n" + 
      "</ns0:createWorkerResponse>\n";
      
      String newThing = thing.afterReceive(original);
      System.out.println("Patched:\n" + newThing);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

}
