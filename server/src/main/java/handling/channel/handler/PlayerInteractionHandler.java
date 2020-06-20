package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.maps.FieldLimitType;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.*;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PlayerShopPacket;

import java.util.Arrays;
import java.util.Iterator;

public class PlayerInteractionHandler extends AbstractMaplePacketHandler {

  public static final byte CREATE = 0x00,
      INVITE_TRADE = 0x02,
      DENY_TRADE = 0x03,
      VISIT = 0x04,
      CHAT = 0x06,
      EXIT = 0x0A,
      OPEN = 0x0B,
      SET_ITEMS = 0x0F,
      SET_MESO = 0x10,
      CONFIRM_TRADE = 0x11,
      TRADE_SOMETHING = 0x13,
      PLAYER_SHOP_ADD_ITEM = 0x14,
      BUY_ITEM_PLAYER_SHOP = 0x15,
      MERCHANT_EXIT = 0x1E,
      ADD_ITEM = 0x21,
      BUY_ITEM_STORE = 0x22,
      BUY_ITEM_HIREDMERCHANT = 0x24,
      REMOVE_ITEM = 0x26,
      MAINTANCE_OFF = 0x27,
      MAINTANCE_ORGANISE = 0x28,
      CLOSE_MERCHANT = 0x29,
      ADMIN_STORE_NAMECHANGE = 0x2D,
      VIEW_MERCHANT_VISITOR = 0x2E,
      VIEW_MERCHANT_BLACKLIST = 0x2F,
      MERCHANT_BLACKLIST_ADD = 0x30,
      MERCHANT_BLACKLIST_REMOVE = 0x31,
      REQUEST_TIE = 0x32,
      ANSWER_TIE = 0x33,
      GIVE_UP = 0x34,
      REQUEST_REDO = 0x36,
      ANSWER_REDO = 0x37,
      EXIT_AFTER_GAME = 0x38,
      CANCEL_EXIT = 0x39,
      READY = 0x3A,
      UN_READY = 0x3B,
      EXPEL = 0x3C,
      START = 0x3D,
      SKIP = 0x3F,
      MOVE_OMOK = 0x40,
      SELECT_CARD = 0x44;


  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    final byte action = slea.readByte();
    switch (action) { // Mode
      case CREATE: {
        if (c.getChannelServer().isShutdown()) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        final byte createType = slea.readByte();
        if (createType == 3) { // trade
          MapleTrade.startTrade(chr);
        } else if (createType == 1 || createType == 2 || createType == 4 || createType == 5) { // shop
          if (createType == 4 && !chr.isAdmin()) { // not hired merch...
            // blocked
            // playershop
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
          }
          if (chr.getMap()
              .getMapObjectsInRange(chr.getPosition(), 20000,
                  Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT))
              .size() != 0) {
            chr.dropMessage(1, "You may not establish a store here.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
          } else if (createType == 1 || createType == 2) {
            if (FieldLimitType.Minigames.check(chr.getMap().getFieldLimit())) {
              chr.dropMessage(1, "You may not use minigames here.");
              c.getSession().write(MaplePacketCreator.enableActions());
              return;
            }
          }
          final String desc = slea.readMapleAsciiString();
          String pass = "";
          if (slea.readByte() > 0 && (createType == 1 || createType == 2)) {
            pass = slea.readMapleAsciiString();
          }
          if (createType == 1 || createType == 2) {
            final int piece = slea.readByte();
            final int itemId = createType == 1 ? (4080000 + piece) : 4080100;
            if (!chr.haveItem(itemId)
                || (c.getPlayer().getMapId() >= 910000001 && c.getPlayer().getMapId() <= 910000022)) {
              return;
            }
            MapleMiniGame game = new MapleMiniGame(chr, itemId, desc, pass, createType); // itemid
            game.setPieceType(piece);
            chr.setPlayerShop(game);
            game.setAvailable(true);
            game.setOpen(true);
            game.send(c);
            chr.getMap().addMapObject(game);
            game.update();
          } else {
            IItem shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) slea.readShort());
            if (shop == null || shop.getQuantity() <= 0 || shop.getItemId() != slea.readInt()
                || c.getPlayer().getMapId() < 910000001 || c.getPlayer().getMapId() > 910000022) {
              return;
            }
            if (createType == 4) {
              MaplePlayerShop mps = new MaplePlayerShop(chr, shop.getItemId(), desc);
              chr.setPlayerShop(mps);
              chr.getMap().addMapObject(mps);
              c.getSession().write(PlayerShopPacket.getPlayerStore(chr, true));
            } else {
              final HiredMerchant merch = new HiredMerchant(chr, shop.getItemId(), desc);
              chr.setPlayerShop(merch);
              chr.getMap().addMapObject(merch);
              c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merch, true));
            }
          }
        }
        break;
      }
      case INVITE_TRADE: {
        if (c.getChannelServer().isShutdown()) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        MapleTrade.inviteTrade(chr, chr.getMap().getCharacterById(slea.readInt()));
        break;
      }
      case DENY_TRADE: {
        MapleTrade.declineTrade(chr);
        break;
      }
      case VISIT: {
        if (c.getChannelServer().isShutdown()) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        if (chr.getTrade() != null && chr.getTrade().getPartner() != null) {
          MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
        } else if (chr.getMap() != null) {
          final int obid = slea.readInt();
          MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
          if (ob == null) {
            ob = chr.getMap().getMapObject(obid, MapleMapObjectType.SHOP);
          }

          if (ob instanceof IMaplePlayerShop && chr.getPlayerShop() == null) {
            final IMaplePlayerShop ips = (IMaplePlayerShop) ob;

            if (ob instanceof HiredMerchant) {
              final HiredMerchant merchant = (HiredMerchant) ips;
              if (merchant.isOwner(chr)) {
                merchant.setOpen(false);
                merchant.removeAllVisitors((byte) 17, (byte) 1);
                chr.setPlayerShop(ips);
                c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
              } else {
                if (!merchant.isOpen() || !merchant.isAvailable()) {
                  chr.dropMessage(1, "This shop is in maintenance, please come by later.");
                  chr.getClient().enableActions();
                } else {
                  if (ips.getFreeSlot() == -1) {
                    chr.dropMessage(1,
                        "This shop has reached it's maximum capacity, please come by later.");
                  } else if (merchant.isInBlackList(chr.getName())) {
                    chr.dropMessage(1, "You have been banned from this store.");
                  } else {
                    chr.setPlayerShop(ips);
                    merchant.addVisitor(chr);
                    c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));

                  }
                }
              }
              merchant.sendChatHistory(c);
            } else {
              if (ips instanceof MaplePlayerShop && ((MaplePlayerShop) ips).isBanned(chr.getName())) {
                chr.dropMessage(1, "You have been banned from this store.");
                return;
              } else {
                if (ips.getFreeSlot() < 0 || ips.getVisitorSlot(chr) > -1 || !ips.isOpen()
                    || !ips.isAvailable()) {
                  c.getSession().write(PlayerShopPacket.getMiniGameFull());
                } else {
                  if (slea.available() > 0 && slea.readByte() > 0) { // a
                    // password
                    // has
                    // been
                    // entered
                    String pass = slea.readMapleAsciiString();
                    if (!pass.equals(ips.getPassword())) {
                      c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                      return;
                    }
                  } else if (ips.getPassword().length() > 0) {
                    c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                    return;
                  }
                  chr.setPlayerShop(ips);
                  ips.addVisitor(chr);
                  if (ips instanceof MapleMiniGame) {
                    ((MapleMiniGame) ips).send(c);
                  } else {
                    c.getSession().write(PlayerShopPacket.getPlayerStore(chr, false));
                  }
                }
              }
            }
          }
        }
        break;
      }
      case CHAT: {
        slea.readInt();
        if (chr.getTrade() != null) {
          chr.getTrade().chat(slea.readMapleAsciiString());
        } else if (chr.getPlayerShop() != null) {
          final HiredMerchant ips = (HiredMerchant) chr.getPlayerShop();
          String text = slea.readMapleAsciiString();
          ips.addChatHistory(chr.getName(), text);
          if (ips.isOwner(chr)) {
            c.getSession().write(PlayerShopPacket.shopChat(chr.getName() + " : " + text, 1));
            return;
          }
          ips.broadcastToVisitors(PlayerShopPacket.shopChat(chr.getName() + " : " + text,
              ips.getVisitorSlot(chr)));
        }
        break;
      }
      case EXIT: {
        if (chr.getTrade() != null) {
          MapleTrade.cancelTrade(chr.getTrade(), chr.getClient());
        } else {
          final IMaplePlayerShop ips = chr.getPlayerShop();
          if (ips == null) {
            return;
          }
          if (!ips.isAvailable() || (ips.isOwner(chr) && ips.getShopType() != 1)) {
            ips.closeShop(false, ips.isAvailable());
          } else {
            ips.removeVisitor(chr);
          }
          chr.setPlayerShop(null);
        }
        break;
      }
      case OPEN: {
        // c.getPlayer().haveItem(mode, 1, false, true)

        final IMaplePlayerShop shop = chr.getPlayerShop();
        if (shop != null && shop.isOwner(chr) && shop.getShopType() < 3) {
          if (chr.getMap().allowPersonalShop()) {
            if (c.getChannelServer().isShutdown()) {
              chr.dropMessage(1, "The server is about to shut down.");
              c.getSession().write(MaplePacketCreator.enableActions());
              shop.closeShop(shop.getShopType() == 1, false);
              return;
            }

            if (shop.getShopType() == 1) {
              final HiredMerchant merchant = (HiredMerchant) shop;
              merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
              merchant.setOpen(true);
              merchant.setAvailable(true);
              chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
              chr.setPlayerShop(null);

            } else if (shop.getShopType() == 2) {
              shop.setOpen(true);
              shop.setAvailable(true);
              shop.update();
            }
          } else {
            c.getSession().close();
          }
        }

        break;
      }
      case SET_ITEMS: {
        final MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
        final IItem item = chr.getInventory(ivType).getItem((byte) slea.readShort());
        final short quantity = slea.readShort();
        final byte targetSlot = slea.readByte();

        if (chr.getTrade() != null && item != null) {
          if ((quantity <= item.getQuantity() && quantity >= 0) || GameConstants.isThrowingStar(item.getItemId())
              || GameConstants.isBullet(item.getItemId())) {
            chr.getTrade().setItems(c, item, targetSlot, quantity);
          }
        }
        break;
      }
      case SET_MESO: {
        final MapleTrade trade = chr.getTrade();
        if (trade != null) {
          trade.setMeso(slea.readInt());
        }
        break;
      }
      case CONFIRM_TRADE: {
        if (chr.getTrade() != null) {
          MapleTrade.completeTrade(chr);
        }
        break;
      }
      case MERCHANT_EXIT: {
        /*
         * final IMaplePlayerShop shop = chr.getPlayerShop(); if (shop !=
         * null && shop instanceof HiredMerchant && shop.isOwner(chr)) {
         * shop.setOpen(true); chr.setPlayerShop(null); }
         */
        break;
      }
      case PLAYER_SHOP_ADD_ITEM:
      case ADD_ITEM: {
        if (slea.available() < 11) {
          break;
        }
        final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        final byte slot = (byte) slea.readShort();
        final short bundles = slea.readShort(); // How many in a bundle
        final short perBundle = slea.readShort(); // Price per bundle
        final int price = slea.readInt();

        if (price <= 0 || bundles <= 0 || perBundle <= 0) {
          return;
        }
        final IMaplePlayerShop shop = chr.getPlayerShop();

        if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame) {
          return;
        }
        final IItem ivItem = chr.getInventory(type).getItem(slot);
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ivItem != null) {
          long check = bundles * perBundle;
          if (check > 32767 || check <= 0) { // This is the better way to
            // check.
            return;
          }
          final short bundles_perbundle = (short) (bundles * perBundle);
          if (bundles_perbundle < 0) { // int_16 overflow
            return;
          }
          if (ivItem.getQuantity() <= 0) {
            return;
          }
          if (ivItem.getQuantity() >= bundles_perbundle) {
            final byte flag = ivItem.getFlag();
            if (ItemFlag.UNTRADEABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
              c.getSession().write(MaplePacketCreator.enableActions());
              return;
            }
            if (ii.isDropRestricted(ivItem.getItemId()) || ii.isAccountShared(ivItem.getItemId())) {
              if (!(ItemFlag.KARMA_EQ.check(flag) || ItemFlag.KARMA_USE.check(flag))) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
              }
            }
            if (bundles_perbundle >= 50 && GameConstants.isUpgradeScroll(ivItem.getItemId())) {
              FileoutputUtil.logUsers(chr.getName(), "[PLAYER_SHOP_ADD_ITEM / ADD_ITEM] Placed "
                  + bundles_perbundle + " of " + ivItem.getItemId());
            }
            if (GameConstants.isThrowingStar(ivItem.getItemId())
                || GameConstants.isBullet(ivItem.getItemId())) {
              // Ignore the bundles
              final IItem sellItem = ivItem.copy();
              MapleInventoryManipulator.removeFromSlot(c, type, slot, ivItem.getQuantity(), true);

              shop.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price));
            } else {
              MapleInventoryManipulator.removeFromSlot(c, type, slot, bundles_perbundle, true);

              final IItem sellItem = ivItem.copy();
              sellItem.setQuantity(perBundle);
              shop.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
            }
            c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
          }
        }
        break;
      }
      case BUY_ITEM_PLAYER_SHOP:
      case BUY_ITEM_STORE:
      case BUY_ITEM_HIREDMERCHANT: { // Buy and Merchant buy
        final int item = slea.readByte();
        final short quantity = slea.readShort();
        if (item < 0 || quantity < 0) {
          c.enableActions();
          return;
        }
        // slea.skip(4);
        final IMaplePlayerShop shop = chr.getPlayerShop();
        if (shop == null || shop.isOwner(chr) || shop instanceof MapleMiniGame || (shop.getItems().size() - 1) < item) {
          c.enableActions();
          return;
        }
        final MaplePlayerShopItem tobuy = shop.getItems().get(item);
        if (tobuy == null || !shop.isOpen()) {
          c.enableActions();
          return;
        }
        long check = tobuy.bundles * quantity;
        long check2 = tobuy.price * quantity;
        long check3 = tobuy.item.getQuantity() * quantity;
        long mesosToHold = shop.getMesos() + check2;
        if (check > 32767 || check <= 0 || check2 > Integer.MAX_VALUE || check2 <= 0 || check3 > Integer.MAX_VALUE
            || check3 <= 0) { // This is the better way to check.
          c.enableActions();
          return;
        }
        if (mesosToHold > Integer.MAX_VALUE) {
          c.getPlayer().dropMessage(1, "The player possess more mesos than it can hold.");
          c.enableActions();
          return;
        }
        if (quantity <= 0 || tobuy.bundles < quantity
            || (tobuy.bundles % quantity != 0 && GameConstants.isEquip(tobuy.item.getItemId())) // Buying
            || (shop.getMesos() + check2) < 0) {
          c.enableActions();
          return;
        }
        if (c.getPlayer().getMeso() < check2) {
          c.getPlayer().dropMessage(1, "You don't have enough mesos.");
          c.enableActions();
          return;
        }
        if (quantity >= 50 && GameConstants.isUpgradeScroll(tobuy.item.getItemId())) {
          FileoutputUtil.logUsers(chr.getName(),
              "[BUY_ITEM_PLAYER_SHOP / BUY_ITEM_STORE | BUY_ITEM_HIREDMERCHANT] Placed " + quantity + " of "
                  + tobuy.item.getItemId());
        }
        shop.buy(c, item, quantity);
        shop.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(shop));
        break;
      }
      case REMOVE_ITEM: {
        int slot = slea.readShort(); // 0
        final IMaplePlayerShop shop = chr.getPlayerShop();

        if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame || shop.getItems().size() <= 0
            || shop.getItems().size() <= slot || slot < 0) {
          return;
        }
        final MaplePlayerShopItem item = shop.getItems().get(slot);
        if (item == null) {
          return;
        }
        if (item != null) {
          if (item.bundles > 0) {
            IItem item_get = item.item.copy();
            long check = item.bundles * item.item.getQuantity();
            if (item.bundles < 0 || item.item.getQuantity() < 0) {
              return;
            }
            if (check <= 0 || check > 32767) {
              c.enableActions();
              return;
            }
            item_get.setQuantity((short) check);
            if (item_get.getQuantity() >= 50 && GameConstants.isUpgradeScroll(item.item.getItemId())) {
              FileoutputUtil.logUsers(chr.getName(),
                  "[REMOVE_ITEM] Removed " + item_get.getQuantity() + " of " + item.item.getItemId());
            }
            if (MapleInventoryManipulator.checkSpace(c, item_get.getItemId(), item_get.getQuantity(),
                item_get.getOwner())) {
              MapleInventoryManipulator.addFromDrop(c, item_get, false);
              item.bundles = 0;
              shop.removeFromSlot(slot);
            }
          }
        }
        c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
        break;
      }
      case MAINTANCE_OFF: {
        final HiredMerchant shop = (HiredMerchant) chr.getPlayerShop();
        if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr)) {
          shop.setOpen(true);
          chr.setPlayerShop(null);
        }
        break;
      }
      case MAINTANCE_ORGANISE: {
        final IMaplePlayerShop imps = chr.getPlayerShop();
        if (imps != null && imps.isOwner(chr) && !(imps instanceof MapleMiniGame)) {
          for (Iterator<MaplePlayerShopItem> iterator = imps.getItems().iterator(); iterator.hasNext(); ) {
            MaplePlayerShopItem myItem = iterator.next();
            if (myItem.bundles == 0) {
              iterator.remove();
            }
          }
          long totalMesos = (long) (chr.getMeso() + imps.getMesos());
          if (totalMesos < 0 || totalMesos > Integer.MAX_VALUE) {
            chr.dropMessage(1,
                "You will have more mesos than you can hold. Please put some money in the bank.");
            c.getSession().write(MaplePacketCreator.enableActions());
          } else {
            chr.gainMeso(imps.getMesos(), false);
            imps.setMesos(0);
            c.getSession().write(PlayerShopPacket.shopItemUpdate(imps));
          }
        }
        break;
      }
      case CLOSE_MERCHANT: {
        final IMaplePlayerShop merchant = chr.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
          merchant.closeShop(true, true);
          chr.setPlayerShop(null);
          if (MerchantItemStoreHandler.takeOutMerchantItems(c)) {
            c.getSession().write(PlayerShopPacket.hiredMerchantOwnerLeave());
          }
          ;
          c.getSession().write(MaplePacketCreator.enableActions());
        }
        break;
      }
      case TRADE_SOMETHING:
      case ADMIN_STORE_NAMECHANGE: { // Changing store name, only Admin
        // 01 00 00 00
        break;
      }
      case VIEW_MERCHANT_VISITOR: {
        final IMaplePlayerShop merchant = chr.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
          ((HiredMerchant) merchant).sendVisitor(c);
        }
        break;
      }
      case VIEW_MERCHANT_BLACKLIST: {
        final IMaplePlayerShop merchant = chr.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
          ((HiredMerchant) merchant).sendBlackList(c);
        }
        break;
      }
      case MERCHANT_BLACKLIST_ADD: {
        final IMaplePlayerShop merchant = chr.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
          ((HiredMerchant) merchant).addBlackList(slea.readMapleAsciiString());
        }
        break;
      }
      case MERCHANT_BLACKLIST_REMOVE: {
        final IMaplePlayerShop merchant = chr.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
          ((HiredMerchant) merchant).removeBlackList(slea.readMapleAsciiString());
        }
        break;
      }
      case GIVE_UP: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
          game.nextLoser();
          game.setOpen(true);
          game.update();
          game.checkExitAfterGame();
        }
        break;
      }
      case EXPEL: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          if (!((MapleMiniGame) ips).isOpen()) {
            break;
          }
          ips.removeAllVisitors(3, 1); // no msg
        }
        break;
      }
      case READY:
      case UN_READY: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (!game.isOwner(chr) && game.isOpen()) {
            game.setReady(game.getVisitorSlot(chr));
            game.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game.isReady(game.getVisitorSlot(chr))));
          }
        }
        break;
      }
      case START: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOwner(chr) && game.isOpen()) {
            for (int i = 1; i < ips.getSize(); i++) {
              if (!game.isReady(i)) {
                return;
              }
            }
            game.setGameType();
            game.shuffleList();
            if (game.getGameType() == 1) {
              game.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game.getLoser()));
            } else {
              game.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game, game.getLoser()));
            }
            game.setOpen(false);
            game.update();
          }
        }
        break;
      }
      case REQUEST_TIE: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          if (game.isOwner(chr)) {
            game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie(), false);
          } else {
            game.getMCOwner().getClient().getSession().write(PlayerShopPacket.getMiniGameRequestTie());
          }
          game.setRequestedTie(game.getVisitorSlot(chr));
        }
        break;
      }
      case ANSWER_TIE: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          if (game.getRequestedTie() > -1 && game.getRequestedTie() != game.getVisitorSlot(chr)) {
            if (slea.readByte() > 0) {
              game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 1, game.getRequestedTie()));
              game.nextLoser();
              game.setOpen(true);
              game.update();
              game.checkExitAfterGame();
            } else {
              game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
            }
            game.setRequestedTie(-1);
          }
        }
        break;
      }
      case SKIP: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          if (game.getLoser() != ips.getVisitorSlot(chr)) {
            ips.broadcastToVisitors(
                PlayerShopPacket
                    .shopChat(
                        "Turn could not be skipped by " + chr.getName() + ". Loser: "
                            + game.getLoser() + " Visitor: " + ips.getVisitorSlot(chr),
                        ips.getVisitorSlot(chr)));
            return;
          }
          ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
          game.nextLoser();
        }
        break;
      }
      case MOVE_OMOK: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          if (game.getLoser() != game.getVisitorSlot(chr)) {
            game.broadcastToVisitors(
                PlayerShopPacket
                    .shopChat(
                        "Omok could not be placed by " + chr.getName() + ". Loser: "
                            + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr),
                        game.getVisitorSlot(chr)));
            return;
          }
          game.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
        }
        break;
      }
      case SELECT_CARD: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          if (game.getLoser() != game.getVisitorSlot(chr)) {
            game.broadcastToVisitors(
                PlayerShopPacket
                    .shopChat(
                        "Card could not be placed by " + chr.getName() + ". Loser: "
                            + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr),
                        game.getVisitorSlot(chr)));
            return;
          }
          if (slea.readByte() != game.getTurn()) {
            game.broadcastToVisitors(PlayerShopPacket.shopChat(
                "Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser()
                    + " Visitor: " + game.getVisitorSlot(chr) + " Turn: " + game.getTurn(),
                game.getVisitorSlot(chr)));
            return;
          }
          final int slot = slea.readByte();
          final int turn = game.getTurn();
          final int fs = game.getFirstSlot();
          if (turn == 1) {
            game.setFirstSlot(slot);
            if (game.isOwner(chr)) {
              game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn), false);
            } else {
              game.getMCOwner().getClient().getSession()
                  .write(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn));
            }
            game.setTurn(0); // 2nd turn nao
            return;
          } else if (fs > 0 && game.getCardId(fs + 1) == game.getCardId(slot + 1)) {
            game.broadcastToVisitors(
                PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 2 : 3));
            game.setPoints(game.getVisitorSlot(chr)); // correct.. so
            // still same
            // loser. diff
            // turn tho
          } else {
            game.broadcastToVisitors(
                PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 0 : 1));
            game.nextLoser();// wrong haha

          }
          game.setTurn(1);
          game.setFirstSlot(0);

        }
        break;
      }
      case EXIT_AFTER_GAME:
      case CANCEL_EXIT: {
        final IMaplePlayerShop ips = chr.getPlayerShop();
        if (ips != null && ips instanceof MapleMiniGame) {
          MapleMiniGame game = (MapleMiniGame) ips;
          if (game.isOpen()) {
            break;
          }
          game.setExitAfter(chr);
          game.broadcastToVisitors(PlayerShopPacket.getMiniGameExitAfter(game.isExitAfter(chr)));
        }
        break;
      }
      default: {
        // some idiots try to send huge amounts of data to this (:
        // System.out.println("Unhandled interaction action by " +
        // chr.getName() + " : " + action + ", " + slea.toString());
        // 19 (0x13) - 00 OR 01 -> itemid(maple leaf) ? who knows what this
        // is
        break;
      }
    }

  }

}
