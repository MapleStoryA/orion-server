package handling.channel.handler;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.ArrayMap;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Map;
import java.util.Map.Entry;

public class RepairAllHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getMapId() != 240000000) {
      return;
    }
    Equip eq;
    double rPercentage;
    int price = 0;
    Map<String, Integer> eqStats;
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    final Map<Equip, Integer> eqs = new ArrayMap<Equip, Integer>();
    final MapleInventoryType[] types = {MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
    for (MapleInventoryType type : types) {
      for (IItem item : c.getPlayer().getInventory(type)) {
        if (item instanceof Equip) { // redundant
          eq = (Equip) item;
          if (eq.getDurability() >= 0) {
            eqStats = ii.getEquipStats(eq.getItemId());
            if (/*eqStats.get("durability") > 0 &&*/ eq.getDurability() <= 0/*eqStats.get("durability")*/) {
              rPercentage = (100.0
                  - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
              eqs.put(eq, eqStats.get("durability"));
              price += (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId())
                  / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0));
            }
          }
        }
      }
    }
    if (eqs.size() <= 0 || c.getPlayer().getMeso() < price) {
      return;
    }
    c.getPlayer().gainMeso(-price, true);
    Equip ez;
    for (Entry<Equip, Integer> eqqz : eqs.entrySet()) {
      ez = eqqz.getKey();
      ez.setDurability(eqqz.getValue());
      c.getPlayer().forceReAddItem(ez.copy(),
          ez.getPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
    }

  }

}
