package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.InventoryHandlerUtils;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseSkillBookHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());
    InventoryHandlerUtils.UseSkillBook((byte) slea.readShort(), slea.readInt(), c, c.getPlayer());

  }

}
