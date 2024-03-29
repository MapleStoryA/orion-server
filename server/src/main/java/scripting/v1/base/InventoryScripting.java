package scripting.v1.base;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.api.Inventory;
import server.MapleInventoryManipulator;
import tools.helper.Api;

@Slf4j
public class InventoryScripting extends PlayerScripting implements Inventory {

    public InventoryScripting(MapleClient client) {
        super(client);
    }

    @Override
    @Api
    public int slotCount(byte type) {
        return player.getInventory(MapleInventoryType.getByType(type)).getSlotLimit();
    }

    @Override
    @Api
    public int holdCount(byte type) {
        return player.getInventory(MapleInventoryType.getByType(type)).getNumFreeSlot();
    }

    @Override
    @Api
    public int itemCount(int item) {
        return player.getItemQuantity(item, true);
    }

    @Override
    @Api
    public int exchange(int money, int id, short quantity) {
        if (money != 0) {
            player.gainMeso(money, true, false, true);
        }
        return MapleInventoryManipulator.gainItem(id, quantity, false, 0, -1, "", client);
    }

    @Override
    @Api
    public void incSlotCount(int type, byte value) {
        player.getStorage().increaseSlots(value);
    }

    // Like in bms, items = item, count * n
    @Override
    @Api
    public int exchange(int money, int... items) {
        if (money != 0) {
            player.gainMeso(money, true, false, true);
        }
        boolean hasSpace = false;
        for (int i = 0; i <= items.length - 1; i += 2) {
            int id = items[i];
            short quantity = (short) items[i + 1];
            if (quantity < 0) {
                int inventoryQuantity = itemCount(id);
                if (inventoryQuantity < (quantity * -1)) {
                    return 0;
                }
            }
            MapleInventoryType type = GameConstants.getInventoryType(id);
            hasSpace = holdCount(type.getType()) >= quantity;
        }
        if (!hasSpace) {
            return 0;
        }
        for (int i = 0; i <= items.length - 1; i += 2) {
            int id = items[i];
            short quantity = (short) items[i + 1];
            MapleInventoryManipulator.gainItem(id, quantity, false, 0, -1, "", client);
        }

        return 1;
    }
}
