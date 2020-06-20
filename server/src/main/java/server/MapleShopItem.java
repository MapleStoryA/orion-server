package server;

public class MapleShopItem {

  private byte position;
  private short quantity, reqLevel; // reqLevel = can buy already
  private int itemId, price, reqItem, reqItemQ, expiration; // In minutes

  public MapleShopItem(byte position, int itemId, int price, int reqItem, int reqItemQ, short quantity, int expiration, short reqLevel) {
    this.position = position;
    this.itemId = itemId;
    this.price = price;
    this.reqItem = reqItem;
    this.reqItemQ = reqItemQ;
    this.quantity = quantity;
    this.expiration = expiration;
    this.reqLevel = reqLevel;
  }

  public byte getPosition() {
    return position;
  }

  public int getItemId() {
    return itemId;
  }

  public int getPrice() {
    return price;
  }

  public int getReqItem() {
    return reqItem;
  }

  public int getReqItemQ() {
    return reqItemQ;
  }

  public short getQuantity() {
    return quantity;
  }

  public int getExpiration() {
    return expiration;
  }

  public short getReqLevel() {
    return reqLevel;
  }
}
