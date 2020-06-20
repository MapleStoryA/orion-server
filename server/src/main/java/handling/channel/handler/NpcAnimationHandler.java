package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.SendPacketOpcode;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NpcAnimationHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
    final int length = (int) slea.available();
    if (length == 6) { // NPC Talk
      mplew.writeInt(slea.readInt());
      mplew.writeShort(slea.readShort());
    } else if (length > 6) { // NPC Move
      mplew.write(slea.read(length - 9));
    } else {
      return;
    }
    c.sendPacket(mplew.getPacket());
  }

}
