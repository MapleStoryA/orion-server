package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseCatchItemHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());
    MapleCharacter chr = c.getPlayer();
    final byte slot = (byte) slea.readShort();
    final int itemid = slea.readInt();
    final MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
    final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

    if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mob != null) {
      switch (itemid) {
        case 2270004: { // Purification Marble
          final MapleMap map = chr.getMap();

          if (mob.getHp() <= mob.getMobMaxHp() / 2) {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
            map.killMonster(mob, chr, true, false, (byte) 0);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
            MapleInventoryManipulator.addById(c, 4001169, (short) 1, "");
          } else {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
            chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
          }
          break;
        }
        case 2270002: { // Characteristic Stone
          final MapleMap map = chr.getMap();

          if (mob.getHp() <= mob.getMobMaxHp() / 2) {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
            map.killMonster(mob, chr, true, false, (byte) 0);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
          } else {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
            chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
          }
          break;
        }
        case 2270000: { // Pheromone Perfume
          if (mob.getId() != 9300101) {
            break;
          }
          final MapleMap map = c.getPlayer().getMap();

          map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
          map.killMonster(mob, chr, true, false, (byte) 0);
          MapleInventoryManipulator.addById(c, 1902000, (short) 1, null);
          MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
          break;
        }
        case 2270003: { // Cliff's Magic Cane
          if (mob.getId() != 9500320) {
            break;
          }
          final MapleMap map = c.getPlayer().getMap();

          if (mob.getHp() <= mob.getMobMaxHp() / 2) {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
            map.killMonster(mob, chr, true, false, (byte) 0);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
          } else {
            map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
            chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
          }
          break;
        }
      }
    }
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
