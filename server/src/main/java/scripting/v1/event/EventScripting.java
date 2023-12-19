package scripting.v1.event;

import client.MapleCharacter;
import client.MapleJob;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.base.FieldScripting;
import tools.Scripting;

@Slf4j
public class EventScripting {

    private Event event;

    public EventScripting(Event event) {
        this.event = event;
    }

    @Scripting
    public void log(String message) {
        log.info(message);
    }

    @Scripting
    public void startEvent(MapleCharacter player, int[] mapIds, int timerInSeconds) {
        event.startEvent(player, mapIds, timerInSeconds);
    }

    @Scripting
    public int[] get3rdJobMobMapInfo() {
        return MapleJob.get3rdJobMobMapInfo(event.getLeader());
    }

    @Scripting
    public MapleCharacter getLeader() {
        return event.getLeader();
    }

    @Scripting
    public FieldScripting getField(int mapId) {
        return event.getField(mapId);
    }
}
