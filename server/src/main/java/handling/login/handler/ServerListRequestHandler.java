package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class ServerListRequestHandler extends AbstractMaplePacketHandler {


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(LoginPacket.getServerList(0, LoginServer.getInstance().getServerName(), LoginServer.getInstance().getServerLoad()));
        c.getSession().write(LoginPacket.getEndOfServerList());
        c.getSession().write(LoginPacket.getRecommendedWorldMessage(0, ServerConstants.RECOMMENDED_MESSAGE));
    }


}
