package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class SnowballHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    // B2 00
    // 01 [team]
    // 00 00 [unknown]
    // 89 [position]
    // 01 [stage]
    c.getSession().write(MaplePacketCreator.enableActions());
    // empty, we do this in closerange

  }

}
