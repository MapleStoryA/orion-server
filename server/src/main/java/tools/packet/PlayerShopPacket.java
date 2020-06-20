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
import handling.SendPacketOpcode;
import server.MerchItemPackage;
import server.shops.AbstractPlayerStore.BoughtItem;
import server.shops.*;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.Set;

public class PlayerShopPacket {

  private static final int BLACK_LIST_SHOP_WINDOW_MAGIC_INT = 47;
  private static final int VISIT_SHOP_WINDOW_MAGIC_INT = 46;

  public static final byte[] addCharBox(final MapleCharacter c, final int type) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
    mplew.writeInt(c.getId());
    PacketHelper.addAnnounceBox(mplew, c);

    return mplew.getPacket();
  }

  public static final byte[] removeCharBox(final MapleCharacter c) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
    mplew.writeInt(c.getId());
    mplew.write(0);

    return mplew.getPacket();
  }

  public static final byte[] sendTitleBox() {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.ENTRUSTED_SHOP.getValue());
    mplew.write(7);

    return mplew.getPacket();
  }

  public static final byte[] sendTitleBoxMessage(byte mode) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 9: Please use this after retrieving items from Fredrick of Free Market.
    // 10: Another character is currently using the item. Please log on as a different character and close the store, or empty out the Store Bank.
    // 11: You are currently unable to open the store.
    // 15: Please retrieve your items from Fredrick.
    mplew.writeShort(SendPacketOpcode.ENTRUSTED_SHOP.getValue());
    mplew.write(mode);
    if (mode == 8 || mode == 16) {
      // 8: Your store is currently open in Channel 2, Free Market 10. Please use this after closing the store.
      mplew.writeInt(0); // Room
      mplew.write(0); // (channel - 1)
      // If mode == 16 and channel -1 = -1/-2/-3 : Unable to use this due to the Remote Shop not being open.
      // The store is open at Channel 2. Would you like to change to that channel?
    } else if (mode == 13) { // Minimap stuffs
      mplew.writeInt(0); // ?? object id?
    } else if (mode == 14) {
      //0: Renaming Failed - Can't find the Hired Mercahtn
      //1: Renaming Successful.
      mplew.write(0);
    } else if (mode == 18) { // Normal popup message
      mplew.write(1);
      mplew.writeMapleAsciiString("");
    }

    return mplew.getPacket();
  }

  public static final byte[] sendPlayerShopBox(final MapleCharacter c) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
    mplew.writeInt(c.getId());
    PacketHelper.addAnnounceBox(mplew, c);

    return mplew.getPacket();
  }

  public static final byte[] getHiredMerch2(final MapleCharacter chr, final HiredMerchant merch, final boolean firstTime) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(5);
    mplew.write(5);
    mplew.write(5); // ?
    mplew.writeShort(merch.getVisitorSlot(chr));
    mplew.writeInt(merch.getItemId());
    mplew.writeMapleAsciiString("Hired Merchant");
    for (final Pair<Byte, MapleCharacter> storechr : merch.getVisitors()) {
      mplew.write(storechr.left);
      PacketHelper.addCharLook(mplew, storechr.right, false);
      mplew.writeMapleAsciiString(storechr.right.getName());
      mplew.writeShort(storechr.right.getJob());
    }
    mplew.write(-1);
    mplew.writeShort(0);
    mplew.writeMapleAsciiString(merch.getOwnerName());
    if (merch.isOwner(chr)) {
      mplew.writeInt(merch.getTimeLeft());
      mplew.write(firstTime ? 1 : 0);
      mplew.write(merch.getBoughtItems().size());
      for (BoughtItem SoldItem : merch.getBoughtItems()) {
        mplew.writeInt(SoldItem.id);
        mplew.writeShort(SoldItem.quantity); // number of purchased
        mplew.writeInt(SoldItem.totalPrice); // total price
        mplew.writeMapleAsciiString(SoldItem.buyer); // name of the buyer
      }
      mplew.writeInt(merch.getMeso());
      mplew.writeInt(0);
    }
    mplew.writeMapleAsciiString(merch.getDescription());
    mplew.write(16); // size
    mplew.writeInt(merch.getMeso()); // meso
    mplew.write(merch.getItems().size());
    for (final MaplePlayerShopItem item : merch.getItems()) {
      mplew.writeShort(item.bundles);
      mplew.writeShort(item.item.getQuantity());
      mplew.writeInt(item.price);
      PacketHelper.addItemInfo(mplew, item.item, true, true);
    }
    mplew.writeShort(0);

    return mplew.getPacket();
  }

  public static byte[] getHiredMerch(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(5);
    mplew.write(5);
    mplew.write(4);
    mplew.write(hm.isOwner(chr) ? 0 : hm.getVisitorSlot(chr));
    mplew.write(0);
    mplew.writeInt(hm.getItemId());
    mplew.writeMapleAsciiString("Hired Merchant");
    for (final Pair<Byte, MapleCharacter> storechr : hm.getVisitors()) {
      mplew.write(storechr.left);
      PacketHelper.addCharLook(mplew, storechr.right, false);
      mplew.writeMapleAsciiString(storechr.right.getName());
      mplew.writeShort(storechr.right.getJob());
    }
    mplew.write(255);
    mplew.writeShort(0);
    mplew.writeMapleAsciiString(hm.getOwnerName());
    if (hm.isOwner(chr)) {
      mplew.writeInt(0);
      mplew.writeInt(firstTime ? 1 : 0);
      mplew.write(0);
      mplew.write(0);
      mplew.writeInt(hm.getMeso());
    }
    mplew.writeMapleAsciiString(hm.getDescription());
    mplew.write(16);
    mplew.writeInt((int) Math.min(hm.getMeso(), 2147483647L));
    mplew.write(hm.getItems().size());
    if (hm.getItems().isEmpty()) {
      mplew.write(0);
    } else {
      for (MaplePlayerShopItem item : hm.getItems()) {
        mplew.writeShort(item.bundles);
        mplew.writeShort(item.item.getQuantity());
        mplew.writeInt(item.price);
        PacketHelper.addItemInfo(mplew, item.item, true, true);
      }
    }
    return mplew.getPacket();
  }

  public static final byte[] getPlayerStore(final MapleCharacter chr, final boolean firstTime) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    IMaplePlayerShop ips = chr.getPlayerShop();

    switch (ips.getShopType()) {
      case 2:
        mplew.write(5);
        mplew.write(4);
        mplew.write(4);
        break;
      case 3:
        mplew.write(5);
        mplew.write(2);
        mplew.write(2);
        break;
      case 4:
        mplew.write(5);
        mplew.write(1);
        mplew.write(2);
        break;
    }
    mplew.writeShort(ips.getVisitorSlot(chr));
    PacketHelper.addCharLook(mplew, ((MaplePlayerShop) ips).getMCOwner(), false);
    mplew.writeMapleAsciiString(ips.getOwnerName());
    mplew.writeShort(((MaplePlayerShop) ips).getMCOwner().getJob());
    for (final Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
      mplew.write(storechr.left);
      PacketHelper.addCharLook(mplew, storechr.right, false);
      mplew.writeMapleAsciiString(storechr.right.getName());
      mplew.writeShort(storechr.right.getJob());
    }
    mplew.write(0xFF);
    mplew.writeMapleAsciiString(ips.getDescription());
    mplew.write(10);
    mplew.write(ips.getItems().size());

    for (final MaplePlayerShopItem item : ips.getItems()) {
      mplew.writeShort(item.bundles);
      mplew.writeShort(item.item.getQuantity());
      mplew.writeInt(item.price);
      PacketHelper.addItemInfo(mplew, item.item, true, true);
    }
    return mplew.getPacket();
  }

  public static final byte[] shopChat(final String message, final int slot) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(6);
    mplew.write(9);
    mplew.write(slot);
    mplew.writeMapleAsciiString(message);

    return mplew.getPacket();
  }

  public static final byte[] shopErrorMessage(final int error, final int type) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x0A);
    mplew.write(type);
    mplew.write(error);

    return mplew.getPacket();
  }

  public static final byte[] spawnHiredMerchant(final HiredMerchant hm) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
    mplew.writeInt(hm.getOwnerId());
    mplew.writeInt(hm.getItemId());
    mplew.writePos(hm.getPosition());
    mplew.writeShort(0);
    mplew.writeMapleAsciiString(hm.getOwnerName());
    PacketHelper.addInteraction(mplew, hm);

    return mplew.getPacket();
  }

  public static final byte[] destroyHiredMerchant(final int id) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue());//Same
    mplew.writeInt(id);

    return mplew.getPacket();
  }

  public static final byte[] shopItemUpdate(final IMaplePlayerShop shop) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(25);
    if (shop.getShopType() == 1) {
      mplew.writeInt(0);
    }
    mplew.write(shop.getItems().size());

    for (final MaplePlayerShopItem item : shop.getItems()) {
      mplew.writeShort(item.bundles);
      mplew.writeShort(item.item.getQuantity());
      mplew.writeInt(item.price);
      PacketHelper.addItemInfo(mplew, item.item, true, true);
    }
    return mplew.getPacket();
  }

  public static final byte[] shopVisitorAdd(final MapleCharacter chr, final int slot) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(4);
    mplew.write(slot);
    PacketHelper.addCharLook(mplew, chr, false);
    mplew.writeMapleAsciiString(chr.getName());
    mplew.writeShort(chr.getJob());

    return mplew.getPacket();
  }

  public static final byte[] shopVisitorLeave(final byte slot) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x0A);
    mplew.write(slot);

    return mplew.getPacket();
  }

  public static byte[] hiredMerchantOwnerLeave() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(42);
    mplew.write(0);
    return mplew.getPacket();
  }

  public static final byte[] Merchant_Buy_Error(final byte message) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    // 2 = You have not enough meso
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x18);
    mplew.write(message);

    return mplew.getPacket();
  }

  public static final byte[] updateHiredMerchant(final HiredMerchant shop) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue());
    mplew.writeInt(shop.getOwnerId());
    PacketHelper.addInteraction(mplew, shop);

    return mplew.getPacket();
  }

  //18: You have retrieved your items and mesos.
  //19: Unable to retrieve mesos and items due to\r\ntoo much money stored\r\nat the Store Bank.
  //20: Unable to retrieve mesos and items due to\r\none of the items\r\nthat can only be possessed one at a time.
  //21: Due to the lack of service fee, you were unable to \r\nretrieve mesos or items.
  //22: Unable to retrieve mesos and items\r\ndue to full inventory.
  public static final byte[] merchItem_Message(final byte op) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
    mplew.write(op);

    return mplew.getPacket();
  }

  public static final byte[] merchItemStore(final byte op) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // [28 01] [22 01] - Invalid Asiasoft Passport
    // [28 01] [22 00] - Open Asiasoft pin typing
    mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
    mplew.write(op);
    switch (op) {
      case 37:
        mplew.writeInt(999999999); // ?
        mplew.writeInt(999999999); // mapid
        mplew.write(0); // >= -2 channel
        // if cc -1 or map = 999,999,999 : I don't think you have any items
        // or money to retrieve here. This is where you retrieve the items
        // and mesos that you couldn't get from your Hired Merchant. You'll
        // also need to see me as the character that opened the Personal
        // Store.
        // Your Personal Store is open #bin Channel %s, Free Market
        // %d#k.\r\nIf you need me, then please close your personal store
        // first before seeing me.
        //mplew.writeMapleAsciiString("test");
        break;
      case 36:
        mplew.writeInt(0); // % tax or days, 1 day = 1%
        mplew.writeInt(01); // feees
        break;
    }

    return mplew.getPacket();
  }

  public static final byte[] merchItemStore_ItemData(final MerchItemPackage pack) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
    mplew.write(0x23);
    mplew.writeInt(9030000); // Fredrick
    mplew.writeInt(32272); // pack.getPackageid()
    mplew.writeZeroBytes(5);
    mplew.writeInt(pack.getMesos());
    mplew.write(0);
    mplew.write(pack.getItems().size());

    for (final IItem item : pack.getItems()) {
      PacketHelper.addItemInfo(mplew, item, true, true);
    }
    mplew.writeZeroBytes(3);

    return mplew.getPacket();
  }

  public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(5);
    mplew.write(minigame.getGameType());
    mplew.write(minigame.getMaxSize());
    mplew.writeShort(minigame.getVisitorSlot(c.getPlayer()));
    PacketHelper.addCharLook(mplew, minigame.getMCOwner(), false);
    mplew.writeMapleAsciiString(minigame.getOwnerName());
    mplew.writeShort(minigame.getMCOwner().getJob());
    for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
      mplew.write(visitorz.getLeft());
      PacketHelper.addCharLook(mplew, visitorz.getRight(), false);
      mplew.writeMapleAsciiString(visitorz.getRight().getName());
      mplew.writeShort(visitorz.getRight().getJob());
    }
    mplew.write(-1);
    mplew.write(0);
    addGameInfo(mplew, minigame.getMCOwner(), minigame);
    for (Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
      mplew.write(visitorz.getLeft());
      addGameInfo(mplew, visitorz.getRight(), minigame);
    }
    mplew.write(-1);
    mplew.writeMapleAsciiString(minigame.getDescription());
    mplew.writeShort(minigame.getPieceType());
    return mplew.getPacket();
  }

  public static byte[] getMiniGameReady(boolean ready) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(ready ? 0x38 : 0x39);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameExitAfter(boolean ready) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(ready ? 0x36 : 0x37);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameStart(int loser) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x3B);
    mplew.write(loser == 1 ? 0 : 1);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameSkip(int slot) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x3D);
    //owner = 1 visitor = 0?
    mplew.write(slot);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameRequestTie() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x30);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameDenyTie() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x31);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameFull() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.writeShort(5);
    mplew.write(2);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameMoveOmok(int move1, int move2, int move3) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x3E);
    mplew.writeInt(move1);
    mplew.writeInt(move2);
    mplew.write(move3);
    return mplew.getPacket();
  }

  public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot, MapleMiniGame game) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(4);
    mplew.write(slot);
    PacketHelper.addCharLook(mplew, c, false);
    mplew.writeMapleAsciiString(c.getName());
    mplew.writeShort(c.getJob());
    addGameInfo(mplew, c, game);
    return mplew.getPacket();
  }

  public static void addGameInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, MapleMiniGame game) {
    mplew.writeInt(game.getGameType()); // start of visitor; unknown
    mplew.writeInt(game.getWins(chr));
    mplew.writeInt(game.getTies(chr));
    mplew.writeInt(game.getLosses(chr));
    mplew.writeInt(game.getScore(chr)); // points
  }

  public static byte[] getMiniGameClose(byte number) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0xA);
    mplew.write(1);
    mplew.write(number);
    return mplew.getPacket();
  }

  public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x3B);
    mplew.write(loser == 1 ? 0 : 1);
    int times = game.getPieceType() == 1 ? 20 : (game.getPieceType() == 2 ? 30 : 12);
    mplew.write(times);
    for (int i = 1; i <= times; i++) {
      mplew.writeInt(game.getCardId(i));
    }
    return mplew.getPacket();
  }

  public static byte[] getMatchCardSelect(int turn, int slot, int firstslot, int type) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x42);
    mplew.write(turn);
    mplew.write(slot);
    if (turn == 0) {
      mplew.write(firstslot);
      mplew.write(type);
    }
    return mplew.getPacket();
  }

  public static byte[] getMiniGameResult(MapleMiniGame game, int type, int x) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(0x3C);
    mplew.write(type); //lose = 0, tie = 1, win = 2
    game.setPoints(x, type);
    if (type != 0) {
      game.setPoints(x == 1 ? 0 : 1, type == 2 ? 0 : 1);
    }
    if (type != 1) {
      if (type == 0) {
        mplew.write(x == 1 ? 0 : 1); //who did it?
      } else {
        mplew.write(x);
      }
    }
    addGameInfo(mplew, game.getMCOwner(), game);
    for (Pair<Byte, MapleCharacter> visitorz : game.getVisitors()) {
      addGameInfo(mplew, visitorz.right, game);
    }

    return mplew.getPacket();
  }

  public static final byte[] MerchantBlackListView(final Set<String> blackList) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(BLACK_LIST_SHOP_WINDOW_MAGIC_INT);
    mplew.writeShort(blackList.size());
    for (String name : blackList) {
      mplew.writeMapleAsciiString(name);
    }

    return mplew.getPacket();
  }

  public static final byte[] MerchantVisitorView(Set<String> visitor) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(VISIT_SHOP_WINDOW_MAGIC_INT);
    mplew.writeShort(visitor.size());
    for (String visit : visitor) {
      mplew.writeMapleAsciiString(visit);
      mplew.write(0);
      mplew.write(0);
      mplew.writeShort(55);
    }
    return mplew.getPacket();
  }

  //BELOW ARE UNUSED PLEASE RECONSIDER.
  public static final byte[] sendHiredMerchantMessage(final byte type) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 07 = send title box
    // 09 = Please pick up your items from Fredrick and then try again.
    // 0A = Your another character is using the item now. Please close the shop with that character or empty your store bank.
    // 0B = You cannot open it now.
    // 0F = Please retrieve your items from Fredrick.
    mplew.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
    mplew.write(type);

    return mplew.getPacket();
  }

  public static final byte[] shopMessage(final int type) { // show when closed the shop
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    // 0x28 = All of your belongings are moved successfully.
    mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
    mplew.write(type);
    mplew.write(0);

    return mplew.getPacket();
  }
}
