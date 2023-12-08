package server.shops;

import client.inventory.IItem;

@lombok.extern.slf4j.Slf4j
public class MaplePlayerShopItem {

    private IItem item;
    private short bundles;
    private int price;

    public MaplePlayerShopItem(IItem item, short bundles, int price) {
        this.setItem(item);
        this.setBundles(bundles);
        this.setPrice(price);
    }

    public IItem getItem() {
        return item;
    }

    public void setItem(IItem item) {
        this.item = item;
    }

    public short getBundles() {
        return bundles;
    }

    public void setBundles(short bundles) {
        this.bundles = bundles;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
