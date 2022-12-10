package scripting.v1.game;

import client.MapleCharacter;
import scripting.v1.game.helper.ApiClass;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.Collection;

@lombok.extern.slf4j.Slf4j
public class FieldScripting {

    private final MapleMap map;

    @ApiClass
    public FieldScripting(MapleMap map) {
        this.map = map;
    }

    @ApiClass
    public void showEffect(String effect) {
        broadcastPacket(MaplePacketCreator.showEffect(effect));
    }

    @ApiClass
    public Collection<MapleCharacter> fieldMembers() {
        return map.getCharacters();
    }

    @ApiClass
    public int fieldMembersCount() {
        return fieldMembers().size();
    }

    @ApiClass
    public void environmentChange(String env) {
        broadcastPacket(MaplePacketCreator.environmentChange(env, 2));
    }

    @ApiClass
    public void playSound(String sound) {
        broadcastPacket(MaplePacketCreator.playSound(sound));
    }

    @ApiClass
    public void changeMusic(String music) {
        broadcastPacket(MaplePacketCreator.musicChange(music));
    }

    @ApiClass
    public void setMapVar(String key, String value) {
        for (MapleCharacter chr : map.getCharacters()) {
            chr.addTemporaryData(key, value);
        }
    }

    @ApiClass
    public void spawnMonster(int id, int x, int y) {
        map.spawnMonsterOnGroundBelow(id, x, y);
    }

    private void broadcastPacket(byte[] packet) {
        map.broadcastMessage(packet);
    }

    @ApiClass
    public void spawnNpcWithEffect(int npcId, int x, int y) {
        map.spawnNpcWithEffect(npcId, new Point(x, y));
    }

    @ApiClass
    public void removeNpcWithEffect(int npcId) {
        map.makeNpcInvisible(npcId);
        map.removeNpc(npcId);
    }

    @ApiClass
    public void killAllMonsters() {
        map.killAllMonsters(false);
    }

    @ApiClass
    public int getMonsterCount() {
        return map.getAllMonster().size();
    }

    @ApiClass
    public void spawnSpecialMonsters() {
        map.spawnSpecialMonsters();
    }

    @ApiClass
    public void clearEventInstance() {
        map.clearEventInstance();
    }


}
