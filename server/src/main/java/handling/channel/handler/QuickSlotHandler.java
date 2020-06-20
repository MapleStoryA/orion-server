package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class QuickSlotHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (slea.available() == 32 && chr != null) {

      final StringBuilder ret = new StringBuilder();
      for (int i = 0; i < 8; i++) {
        ret.append(slea.readInt()).append(",");
      }
      ret.deleteCharAt(ret.length() - 1);
    }

  }

}
