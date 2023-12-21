package scripting.v1.base;

import client.MapleCharacter;
import java.awt.*;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.helper.Api;

@Slf4j
public class FieldScripting {

    private final MapleMap map;

    @Api
    public FieldScripting(MapleMap map) {
        this.map = map;
    }

    public int id() {
        return map.getId();
    }

    @Api
    public void showEffect(String effect) {
        broadcastPacket(MaplePacketCreator.showEffect(effect));
    }

    @Api
    public Collection<MapleCharacter> fieldMembers() {
        return map.getCharacters();
    }

    @Api
    public int fieldMembersCount() {
        return fieldMembers().size();
    }

    @Api
    public void environmentChange(String env) {
        broadcastPacket(MaplePacketCreator.environmentChange(env, 2));
    }

    @Api
    public void playSound(String sound) {
        broadcastPacket(MaplePacketCreator.playSound(sound));
    }

    @Api
    public void changeMusic(String music) {
        broadcastPacket(MaplePacketCreator.musicChange(music));
    }

    @Api
    public void setMapVar(String key, String value) {
        for (MapleCharacter chr : map.getCharacters()) {
            chr.addTemporaryData(key, value);
        }
    }

    @Api
    public void spawnMonster(int id, int x, int y) {
        map.spawnMonsterOnGroundBelow(id, x, y);
    }

    private void broadcastPacket(byte[] packet) {
        map.broadcastMessage(packet);
    }

    @Api
    public void spawnNpcWithEffect(int npcId, int x, int y) {
        map.spawnNpcWithEffect(npcId, new Point(x, y));
    }

    @Api
    public void removeNpcWithEffect(int npcId) {
        map.makeNpcInvisible(npcId);
        map.removeNpc(npcId);
    }

    @Api
    public void killAllMonsters() {
        map.killAllMonsters(false);
    }

    @Api
    public int getMonsterCount() {
        return map.getAllMonster().size();
    }

    @Api
    public void spawnSpecialMonsters() {
        map.spawnSpecialMonsters();
    }
}
