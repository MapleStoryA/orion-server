package client.commands;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.Scripting;

@Scripting
class GainItemCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        if (args.length < 2) {
            sendSyntaxMessage(c);
            return;
        }

        final int itemId = Integer.parseInt(args[0]);
        final short quantity = (short) Command.getOptionalIntArg(args, 1, 1);

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.isPet(itemId)) {
            c.getPlayer().dropMessage(5, "Please purchase a pet from the cash shop instead.");
        } else if (!ii.itemExists(itemId)) {
            c.getPlayer().dropMessage(5, itemId + " does not exist");
        } else {
            IItem item;
            byte flag = 0;
            flag |= ItemFlag.SPIKES.getValue();

            if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                item.setFlag(flag);
            } else {
                item = new client.inventory.Item(itemId, (byte) 0, quantity, (byte) 0);
                item.setFlag(flag);
            }
            item.setOwner(c.getPlayer().getName());

            MapleInventoryManipulator.addbyItem(c, item);
        }
    }

    @Override
    public String getTrigger() {
        return "item";
    }

    private void sendSyntaxMessage(MapleClient client) {
        client.getPlayer().dropMessage(5, "[Syntax] !" + getTrigger() + " <ItemId> <Quantity>");
    }
}
