package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    // 0 = Select world normally
    // 1 = "Since there are many users, you may encounter some..."
    // 2 = "The concurrent users in this world have reached the max"
    slea.readShort();
    final int numPlayer = LoginServer.getUsersOn();
    final int userLimit = LoginServer.getUserLimit();
    if (numPlayer >= userLimit) {
      c.getSession().write(LoginPacket.getServerStatus(2));
    } else if (numPlayer * 2 >= userLimit) {
      c.getSession().write(LoginPacket.getServerStatus(1));
    } else {
      c.getSession().write(LoginPacket.getServerStatus(0));
    }

  }


}
