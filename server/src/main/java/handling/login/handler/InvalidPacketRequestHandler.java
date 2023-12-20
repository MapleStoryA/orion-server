package handling.login.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.MaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class InvalidPacketRequestHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getSession().close();
    }

    @Override
    public boolean validateState(MapleClient c) {
        return true;
    }
}
