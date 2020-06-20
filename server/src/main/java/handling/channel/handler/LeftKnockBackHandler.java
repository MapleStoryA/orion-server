package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class LeftKnockBackHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getMapId() / 10000 == 10906) { // must be in snowball
      // map or else its
      // like infinite FJ
      c.getSession().write(MaplePacketCreator.leftKnockBack());
      c.getSession().write(MaplePacketCreator.enableActions());
    }
  }

}
