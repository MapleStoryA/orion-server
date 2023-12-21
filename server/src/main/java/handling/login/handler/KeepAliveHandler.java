package handling.login.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.MaplePacketHandler;

@Slf4j
public class KeepAliveHandler implements MaplePacketHandler {
    public void handlePacket(InPacket packet, MapleClient c) {
        c.pongReceived();
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}
