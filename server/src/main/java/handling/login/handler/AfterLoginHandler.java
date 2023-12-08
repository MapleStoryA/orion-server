package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
public class AfterLoginHandler implements MaplePacketHandler {

    private static final byte ACCEPT_OPERATION = 0;

    @Override
    public void handlePacket(InPacket packet, MapleClient client) {
        // Write a response to the client to indicate successful login
        client.getSession().write(LoginPacket.pinOperation(ACCEPT_OPERATION));
    }

    @Override
    public boolean validateState(MapleClient client) {
        return true;
    }
}
