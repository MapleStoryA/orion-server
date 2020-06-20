package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetAutoPotHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final int petid = (int) slea.readLong();
    slea.skip(1);
    c.getPlayer().updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    if (chr == null || !chr.isAlive() || chr.getPetIndex(petid) < 0 || chr.getMap() == null
        || chr.getMapId() == 749040100 || chr.getMap() == null || chr.hasDisease(MapleDisease.POTION)) {
      return;
    }
    final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
    if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    final long time = System.currentTimeMillis();
    if (chr.getNextConsume() > time) {
      chr.dropMessage(5, "You may not use this item yet.");
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit()) || chr.getMapId() == 610030600) { // cwk
      // quick
      // hack
      if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        if (chr.getMap().getConsumeItemCoolTime() > 0) {
          chr.setNextConsume(time + (chr.getMap().getConsumeItemCoolTime() * 1000));
        }
      }
    } else {
      c.getSession().write(MaplePacketCreator.enableActions());
    }

  }

}
