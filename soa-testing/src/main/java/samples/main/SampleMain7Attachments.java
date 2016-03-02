package samples.main;

import java.util.Iterator;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;

import oracle.j2ee.ws.saaj.soap.AttachmentPartImpl;

import oracle.xml.parser.v2.XMLElement;
import testhelper.ServiceUnitTestHelper;

public class SampleMain7Attachments
{

  // WSDL: http://adc60016fems.us.oracle.com:6027/pjcTransactions/ProjectExpenditureItemService?WSDL
  // For the synchronous service
  private static final String SERVICE_NAME = "WorkerService";
  private static final String PORT = "WorkerServiceSoapHttpPort";

  private static final String SERVICE_ENDPOINT =
    "http://rws65094fwks.us.oracle.com:7202/MySampleApp-DataTypeService-context-root/WorkerService";
  private static final String WSDL_LOC = SERVICE_ENDPOINT + "?WSDL";
  private static final String SERVICE_NS_URI =
    "http://xmlns.oracle.com/apps/sample/dtsvc/";

  private static final String SERVICE_REQUEST =
    "<ns1:getWorker xmlns:ns1=\"http://xmlns.oracle.com/apps/sample/dtsvc/types/\">\n" + 
    "  <ns1:workerId>111</ns1:workerId>\n" + 
    "</ns1:getWorker>";
  
  public static void main(String[] args)
    throws Exception
  {
    try
    {
      XMLElement x = null;

      ServiceUnitTestHelper suth = new ServiceUnitTestHelper();
      suth.setVerbose(true);
//    suth.setHandlerResolver(new CustomHandlerResolver());
      
      System.out.println("+-------+");
      System.out.println("|  one  |");
      System.out.println("+-------+");
      String svcEndpoint = suth.getEndpointURL(WSDL_LOC);
      x = suth.invokeSyncService(svcEndpoint, 
                                 WSDL_LOC, 
                                 SERVICE_NS_URI, 
                                 SERVICE_REQUEST,
                                 SERVICE_NAME, 
                                 PORT);
      System.out.println("==== Synchronous, response ======");
      x.print(System.out);
      System.out.println("=================================");
      // Attachement?
      Iterator it = suth.getAttachmentIterator();
      if (it != null)
      {
        int nbAtt = 1;
        System.out.println("=========== There is attachment ============");
        while (it.hasNext())
        {
          Object attachment = it.next();
          System.out.println("************** Start of attachment #" + Integer.toString(nbAtt) + " ***************");
          if (attachment instanceof AttachmentPartImpl)
          {
            AttachmentPartImpl api = (AttachmentPartImpl)attachment;
            Object content = api.getContent();
            MimeHeaders mh = api.getMimeHeaders();
            Iterator mimeHeadersIterator = mh.getAllHeaders();
            System.out.println("Content is a " + content.getClass().getName() + ":");
            System.out.println(content.toString());
            System.out.println("--------------------------------------------");
            System.out.println("MimeHeaders:");
            while (mimeHeadersIterator.hasNext())
            {
              Object mimeHeader = mimeHeadersIterator.next();
              if (mimeHeader instanceof MimeHeader)
              {
                MimeHeader mHeader = (MimeHeader)mimeHeader;
                System.out.println(mHeader.getName() + "=" + mHeader.getValue());
              }
              else
                System.out.println("Mime-Header is a " + mimeHeader.getClass().getName());
            }
          }
          else
            System.out.println("Attachment is a " + attachment.getClass().getName());
          System.out.println("************** End of attachment #" + Integer.toString(nbAtt) + " ***************");
          nbAtt++;
        }        
        System.out.println("=========== There was attachment ===========");
      }
      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
