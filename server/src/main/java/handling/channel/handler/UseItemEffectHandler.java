package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseItemEffectHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    int itemId = slea.readInt();
    final IItem toUse = chr.getInventory(MapleInventoryType.CASH).findById(itemId);
    if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (itemId != 5510000) {
      chr.setItemEffect(itemId);
    }
    chr.getMap().broadcastMessage(chr, MaplePacketCreator.itemEffect(chr.getId(), itemId), false);

  }

}
