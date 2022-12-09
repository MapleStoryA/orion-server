/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import handling.SendPacketOpcode;
import provider.MapleData;
import server.cashshop.CashItemFactory;
import server.cashshop.CashItemInfo;
import server.cashshop.CashItemInfo.CashModInfo;
import server.cashshop.CashShop;
import server.config.ServerEnvironment;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@lombok.extern.slf4j.Slf4j
public class MTSCSPacket {


    private static final int[] modified = {
            50600001, //Change server
            50600004, //Change name
            50200206, //Maple Life
            50200121, //Maple Life
            50100043, //Remote store
            50100039, //Regular store
            50100012, //Holiday store
            50200112, //Extra char

    };


    public static void addCashItemInfo(final MaplePacketLittleEndianWriter mplew, IItem item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null && !giftMessage.isEmpty();
        boolean isRing = false;
        MapleRing ring = null;
        if (item.getType() == 1 && item.getRing() != null) {
            ring = item.getRing();
            isRing = ring.getRingId() > -1;
        }
        MaplePet pet = item.getPet();
        int sn = pet != null && pet.getUniqueId() > -1 ? pet.getUniqueId() : isRing ? ring.getRingId() : item.getSN();
        mplew.writeLong(sn);
        if (!isGift) {
            mplew.writeInt(accountId);
            mplew.writeInt(0);// dwCharacterID
        }
        mplew.writeInt(item.getItemId());
        if (!isGift) {
            mplew.writeInt(item.getSN());
            mplew.writeShort(item.getQuantity());
        }
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        PacketHelper.addExpirationTime(mplew, item.getExpiration());
        mplew.writeInt(0);// nPaybackRate
        mplew.writeInt(0);// nDiscountRate
        /**
         * public void Encode(OutPacket oPacket) {
         * oPacket.EncodeBuffer(liSN, 8);
         * oPacket.Encode4(dwAccountID);
         * oPacket.Encode4(dwCharacterID);
         * oPacket.Encode4(nItemID);
         * oPacket.Encode4(nCommodityID);
         * oPacket.Encode2(nNumber);
         * oPacket.EncodeBuffer(sBuyCharacterID, 13);
         * oPacket.EncodeBuffer(dateExpire, 8);
         * oPacket.Encode4(nPaybackRate);
         * oPacket.Encode4(nDiscountRate);
         * }
         */
    }


    public static byte[] warpCS(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());

        PacketHelper.addCharacterInfo(mplew, c.getPlayer());

        mplew.write(1);
        mplew.writeMapleAsciiString(c.getAccountData().getName());


        mplew.writeInt(0); // limit sell data, for each, one int

        if (ServerEnvironment.isDebugEnabled()) {
            CashItemFactory.getInstance().loadCashShopData();
        }


        mplew.writeShort(modified.length);
        for (int i = 0; i < modified.length; i++) {
            mplew.writeInt(modified[i]);
            mplew.writeInt(0x400);
            mplew.write(0);
        }

        mplew.write(0); // for each, 3 bytes

        mplew.writeZeroBytes(120);
        for (int i = 1; i <= 8; i++) {
            for (int j = 0; j < 2; j++) {
                mplew.writeInt(i);
                mplew.writeInt(j);
                mplew.writeInt(50200004);
                mplew.writeInt(i);
                mplew.writeInt(j);
                mplew.writeInt(50200069);
                mplew.writeInt(i);
                mplew.writeInt(j);
                mplew.writeInt(50200117);
                mplew.writeInt(i);
                mplew.writeInt(j);
                mplew.writeInt(50100008);
                mplew.writeInt(i);
                mplew.writeInt(j);
                mplew.writeInt(50000047);
            }
        }
        mplew.writeShort(0);// stock
        mplew.writeShort(0); // limit goods (for each size, 104 bytes. each)
        mplew.writeShort(0); // for each 68 bytes each
        mplew.write(0); //eventON
        mplew.writeInt(0x8D); // ? 0 also works

