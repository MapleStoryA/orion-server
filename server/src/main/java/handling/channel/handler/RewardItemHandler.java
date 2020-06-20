package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructRewardItem;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class RewardItemHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    final IItem toUse = c.getPlayer().getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
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
              if (reward.prob > 0 && Randomizer.nextInt(rewards.getLeft()) < reward.prob) { // Total
                // prob
                if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                  final IItem item = ii.getEquipById(reward.itemid);
                  if (reward.period > 0) {
                    item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
                  }
                  MapleInventoryManipulator.addbyItem(c, item);
                } else {
                  MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                }
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId,
                    1, false, false);

                c.getSession().write(
                    MaplePacketCreator.showRewardItemAnimation(reward.itemid, reward.effect));
                chr.getMap().broadcastMessage(chr, MaplePacketCreator
                    .showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
                c.getSession()
                    .write(MaplePacketCreator.getShowItemGain(reward.itemid, (short) 1, true));
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
