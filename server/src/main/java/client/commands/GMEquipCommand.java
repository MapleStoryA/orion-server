package client.commands;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import server.MapleInventoryManipulator;


public class GMEquipCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        addItem(c, 1002140); // Invincible Hat
        addItem(c, 1042003); // Plain Suit
        addItem(c, 1062007); // Plain Suit pants
        addItem(c, 1322013); // Secret Agent case
    }

    private static void addItem(MapleClient c, int itemId) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).listById(itemId).isEmpty()) {
            MapleInventoryManipulator.addById(c, itemId, (short) 1, c.getPlayer().getName());
        }
    }

    @Override
    public String getTrigger() {
        return "gmequip";
    }
}
