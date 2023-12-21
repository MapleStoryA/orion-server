package scripting.v1.event;

import client.MapleCharacter;
import client.MapleJob;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.base.FieldScripting;
import tools.helper.Api;

@Slf4j
public class EventScripting {

    private final Event event;

    public EventScripting(Event event) {
        this.event = event;
    }

    @Api
    public void log(String message) {
        log.info(message);
    }

    @Api
    public void startEvent(MapleCharacter player, int[] mapIds, int timerInSeconds) {
        event.startEvent(player, mapIds, timerInSeconds);
    }

    @Api
    public int[] get3rdJobMobMapInfo() {
        return MapleJob.get3rdJobMobMapInfo(event.getLeader());
    }

    @Api
    public MapleCharacter getLeader() {
        return event.getLeader();
    }

    @Api
    public FieldScripting getField(int mapId) {
        return event.getField(mapId);
    }
}
