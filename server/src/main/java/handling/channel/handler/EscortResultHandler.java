package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.FileoutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class EscortResultHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    FileoutputUtil.logPacket("", "[ESCORT_RESULT] " + slea.toString());

  }

}
