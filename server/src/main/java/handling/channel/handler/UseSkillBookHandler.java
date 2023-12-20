package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.InventoryHandlerUtils;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class UseSkillBookHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        InventoryHandlerUtils.UseSkillBook((byte) packet.readShort(), packet.readInt(), c, c.getPlayer());
    }
}
