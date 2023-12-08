package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class RelogRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        c.getSession().write(LoginPacket.getRelogResponse());
    }

}
