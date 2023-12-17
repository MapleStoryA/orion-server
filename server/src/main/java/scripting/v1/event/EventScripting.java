package scripting.v1.event;

public class EventScripting {

    private Event event;

    public EventScripting(Event event) {
        this.event = event;
    }

    public void log(String message) {
      System.out.println(message);
    }

    public void schedule(String name, long timeInSeconds) {
        event.schedule(name, timeInSeconds);
    }
}
