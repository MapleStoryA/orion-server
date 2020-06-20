package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DisplayNodeHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From

    if (mob_from != null) {
      chr.getClient().getSession().write(MaplePacketCreator.getNodeProperties(mob_from, chr.getMap()));
    }

  }

}
