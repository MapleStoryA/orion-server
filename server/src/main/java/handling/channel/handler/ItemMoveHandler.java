package handling.channel.handler;

import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class ItemMoveHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        if (c.getPlayer().getPlayerShop() != null
                || c.getPlayer().getConversation() > 0
                || c.getPlayer().getTrade() != null) { // hack
            return;
        }
        c.getPlayer().updateTick(packet.readInt());
        final MapleInventoryType type = MapleInventoryType.getByType(packet.readByte()); // 04
        final short src = packet.readShort(); // 01 00
        final short dst = packet.readShort(); // 00 00
        final short quantity = packet.readShort(); // 53 01

        if (src < 0 && dst > 0) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }
}
