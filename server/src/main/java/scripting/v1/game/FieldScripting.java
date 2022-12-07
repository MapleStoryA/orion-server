package scripting.v1.game;

import client.MapleCharacter;
import scripting.v1.game.helper.ScriptingApi;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.Collection;

@lombok.extern.slf4j.Slf4j
public class FieldScripting {

    private final MapleMap map;

    @ScriptingApi
    public FieldScripting(MapleMap map) {
        this.map = map;
    }

    @ScriptingApi
    public void showEffect(String effect) {
        broadcastPacket(MaplePacketCreator.showEffect(effect));
    }

    @ScriptingApi
    public Collection<MapleCharacter> fieldMembers() {
        return map.getCharacters();
    }

    @ScriptingApi
    public int fieldMembersCount() {
        return fieldMembers().size();
    }

    @ScriptingApi
    public void environmentChange(String env) {
        broadcastPacket(MaplePacketCreator.environmentChange(env, 2));
    }

    @ScriptingApi
    public void playSound(String sound) {
        broadcastPacket(MaplePacketCreator.playSound(sound));
    }

    @ScriptingApi
    public void changeMusic(String music) {
        broadcastPacket(MaplePacketCreator.musicChange(music));
    }

    @ScriptingApi
    public void setMapVar(String key, String value) {
        for (MapleCharacter chr : map.getCharacters()) {
            chr.addTemporaryData(key, value);
        }
    }

    @ScriptingApi
    public void spawnMonster(int id, int x, int y) {
        map.spawnMonsterOnGroundBelow(id, x, y);
    }

    private void broadcastPacket(byte[] packet) {
        map.broadcastMessage(packet);
    }

    @ScriptingApi
    public void spawnNpcWithEffect(int npcId, int x, int y) {
        map.spawnNpcWithEffect(npcId, new Point(x, y));
    }

    @ScriptingApi
    public void removeNpcWithEffect(int npcId) {
        map.makeNpcInvisible(npcId);
        map.removeNpc(npcId);
    }

    @ScriptingApi
    public void killAllMonsters() {
        map.killAllMonsters(false);
    }

    @ScriptingApi
    public int getMonsterCount() {
        return map.getAllMonster().size();
    }

    @ScriptingApi
    public void spawnSpecialMonsters() {
        map.spawnSpecialMonsters();
    }

    @ScriptingApi
    public void clearEventInstance() {
        map.clearEventInstance();
    }


}
