package handling.channel.handler;

import client.MapleClient;
import client.MapleQuestStatus;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class UseItemQuestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final short slot = slea.readShort();
    final int itemId = slea.readInt();
    final IItem item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
    final short qid = slea.readShort();
    slea.readShort();
    final MapleQuest quest = MapleQuest.getInstance(qid);
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    Pair<Integer, List<Integer>> questItemInfo = null;
    boolean found = false;
    for (IItem i : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
      if (i.getItemId() / 10000 == 422) {
        questItemInfo = ii.questItemInfo(i.getItemId());
        if (questItemInfo != null && questItemInfo.getLeft() == qid && questItemInfo.getRight().contains(itemId)) {
          found = true;
          break; //i believe it's any order
        }
      }
    }
    if (quest != null && found && item != null && item.getQuantity() > 0 && item.getItemId() == itemId) {
      final int newData = slea.readInt();
      final MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
      if (stats != null && stats.getStatus() == 1) {
        stats.setCustomData(String.valueOf(newData));
        c.getPlayer().updateQuest(stats, true);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short) 1, false);
      }
    }

  }

}
