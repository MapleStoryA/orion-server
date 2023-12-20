package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import networking.data.output.OutPacket;
import networking.packet.SendPacketOpcode;
import provider.MapleData;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashItemInfo.CashModInfo;
import server.cashshop.CashShop;
import server.config.ServerConfig;
import tools.StringUtil;
import tools.collection.Pair;

@lombok.extern.slf4j.Slf4j
public class MTSCSPacket {

    private static final int[] modified = {
        50600001, // Change server
        50600004, // Change name
        50200206, // Maple Life
        50200121, // Maple Life
        50100043, // Remote store
        50100039, // Regular store
        50100012, // Holiday store
        50200112, // Extra char
    };

    public static void addCashItemInfo(final OutPacket packet, IItem item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null && !giftMessage.isEmpty();
        boolean isRing = false;
        MapleRing ring = null;
        if (item.getType() == 1 && item.getRing() != null) {
            ring = item.getRing();
            isRing = ring.getRingId() > -1;
        }
        MaplePet pet = item.getPet();
        int sn = pet != null && pet.getUniqueId() > -1 ? pet.getUniqueId() : isRing ? ring.getRingId() : item.getSN();
        packet.writeLong(sn);
        if (!isGift) {
            packet.writeInt(accountId);
            packet.writeInt(0); // dwCharacterID
        }
        packet.writeInt(item.getItemId());
        if (!isGift) {
            packet.writeInt(item.getSN());
            packet.writeShort(item.getQuantity());
        }
        packet.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            packet.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        PacketHelper.addExpirationTime(packet, item.getExpiration());
        packet.writeInt(0); // nPaybackRate
        packet.writeInt(0); // nDiscountRate
        /**
         * public void Encode(OutPacket oPacket) { oPacket.EncodeBuffer(liSN, 8);
         * oPacket.Encode4(dwAccountID); oPacket.Encode4(dwCharacterID); oPacket.Encode4(nItemID);
         * oPacket.Encode4(nCommodityID); oPacket.Encode2(nNumber);
         * oPacket.EncodeBuffer(sBuyCharacterID, 13); oPacket.EncodeBuffer(dateExpire, 8);
         * oPacket.Encode4(nPaybackRate); oPacket.Encode4(nDiscountRate); }
         */
    }

