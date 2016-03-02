package unittests.patterns.services;

import oracle.xml.parser.v2.XMLElement;

public interface ServiceUnitTestInterface
{
  public void firstOfAll();
  public String beforeInvoke(String payload);
  public void afterInvoke();
  public void beforeReceive();
  public String afterReceive(String payload);
  public void evaluate(String payload);
  public void onError(Exception ex);
  public XMLElement getResponsePayload();
}
