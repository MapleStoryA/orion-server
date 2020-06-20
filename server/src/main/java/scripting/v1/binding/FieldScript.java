package scripting.v1.binding;

import client.MapleCharacter;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.Collection;

public class FieldScript {

  private final MapleMap map;

  public FieldScript(MapleMap map) {
    this.map = map;
  }

  public void showEffect(String effect) {
    broadcastPacket(MaplePacketCreator.showEffect(effect));
  }

  public Collection<MapleCharacter> fieldMembers() {
    return map.getCharacters();
  }

  public int fieldMembersCount() {
    return fieldMembers().size();
  }

  public void environmentChange(String env) {
    broadcastPacket(MaplePacketCreator.environmentChange(env, 2));
  }

  public void playSound(String sound) {
    broadcastPacket(MaplePacketCreator.playSound(sound));
  }

  public void changeMusic(String music) {
    broadcastPacket(MaplePacketCreator.musicChange(music));
  }

  public void setMapVar(String key, String value) {
    for (MapleCharacter chr : map.getCharacters()) {
      chr.addTemporaryData(key, value);
    }
  }

  public void spawnMonster(int id, int x, int y) {
    map.spawnMonsterOnGroundBelow(id, x, y);
  }

  private void broadcastPacket(byte[] packet) {
    map.broadcastMessage(packet);
  }

  public void spawnNpcWithEffect(int npcId, int x, int y) {
    map.spawnNpcWithEffect(npcId, new Point(x, y));
  }

  public void removeNpcWithEffect(int npcId) {
    map.makeNpcInvisible(npcId);
    map.removeNpc(npcId);
  }

  public void killAllMonsters() {
    map.killAllMonsters(false);
  }

  public int getMonsterCount() {
    return map.getAllMonster().size();
  }

  public void spawnSpecialMonsters() {
    map.spawnSpecialMonsters();
  }

  public void clearEventInstance() {
    map.clearEventInstance();
  }


}
