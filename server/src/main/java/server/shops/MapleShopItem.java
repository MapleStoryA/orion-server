package server.shops;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleShopItem {

    private final byte position;
    private final short quantity;
    private final short reqLevel; // reqLevel = can buy already
    private final int itemId;
    private final int price;
    private final int reqItem;
    private final int reqItemQ;
    private final int expiration; // In minutes

    public MapleShopItem(
            byte position,
            int itemId,
            int price,
            int reqItem,
            int reqItemQ,
            short quantity,
            int expiration,
            short reqLevel) {
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
