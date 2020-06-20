package server.maps.event;

import client.MapleCharacter;
import server.life.MapleMonster;

public interface MobObserver {

  void onMonsterDie(MapleCharacter killer, MapleMonster monster);

  void onMonsterSpawn(MapleCharacter character, MapleMonster monster);
}
