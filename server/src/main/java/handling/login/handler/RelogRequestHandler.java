package handling.login.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.packet.LoginPacket;

@Slf4j
public class RelogRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getSession().write(LoginPacket.getRelogResponse());
    }
}
