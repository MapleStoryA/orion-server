package handling.channel.handler;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemMoveHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getPlayerShop() != null || c.getPlayer().getConversation() > 0
        || c.getPlayer().getTrade() != null) { // hack
      return;
    }
    c.getPlayer().updateTick(slea.readInt());
    final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte()); // 04
    final short src = slea.readShort(); // 01 00
    final short dst = slea.readShort(); // 00 00
    final short quantity = slea.readShort(); // 53 01

    if (src < 0 && dst > 0) {
      MapleInventoryManipulator.unequip(c, src, dst);
    } else if (dst < 0) {
      MapleInventoryManipulator.equip(c, src, dst);
    } else if (dst == 0) {
      MapleInventoryManipulator.drop(c, type, src, quantity);
    } else {
      MapleInventoryManipulator.move(c, type, src, dst);
    }

  }

}
