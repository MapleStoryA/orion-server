package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.AllianceHandlerUtils;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class DenyAllianceRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        AllianceHandlerUtils.HandleAlliance(packet, c, true);
    }
}
