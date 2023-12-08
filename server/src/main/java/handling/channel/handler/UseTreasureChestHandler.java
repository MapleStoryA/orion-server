package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import handling.world.helper.BroadcastHelper;
import server.MapleInventoryManipulator;
import server.RandomRewards;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class UseTreasureChestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final short slot = packet.readShort();
        final int itemid = packet.readInt();
        MapleCharacter chr = c.getPlayer();

        final IItem toUse = chr.getInventory(MapleInventoryType.ETC).getItem((byte) slot);
        if (toUse == null || toUse.getQuantity() <= 0 || toUse.getItemId() != itemid) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int reward;
        int keyIDforRemoval = 0;
        String box;

        switch (toUse.getItemId()) {
            case 4280000: // Gold box
                reward = RandomRewards.getInstance().getGoldBoxReward();
                keyIDforRemoval = 5490000;
                box = "Gold";
                break;
            case 4280001: // Silver box
                reward = RandomRewards.getInstance().getSilverBoxReward();
                keyIDforRemoval = 5490001;
                box = "Silver";
                break;
            default: // Up to no good
                return;
        }

        // Get the quantity
        int amount = 1;
        switch (reward) {
            case 2000004:
                amount = 200; // Elixir
                break;
            case 2000005:
                amount = 100; // Power Elixir
                break;
        }
        if (chr.getInventory(MapleInventoryType.CASH).countById(keyIDforRemoval) > 0) {
            final IItem item = MapleInventoryManipulator.addbyId_Gachapon(c, reward, (short) amount);

            if (item == null) {
                chr.dropMessage(
                        5,
                        "Please check your item inventory and see if you have a Master Key, or if"
                                + " the inventory is full.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) slot, (short) 1, true);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, keyIDforRemoval, 1, true, false);
            c.getSession().write(MaplePacketCreator.getShowItemGain(reward, (short) amount, true));

            if (GameConstants.gachaponRareItem(item.getItemId()) > 0) {
                BroadcastHelper.broadcastMessage(MaplePacketCreator.getGachaponMega(
                        "[" + box + " Chest] " + c.getPlayer().getName(), " : Lucky winner of Gachapon!", item, (byte)
                                2));
            }
        } else {
            chr.dropMessage(
                    5,
                    "Please check your item inventory and see if you have a Master Key, or if the"
                            + " inventory is full.");
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }
}
