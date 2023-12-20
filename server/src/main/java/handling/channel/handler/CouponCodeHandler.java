package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.CashShopOperationHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;
import server.cashshop.CashCouponData;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashShopCoupon;
import tools.collection.Pair;
import tools.data.input.InPacket;
import tools.packet.MTSCSPacket;

/*
 *
 * How it works:
 * - Coupons are added to the 'coupon' table in the database.
 * - The rewards associated with each coupon are stored in the 'coupon_data' table.
 *   For example, a coupon code like 'KQVT-GPH4-WJQG-PI8W-T24T-T9VE' is linked to
 *   a particular type of reward (e.g., Maple Points, Cash Items, Normal items, Mesos).
 *
 * Key fields:
 * - itemData: Controls the ID of the item or the quantity when dealing with currency.
 * - quantity: Specifies the quantity of the item to be granted.
 *
 * Reward Types:
 * - 0: Maple Points
 * - 1: Cash Items
 * - 2: Normal Items
 * - 3: Mesos
 * */
@Slf4j
public class CouponCodeHandler extends AbstractMaplePacketHandler {

    public static final int MESOS = 3;
    public static final int NORMAL_ITEMS = 2;
    public static final int CASHSHOP_ITEMS = 1;
    public static final int MAPLE_POINTS = 0;

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final boolean gift = packet.readShort() > 0;
        if (gift) {
            c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x30));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        final String code = packet.readMapleAsciiString();
        if (code == null || code.length() < 16 || code.length() > 32) {
            c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x0E));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        final boolean isValidCode = CashShopCoupon.getCouponCodeValid(code.toUpperCase());
        if (!isValidCode) {
            c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x0E));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        final List<CashCouponData> rewards = CashShopCoupon.getCouponData(code.toUpperCase());
        if (rewards == null) {
            CashShopCoupon.setCouponCodeUsed("ERROR", code);
            c.getSession().write(MTSCSPacket.sendCouponFail(c, 0x11));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        final Pair<Pair<Integer, Integer>, Pair<List<IItem>, Integer>> couponItemsSize =
                CashShopCoupon.getSize(rewards);
        if ((c.getPlayer().getCSPoints(2) + couponItemsSize.getLeft().getLeft()) < 0) {
            c.getPlayer().dropMessage(1, "You have too much Maple Points.");
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        if (c.getPlayer().getCashInventory().getItemsSize()
                >= (100 - couponItemsSize.getLeft().getRight())) {
            c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        if (c.getPlayer().getMeso() + couponItemsSize.getRight().getRight() < 0) {
            c.getPlayer().dropMessage(1, "You have too much mesos.");
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }
        if (!CashShopOperationHandlers.haveSpace(
                c.getPlayer(), couponItemsSize.getRight().getLeft())) {
            c.getSession().write(MTSCSPacket.sendCSFail(0x19));
            CashShopOperationHandlers.doCSPackets(c);
            return;
        }

        CashShopCoupon.setCouponCodeUsed(c.getPlayer().getName(), code);

        int MaplePoints = 0, mesos = 0;
        final Map<Integer, IItem> togiveCS = new HashMap<>();
        final List<Pair<Integer, Integer>> togiveII = new ArrayList<>();
        for (final CashCouponData reward : rewards) {
            switch (reward.getType()) {
                case MAPLE_POINTS: {
                    if (reward.getData() > 0) {
                        c.getPlayer().modifyCSPoints(2, reward.getData(), false);
                        MaplePoints = reward.getData();
                    }
                    break;
                }
                case CASHSHOP_ITEMS: {
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
                case NORMAL_ITEMS: {
                    if (reward.getQuantity() <= Short.MAX_VALUE && reward.getQuantity() > 0) {
                        final byte pos = MapleInventoryManipulator.addId(
                                c, reward.getData(), (short) reward.getQuantity(), "MapleSystem");
                        if (pos >= 0) { // Failed
                            togiveII.add(new Pair<>(reward.getData(), reward.getQuantity()));
                        }
                    }
                    break;
                }
                case MESOS: {
                    if (reward.getData() > 0) {
                        c.getPlayer().gainMeso(reward.getData(), false);
                        mesos = reward.getData();
                    }
                    break;
                }
            }
        }
        c.getSession()
                .write(MTSCSPacket.showCouponRedeemedItem(
                        c.getAccountData().getId(), MaplePoints, togiveCS, togiveII, mesos));
        CashShopOperationHandlers.doCSPackets(c);
    }
}
