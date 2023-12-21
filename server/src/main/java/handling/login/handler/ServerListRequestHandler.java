package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
public class ServerListRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getSession()
                .write(LoginPacket.getServerList(
                        0,
                        LoginServer.getInstance().getServerName(),
                        LoginServer.getInstance().getServerLoad()));
        c.getSession().write(LoginPacket.getEndOfServerList());
        c.getSession().write(LoginPacket.getRecommendedWorldMessage(0, ServerConstants.RECOMMENDED_MESSAGE));
        c.getSession().write(LoginPacket.sendCMapLoadable__OnSetMapObjectVisible());
    }
}
