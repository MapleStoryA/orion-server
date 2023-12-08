package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class EscortResultHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        log.info("", "[ESCORT_RESULT] " + packet.toString());
    }
}
