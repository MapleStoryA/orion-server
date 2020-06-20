package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseReturnScrollHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (!chr.isAlive() || chr.getMapId() == 749040100) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    c.getPlayer().updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

    if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
      if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
      } else {
        c.getSession().write(MaplePacketCreator.enableActions());
      }
    } else {
      c.getSession().write(MaplePacketCreator.enableActions());
    }

  }

}
