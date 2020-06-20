package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class UseSummonBagHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (!chr.isAlive()) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    c.getPlayer().updateTick(slea.readInt());
    final byte slot = (byte) slea.readShort();
    final int itemId = slea.readInt();
    final IItem toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

    if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId) {

      MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);

      if (c.getPlayer().isGM() || !FieldLimitType.SummoningBag.check(chr.getMap().getFieldLimit())) {
        final List<Pair<Integer, Integer>> toSpawn = MapleItemInformationProvider.getInstance()
            .getSummonMobs(itemId);

        if (toSpawn == null) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        MapleMonster ht;
        int type = 0;

        for (int i = 0; i < toSpawn.size(); i++) {
          if (Randomizer.nextInt(99) <= toSpawn.get(i).getRight()) {
            ht = MapleLifeFactory.getMonster(toSpawn.get(i).getLeft());
            chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
          }
        }
      }
    }
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
