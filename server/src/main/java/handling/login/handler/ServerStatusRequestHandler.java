package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import tools.data.input.CInPacket;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        // 0 = Select world normally
        // 1 = "Since there are many users, you may encounter some..."
        // 2 = "The concurrent users in this world have reached the max"
        packet.readShort();
        final int numPlayer = LoginServer.getInstance().getUsersOn();
        final int userLimit = LoginServer.getInstance().getUserLimit();
        if (numPlayer >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(2));
        } else if (numPlayer * 2 >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(1));
        } else {
            c.getSession().write(LoginPacket.getServerStatus(0));
        }
    }
}
