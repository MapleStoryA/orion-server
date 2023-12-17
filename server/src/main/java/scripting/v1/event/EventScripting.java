package scripting.v1.event;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventScripting {

    private Event event;

    public EventScripting(Event event) {
        this.event = event;
    }

    public void log(String message) {
        log.info(message);
    }

    public void startEvent(MapleCharacter player, int startMapId, int endMapId, int timerInSeconds) {
        event.startEvent(player, startMapId, endMapId, timerInSeconds);
    }
}