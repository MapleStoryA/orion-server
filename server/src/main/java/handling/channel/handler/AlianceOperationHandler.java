package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.AllianceHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@Slf4j
public class AlianceOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        AllianceHandlerUtils.HandleAlliance(packet, c, false);
    }
}
