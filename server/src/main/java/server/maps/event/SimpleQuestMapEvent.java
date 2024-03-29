package server.maps.event;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

@Slf4j
public class SimpleQuestMapEvent extends AbstractMapEvent {

    public SimpleQuestMapEvent(MapleMap map) {
        super(map);
    }

    @Override
    public void onUserEnter(MapleCharacter c) {
        map.broadcastMessage(MaplePacketCreator.getClock(10 * 60));
    }

    @Override
    public void onUserExit(MapleCharacter c) {
        map.killAllMonsters(true);
    }
}