        return mplew.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CASH_SONG.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] useCharm(byte charmsleft, byte daysleft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(6);
        mplew.write(1);
        mplew.write(charmsleft);
        mplew.write(daysleft);

        return mplew.getPacket();
    }

    public static byte[] useWheel(byte charmsleft) {
        // You have used 1 Wheel of Destiny in order to revive at the current map. (<left> left)
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(21);
        mplew.write(charmsleft); // left

        return mplew.getPacket();
    }

    public static byte[] ViciousHammer(boolean start, int hammered) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        if (start) {
            mplew.write(60);
            mplew.writeInt(0);
            mplew.writeInt(hammered);
        } else {
            mplew.write(64);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] VegasScroll(int action) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VEGAS_SCROLL.getValue());
        mplew.write(action);
        // 1: This item cannot be used.

        return mplew.getPacket();
    }


    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());

        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(slot);

        return mplew.getPacket();
    }

    public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(notes.getInt("gift"));
            notes.next();
        }

        return mplew.getPacket();
    }

    public static byte[] useChalkboard(final int charid, final String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());

        mplew.writeInt(charid);
        if (msg == null || msg.length() <= 0) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    public static byte[] receiveGachaponTicket(int amount) {
        // You have acquired <> Gachapon Stamps by purchasing the Gachapon Ticket.
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_GACHAPON_STAMPS.getValue());
        mplew.write(amount > 0 ? 1 : 0);
        if (amount > 0) {
            mplew.writeInt(amount);
        }

        return mplew.getPacket();
    }

    public static byte[] getTrockRefresh(MapleCharacter chr, boolean vip, boolean delete) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(delete ? 2 : 3);
        mplew.write(vip ? 1 : 0);
        if (vip) {
            int[] map = chr.getRocks();
            for (int i = 0; i < 10; i++) {
                mplew.writeInt(map[i]);
            }
        } else {
            int[] map = chr.getRegRocks();
            for (int i = 0; i < 5; i++) {
                mplew.writeInt(map[i]);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] sendWishList(MapleCharacter chr, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(update ? 0x61 : 0x5B); //+12
        int[] list = chr.getWishlist();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(list[i] != -1 ? list[i] : 0);
        }
        return mplew.getPacket();
    }

    public static byte[] showNXMapleTokens(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // Combined both NxCredit and NxPrepaid
        mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(0); // NXCredit [1]
        mplew.writeInt(chr.getCSPoints(2)); // MPoint [2]
        mplew.writeInt(chr.getCSPoints(1)); // NXPrepaid [4]

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSPackage(Map<Integer, IItem> ccc, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x97);
        mplew.write(ccc.size());
        for (Entry<Integer, IItem> sn : ccc.entrySet()) {
            addCashItemInfo(mplew, sn.getValue(), accid, "");
        }
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(IItem item, int accId, String giftFrom, long expire) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x63); //use to be 4a
        addCashItemInfo(mplew, item, accId, "");

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(IItem item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x63);
        addCashItemInfo(mplew, item, accid, "");

        return mplew.getPacket();
    }

    public static byte[] redeemResponse(final int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0xAC);
        mplew.writeInt(sn); // ? sn?
        mplew.write(1); // byte, must be 1.

        return mplew.getPacket();
    }

    public static byte[] cashShopSurpriseFail() {
        // Please check and see if you have exceeded the number of cash items you can have.
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_SURPRISE.getValue());
        mplew.write(189);

        return mplew.getPacket();
    }

    public static byte[] showCashShopSurprise(int idFirst, IItem item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_SURPRISE.getValue());
        mplew.write(190);
        mplew.writeLong(idFirst); //uniqueid of the xmas surprise itself
        mplew.writeInt(0);
        addCashItemInfo(mplew, item, accid, ""); //info of the new item, but packet shows 0 for sn?
        mplew.writeInt(item.getItemId());
        mplew.write(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] showTwinDragonEgg(int idFirst) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_TWIN_DRAGON_EGG.getValue());
        mplew.writeLong(idFirst); //uniqueid of the dragon egg itself
        mplew.writeInt(0);
        mplew.writeZeroBytes(12);

        return mplew.getPacket();
    }


    public static void addModCashItemInfo(MaplePacketLittleEndianWriter mplew, CashModInfo item) {
        int flags = item.flags;
        mplew.writeInt(item.sn);
        mplew.writeInt(flags);
        if ((flags & 0x1) != 0) {
            mplew.writeInt(item.itemid);
        }
        if ((flags & 0x2) != 0) {
            mplew.writeShort(item.count);
        }
        if ((flags & 0x4) != 0) {
            mplew.writeInt(item.discountPrice);
        }
        if ((flags & 0x8) != 0) {
            mplew.write(item.unk_1 - 1);
        }
        if ((flags & 0x10) != 0) {
            mplew.write(item.priority);
        }
        if ((flags & 0x20) != 0) {
            mplew.writeShort(item.period);
        }
        if ((flags & 0x40) != 0) {
            mplew.writeInt(0);
        }
        if ((flags & 0x80) != 0) {
            mplew.writeInt(item.meso);
        }
        if ((flags & 0x100) != 0) {
            mplew.write(item.unk_2 - 1);
        }
        if ((flags & 0x200) != 0) {
            mplew.write(item.gender);
        }
        if ((flags & 0x400) != 0) {
            mplew.write(item.showUp ? 1 : 0);
        }
        if ((flags & 0x800) != 0) {
            mplew.write(item.mark);
        }
        if ((flags & 0x1000) != 0) {
            mplew.write(item.unk_3 - 1);
        }
        if ((flags & 0x2000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x4000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x8000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x10000) != 0) {
            //TODO: Refactor from here.. Loading xml files inside packet!
            final MapleData rootNode = CashItemFactory.data.getData("CashPackage.img");
            List<MapleData> children = rootNode.getChildren();
            List<CashItemInfo> pack = CashItemFactory.getInstance().getPackageItems(item.sn, children);
            if (pack == null) {
                mplew.write(0);
            } else {
                mplew.write(pack.size());
                for (int i = 0; i < pack.size(); i++) {
                    mplew.writeInt(pack.get(i).getSN());
                }
            }
        }
    }

    public static byte[] showBoughtCSQuestItem(int price, short quantity, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x9B);
        mplew.writeInt(1); // size.
        // for each size above = 8 bytes below.
        mplew.writeInt(quantity);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] sendCouponFail(final MapleClient c, int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        // TODO: Do we need more than one login attempt ? mplew.write(c.csAttempt > 2 ? 0x58 : 0x62);
        mplew.write(0x62);
        mplew.write(err);

        return mplew.getPacket();
    }

    public static byte[] sendCSFail(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x62);
        mplew.write(err);

        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(final int accid, final int MaplePoints, final Map<Integer, IItem> items1, final List<Pair<Integer, Integer>> items2, final int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x65);
        mplew.write(items1.size()); // Cash Item
        for (Entry<Integer, IItem> sn : items1.entrySet()) {
            addCashItemInfo(mplew, sn.getValue(), accid, "");
        }
        mplew.writeInt(MaplePoints);
        mplew.writeInt(items2.size()); // Normal items size
        for (Pair<Integer, Integer> item : items2) {
            mplew.writeInt(item.getRight()); // Count
            mplew.writeInt(item.getLeft());  // Item ID
        }
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static byte[] enableCSUse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(0x12);
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getCSInventory(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x57); // use to be 3e
        CashShop mci = c.getPlayer().getCashInventory();
        mplew.writeShort(mci.getItemsSize());
        for (IItem itemz : mci.getInventory()) {
            addCashItemInfo(mplew, itemz, c.getAccID(), ""); //test
        }
        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());
        mplew.writeInt(0); //00 00 04 00 <-- added?

        return mplew.getPacket();
    }

    //work on this packet a little more
    public static byte[] getCSGifts(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());

        mplew.write(0x59); //use to be 40
        List<Pair<IItem, String>> mci = c.getPlayer().getCashInventory().loadGifts();
        mplew.writeShort(mci.size());
        for (Pair<IItem, String> mcz : mci) {
            mplew.writeLong(mcz.getLeft().getSN());
            mplew.writeInt(mcz.getLeft().getItemId());
            mplew.writeAsciiString(mcz.getLeft().getGiftFrom(), 13);
            mplew.writeAsciiString(mcz.getRight(), 73);
        }

        return mplew.getPacket();
    }

    public static byte[] cashItemExpired(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x7C); //use to be 5d
        mplew.writeLong(uniqueid);
        return mplew.getPacket();
    }

    public static byte[] OnCashItemResCoupleDone(IItem item, int sn, int accid, String receiver, boolean couple) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(couple ? 0x95 : 0x9F); // Same as friendship. (0x9F)
        addCashItemInfo(mplew, item, accid, "");
        mplew.writeMapleAsciiString(receiver); // parter name?
        mplew.writeInt(item.getItemId());
        mplew.writeShort(1); // Count

        return mplew.getPacket();
    }

    public static byte[] showGiftSucceed(int price, int itemid, int quantity, String receiver, boolean packages) {
        // "%d [ %s ] \r\nwas sent to %s. \r\n%d NX Prepaid \r\nwere spent in the process.",
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(packages ? 0x99 : 0x6A);
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemid);
        mplew.writeShort(quantity);
        if (packages) {
            mplew.writeShort(0);
        }
        mplew.writeInt(price);

        return mplew.getPacket();
    }

    public static byte[] increasedInvSlots(int inv, int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x6C);
        mplew.write(inv);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    //also used for character slots !
    public static byte[] increasedStorageSlots(int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x6E);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] confirmToCSInventory(IItem item, int accId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x78);
        addCashItemInfo(mplew, item, accId, "");

        return mplew.getPacket();
    }

    public static byte[] confirmFromCSInventory(IItem item, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x76);
        mplew.writeShort(pos);
        PacketHelper.addItemInfo(mplew, item, true, true);

        return mplew.getPacket();
    }

    public static byte[] sendMesobagFailed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESOBAG_FAILURE.getValue());
        return mplew.getPacket();
    }

    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESOBAG_SUCCESS.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] getOneDayPacket(int remainingHours, int itemSN) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(387);
        mplew.writeInt(remainingHours);
        mplew.writeInt(itemSN);
        mplew.writeInt(0);
        //Size
        //int, int, int for past one day items
        return mplew.getPacket();
    }
}