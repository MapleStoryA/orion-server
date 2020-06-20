package handling.channel.handler;

import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ItemGatherHandler extends AbstractMaplePacketHandler {

  private static final List<IItem> sortItems(final List<IItem> passedMap) {
    final List<Integer> itemIds = new ArrayList<Integer>(); // empty list.
    for (IItem item : passedMap) {
      itemIds.add(item.getItemId()); // adds all item ids to the empty
      // list to be sorted.
    }
    Collections.sort(itemIds); // sorts item ids

    final List<IItem> sortedList = new LinkedList<IItem>(); // ordered list
    // pl0x <3.

    for (Integer val : itemIds) {
      for (IItem item : passedMap) {
        if (val == item.getItemId()) { // Goes through every index and
          // finds the first value that
          // matches
          sortedList.add(item);
          passedMap.remove(item);
          break;
        }
      }
    }
    return sortedList;
  }

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    // [41 00] [E5 1D 55 00] [01]
    // [32 00] [01] [01] // Sent after

    c.getPlayer().updateTick(slea.readInt());
    final byte mode = slea.readByte();
    final MapleInventoryType invType = MapleInventoryType.getByType(mode);
    MapleInventory Inv = c.getPlayer().getInventory(invType);

    final List<IItem> itemMap = new LinkedList<>();
    for (IItem item : Inv.list()) {
      if (GameConstants.isPet(item.getItemId())) {
        continue;
      }
      itemMap.add(item.copy());
    }
    for (IItem itemStats : itemMap) {
      if (GameConstants.isPet(itemStats.getItemId())) {
        continue;
      }
      MapleInventoryManipulator.removeById(c, invType, itemStats.getItemId(), itemStats.getQuantity(), true,
          false);
    }

    final List<IItem> sortedItems = sortItems(itemMap);
    for (IItem item : sortedItems) {
      if (GameConstants.isPet(item.getItemId())) {
        continue;
      }
      MapleInventoryManipulator.addFromDrop(c, item, false);
    }
    c.getSession().write(MaplePacketCreator.finishedGather(mode));
    c.getSession().write(MaplePacketCreator.enableActions());
    itemMap.clear();
    sortedItems.clear();

  }

}
