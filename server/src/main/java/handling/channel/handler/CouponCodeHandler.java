package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.handler.CashShopOperationUtils;
import server.MapleInventoryManipulator;
import server.cashShop.CashCouponData;
import server.cashShop.CashItemFactory;
import server.cashShop.CashItemInfo;
import server.cashShop.CashShopCoupon;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouponCodeHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final boolean gift = slea.readShort() > 0;
    if (gift) {
      c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x30));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    final String code = slea.readMapleAsciiString();
    if (code == null || code.length() < 16 || code.length() > 32) { // Please check and see if the coupon id is correct or not.
      // XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX
      c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x0E));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    final boolean validcode = CashShopCoupon.getCouponCodeValid(code.toUpperCase());
    if (!validcode) {
      c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x0E));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    final List<CashCouponData> rewards = CashShopCoupon.getCcData(code.toUpperCase());
    if (rewards == null) { // Actually impossible
      CashShopCoupon.setCouponCodeUsed("ERROR", code);
      c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x11));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    // maple point, cs item, normal, mesos
    final Pair<Pair<Integer, Integer>, Pair<List<IItem>, Integer>> cscsize = CashShopCoupon.getSize(rewards);
    if ((c.getPlayer().getCSPoints(2) + cscsize.getLeft().getLeft()) < 0) {
      c.getPlayer().dropMessage(1, "You have too much Maple Points.");
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    if (c.getPlayer().getCashInventory().getItemsSize() >= (100 - cscsize.getLeft().getRight())) {
      c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    if (c.getPlayer().getMeso() + cscsize.getRight().getRight() < 0) {
      c.getPlayer().dropMessage(1, "You have too much mesos.");
      CashShopOperationUtils.doCSPackets(c);
      return;
    }
    if (!CashShopOperationUtils.haveSpace(c.getPlayer(), cscsize.getRight().getLeft())) {
      c.getSession().write(MTSCSPacket.sendCSFail(0x19));
      CashShopOperationUtils.doCSPackets(c);
      return;
    }

    CashShopCoupon.setCouponCodeUsed(c.getPlayer().getName(), code);

    int MaplePoints = 0, mesos = 0;
    final Map<Integer, IItem> togiveCS = new HashMap<>();
    final List<Pair<Integer, Integer>> togiveII = new ArrayList<>();
    for (final CashCouponData reward : rewards) {
      switch (reward.getType()) {
        case 0: { // MaplePoints
          if (reward.getData() > 0) {
            c.getPlayer().modifyCSPoints(2, reward.getData(), false);
            MaplePoints = reward.getData();
          }
          break;
        }
        case 1: { // Cash Shop Items
          final CashItemInfo item = CashItemFactory.getInstance().getItem(reward.getData());
          if (item != null) {
            final IItem itemz = c.getPlayer().getCashInventory().toItem(item, "");
            if (itemz != null && itemz.getSN() > 0) {
              togiveCS.put(item.getSN(), itemz);
              c.getPlayer().getCashInventory().addToInventory(itemz);
            }
          }
          break;
        }
        case 2: { // Normal Items
          if (reward.getQuantity() <= Short.MAX_VALUE && reward.getQuantity() > 0) {
            final byte pos = MapleInventoryManipulator.addId(c, reward.getData(), (short) reward.getQuantity(), "MapleSystem");
            if (pos >= 0) { // Failed
              togiveII.add(new Pair<>(reward.getData(), (int) reward.getQuantity()));
            }
          }
          break;
        }
        case 3: { // Mesos
          if (reward.getData() > 0) {
            c.getPlayer().gainMeso(reward.getData(), false);
            mesos = reward.getData();
          }
          break;
        }
      }
    }
    CashShopCoupon.deleteCouponData(c.getPlayer().getName(), code);
    c.getSession().write(MTSCSPacket.showCouponRedeemedItem(c.getAccID(), MaplePoints, togiveCS, togiveII, mesos));
    CashShopOperationUtils.doCSPackets(c);

  }

}
