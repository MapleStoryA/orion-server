package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelChairHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    int id = slea.readShort();
    MapleCharacter chr = c.getPlayer();
    if (id == -1) { // Cancel Chair
      if (chr.getChair() == 3011000) {
        chr.cancelFishingTask();
      }
      chr.setChair(0);
      c.getSession().write(MaplePacketCreator.cancelChair(-1));
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), 0), false);
    } else { // Use In-Map Chair
      chr.setChair(id);
      c.getSession().write(MaplePacketCreator.cancelChair(id));
    }

  }

}
