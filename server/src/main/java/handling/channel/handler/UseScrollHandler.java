package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.InventoryHandlerUtils;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class UseScrollHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        byte slot = (byte) packet.readShort();
        byte dst = (byte) packet.readShort();
        byte ws = (byte) packet.readShort();
        byte type = (byte) 0;

        InventoryHandlerUtils.UseUpgradeScroll(slot, dst, ws, c, c.getPlayer(), 0, type);
    }
}
