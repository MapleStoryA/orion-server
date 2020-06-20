package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    slea.readByte();
    String portal_name = slea.readMapleAsciiString();
    final MaplePortal portal = chr.getMap().getPortal(portal_name);
    // slea.skip(2);
    if (portal != null) {
      portal.enterPortal(c);
    }

  }

}