    public static byte[] warpCS(MapleClient c) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPEN.getValue());

        PacketHelper.addCharacterInfo(packet, c.getPlayer());

        packet.write(1);
        packet.writeMapleAsciiString(c.getAccountData().getName());

        packet.writeInt(0); // limit sell data, for each, one int

        if (ServerConfig.isDebugEnabled()) {
            CashItemFactory.getInstance().loadCashShopData();
        }

        packet.writeShort(modified.length);
        for (int i = 0; i < modified.length; i++) {
            packet.writeInt(modified[i]);
            packet.writeInt(0x400);
            packet.write(0);
        }

        packet.write(0); // for each, 3 bytes

        packet.writeZeroBytes(120);
        for (int i = 1; i <= 8; i++) {
            for (int j = 0; j < 2; j++) {
                packet.writeInt(i);
                packet.writeInt(j);
                packet.writeInt(50200004);
                packet.writeInt(i);
                packet.writeInt(j);
                packet.writeInt(50200069);
                packet.writeInt(i);
                packet.writeInt(j);
                packet.writeInt(50200117);
                packet.writeInt(i);
                packet.writeInt(j);
                packet.writeInt(50100008);
                packet.writeInt(i);
                packet.writeInt(j);
                packet.writeInt(50000047);
            }
        }
        packet.writeShort(0); // stock
        packet.writeShort(0); // limit goods (for each size, 104 bytes. each)
        packet.writeShort(0); // for each 68 bytes each
        packet.write(0); // eventON
        packet.writeInt(0x8D); // ? 0 also works

        return packet.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CASH_SONG.getValue());
        packet.writeInt(itemid);
        packet.writeMapleAsciiString(name);
        return packet.getPacket();
    }

    public static byte[] useCharm(byte charmsleft, byte daysleft) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(6);
        packet.write(1);
        packet.write(charmsleft);
        packet.write(daysleft);

        return packet.getPacket();
    }

    public static byte[] useWheel(byte charmsleft) {
        // You have used 1 Wheel of Destiny in order to revive at the current map. (<left> left)
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(21);
        packet.write(charmsleft); // left

        return packet.getPacket();
    }

    public static byte[] ViciousHammer(boolean start, int hammered) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        if (start) {
            packet.write(60);
            packet.writeInt(0);
            packet.writeInt(hammered);
        } else {
            packet.write(64);
            packet.writeInt(0);
        }

        return packet.getPacket();
    }

    public static byte[] VegasScroll(int action) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.VEGAS_SCROLL.getValue());
        packet.write(action);
        // 1: This item cannot be used.

        return packet.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());

        packet.writeInt(chr.getId());
        packet.write(0);
        packet.writeMapleAsciiString(newname);
        packet.write(slot);

        return packet.getPacket();
    }

    public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        packet.write(3);
        packet.write(count);
        for (int i = 0; i < count; i++) {
            packet.writeInt(notes.getInt("id"));
            packet.writeMapleAsciiString(notes.getString("from"));
            packet.writeMapleAsciiString(notes.getString("message"));
            packet.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
            packet.write(notes.getInt("gift"));
            notes.next();
        }

        return packet.getPacket();
    }

    public static byte[] useChalkboard(final int charid, final String msg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CHALKBOARD.getValue());

        packet.writeInt(charid);
        if (msg == null || msg.length() <= 0) {
            packet.write(0);
        } else {
            packet.write(1);
            packet.writeMapleAsciiString(msg);
        }

        return packet.getPacket();
    }

    public static byte[] receiveGachaponTicket(int amount) {
        // You have acquired <> Gachapon Stamps by purchasing the Gachapon Ticket.
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_GACHAPON_STAMPS.getValue());
        packet.write(amount > 0 ? 1 : 0);
        if (amount > 0) {
            packet.writeInt(amount);
        }

        return packet.getPacket();
    }

    public static byte[] getTeleportRockRefresh(MapleCharacter chr, boolean vip, boolean delete) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        packet.write(delete ? 2 : 3);
        packet.write(vip ? 1 : 0);
        if (vip) {
            chr.getVipTeleportRock().encode(packet);
        } else {
            chr.getRegTeleportRock().encode(packet);
        }
        return packet.getPacket();
    }

    public static byte[] sendWishList(MapleCharacter chr, boolean update) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(update ? 0x61 : 0x5B);
        chr.getWishlist().encodeToCashShop(packet);
        return packet.getPacket();
    }

    public static byte[] showNXMapleTokens(MapleCharacter chr) {
        OutPacket packet = new OutPacket();

        // Combined both NxCredit and NxPrepaid
        packet.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        packet.writeInt(0); // NXCredit [1]
        packet.writeInt(chr.getCSPoints(2)); // MPoint [2]
        packet.writeInt(chr.getCSPoints(1)); // NXPrepaid [4]

        return packet.getPacket();
    }

    public static byte[] showBoughtCSPackage(Map<Integer, IItem> ccc, int accid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x97);
        packet.write(ccc.size());
        for (Entry<Integer, IItem> sn : ccc.entrySet()) {
            addCashItemInfo(packet, sn.getValue(), accid, "");
        }
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static byte[] showBoughtCSItem(IItem item, int accId, String giftFrom, long expire) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x63); // use to be 4a
        addCashItemInfo(packet, item, accId, "");

        return packet.getPacket();
    }

    public static byte[] showBoughtCSItem(IItem item, int sn, int accid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x63);
        addCashItemInfo(packet, item, accid, "");

        return packet.getPacket();
    }

    public static byte[] redeemResponse(final int sn) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0xAC);
        packet.writeInt(sn); // ? sn?
        packet.write(1); // byte, must be 1.

        return packet.getPacket();
    }

    public static byte[] cashShopSurpriseFail() {
        // Please check and see if you have exceeded the number of cash items you can have.
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_SURPRISE.getValue());
        packet.write(189);

        return packet.getPacket();
    }

    public static byte[] showCashShopSurprise(int idFirst, IItem item, int accid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_SURPRISE.getValue());
        packet.write(190);
        packet.writeLong(idFirst); // uniqueid of the xmas surprise itself
        packet.writeInt(0);
        addCashItemInfo(packet, item, accid, ""); // info of the new item, but packet shows 0 for sn?
        packet.writeInt(item.getItemId());
        packet.write(1);
        packet.write(1);

        return packet.getPacket();
    }

    public static byte[] showTwinDragonEgg(int idFirst) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_TWIN_DRAGON_EGG.getValue());
        packet.writeLong(idFirst); // uniqueid of the dragon egg itself
        packet.writeInt(0);
        packet.writeZeroBytes(12);

        return packet.getPacket();
    }

    public static void addModCashItemInfo(OutPacket packet, CashModInfo item) {
        int flags = item.flags;
        packet.writeInt(item.sn);
        packet.writeInt(flags);
        if ((flags & 0x1) != 0) {
            packet.writeInt(item.itemid);
        }
        if ((flags & 0x2) != 0) {
            packet.writeShort(item.count);
        }
        if ((flags & 0x4) != 0) {
            packet.writeInt(item.discountPrice);
        }
        if ((flags & 0x8) != 0) {
            packet.write(item.unk_1 - 1);
        }
        if ((flags & 0x10) != 0) {
            packet.write(item.priority);
        }
        if ((flags & 0x20) != 0) {
            packet.writeShort(item.period);
        }
        if ((flags & 0x40) != 0) {
            packet.writeInt(0);
        }
        if ((flags & 0x80) != 0) {
            packet.writeInt(item.meso);
        }
        if ((flags & 0x100) != 0) {
            packet.write(item.unk_2 - 1);
        }
        if ((flags & 0x200) != 0) {
            packet.write(item.gender);
        }
        if ((flags & 0x400) != 0) {
            packet.write(item.showUp ? 1 : 0);
        }
        if ((flags & 0x800) != 0) {
            packet.write(item.mark);
        }
        if ((flags & 0x1000) != 0) {
            packet.write(item.unk_3 - 1);
        }
        if ((flags & 0x2000) != 0) {
            packet.writeShort(0);
        }
        if ((flags & 0x4000) != 0) {
            packet.writeShort(0);
        }
        if ((flags & 0x8000) != 0) {
            packet.writeShort(0);
        }
        if ((flags & 0x10000) != 0) {
            // TODO: Refactor from here.. Loading xml files inside packet!
            final MapleData rootNode = CashItemFactory.data.getData("CashPackage.img");
            List<MapleData> children = rootNode.getChildren();
            List<CashItemInfo> pack = CashItemFactory.getInstance().getPackageItems(item.sn, children);
            if (pack == null) {
                packet.write(0);
            } else {
                packet.write(pack.size());
                for (int i = 0; i < pack.size(); i++) {
                    packet.writeInt(pack.get(i).getSN());
                }
            }
        }
    }

    public static byte[] showBoughtCSQuestItem(int price, short quantity, int itemid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x9B);
        packet.writeInt(1); // size.
        // for each size above = 8 bytes below.
        packet.writeInt(quantity);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static byte[] sendCouponFail(final MapleClient c, int err) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        // TODO: Do we need more than one login attempt ? packet.write(c.csAttempt > 2 ? 0x58 :
        // 0x62);
        packet.write(0x62);
        packet.write(err);

        return packet.getPacket();
    }

    public static byte[] sendCSFail(int err) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x62);
        packet.write(err);

        return packet.getPacket();
    }

    public static byte[] showCouponRedeemedItem(
            final int accid,
            final int MaplePoints,
            final Map<Integer, IItem> items1,
            final List<Pair<Integer, Integer>> items2,
            final int mesos) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x65);
        packet.write(items1.size()); // Cash Item
        for (Entry<Integer, IItem> sn : items1.entrySet()) {
            addCashItemInfo(packet, sn.getValue(), accid, "");
        }
        packet.writeInt(MaplePoints);
        packet.writeInt(items2.size()); // Normal items size
        for (Pair<Integer, Integer> item : items2) {
            packet.writeInt(item.getRight()); // Count
            packet.writeInt(item.getLeft()); // Item ID
        }
        packet.writeInt(mesos);

        return packet.getPacket();
    }

    public static byte[] enableCSUse() {
        OutPacket packet = new OutPacket();

        packet.writeShort(0x12);
        packet.write(1);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] getCSInventory(MapleClient c) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x57); // use to be 3e
        CashShop mci = c.getPlayer().getCashInventory();
        packet.writeShort(mci.getItemsSize());
        for (IItem itemz : mci.getInventory()) {
            addCashItemInfo(packet, itemz, c.getAccountData().getId(), ""); // test
        }
        packet.writeShort(c.getPlayer().getStorage().getSlots());
        packet.writeShort(c.getCharacterSlots());
        packet.writeInt(0); // 00 00 04 00 <-- added?

        return packet.getPacket();
    }

    // work on this packet a little more
    public static byte[] getCSGifts(MapleClient c) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());

        packet.write(0x59); // use to be 40
        List<Pair<IItem, String>> mci = c.getPlayer().getCashInventory().loadGifts();
        packet.writeShort(mci.size());
        for (Pair<IItem, String> mcz : mci) {
            packet.writeLong(mcz.getLeft().getSN());
            packet.writeInt(mcz.getLeft().getItemId());
            packet.writeAsciiString(mcz.getLeft().getGiftFrom(), 13);
            packet.writeAsciiString(mcz.getRight(), 73);
        }

        return packet.getPacket();
    }

    public static byte[] cashItemExpired(int uniqueid) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x7C); // use to be 5d
        packet.writeLong(uniqueid);
        return packet.getPacket();
    }

    public static byte[] OnCashItemResCoupleDone(IItem item, int sn, int accid, String receiver, boolean couple) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(couple ? 0x95 : 0x9F); // Same as friendship. (0x9F)
        addCashItemInfo(packet, item, accid, "");
        packet.writeMapleAsciiString(receiver); // parter name?
        packet.writeInt(item.getItemId());
        packet.writeShort(1); // Count

        return packet.getPacket();
    }

    public static byte[] showGiftSucceed(int price, int itemid, int quantity, String receiver, boolean packages) {
        // "%d [ %s ] \r\nwas sent to %s. \r\n%d NX Prepaid \r\nwere spent in the process.",
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(packages ? 0x99 : 0x6A);
        packet.writeMapleAsciiString(receiver);
        packet.writeInt(itemid);
        packet.writeShort(quantity);
        if (packages) {
            packet.writeShort(0);
        }
        packet.writeInt(price);

        return packet.getPacket();
    }

    public static byte[] increasedInvSlots(int inv, int slots) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x6C);
        packet.write(inv);
        packet.writeShort(slots);

        return packet.getPacket();
    }

    // also used for character slots !
    public static byte[] increasedStorageSlots(int slots) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x6E);
        packet.writeShort(slots);

        return packet.getPacket();
    }

    public static byte[] confirmToCSInventory(IItem item, int accId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x78);
        addCashItemInfo(packet, item, accId, "");

        return packet.getPacket();
    }

    public static byte[] confirmFromCSInventory(IItem item, short pos) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        packet.write(0x76);
        packet.writeShort(pos);
        PacketHelper.addItemInfo(packet, item, true, true);

        return packet.getPacket();
    }

    public static byte[] sendMesobagFailed() {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.MESOBAG_FAILURE.getValue());
        return packet.getPacket();
    }

    public static byte[] sendMesobagSuccess(int mesos) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.MESOBAG_SUCCESS.getValue());
        packet.writeInt(mesos);
        return packet.getPacket();
    }

    public static byte[] getOneDayPacket(int remainingHours, int itemSN) {
        OutPacket packet = new OutPacket();
        packet.writeShort(387);
        packet.writeInt(remainingHours);
        packet.writeInt(itemSN);
        packet.writeInt(0);
        // Size
        // int, int, int for past one day items
        return packet.getPacket();
    }
}
