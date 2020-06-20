package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class RingActionHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final byte mode = slea.readByte();
    if (mode == 0) {
      final String name = slea.readMapleAsciiString();
      final int itemid = slea.readInt();
      final int newItemId = 1112300 + (itemid - 2240004);
      final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
      int errcode = 0;
      if (c.getPlayer().getMarriageId() > 0) {
        errcode = 0x17;
      } else if (chr == null) {
        errcode = 0x12;
      } else if (chr.getMapId() != c.getPlayer().getMapId()) {
        errcode = 0x13;
      } else if (!c.getPlayer().haveItem(itemid, 1) || itemid < 2240004 || itemid > 2240015) {
        errcode = 0x0D;
      } else if (chr.getMarriageId() > 0 || chr.getMarriageItemId() > 0) {
        errcode = 0x18;
      } else if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")) {
        errcode = 0x14;
      } else if (!MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
        errcode = 0x15;
      }
      if (errcode > 0) {
        c.getSession().write(MaplePacketCreator.sendEngagement((byte) errcode, 0, null, null));
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      c.getPlayer().setMarriageItemId(itemid);
      chr.getClient().getSession()
          .write(MaplePacketCreator.sendEngagementRequest(c.getPlayer().getName(), c.getPlayer().getId()));
      // 1112300 + (itemid - 2240004)
    } else if (mode == 1) {
      c.getPlayer().setMarriageItemId(0);
    } else if (mode == 2) { // accept/deny proposal
      final boolean accepted = slea.readByte() > 0;
      final String name = slea.readMapleAsciiString();
      final int id = slea.readInt();
      final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
      if (c.getPlayer().getMarriageId() > 0 || chr == null || chr.getId() != id || chr.getMarriageItemId() <= 0
          || !chr.haveItem(chr.getMarriageItemId(), 1) || chr.getMarriageId() > 0) {
        c.getSession().write(MaplePacketCreator.sendEngagement((byte) 0x1D, 0, null, null));
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (accepted) {
        final int newItemId = 1112300 + (chr.getMarriageItemId() - 2240004);
        if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")
            || !MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
          c.getSession().write(MaplePacketCreator.sendEngagement((byte) 0x15, 0, null, null));
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        MapleInventoryManipulator.addById(c, newItemId, (short) 1, "");
        MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, chr.getMarriageItemId(),
            1, false, false);
        MapleInventoryManipulator.addById(chr.getClient(), newItemId, (short) 1, "");
        chr.getClient().getSession()
            .write(MaplePacketCreator.sendEngagement((byte) 0x10, newItemId, chr, c.getPlayer()));
        chr.setMarriageId(c.getPlayer().getId());
        c.getPlayer().setMarriageId(chr.getId());
      } else {
        chr.getClient().getSession().write(MaplePacketCreator.sendEngagement((byte) 0x1E, 0, null, null));
      }
      c.getSession().write(MaplePacketCreator.enableActions());
      chr.setMarriageItemId(0);
    } else if (mode == 3) { // drop, only works for ETC
      final int itemId = slea.readInt();
      final MapleInventoryType type = GameConstants.getInventoryType(itemId);
      final IItem item = c.getPlayer().getInventory(type).findById(itemId);
      if (item != null && type == MapleInventoryType.ETC && itemId / 10000 == 421) {
        MapleInventoryManipulator.drop(c, type, item.getPosition(), item.getQuantity());
      }
    }

  }

}
