package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.InventoryHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@Slf4j
public class UseEquipScrollHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        InventoryHandlerUtils.UseUpgradeScroll(
                (byte) packet.readShort(), (byte) packet.readShort(), (byte) 0, c, c.getPlayer(), (byte) 2);
    }
}
