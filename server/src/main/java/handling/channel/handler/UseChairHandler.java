package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseChairHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    int itemId = slea.readInt();
    if (chr == null) {
      return;
    }
    final IItem toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);

    if (toUse == null) {
      chr.getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(itemId));
      return;
    }
    if (itemId == 3011000) {
      boolean haz = false;
      for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH).list()) {
        if (item.getItemId() == 5340000) {
          haz = true;
        } else if (item.getItemId() == 5340001) {
          haz = false;
          chr.startFishingTask(true);
          break;
        }
      }
      if (haz) {
        chr.startFishingTask(false);
      }
    }
    chr.setChair(itemId);
    chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), itemId), false);
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
