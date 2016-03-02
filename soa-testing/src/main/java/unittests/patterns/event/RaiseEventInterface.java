package unittests.patterns.event;

public interface RaiseEventInterface
{
  public String beforeRaisingEvent(String payload);
  public void afterRaisingEvent();
}
