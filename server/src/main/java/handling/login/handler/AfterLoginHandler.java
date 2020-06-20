package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class AfterLoginHandler implements MaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    c.getSession().write(LoginPacket.pinOperation((byte) 0)); // Accept
  }

  @Override
  public boolean validateState(MapleClient c) {
    return true;
  }

}
