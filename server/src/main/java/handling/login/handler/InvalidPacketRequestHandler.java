package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class InvalidPacketRequestHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        c.getSession().close();
    }

    @Override
    public boolean validateState(MapleClient c) {
        return true;
    }

}
