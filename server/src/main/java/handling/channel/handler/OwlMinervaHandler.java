package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.shops.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class OwlMinervaHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final byte slot = (byte) slea.readShort();
    final int itemid = slea.readInt();
    final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
    if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && itemid == 2310000) {
      final int itemSearch = slea.readInt();
      final List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
      if (hms.size() > 0) {
        c.getSession().write(MaplePacketCreator.getOwlSearched(itemSearch, hms));
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
      } else {
        c.getPlayer().dropMessage(1, "Unable to find the item.");
      }
    }
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
