package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import handling.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class UseInnerPortalHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    slea.skip(1);
    if (chr == null) {
      return;
    }
    final MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
    final int toX = slea.readShort();
    final int toY = slea.readShort();

    if (portal == null) {
      return;
    } else if (portal.getPosition().distanceSq(chr.getPosition()) > 22500) {
      chr.getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL);
    }
    chr.getMap().movePlayer(chr, new Point(toX, toY));
    chr.checkFollow();

  }

}
