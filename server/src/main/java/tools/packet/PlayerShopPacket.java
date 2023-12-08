package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import handling.SendPacketOpcode;
import java.util.Set;
import server.MerchItemPackage;
import server.shops.AbstractPlayerStore.BoughtItem;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MapleMiniGame;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import tools.Pair;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class PlayerShopPacket {

    private static final int BLACK_LIST_SHOP_WINDOW_MAGIC_INT = 47;
    private static final int VISIT_SHOP_WINDOW_MAGIC_INT = 46;

    public static final byte[] addCharBox(final MapleCharacter c, final int type) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        packet.writeInt(c.getId());
        PacketHelper.addAnnounceBox(packet, c);

        return packet.getPacket();
    }

    public static final byte[] removeCharBox(final MapleCharacter c) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        packet.writeInt(c.getId());
        packet.write(0);

        return packet.getPacket();
    }

    public static final byte[] sendTitleBox() {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.ENTRUSTED_SHOP.getValue());
        packet.write(7);

        return packet.getPacket();
    }

    public static final byte[] sendTitleBoxMessage(byte mode) {
        final COutPacket packet = new COutPacket();
        // 9: Please use this after retrieving items from Fredrick of Free Market.
        // 10: Another character is currently using the item. Please log on as a different character
        // and close the store, or empty out the Store Bank.
        // 11: You are currently unable to open the store.
        // 15: Please retrieve your items from Fredrick.
        packet.writeShort(SendPacketOpcode.ENTRUSTED_SHOP.getValue());
        packet.write(mode);
        if (mode == 8 || mode == 16) {
            // 8: Your store is currently open in Channel 2, Free Market 10. Please use this after
            // closing the store.
            packet.writeInt(0); // Room
            packet.write(0); // (channel - 1)
            // If mode == 16 and channel -1 = -1/-2/-3 : Unable to use this due to the Remote Shop
            // not being open.
            // The store is open at Channel 2. Would you like to change to that channel?
        } else if (mode == 13) { // Minimap stuffs
            packet.writeInt(0); // ?? object id?
        } else if (mode == 14) {
            // 0: Renaming Failed - Can't find the Hired Mercahtn
            // 1: Renaming Successful.
            packet.write(0);
        } else if (mode == 18) { // Normal popup message
            packet.write(1);
            packet.writeMapleAsciiString("");
        }

        return packet.getPacket();
    }

    public static final byte[] sendPlayerShopBox(final MapleCharacter c) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        packet.writeInt(c.getId());
        PacketHelper.addAnnounceBox(packet, c);

        return packet.getPacket();
    }

    public static final byte[] getHiredMerch2(
            final MapleCharacter chr, final HiredMerchant merch, final boolean firstTime) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(5);
        packet.write(5);
        packet.write(5); // ?
        packet.writeShort(merch.getVisitorSlot(chr));
        packet.writeInt(merch.getItemId());
        packet.writeMapleAsciiString("Hired Merchant");
        for (final Pair<Byte, MapleCharacter> storechr : merch.getVisitors()) {
            packet.write(storechr.left);
            PacketHelper.addCharLook(packet, storechr.right, false);
            packet.writeMapleAsciiString(storechr.right.getName());
            packet.writeShort(storechr.right.getJob().getId());
        }
        packet.write(-1);
        packet.writeShort(0);
        packet.writeMapleAsciiString(merch.getOwnerName());
        if (merch.isOwner(chr)) {
            packet.writeInt(merch.getTimeLeft());
            packet.write(firstTime ? 1 : 0);
            packet.write(merch.getBoughtItems().size());
            for (BoughtItem SoldItem : merch.getBoughtItems()) {
                packet.writeInt(SoldItem.id);
                packet.writeShort(SoldItem.quantity); // number of purchased
                packet.writeInt(SoldItem.totalPrice); // total price
                packet.writeMapleAsciiString(SoldItem.buyer); // name of the buyer
            }
            packet.writeInt(merch.getMeso());
            packet.writeInt(0);
        }
        packet.writeMapleAsciiString(merch.getDescription());
        packet.write(16); // size
        packet.writeInt(merch.getMeso()); // meso
        packet.write(merch.getItems().size());
        for (final MaplePlayerShopItem item : merch.getItems()) {
            packet.writeShort(item.getBundles());
            packet.writeShort(item.getItem().getQuantity());
            packet.writeInt(item.getPrice());
            PacketHelper.addItemInfo(packet, item.getItem(), true, true);
        }
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static byte[] getHiredMerch(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(5);
        packet.write(5);
        packet.write(4);
        packet.write(hm.isOwner(chr) ? 0 : hm.getVisitorSlot(chr));
        packet.write(0);
        packet.writeInt(hm.getItemId());
        packet.writeMapleAsciiString("Hired Merchant");
        for (final Pair<Byte, MapleCharacter> storechr : hm.getVisitors()) {
            packet.write(storechr.left);
            PacketHelper.addCharLook(packet, storechr.right, false);
            packet.writeMapleAsciiString(storechr.right.getName());
            packet.writeShort(storechr.right.getJob().getId());
        }
        packet.write(255);
        packet.writeShort(0);
        packet.writeMapleAsciiString(hm.getOwnerName());
        if (hm.isOwner(chr)) {
            packet.writeInt(0);
            packet.writeInt(firstTime ? 1 : 0);
            packet.write(0);
            packet.write(0);
            packet.writeInt(hm.getMeso());
        }
        packet.writeMapleAsciiString(hm.getDescription());
        packet.write(16);
        packet.writeInt((int) Math.min(hm.getMeso(), 2147483647L));
        packet.write(hm.getItems().size());
        if (hm.getItems().isEmpty()) {
            packet.write(0);
        } else {
            for (MaplePlayerShopItem item : hm.getItems()) {
                packet.writeShort(item.getBundles());
                packet.writeShort(item.getItem().getQuantity());
                packet.writeInt(item.getPrice());
                PacketHelper.addItemInfo(packet, item.getItem(), true, true);
            }
        }
        return packet.getPacket();
    }

    public static final byte[] getPlayerStore(final MapleCharacter chr, final boolean firstTime) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        IMaplePlayerShop ips = chr.getPlayerShop();

        switch (ips.getShopType()) {
            case 2:
                packet.write(5);
                packet.write(4);
                packet.write(4);
                break;
            case 3:
                packet.write(5);
                packet.write(2);
                packet.write(2);
                break;
            case 4:
                packet.write(5);
                packet.write(1);
                packet.write(2);
                break;
        }
        packet.writeShort(ips.getVisitorSlot(chr));
        PacketHelper.addCharLook(packet, ((MaplePlayerShop) ips).getMCOwner(), false);
        packet.writeMapleAsciiString(ips.getOwnerName());
        packet.writeShort(((MaplePlayerShop) ips).getMCOwner().getJob().getId());
        for (final Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
            packet.write(storechr.left);
            PacketHelper.addCharLook(packet, storechr.right, false);
            packet.writeMapleAsciiString(storechr.right.getName());
            packet.writeShort(storechr.right.getJob().getId());
        }
        packet.write(0xFF);
        packet.writeMapleAsciiString(ips.getDescription());
        packet.write(10);
        packet.write(ips.getItems().size());

        for (final MaplePlayerShopItem item : ips.getItems()) {
            packet.writeShort(item.getBundles());
            packet.writeShort(item.getItem().getQuantity());
            packet.writeInt(item.getPrice());
            PacketHelper.addItemInfo(packet, item.getItem(), true, true);
        }
        return packet.getPacket();
    }

    public static final byte[] shopChat(final String message, final int slot) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(6);
        packet.write(9);
        packet.write(slot);
        packet.writeMapleAsciiString(message);

        return packet.getPacket();
    }

    public static final byte[] shopErrorMessage(final int error, final int type) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x0A);
        packet.write(type);
        packet.write(error);

        return packet.getPacket();
    }

    public static final byte[] spawnHiredMerchant(final HiredMerchant hm) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
        packet.writeInt(hm.getOwnerId());
        packet.writeInt(hm.getItemId());
        packet.writePos(hm.getPosition());
        packet.writeShort(0);
        packet.writeMapleAsciiString(hm.getOwnerName());
        PacketHelper.addInteraction(packet, hm);

        return packet.getPacket();
    }

    public static final byte[] destroyHiredMerchant(final int id) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue()); // Same
        packet.writeInt(id);

        return packet.getPacket();
    }

    public static final byte[] shopItemUpdate(final IMaplePlayerShop shop) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(25);
        if (shop.getShopType() == 1) {
            packet.writeInt(0);
        }
        packet.write(shop.getItems().size());

        for (final MaplePlayerShopItem item : shop.getItems()) {
            packet.writeShort(item.getBundles());
            packet.writeShort(item.getItem().getQuantity());
            packet.writeInt(item.getPrice());
            PacketHelper.addItemInfo(packet, item.getItem(), true, true);
        }
        return packet.getPacket();
    }

    public static final byte[] shopVisitorAdd(final MapleCharacter chr, final int slot) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(4);
        packet.write(slot);
        PacketHelper.addCharLook(packet, chr, false);
        packet.writeMapleAsciiString(chr.getName());
        packet.writeShort(chr.getJob().getId());

        return packet.getPacket();
    }

    public static final byte[] shopVisitorLeave(final byte slot) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x0A);
        packet.write(slot);

        return packet.getPacket();
    }

    public static byte[] hiredMerchantOwnerLeave() {
        COutPacket packet = new COutPacket(4);
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(42);
        packet.write(0);
        return packet.getPacket();
    }

    public static final byte[] Merchant_Buy_Error(final byte message) {
        final COutPacket packet = new COutPacket();

        // 2 = You have not enough meso
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x18);
        packet.write(message);

        return packet.getPacket();
    }

    public static final byte[] updateHiredMerchant(final HiredMerchant shop) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue());
        packet.writeInt(shop.getOwnerId());
        PacketHelper.addInteraction(packet, shop);

        return packet.getPacket();
    }

    // 18: You have retrieved your items and mesos.
    // 19: Unable to retrieve mesos and items due to\r\ntoo much money stored\r\nat the Store Bank.
    // 20: Unable to retrieve mesos and items due to\r\none of the items\r\nthat can only be
    // possessed one at a time.
    // 21: Due to the lack of service fee, you were unable to \r\nretrieve mesos or items.
    // 22: Unable to retrieve mesos and items\r\ndue to full inventory.
    public static final byte[] merchItem_Message(final byte op) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
        packet.write(op);

        return packet.getPacket();
    }

    public static final byte[] merchItemStore(final byte op) {
        final COutPacket packet = new COutPacket();
        // [28 01] [22 01] - Invalid Asiasoft Passport
        // [28 01] [22 00] - Open Asiasoft pin typing
        packet.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
        packet.write(op);
        switch (op) {
            case 37:
                packet.writeInt(999999999); // ?
                packet.writeInt(999999999); // mapid
                packet.write(0); // >= -2 channel
                // if cc -1 or map = 999,999,999 : I don't think you have any items
                // or money to retrieve here. This is where you retrieve the items
                // and mesos that you couldn't get from your Hired Merchant. You'll
                // also need to see me as the character that opened the Personal
                // Store.
                // Your Personal Store is open #bin Channel %s, Free Market
                // %d#k.\r\nIf you need me, then please close your personal store
                // first before seeing me.
                // packet.writeMapleAsciiString("test");
                break;
            case 36:
                packet.writeInt(0); // % tax or days, 1 day = 1%
                packet.writeInt(01); // feees
                break;
        }

        return packet.getPacket();
    }

    public static final byte[] merchItemStore_ItemData(final MerchItemPackage pack) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
        packet.write(0x23);
        packet.writeInt(9030000); // Fredrick
        packet.writeInt(32272); // pack.getPackageid()
        packet.writeZeroBytes(5);
        packet.writeInt(pack.getMesos());
        packet.write(0);
        packet.write(pack.getItems().size());

        for (final IItem item : pack.getItems()) {
            PacketHelper.addItemInfo(packet, item, true, true);
        }
        packet.writeZeroBytes(3);

        return packet.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(5);
        packet.write(minigame.getGameType());
        packet.write(minigame.getMaxSize());
        packet.writeShort(minigame.getVisitorSlot(c.getPlayer()));
        PacketHelper.addCharLook(packet, minigame.getMCOwner(), false);
        packet.writeMapleAsciiString(minigame.getOwnerName());
        packet.writeShort(minigame.getMCOwner().getJob().getId());
        for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            packet.write(visitorz.getLeft());
            PacketHelper.addCharLook(packet, visitorz.getRight(), false);
            packet.writeMapleAsciiString(visitorz.getRight().getName());
            packet.writeShort(visitorz.getRight().getJob().getId());
        }
        packet.write(-1);
        packet.write(0);
        addGameInfo(packet, minigame.getMCOwner(), minigame);
        for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            packet.write(visitorz.getLeft());
            addGameInfo(packet, visitorz.getRight(), minigame);
        }
        packet.write(-1);
        packet.writeMapleAsciiString(minigame.getDescription());
        packet.writeShort(minigame.getPieceType());
        return packet.getPacket();
    }

    public static byte[] getMiniGameReady(boolean ready) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(ready ? 0x38 : 0x39);
        return packet.getPacket();
    }

    public static byte[] getMiniGameExitAfter(boolean ready) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(ready ? 0x36 : 0x37);
        return packet.getPacket();
    }

    public static byte[] getMiniGameStart(int loser) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x3B);
        packet.write(loser == 1 ? 0 : 1);
        return packet.getPacket();
    }

    public static byte[] getMiniGameSkip(int slot) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x3D);
        // owner = 1 visitor = 0?
        packet.write(slot);
        return packet.getPacket();
    }

    public static byte[] getMiniGameRequestTie() {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x30);
        return packet.getPacket();
    }

    public static byte[] getMiniGameDenyTie() {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x31);
        return packet.getPacket();
    }

    public static byte[] getMiniGameFull() {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.writeShort(5);
        packet.write(2);
        return packet.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(int move1, int move2, int move3) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x3E);
        packet.writeInt(move1);
        packet.writeInt(move2);
        packet.write(move3);
        return packet.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot, MapleMiniGame game) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(4);
        packet.write(slot);
        PacketHelper.addCharLook(packet, c, false);
        packet.writeMapleAsciiString(c.getName());
        packet.writeShort(c.getJob().getId());
        addGameInfo(packet, c, game);
        return packet.getPacket();
    }

    public static void addGameInfo(
            COutPacket packet, MapleCharacter chr, MapleMiniGame game) {
        packet.writeInt(game.getGameType()); // start of visitor; unknown
        packet.writeInt(game.getWins(chr));
        packet.writeInt(game.getTies(chr));
        packet.writeInt(game.getLosses(chr));
        packet.writeInt(game.getScore(chr)); // points
    }

    public static byte[] getMiniGameClose(byte number) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0xA);
        packet.write(1);
        packet.write(number);
        return packet.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x3B);
        packet.write(loser == 1 ? 0 : 1);
        int times = game.getPieceType() == 1 ? 20 : (game.getPieceType() == 2 ? 30 : 12);
        packet.write(times);
        for (int i = 1; i <= times; i++) {
            packet.writeInt(game.getCardId(i));
        }
        return packet.getPacket();
    }

    public static byte[] getMatchCardSelect(int turn, int slot, int firstslot, int type) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x42);
        packet.write(turn);
        packet.write(slot);
        if (turn == 0) {
            packet.write(firstslot);
            packet.write(type);
        }
        return packet.getPacket();
    }

    public static byte[] getMiniGameResult(MapleMiniGame game, int type, int x) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x3C);
        packet.write(type); // lose = 0, tie = 1, win = 2
        game.setPoints(x, type);
        if (type != 0) {
            game.setPoints(x == 1 ? 0 : 1, type == 2 ? 0 : 1);
        }
        if (type != 1) {
            if (type == 0) {
                packet.write(x == 1 ? 0 : 1); // who did it?
            } else {
                packet.write(x);
            }
        }
        addGameInfo(packet, game.getMCOwner(), game);
        for (Pair<Byte, MapleCharacter> visitorz : game.getVisitors()) {
            addGameInfo(packet, visitorz.right, game);
        }

        return packet.getPacket();
    }

    public static final byte[] MerchantBlackListView(final Set<String> blackList) {
        final COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(BLACK_LIST_SHOP_WINDOW_MAGIC_INT);
        packet.writeShort(blackList.size());
        for (String name : blackList) {
            packet.writeMapleAsciiString(name);
        }

        return packet.getPacket();
    }

    public static final byte[] MerchantVisitorView(Set<String> visitor) {
        final COutPacket packet = new COutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(VISIT_SHOP_WINDOW_MAGIC_INT);
        packet.writeShort(visitor.size());
        for (String visit : visitor) {
            packet.writeMapleAsciiString(visit);
            packet.write(0);
            packet.write(0);
            packet.writeShort(55);
        }
        return packet.getPacket();
    }

    // BELOW ARE UNUSED PLEASE RECONSIDER.
    public static final byte[] sendHiredMerchantMessage(final byte type) {
        final COutPacket packet = new COutPacket();
        // 07 = send title box
        // 09 = Please pick up your items from Fredrick and then try again.
        // 0A = Your another character is using the item now. Please close the shop with that
        // character or empty your store bank.
        // 0B = You cannot open it now.
        // 0F = Please retrieve your items from Fredrick.
        packet.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
        packet.write(type);

        return packet.getPacket();
    }

    public static final byte[] shopMessage(final int type) { // show when closed the shop
        final COutPacket packet = new COutPacket();
        // 0x28 = All of your belongings are moved successfully.
        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(type);
        packet.write(0);

        return packet.getPacket();
    }
}
