package handling.login.handler;

import client.MapleClient;
import constants.ServerConstants;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class ServerlistRequestHandler extends AbstractMaplePacketHandler {


  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getSession().write(LoginPacket.getServerList(0, LoginServer.getServerName(), LoginServer.getLoad()));
    c.getSession().write(LoginPacket.getEndOfServerList());
    c.getSession().write(LoginPacket.getRecommendedWorldMessage(0, ServerConstants.RECOMMENDED_MESSAGE));


  }


}
