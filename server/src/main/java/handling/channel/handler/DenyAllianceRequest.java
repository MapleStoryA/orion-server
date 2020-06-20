package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.AllianceHandlerUtils;
import tools.data.input.SeekableLittleEndianAccessor;

public class DenyAllianceRequest extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    AllianceHandlerUtils.HandleAlliance(slea, c, true);

  }

}
