package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;

public class SelfDestructHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    int oid = slea.readInt();
    final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

    if (monster == null || !chr.isAlive() || chr.isHidden()) {
      return;
    }
    final byte selfd = monster.getStats().getSelfD();
    if (selfd != -1) {
      chr.getMap().killMonster(monster, chr, false, false, selfd);
    }

  }

}
