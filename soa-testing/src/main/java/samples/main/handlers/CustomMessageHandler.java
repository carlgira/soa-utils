package samples.main.handlers;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class CustomMessageHandler implements SOAPHandler<SOAPMessageContext>
{
  public Set<QName> getHeaders()
  {
    return Collections.emptySet();
  }

  public boolean handleMessage(SOAPMessageContext context)
  {
    System.out.println("Handling SOAP Message");
    Boolean outbound = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    System.out.println("Outbound:" + outbound.booleanValue());
    try
    {
      System.out.println("---------- Message ----------");
      context.getMessage().writeTo(System.out);
      System.out.println("\n-----------------------------");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return true; // true means continue
  }

  public boolean handleFault(SOAPMessageContext context)
  {
    return false;
  }

  public void close(javax.xml.ws.handler.MessageContext context)
  {
    System.out.println("** Closing.");
  }
}
