package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.handler.CashShopOperationUtils;
import server.RandomRewards;
import server.cashShop.CashItemFactory;
import server.cashShop.CashItemInfo;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class CsSurpriseHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
      c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    if (slea.available() <= 2) {
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    final int uniqueId = (int) slea.readLong();
    final IItem box = c.getPlayer().getCashInventory().findByCashId(uniqueId);
    final CashItemInfo ciibox = CashItemFactory.getInstance().getItem(10102345);
    if (box != null && box.getQuantity() > 0 && box.getItemId() == 5222000 && ciibox != null) {
      boolean success = false;
      while (!success) {
        final CashItemInfo cii = CashItemFactory.getInstance().getItem(RandomRewards.getInstance().getCSSReward(), true);
        if (cii != null) {
          final IItem itemz = c.getPlayer().getCashInventory().toItem(cii, "");
          IItem newBox = null;
          if (box.getQuantity() > 1) {
            newBox = c.getPlayer().getCashInventory().toItemWithQuantity(ciibox, (box.getQuantity() - 1), "");
          }
          if (itemz != null && itemz.getSN() > 0 && itemz.getItemId() == cii.getId() && itemz.getQuantity() == cii.getCount()) {
            c.getPlayer().getCashInventory().removeFromInventory(box);
            if (newBox != null && newBox.getSN() > 0 && newBox.getItemId() == ciibox.getId()) {
              c.getPlayer().getCashInventory().addToInventory(newBox); // add the balance back
            }
            c.getPlayer().getCashInventory().addToInventory(itemz);
            c.getSession().write(MTSCSPacket.showCashShopSurprise(uniqueId, itemz, c.getAccID()));
            success = true;
          }
        }
      }
    } else {
      c.getSession().write(MTSCSPacket.sendCSFail(0));
    }
    CashShopOperationUtils.doCSPackets(c);

  }

}
