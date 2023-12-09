package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import lombok.extern.slf4j.Slf4j;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
public class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

    private static final int NORMAL_STATUS = 0;
    private static final int BUSY_STATUS = 1;
    private static final int FULL_STATUS = 2;

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.readShort(); // Consider explaining what this is for
        final int numPlayer = LoginServer.getInstance().getUsersOn();
        final int userLimit = LoginServer.getInstance().getUserLimit();

        int status = getServerStatus(numPlayer, userLimit);
        c.getSession().write(LoginPacket.getServerStatus(status));
    }

    private int getServerStatus(int numPlayer, int userLimit) {
        if (numPlayer >= userLimit) {
            return FULL_STATUS;
        } else if (numPlayer * 2 >= userLimit) {
            return BUSY_STATUS;
        } else {
            return NORMAL_STATUS;
        }
    }
}
