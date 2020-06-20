package scripting.v1.binding;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;

public class InventoryOperations {

  public static final int gainItem(final int id, final short quantity, final boolean randomStats, final long period,
                                   final int slots, final String owner, final MapleClient client) {
    if (quantity >= 0) {
      final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      final MapleInventoryType type = GameConstants.getInventoryType(id);

      if (!MapleInventoryManipulator.checkSpace(client, id, quantity, "")) {
        return 0;
      }
      if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id)
          && !GameConstants.isBullet(id)) {
        final Equip item = (Equip) (randomStats ? ii.randomizeStats((Equip) ii.getEquipById(id))
            : ii.getEquipById(id));
        if (period > 0) {
          item.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
        }
        if (slots > 0) {
          item.setUpgradeSlots((byte) (item.getUpgradeSlots() + slots));
        }
        if (owner != null) {
          item.setOwner(owner);
        }
        final String name = ii.getName(id);
        if (id / 10000 == 114 && name != null && name.length() > 0) { // medal
          final String msg = "You have attained title <" + name + ">";
          client.getPlayer().dropMessage(-1, msg);
          client.getPlayer().dropMessage(5, msg);
        }
        MapleInventoryManipulator.addbyItem(client, item.copy());
      } else {
        MapleInventoryManipulator.addById(client, id, quantity, owner == null ? "" : owner, null, period);
      }
    } else {
      MapleInventoryManipulator.removeById(client, GameConstants.getInventoryType(id), id, -quantity, true, false);
    }
    client.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
    return 1;
  }


}
