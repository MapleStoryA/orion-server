package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.AllianceHandlerUtils;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class AlianceOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        AllianceHandlerUtils.HandleAlliance(packet, c, false);
    }
}
