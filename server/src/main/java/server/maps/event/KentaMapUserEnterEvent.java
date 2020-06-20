package server.maps.event;

import client.MapleCharacter;
import server.Timer.MapTimer;
import server.Timer.MobTimer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class KentaMapUserEnterEvent implements MapEvent {

  public static final int HOG_ID = 9300101;

  public static final int PHEROMONE_ID = 4031507;

  private MapleMap map;

  private KentaMapUserEnterEvent() {

  }

  public KentaMapUserEnterEvent(MapleMap map) {
    this();
    this.map = map;
  }


  @Override
  public void onUserEnter(MapleCharacter c) {

    map.broadcastMessage(MaplePacketCreator.getClock((int) (5 * 60)));
    map.spawnMonsterOnGroundBelow(HOG_ID, 150, 150);

    MapleMonster monster = map.getMonsterById(HOG_ID);

    final MobTimer timerManager = MobTimer.getInstance();

    for (int i = 0; i < 10; i++) {
      timerManager.schedule(() -> {
        map.spawnAutoDrop(PHEROMONE_ID, monster.getPosition());
      }, 1000 * i);
    }
    MapTimer.getInstance().schedule(() -> {
      for (MapleCharacter c1 : map.getCharacters()) {
        c1.changeMap(230000003);
      }
    }, 1000 * 60 * 5);

  }

  @Override
  public void onUserExit(MapleCharacter c) {
    map.killMonster(HOG_ID);
    map.broadcastMessage(MaplePacketCreator.stopClock());

  }

}
