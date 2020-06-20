package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class TransformPlayerHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    chr.updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    final String target = slea.readMapleAsciiString();

    final IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

    if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    switch (itemId) {
      case 2212000:
        final MapleCharacter search_chr = chr.getMap().getCharacterByName(target);
        if (search_chr != null) {
          if (search_chr.getId() != chr.getId()) {
            if (!search_chr.isGM() && !chr.isGM() || chr.isGM()) {
              if (search_chr.getBuffedValue(MapleBuffStat.MORPH) == null) {
                MapleItemInformationProvider.getInstance().getItemEffect(2212000).applyTo(search_chr);
                search_chr.dropMessage(6, chr.getName() + " had played a prank on you!"); // there's
                // a
                // packet
                // for
                // this!
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
              } else {
                chr.dropMessage(5, "You may not use this item on '" + target + "' right now.");
              }
            } else {
              chr.dropMessage(5, "'" + target + "' was not found in the current map.");
            }
          } else {
            chr.dropMessage(5, "You may not use this item on yourself.");
          }
        } else {
          chr.dropMessage(5, "'" + target + "' was not found in the current map.");
        }
        c.getSession().write(MaplePacketCreator.enableActions());
        break;
    }

  }

}
