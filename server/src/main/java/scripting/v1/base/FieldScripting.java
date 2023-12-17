package scripting.v1.base;

import client.MapleCharacter;
import scripting.v1.api.IFieldScripting;
import server.maps.MapleMap;
import tools.ApiClass;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.Collection;

@lombok.extern.slf4j.Slf4j
public class FieldScripting implements IFieldScripting {

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

    @Override
    public int id() {
        return map.getId();
    }

    @Override
    public void Field(int id) {

    }

    @Override
    public int getUserCount() {
        return 0;
    }

    @Override
    public int getMobCount(int mobId) {
        return 0;
    }

    @Override
    public int getMobHP(int mobId) {
        return 0;
    }

    @Override
    public int countUserInArea(String areaName) {
        return 0;
    }

    @Override
    public int countMaleInArea(String areaName) {
        return 0;
    }

    @Override
    public int countFemaleInArea(String areaName) {
        return 0;
    }

    @Override
    public void enablePortal(String portalName, int status) {

    }

    @Override
    public void effectObject(String objName) {

    }

    @Override
    public void effectScreen(String name) {

    }

    @Override
    public void effectSound(String soundName) {

    }

    @Override
    public void effectTremble(int type, int delay) {

    }

    @Override
    public void notice(int type, String message, Object... args) {

    }

    @Override
    public int isItemInArea(String areaName, int itemId) {
        return 0;
    }

    @Override
    public void summonMob(int x, int y, int itemId) {

    }

    @Override
    public int transferFieldAll(int mapCode, String portalName) {
        return 0;
    }

    @Override
    public void setNpcVar(int npcId, String key, String var) {

    }

    @Override
    public String getNpcStrVar(int npcId, String varName) {
        return null;
    }

    @Override
    public int getNpcIntVar(int npcId, String varName) {
        return 0;
    }

    @Override
    public void setProtectMobDamagedByMob(int setting) {

    }

    @Override
    public void removeAllMob() {

    }

    @Override
    public void setMobGen(int onOff) {

    }

    @Override
    public void removeMob(int mobId) {

    }

    @Override
    public int snowOn(int setting) {
        return 0;
    }

    @Override
    public void buffMob(int mobId, int effect, int duration) {

    }

    @Override
    public int isUserExist(int userId) {
        return 0;
    }

    @Override
    public void startEvent() {

    }

    @Override
    public void summonNpc(int templateId, int x, int y) {

    }

    @Override
    public void vanishNpc(int templateId) {

    }
}
