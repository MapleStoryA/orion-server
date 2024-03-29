package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.base.MapleCharacterHelper;
import client.inventory.IItem;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.ServerConstants;
import handling.cashshop.CashItemFactory;
import handling.cashshop.CashItemInfo;
import handling.cashshop.CashShopOperationHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import provider.MapleData;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.collection.Triple;
import tools.packet.MTSCSPacket;

@Slf4j
public class BuyCSItemHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final int action = packet.readByte();
        MapleCharacter chr = c.getPlayer();
        final boolean isNxWhore = false;
        if (action == 3) { // Buy Cash Item
            packet.skip(1);
            final int toCharge = packet.readInt();
            final int itemId = packet.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(itemId);
            if (item == null
                    || (!isNxWhore && chr.getCSPoints(toCharge) < item.getPrice())
                    || (isNxWhore && toCharge != 2 && chr.getCSPoints(toCharge) < item.getPrice())
                    || item.getPrice() <= 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (!item.genderEquals(chr.getGender())) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x0B));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (chr.getCashInventory().getItemsSize() >= 100) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }

            if (!isNxWhore || toCharge != 2) {
                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
            }
            final IItem itemz = chr.getCashInventory().toItem(item);
            if (itemz != null
                    && itemz.getSN() > 0
                    && itemz.getItemId() == item.getId()
                    && itemz.getQuantity() == item.getCount()) {
                chr.getCashInventory().addToInventory(itemz);
                c.getSession()
                        .write(MTSCSPacket.showBoughtCSItem(
                                itemz, item.getSN(), c.getAccountData().getId()));
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
            }
        } else if (action == 4 || action == 32) { // Gifting Items / Package
            packet.skip(4); // birthday
            final CashItemInfo item = CashItemFactory.getInstance().getItem(packet.readInt());
            if (action == 4) {
                packet.skip(1); // size?
            }
            final String partnerName = packet.readMapleAsciiString();
            final String msg = packet.readMapleAsciiString();
            if (item == null
                    || chr.getCSPoints(1) < item.getPrice()
                    || msg.length() > 73
                    || msg.length() < 1
                    || item.getPrice() <= 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            final Triple<Integer, Integer, Integer> info =
                    MapleCharacterHelper.getInfoByName(partnerName, chr.getWorld());
            if (info == null
                    || info.getLeft().intValue() <= 0
                    || info.getLeft().intValue() == chr.getId()
                    || info.getMid().intValue() == c.getAccountData().getId()) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x07));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (!item.genderEquals(info.getRight().intValue())) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x08));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            chr.getCashInventory()
                    .gift(
                            info.getLeft().intValue(),
                            chr.getName(),
                            msg,
                            item.getSN(),
                            MapleInventoryIdentifier.getInstance());
            chr.modifyCSPoints(1, -item.getPrice(), false);
            c.getSession()
                    .write(MTSCSPacket.showGiftSucceed(
                            item.getPrice(), item.getId(), item.getCount(), partnerName, action == 32));
        } else if (action == 5) { // Wishlist
            chr.getWishlist().clear();
            if (packet.available() < 40) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            int[] wishlist = new int[10];
            for (int i = 0; i < 10; i++) {
                wishlist[i] = packet.readInt();
            }
            chr.getWishlist().update(wishlist);
            c.getSession().write(MTSCSPacket.sendWishList(chr, true));
        } else if (action == 7) { // Increase Storage Slots
            packet.skip(1);
            final int toCharge = packet.readInt();
            if (toCharge == 2) { // Maple Points
                chr.dropMessage(1, "You cannot use MaplePoints to buy this item.");
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            final int coupon = packet.readByte() > 0 ? 2 : 1;
            if (coupon > 1) {
                final CashItemInfo item = CashItemFactory.getInstance().getItem(packet.readInt());
                if (item == null
                        || chr.getCSPoints(toCharge) < item.getPrice()
                        || chr.getStorage().getSlots() >= 41
                        || item.getPrice() <= 0) {
                    c.getSession().write(MTSCSPacket.sendCSFail(0));
                    CashShopOperationHandlers.doCSPackets(c);
                    return;
                }
            }
            if (chr.getCSPoints(toCharge) >= 4000 && chr.getStorage().getSlots() < 45) {
                chr.modifyCSPoints(toCharge, -(4000 * coupon), false);
                chr.getStorage().increaseSlots((byte) (4 * coupon));
                chr.getStorage().saveToDB();
                c.getSession()
                        .write(MTSCSPacket.increasedStorageSlots(
                                chr.getStorage().getSlots()));
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
            }
        } else if (action == 14) { // Take from Cash Inventory (UniqueId -> type(byte) -> position(short))
            final IItem item = chr.getCashInventory().findByCashId((int) packet.readLong());
            if (item != null
                    && item.getQuantity() > 0
                    && MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                final IItem item_ = item.copy();
                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
                if (pos >= 0) {
                    if (item_.getPet() != null) {
                        item_.getPet().setInventoryPosition(pos);
                        chr.addPet(item_.getPet());
                    }
                    chr.getCashInventory().removeFromInventory(item);
                    c.getSession().write(MTSCSPacket.confirmFromCSInventory(item_, pos));
                } else {
                    c.getSession().write(MTSCSPacket.sendCSFail(0x19));
                }
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0x19));
            }
        } else if (action == 15) { // Put Into Cash Inventory
            final int uniqueid = packet.readInt();
            packet.readInt();
            final MapleInventoryType type = MapleInventoryType.getByType(packet.readByte());
            final IItem item = chr.getInventory(type).findByUniqueId(uniqueid);
            if (item != null
                    && item.getQuantity() > 0
                    && item.getSN() > 0
                    && chr.getCashInventory().getItemsSize() < 100) {
                final IItem item_ = item.copy();
                chr.getCashInventory().addToInventory(item_);
                c.getPlayer().getInventory(type).removeSlot(item.getPosition());
                if (item_.getPet() != null) {
                    chr.removePetCS(item_.getPet());
                }
                c.getSession().write(MTSCSPacket.confirmToCSInventory(item_, chr.getAccountID()));
                item_.setPosition((byte) 0);

            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0xB1));
            }
        } else if (action == 36 || action == 30) { // 36 = friendship, 30 = crush
            // c.getSession().write(MTSCSPacket.sendCSFail(0));
            packet.readInt(); // birthday
            final int useNx = packet.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(packet.readInt());
            final String partnerName = packet.readMapleAsciiString();
            final String msg = packet.readMapleAsciiString();
            if (item == null
                    || !GameConstants.isEffectRing(item.getId())
                    || (!isNxWhore && chr.getCSPoints(useNx) < item.getPrice())
                    || (isNxWhore && useNx != 2 && chr.getCSPoints(useNx) < item.getPrice())
                    || msg.length() > 73
                    || msg.length() < 1
                    || item.getPrice() <= 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } /*else if (!item.genderEquals(chr.getGender())) {
              	c.getSession().write(MTSCSPacket.sendCSFail(0x0B));
              	CashShopOperationUtils.doCSPackets(c);
              	return;
              }*/ else if (chr.getCashInventory().getItemsSize() >= 100) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            final Triple<Integer, Integer, Integer> info =
                    MapleCharacterHelper.getInfoByName(partnerName, chr.getWorld());
            if (info == null
                    || info.getLeft().intValue() <= 0
                    || info.getLeft().intValue() == chr.getId()
                    || info.getMid().intValue() == c.getAccountData().getId()) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x07));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } /*else if (info.getRight().intValue() == chr.getGender() && action == 30) {
              	c.getSession().write(MTSCSPacket.sendCSFail(0x08));
              	CashShopOperationUtils.doCSPackets(c);
              	return;
              }*/

            int err = MapleRing.createRing(
                    item.getId(), chr, partnerName, msg, info.getLeft().intValue(), item.getSN());
            if (err != 1) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            if (!isNxWhore || useNx != 2) {
                chr.modifyCSPoints(useNx, -item.getPrice(), false);
            }
        } else if (action == 31) { // Buying Packages
            packet.skip(1);
            final int useNx = packet.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(packet.readInt());
            List<CashItemInfo> ccc = null;
            if (item != null) {
                // TODO: Remove from here.
                final MapleData rootNode = CashItemFactory.data.getData("CashPackage.img");
                List<MapleData> children = rootNode.getChildren();
                ccc = CashItemFactory.getInstance().getPackageItems(item.getId(), children);
            }
            if (item == null
                    || ccc == null
                    || (!isNxWhore && chr.getCSPoints(useNx) < item.getPrice())
                    || (isNxWhore && useNx != 2 && chr.getCSPoints(useNx) < item.getPrice())
                    || item.getPrice() <= 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (!item.genderEquals(chr.getGender())) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x0B));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (chr.getCashInventory().getItemsSize() >= (100 - ccc.size())) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x0A));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }

            final Map<Integer, IItem> ccz = new HashMap<>();
            for (final CashItemInfo i : ccc) {
                for (final int iz : GameConstants.cashBlock) {
                    if (i.getId() == iz && !chr.isGameMaster()) {
                        continue;
                    }
                }
                final IItem itemz = chr.getCashInventory().toItem(i);
                if (itemz == null || itemz.getSN() <= 0 || itemz.getItemId() != i.getId()) {
                    continue;
                }
                ccz.put(i.getSN(), itemz);
                chr.getCashInventory().addToInventory(itemz);
            }
            if (!isNxWhore || useNx != 2) {
                chr.modifyCSPoints(useNx, -item.getPrice(), false);
            }
            c.getSession()
                    .write(MTSCSPacket.showBoughtCSPackage(
                            ccz, c.getAccountData().getId()));
        } else if (action == 33) { // Buying Quest Items
            final int sn = packet.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(sn);
            if (item == null
                    || !MapleItemInformationProvider.getInstance().isQuestItem(item.getId())
                    || item.getPrice() <= 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (chr.getMeso() < item.getPrice()) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x20));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            } else if (chr.getInventory(GameConstants.getInventoryType(item.getId()))
                            .getNextFreeSlot()
                    < 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(0x19));
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            final byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null);
            if (pos >= 0) {
                chr.gainMeso(-item.getPrice(), false);
                c.getSession()
                        .write(MTSCSPacket.showBoughtCSQuestItem(
                                item.getPrice(), (short) item.getCount(), item.getId()));
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0x19));
            }
        } else if (action == 6) { // Increase Character Inventory Slots
            int b = packet.readInt();
            short ce = packet.readShort();
            int toCharge = 0;
            int upgradeSlotItem;
            switch (b) {
                case 513:
                    toCharge = 2;
                    break;
                case 1024:
                    toCharge = 4;
                    break;
                default:
                    toCharge = 1;
                    break;
            }
            if (packet.available() >= 4) {
                upgradeSlotItem = packet.readInt();
            } else {
                upgradeSlotItem = packet.readByteAsInt();
                switch (upgradeSlotItem) {
                    case 1:
                        upgradeSlotItem = 50200093; // Equip
                        break;
                    case 2:
                        upgradeSlotItem = 50200094; // Use
                        break;
                    case 3:
                        upgradeSlotItem = 50200197; // Setup
                        break;
                    case 4:
                        upgradeSlotItem = 50200095; // 50200095
                        break;
                    default:
                        return;
                }
            }

            MapleInventoryType type = null;
            final CashItemInfo item = CashItemFactory.getInstance().getItem(upgradeSlotItem);
            if (item == null || !item.onSale()) {
                c.enableActions();
                return;
            }
            if (!(chr.getCSPoints(toCharge) >= item.getPrice())) {
                c.getPlayer().dropMessage(1, "You don't have enought cash");
                CashShopOperationHandlers.doCSPackets(c);
            }
            switch (upgradeSlotItem) {
                case 50200197: // setup
                    type = MapleInventoryType.SETUP;
                    break;
                case 50200093: // equip
                    type = MapleInventoryType.EQUIP;
                    break;
                case 50200095: // etc
                    type = MapleInventoryType.ETC;
                    break;
                case 50200094: // use
                    type = MapleInventoryType.USE;
                    break;
                default: // hacking
                    c.enableActions();
                    return;
            }
            int itemsCount = chr.getInventory(type).getNumFreeSlot()
                    + chr.getInventory(type).list().size();
            if ((itemsCount + 4) > 96) {
                c.getPlayer().dropMessage(1, "Your slot is already full");
                CashShopOperationHandlers.doCSPackets(c);
                return;
            }
            if (type != null) {
                chr.getInventory(type).addSlot((byte) 4);
                chr.modifyCSPoints(toCharge, item.getPrice() * -1, false);
                c.getPlayer().dropMessage(1, "4 Slots added");
                c.enableActions();
            }

            // c.getSession().write(MTSCSPacket.sendCSFail(0x1A));
        } else if (action == 8) { // Increase Character Slots
            c.getSession().write(MTSCSPacket.sendCSFail(0x1A));
        } else if (action == 9) { // Pendant Slot Expansion
            c.getSession().write(MTSCSPacket.sendCSFail(0x1A));
        } else if (action == 43) { // Received upon entering Cash Shop
            final int sn = CashItemFactory.getInstance().getSNFromItemId(ServerConstants.ONE_DAY_ITEM);
            c.getSession().write(MTSCSPacket.getOneDayPacket(60 * 60, sn));
            c.getSession().write(MTSCSPacket.redeemResponse(packet.readInt()));
        } else {
            log.info("Unhandled operation found. Remaining: " + packet);
            c.getSession().write(MTSCSPacket.sendCSFail(0));
        }
        CashShopOperationHandlers.doCSPackets(c);
    }
}
