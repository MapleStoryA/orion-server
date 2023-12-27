package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.login.LoginServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.packet.LoginPacket;

@Slf4j
public class ServerListRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        // This is a bad workaround for the duplicating world when you come back from select world
        // It's not clear what is causing the client to send CLogin::OnWorldInformation twice
        if (System.currentTimeMillis() - c.getLastCharList() < 100) {
            return;
        }
        c.getSession()
                .write(LoginPacket.getServerList(
                        0,
                        LoginServer.getInstance().getServerName(),
                        LoginServer.getInstance().getServerLoad()));
        c.getSession().write(LoginPacket.getRecommendedWorldMessage(0, ServerConstants.RECOMMENDED_MESSAGE));
        c.getSession().write(LoginPacket.getEndOfServerList());
        c.setLastCharList(System.currentTimeMillis());
    }
}
