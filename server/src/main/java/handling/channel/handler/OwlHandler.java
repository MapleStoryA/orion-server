package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class OwlHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().haveItem(5230000, 1) || c.getPlayer().haveItem(2310000, 1)) {
      if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022) {
        c.getSession().write(MaplePacketCreator.getOwlOpen());
      } else {
        c.getPlayer().dropMessage(5, "This can only be used inside the Free Market.");
        c.getSession().write(MaplePacketCreator.enableActions());
      }
    }

  }

}
