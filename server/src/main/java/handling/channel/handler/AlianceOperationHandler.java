package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.AllianceHandlerUtils;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class AlianceOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        AllianceHandlerUtils.HandleAlliance(packet, c, false);
    }

}
