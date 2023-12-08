package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.InventoryHandlerUtils;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class UseSkillBookHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        InventoryHandlerUtils.UseSkillBook((byte) packet.readShort(), packet.readInt(), c, c.getPlayer());

    }

}
