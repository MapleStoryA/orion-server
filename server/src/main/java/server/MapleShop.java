package server;

import client.MapleClient;
import client.SkillFactory;
import client.inventory.*;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MapleShop {

  private int id;
  private int npcId;
  private List<MapleShopItem> items;
  private static final Set<Integer> rechargeableItems = new LinkedHashSet<Integer>();

  static {
    for (int i = 2070000; i <= 2070018; i++) {
      rechargeableItems.add(i);
    }
    rechargeableItems.remove(2070014); // doesn't exist
    rechargeableItems.remove(2070017);

    for (int i = 2330000; i <= 2330005; i++) {
      rechargeableItems.add(i);
    }
    rechargeableItems.add(2331000);//Blaze Capsule
    rechargeableItems.add(2332000);//Glaze Capsule
  }


  private MapleShop(int id, int npcId) {
    this.id = id;
    this.npcId = npcId;
    items = new LinkedList<MapleShopItem>();
  }

  public void addItem(MapleShopItem item) {
    items.add(item);
  }

  public void sendShop(MapleClient c) {
    c.getPlayer().setShop(this);
    c.getSession().write(MaplePacketCreator.getNPCShop(c, getNpcId(), items));
  }

  public void buy(MapleClient c, int itemId, short quantity, byte slot) {
    if (quantity <= 0) {
      AutobanManager.getInstance().addPoints(c, 1000, 0, "Buying " + quantity + " " + itemId);
      return;
    }
    if (!GameConstants.isMountItemAvailable(itemId, c.getPlayer().getJob())) {
      c.getPlayer().dropMessage(1, "You may not buy this item.");
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    MapleShopItem item = findById(itemId, slot);
    if (item != null && item.getPrice() > 0 && item.getReqItem() == 0) {
      if (c.getPlayer().getLevel() < item.getReqLevel()) { // Packet editing
        return;
      }
      final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (GameConstants.isRechargable(itemId)) {
        quantity = ii.getSlotMax(c, item.getItemId());
      } else if (item.getQuantity() > 1) {
        quantity = item.getQuantity();
      }

      final int price = (GameConstants.isRechargable(itemId) || item.getQuantity() > 1) ? item.getPrice() : (item.getPrice() * quantity);
      final int tprice = (ServerConstants.SHOP_DISCOUNT && ii.getItemEffect(item.getItemId()).isPotion() && !ii.getItemEffect(item.getItemId()).isSkill()) ? (price - (int) Math.round((price * ServerConstants.SHOP_DISCOUNT_PERCENT) / 100f)) : price;
      if (tprice >= 0 && c.getPlayer().getMeso() >= tprice) {
        if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
          c.getPlayer().gainMeso(-tprice, false);
          if (GameConstants.isPet(itemId)) {
            MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1);
          } else {
            MapleInventoryManipulator.addByIdMinutes(c, itemId, quantity, item.getExpiration());
          }
        } else {
          c.getPlayer().dropMessage(1, "Your Inventory is full");
        }
        c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
      }
    } else if (item != null && item.getReqItem() > 0 && quantity == 1 && c.getPlayer().haveItem(item.getReqItem(), item.getReqItemQ(), false, true)) {
      if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
        MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(item.getReqItem()), item.getReqItem(), item.getReqItemQ(), false, false);
        if (GameConstants.isPet(itemId)) {
          MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1);
        } else {
          MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

          if (GameConstants.isRechargable(itemId)) {
            quantity = ii.getSlotMax(c, item.getItemId());
          }
          MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + id + ", " + npcId + " on " + FileoutputUtil.CurrentReadable_Date());
        }
      } else {
        c.getPlayer().dropMessage(1, "Your Inventory is full");
      }
      c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0));
    }
  }

  public void sell(MapleClient c, MapleInventoryType type, byte slot, short quantity) {
    if (quantity == 0xFFFF || quantity == 0) {
      quantity = 1;
    }
    IItem item = c.getPlayer().getInventory(type).getItem(slot);
    if (item == null) {
      return;
    }

    if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
      quantity = item.getQuantity();
    }
    if (quantity < 0) {
      AutobanManager.getInstance().addPoints(c, 1000, 0, "Selling " + quantity + " " + item.getItemId() + " (" + type.name() + "/" + slot + ")");
      return;
    }
    short iQuant = item.getQuantity();
    if (iQuant == 0xFFFF) {
      iQuant = 1;
    }
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    if (ii.cantSell(item.getItemId())) {
      return;
    }
    if (quantity <= iQuant && iQuant > 0) {
      MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
      double price;
      if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
        price = ii.getWholePrice(item.getItemId()) / (double) ii.getSlotMax(c, item.getItemId());
      } else {
        price = ii.getPrice(item.getItemId());
      }
      final int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0);
      if (price != -1.0 && recvMesos > 0) {
        c.getPlayer().gainMeso(recvMesos, false);
      }
      c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0x8));
    }
  }

  public void recharge(final MapleClient c, final byte slot) {
    final IItem item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

    if (item == null || (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId()))) {
      return;
    }
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    short slotMax = ii.getSlotMax(c, item.getItemId());
    final int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());

    if (skill != 0) {
      slotMax += c.getPlayer().getSkillLevel(SkillFactory.getSkill(skill)) * 10;
    }
    if (item.getQuantity() < slotMax) {
      final int price = (int) Math.round(ii.getPrice(item.getItemId()) * (slotMax - item.getQuantity()));
      if (c.getPlayer().getMeso() >= price) {
        item.setQuantity(slotMax);
        c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) item, false));
        c.getPlayer().gainMeso(-price, false, true, false);
        c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 0x8));
      }
    }
  }

  protected MapleShopItem findById(int itemId, byte position) {
    for (MapleShopItem item : items) {
      if (item.getItemId() == itemId && item.getPosition() == position) {
        return item;
      }
    }
    return null;
  }

  public static MapleShop createFromDB(int id, boolean isShopId) {
    MapleShop ret = null;
    int shopId;

    try {
      Connection con = DatabaseConnection.getConnection();
      PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");

      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        shopId = rs.getInt("shopid");
        ret = new MapleShop(shopId, rs.getInt("npcid"));
        rs.close();
        ps.close();
      } else {
        rs.close();
        ps.close();
        return null;
      }
      ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
      ps.setInt(1, shopId);
      rs = ps.executeQuery();
      List<Integer> recharges = new ArrayList<Integer>(rechargeableItems);
      while (rs.next()) {
        int itemid = rs.getInt("itemid");
        if (rechargeableItems.contains(itemid)) {
          recharges.remove(Integer.valueOf(itemid));
        }
        ret.addItem(new MapleShopItem(
            (byte) (rs.getByte("position") - 1),
            rs.getInt("itemid"),
            rs.getInt("price"),
            rs.getInt("reqitem"),
            rs.getInt("reqitemq"),
            rs.getShort("quantity"),
            rs.getInt("expiration"),
            rs.getShort("reqlevel")));
      }
      for (Integer recharge : recharges) {
        ret.addItem(new MapleShopItem((byte) -1, recharge, 0, 0, 0, (short) 0, 0, (short) 0));
      }
      rs.close();
      ps.close();
    } catch (SQLException e) {
      System.err.println("Could not load shop" + e);
    }
    return ret;
  }

  public int getNpcId() {
    return npcId;
  }

  public int getId() {
    return id;
  }
}
