package server.maps.event;

import client.MapleCharacter;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class SimpleQuestMapEvent extends AbstractMapEvent {

  public SimpleQuestMapEvent(MapleMap map) {
    super(map);
  }

  @Override
  public void onUserEnter(MapleCharacter c) {
    map.broadcastMessage(MaplePacketCreator.getClock((int) (10 * 60)));
  }

  @Override
  public void onUserExit(MapleCharacter c) {
    map.killAllMonsters(true);

  }

}
