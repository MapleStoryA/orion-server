package scripting.v1.event.engine;

import handling.world.party.MapleParty;
import scripting.v1.game.TargetScripting;
import server.life.MapleMonster;
import server.maps.MapleMap;

public interface EventEngine {

    void loadScript();

    void onEventStart();

    void onEventFinish();

    void onPlayerDisconnected(TargetScripting player);

    void onPlayerJoin(TargetScripting player);

    void onPlayerLeave(TargetScripting player);

    void onPartyDisband(MapleParty party);

    void onPlayerDied(TargetScripting player);

    void onPlayerExitMap(TargetScripting mapleCharacter, MapleMap map);

    void onMobKilled(TargetScripting killer, MapleMonster mob);

    void addToContext(String key, Object obj);

    void removeFromContext(String key);

    void invokeAction(String method);

    String getProperty(String key);


}
