package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
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

public class ItemPickupHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr.getPlayerShop() != null || chr.getConversation() > 0 || chr.getTrade() != null || !chr.isAlive()) { // hack
      return;
    }
    chr.updateTick(slea.readInt());
    slea.skip(1); // [4] Seems to be tickcount, [1] always 0
    final Point Client_Reportedpos = slea.readPos();

    final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
    if (ob == null) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }

    boolean vac = false;
    double multiplier = 0;
    if (multiplier > 0 && !chr.getMap().getEverlast()) {
      vac = true;
    }

    final MapleMapItem mapitem = (MapleMapItem) ob; // Main map item
    final Lock lock = mapitem.getLock();
    lock.lock();
    try {
      if (mapitem.isPickedUp() || !mapitem.canLoot(c)) {
        c.getSession().write(MaplePacketCreator.enableActions());
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
      if (vac && mapitem.isPlayerDrop()) { // Only can vac from mob drops
        // (main item)
        vac = false;
      }
      if (!vac) {
        final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
        if (Distance > 2500) {
          chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_CLIENT, String.valueOf(Distance));
        } else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 640000.0) {
          chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_SERVER);
        }
        if (mapitem.getMeso() > 0) {
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
          InventoryHandlerUtils.removeItem(chr, mapitem, ob);
        } else {
          if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItem().getItemId())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            c.getPlayer().dropMessage(5, "This item cannot be picked up.");
          } else if (InventoryHandlerUtils.useItem(c, mapitem.getItemId())) {
            InventoryHandlerUtils.removeItem(c.getPlayer(), mapitem, ob);
          } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItem().getItemId(),
              mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
            if (mapitem.getItem().getQuantity() >= 50
                && GameConstants.isUpgradeScroll(mapitem.getItem().getItemId())) {
              FileoutputUtil.logUsers(chr.getName(), "Player picked up " + mapitem.getItem().getQuantity()
                  + " of " + mapitem.getItem().getItemId());
            }
            if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true,
                mapitem.getDropper() instanceof MapleMonster)) {
              InventoryHandlerUtils.removeItem(chr, mapitem, ob);
            }
          } else {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            c.getSession().write(MaplePacketCreator.enableActions());
          }
        }
      } else { // Vac
        int i = 0;
        boolean isVortex = false;
        List<MapleMapObject> items = chr.getMap().getItemsInRange(Client_Reportedpos,
            (GameConstants.maxViewRangeSq() * multiplier));
        for (MapleMapObject obb : items) {
          i++;
          if (!isVortex) {
            if (i == 11) { // max 10 items.. // looting spped it way
              // too slow.
              break;
            }
          } else {
            if (i == 31) { // Maximum 30 items per "Z" button
              break;
            }
          }
          UseCashItemHandler.PickupItemAtSpot(c, chr, obb);
        }
      }
    } finally {
      lock.unlock();
    }
  }

}
