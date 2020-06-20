package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.InventoryHandlerUtils;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseScrollHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());
    byte slot = (byte) slea.readShort();
    byte dst = (byte) slea.readShort();
    byte ws = (byte) slea.readShort();
    byte type = (byte) 0;

    InventoryHandlerUtils.UseUpgradeScroll(slot, dst, ws, c, c.getPlayer(), 0, type);
  }

}
