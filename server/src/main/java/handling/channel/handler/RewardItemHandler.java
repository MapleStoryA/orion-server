package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.StructRewardItem;
import tools.MaplePacketCreator;
import tools.collection.Pair;
import tools.helper.DateHelper;
import tools.helper.Randomizer;

@Slf4j
public class RewardItemHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final byte slot = (byte) packet.readShort();
        final int itemId = packet.readInt();
        final IItem toUse = c.getPlayer()
                .getInventory(GameConstants.getInventoryType(itemId))
                .getItem(slot);
        c.getSession().write(MaplePacketCreator.enableActions());
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {
            if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1
                    && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1
                    && chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1
                    && chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);
                // hot time event, 2022336 < custom (Secret Box)
                if (rewards != null && rewards.getLeft() > 0) {
                    boolean rewarded = false;
                    while (!rewarded) {
                        for (StructRewardItem reward : rewards.getRight()) {
                            if (reward.getProb() > 0
                                    && Randomizer.nextInt(rewards.getLeft()) < reward.getProb()) { // Total
                                // prob
                                if (GameConstants.getInventoryType(reward.getItemId()) == MapleInventoryType.EQUIP) {
                                    final IItem item = ii.getEquipById(reward.getItemId());
                                    if (reward.getPeriod() > 0) {
                                        item.setExpiration(
                                                System.currentTimeMillis() + (reward.getPeriod() * 60 * 60 * 10));
                                    }
                                    MapleInventoryManipulator.addbyItem(c, item);
                                } else {
                                    MapleInventoryManipulator.addById(
                                            c,
                                            reward.getItemId(),
                                            reward.getQuantity(),
                                            "Reward item: " + itemId + " on " + DateHelper.getCurrentReadableDate());
                                }
                                MapleInventoryManipulator.removeById(
                                        c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);

                                c.getSession()
                                        .write(MaplePacketCreator.showRewardItemAnimation(
                                                reward.getItemId(), reward.getEffect()));
                                chr.getMap()
                                        .broadcastMessage(
                                                chr,
                                                MaplePacketCreator.showRewardItemAnimation(
                                                        reward.getItemId(), reward.getEffect(), chr.getId()),
                                                false);
                                c.getSession()
                                        .write(MaplePacketCreator.getShowItemGain(reward.getItemId(), (short) 1, true));
                                rewarded = true;
                            }
                        }
                    }
                } else {
                    chr.dropMessage(6, "Unknown error.");
                }
            } else {
                chr.dropMessage(6, "Insufficient inventory slot.");
            }
        }
    }
}
