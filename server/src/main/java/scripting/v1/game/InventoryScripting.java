package scripting.v1.game;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import scripting.v1.game.api.IInventoryScripting;
import scripting.v1.game.helper.InventoryHelper;
import tools.ApiClass;

@lombok.extern.slf4j.Slf4j
public class InventoryScripting extends PlayerScripting implements IInventoryScripting {

    public InventoryScripting(MapleClient client) {
        super(client);
    }

    @Override
    @ApiClass
    public int slotCount(byte type) {
        return player.getInventory(MapleInventoryType.getByType(type)).getSlotLimit();
    }

    @Override
    @ApiClass
    public int holdCount(byte type) {
        return player.getInventory(MapleInventoryType.getByType(type)).getNumFreeSlot();
    }

    @Override
    @ApiClass
    public int itemCount(int item) {
        return player.getItemQuantity(item, true);
    }

    @Override
    @ApiClass
    public int exchange(int money, int id, short quantity) {
        if (money != 0) {
            player.gainMeso(money, true, false, true);
        }
        return InventoryHelper.gainItem(id, quantity, false, 0, -1, "", client);
    }

    @Override
    @ApiClass
    public void incSlotCount(int type, byte value) {
        player.getStorage().increaseSlots(value);
    }

    // Like in bms, items = item, count * n
    @Override
    @ApiClass
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
            InventoryHelper.gainItem(id, quantity, false, 0, -1, "", client);
        }

        return 1;
    }
}
