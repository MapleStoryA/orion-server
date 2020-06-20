package handling.channel.handler;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Map;

public class RepairHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getMapId() != 240000000 || slea.available() < 4) { //leafre for now
      return;
    }
    final int position = slea.readInt(); //who knows why this is a int
    final MapleInventoryType type = position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
    final IItem item = c.getPlayer().getInventory(type).getItem((byte) position);
    if (item == null) {
      return;
    }
    final Equip eq = (Equip) item;
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    final Map<String, Integer> eqStats = ii.getEquipStats(item.getItemId());
    if (eq.getDurability() < 0 || eqStats.get("durability") <= 0 || eq.getDurability() >= eqStats.get("durability")) {
      return;
    }
    final double rPercentage = (100.0 - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
    //drpq level 105 weapons - ~420k per %; 2k per durability point
    //explorer level 30 weapons - ~10 mesos per %
    final int price = (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0)); // / 100 for level 30?
    //TODO: need more data on calculating off client
    if (c.getPlayer().getMeso() < price) {
      return;
    }
    c.getPlayer().gainMeso(-price, false);
    eq.setDurability(eqStats.get("durability"));
    c.getPlayer().forceReAddItem(eq.copy(), type);

  }

}
