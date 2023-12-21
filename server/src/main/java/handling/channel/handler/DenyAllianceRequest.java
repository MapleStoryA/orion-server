package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.AllianceHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;

@Slf4j
public class DenyAllianceRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        AllianceHandlerUtils.HandleAlliance(packet, c, true);
    }
}
