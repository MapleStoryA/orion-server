package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;

public class AutoAggroHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    int monsteroid = slea.readInt();
    MapleCharacter chr = c.getPlayer();
    if (chr == null || chr.getMap() == null || chr.isHidden()) { //no evidence :)
      return;
    }
    final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);

    if (monster != null && chr.getPosition().distanceSq(monster.getPosition()) < 200000) {
      if (monster.getController() != null) {
        if (chr.getMap().getCharacterById(monster.getController().getId()) == null) {
          monster.switchController(chr, true);
        } else {
          monster.switchController(monster.getController(), true);
        }
      } else {
        monster.switchController(chr, true);
      }
    }

  }

}
