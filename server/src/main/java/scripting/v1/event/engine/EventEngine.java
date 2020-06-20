package scripting.v1.event.engine;

import handling.world.party.MapleParty;
import scripting.v1.binding.TargetScript;
import server.life.MapleMonster;
import server.maps.MapleMap;

public interface EventEngine {

  void loadScript();

  void onEventStart();

  void onEventFinish();

  void onPlayerDisconnected(TargetScript player);

  void onPlayerJoin(TargetScript player);

  void onPlayerLeave(TargetScript player);

  void onPartyDisband(MapleParty party);

  void onPlayerDied(TargetScript player);

  void onPlayerExitMap(TargetScript mapleCharacter, MapleMap map);

  void onMobKilled(TargetScript killer, MapleMonster mob);

  void addToContext(String key, Object obj);

  void removeFromContext(String key);

  void invokeAction(String method);

  String getProperty(String key);


}
