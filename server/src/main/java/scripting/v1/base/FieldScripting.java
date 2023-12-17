package scripting.v1.base;

import client.MapleCharacter;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Scripting;

import java.awt.*;
import java.util.Collection;

@lombok.extern.slf4j.Slf4j
public class FieldScripting {

    private final MapleMap map;

    @Scripting
    public FieldScripting(MapleMap map) {
        this.map = map;
    }

    public int id() {
        return map.getId();
    }

    @Scripting
    public void showEffect(String effect) {
        broadcastPacket(MaplePacketCreator.showEffect(effect));
    }

    @Scripting
    public Collection<MapleCharacter> fieldMembers() {
        return map.getCharacters();
    }

    @Scripting
    public int fieldMembersCount() {
        return fieldMembers().size();
    }

    @Scripting
    public void environmentChange(String env) {
        broadcastPacket(MaplePacketCreator.environmentChange(env, 2));
    }

    @Scripting
    public void playSound(String sound) {
        broadcastPacket(MaplePacketCreator.playSound(sound));
    }

    @Scripting
    public void changeMusic(String music) {
        broadcastPacket(MaplePacketCreator.musicChange(music));
    }

    @Scripting
    public void setMapVar(String key, String value) {
        for (MapleCharacter chr : map.getCharacters()) {
            chr.addTemporaryData(key, value);
        }
    }

    @Scripting
    public void spawnMonster(int id, int x, int y) {
        map.spawnMonsterOnGroundBelow(id, x, y);
    }

    private void broadcastPacket(byte[] packet) {
        map.broadcastMessage(packet);
    }

    @Scripting
    public void spawnNpcWithEffect(int npcId, int x, int y) {
        map.spawnNpcWithEffect(npcId, new Point(x, y));
    }

    @Scripting
    public void removeNpcWithEffect(int npcId) {
        map.makeNpcInvisible(npcId);
        map.removeNpc(npcId);
    }

    @Scripting
    public void killAllMonsters() {
        map.killAllMonsters(false);
    }

    @Scripting
    public int getMonsterCount() {
        return map.getAllMonster().size();
    }

    @Scripting
    public void spawnSpecialMonsters() {
        map.spawnSpecialMonsters();
    }


}
