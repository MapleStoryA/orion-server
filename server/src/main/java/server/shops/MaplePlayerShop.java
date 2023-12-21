package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.MapleInventoryManipulator;
import tools.packet.PlayerShopPacket;

@Slf4j
public class MaplePlayerShop extends AbstractPlayerStore {

    private final List<String> bannedList = new ArrayList<String>();
    private int bought_number = 0;

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 3);
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        if (pItem.getBundles() > 0) {
            IItem newItem = pItem.getItem().copy();
            newItem.setQuantity((short) (quantity * newItem.getQuantity()));
            byte flag = newItem.getFlag();

            if (ItemFlag.KARMA_EQ.check(flag)) {
                newItem.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
            } else if (ItemFlag.KARMA_USE.check(flag)) {
                newItem.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
            }
            final int gainmeso = pItem.getPrice() * quantity;
            if (c.getPlayer().getMeso() >= gainmeso) {
                if (getMCOwner().getMeso() + gainmeso > 0
                        && MapleInventoryManipulator.checkSpace(
                                c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner())
                        && MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    bought.add(new BoughtItem(
                            newItem.getItemId(),
                            quantity,
                            gainmeso,
                            c.getPlayer().getName()));
                    c.getPlayer().gainMeso(-gainmeso, false);
                    getMCOwner().gainMeso(gainmeso, false);
                    if (pItem.getBundles() <= 0) {
                        bought_number++;
                        if (bought_number == items.size()) {
                            closeShop(false, true);
                            return;
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
                // }
            }
            getMCOwner().getClient().getSession().write(PlayerShopPacket.shopItemUpdate(this));
        }
    }

    @Override
    public byte getShopType() {
        return IMaplePlayerShop.PLAYER_SHOP;
    }

    @Override
    public void closeShop(boolean saveItems, boolean remove) {
        MapleCharacter owner = getMCOwner();
        removeAllVisitors(10, 1);
        getMap().removeMapObject(this);

        for (MaplePlayerShopItem items : getItems()) {
            if (items.getBundles() > 0) {
                IItem newItem = items.getItem().copy();
                newItem.setQuantity((short) (items.getBundles() * newItem.getQuantity()));
                if (MapleInventoryManipulator.addFromDrop(owner.getClient(), newItem, false)) {
                    items.setBundles((short) 0);
                } else {
                    saveItems(); // O_o
                    break;
                }
            }
        }
        owner.setPlayerShop(null);
        update();
        getMCOwner().getClient().getSession().write(PlayerShopPacket.shopErrorMessage(10, 1));
    }

    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        for (int i = 0; i < 3; i++) {
            MapleCharacter chr = getVisitor(i);
            if (chr.getName().equals(name)) {
                chr.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(5, 1));
                chr.setPlayerShop(null);
                removeVisitor(chr);
            }
        }
    }

    public boolean isBanned(String name) {
        return bannedList.contains(name);
    }
}
