package handling.channel.handler;

import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemSortHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());

    final MapleInventoryType pInvType = MapleInventoryType.getByType(slea.readByte());
    if (pInvType == MapleInventoryType.UNDEFINED) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    final MapleInventory pInv = c.getPlayer().getInventory(pInvType); // Mode
    // should
    // correspond
    // with
    // MapleInventoryType
    boolean sorted = false;

    while (!sorted) {
      final byte freeSlot = (byte) pInv.getNextFreeSlot();
      if (freeSlot != -1) {
        byte itemSlot = -1;
        for (byte i = (byte) (freeSlot + 1); i <= pInv.getSlotLimit(); i++) {
          if (pInv.getItem(i) != null) {
            itemSlot = i;
            break;
          }
        }
        if (itemSlot > 0) {
          MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
        } else {
          sorted = true;
        }
      } else {
        sorted = true;
      }
    }
    c.getSession().write(MaplePacketCreator.finishedSort(pInvType.getType()));
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
