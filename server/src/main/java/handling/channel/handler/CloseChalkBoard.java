package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class CloseChalkBoard extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().setChalkboard(null);
  }

}
