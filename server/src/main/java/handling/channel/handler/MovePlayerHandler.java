package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleMap;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class MovePlayerHandler extends AbstractMaplePacketHandler {




  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    slea.skip(29);
    final MovePath path = new MovePath();
    path.decode(slea);
    if (path != null && c.getPlayer().getMap() != null) {
      final MapleMap map = c.getPlayer().getMap();
      if (chr.isHidden()) {
        c.getPlayer().getMap().broadcastGMMessage(chr, MaplePacketCreator.movePlayer(chr.getId(), path), false);
      } else {
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.movePlayer(chr.getId(), path), false);
      }
      updatePosition(path, chr, 0);
      final Point pos = chr.getPosition();
      map.movePlayer(chr, pos);
    }

  }

}
