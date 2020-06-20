package server.maps.event;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class Aran3thJobThiefEvent extends AbstractMapEvent {

  public Aran3thJobThiefEvent(MapleMap map) {
    super(map);
  }

  @Override
  public void onUserEnter(MapleCharacter c) {
    boolean gem1 = c.getInventory(MapleInventoryType.ETC).countById(4032312) >= 1;
    boolean gem2 = c.getInventory(MapleInventoryType.ETC).countById(4032339) >= 1;
    if (!(gem1 && gem2)) {
      map.broadcastMessage(MaplePacketCreator.getClock((int) (20 * 60)));
      map.spawnMonsterOnGroundBelow(9001013, -177, 454);
    }


  }

  @Override
  public void onUserExit(MapleCharacter c) {
    map.killAllMonsters(false);
  }

}
