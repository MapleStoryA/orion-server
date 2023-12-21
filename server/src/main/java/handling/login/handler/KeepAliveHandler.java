package handling.login.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.MaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class KeepAliveHandler implements MaplePacketHandler {
    public void handlePacket(InPacket packet, MapleClient c) {
        c.pongReceived();
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}
