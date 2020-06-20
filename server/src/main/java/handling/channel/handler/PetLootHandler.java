package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.inventory.MaplePet;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.InventoryHandlerUtils;
import handling.world.party.MaplePartyCharacter;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class PetLootHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    if (!chr.isAlive() || chr.getPlayerShop() != null || chr.getConversation() > 0 || chr.getTrade() != null) { // hack
      // return;
    }
    int petz = chr.getPetIndex((int) slea.readLong());
    final MaplePet pet = chr.getPet(petz);
    chr.updateTick(slea.readInt());
    slea.skip(1); // [4] Zero, [4] Seems to be tickcount, [1] Always zero
    final Point Client_Reportedpos = slea.readPos();
    final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);

    if (ob == null || pet == null) {
      return;
    }
    final MapleMapItem mapitem = (MapleMapItem) ob;
    final Lock lock = mapitem.getLock();
    lock.lock();
    try {
      if (mapitem.isPickedUp() || !mapitem.canLoot(c)) {
        c.getSession().write(MaplePacketCreator.getInventoryFull());
        return;
      }
      if (mapitem.getOwner() != chr.getId() && mapitem.isPlayerDrop()) {
        return;
      }
      if (mapitem.getOwner() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0)
          || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getId()
          && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
      if (Distance > 2500) {
        chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_CLIENT, String.valueOf(Distance));

        // } else if (pet.getPos().distanceSq(mapitem.getPosition()) >
        // 90000.0) {
        // chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_SERVER);

      } else if (pet.getPos().distanceSq(mapitem.getPosition()) > 640000.0) {
        chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_SERVER);

      }

      final List<Integer> petIgnore = chr.getPetItemIgnore(pet);
      if (mapitem.getMeso() > 0) {
        if ((!chr.getStat().hasMeso && pet.getPetItemId() != 5000054)
            || petIgnore.contains(Integer.MAX_VALUE)) { // Ignore
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }

        if (chr.getParty() != null && mapitem.getOwner() != chr.getId()) {
          final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();

          for (MaplePartyCharacter z : chr.getParty().getMembers()) {
            MapleCharacter m = chr.getMap().getCharacterById(z.getId());
            if (m != null) {
              toGive.add(m);
            }
          }
          for (final MapleCharacter m : toGive) {
            m.gainMeso(
                mapitem.getMeso() / toGive.size()
                    + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0) : 0),
                true, true);
          }
        } else {
          chr.gainMeso(mapitem.getMeso(), true, true);
        }
        InventoryHandlerUtils.removeItem_Pet(chr, mapitem, petz);
      } else {
        if ((!chr.getStat().hasItem && pet.getPetItemId() != 5000054)
            || petIgnore.contains(mapitem.getItem().getItemId())) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItem().getItemId())) {
          c.getSession().write(MaplePacketCreator.enableActions());
          c.getPlayer().dropMessage(5, "This item cannot be picked up.");
        } else if (InventoryHandlerUtils.useItem(c, mapitem.getItemId())) {
          InventoryHandlerUtils.removeItem_Pet(chr, mapitem, petz);
        } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItem().getItemId(),
            mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
          if (mapitem.getItem().getQuantity() >= 50
              && GameConstants.isUpgradeScroll(mapitem.getItem().getItemId())) {
            FileoutputUtil.logUsers(chr.getName(), "Pet picked up " + mapitem.getItem().getQuantity()
                + " of " + mapitem.getItem().getItemId());
          }
          if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true,
              mapitem.getDropper() instanceof MapleMonster)) {
            InventoryHandlerUtils.removeItem_Pet(chr, mapitem, petz);
          }
        }
      }
    } finally {
      lock.unlock();
    }

  }

}
