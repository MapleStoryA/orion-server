package handling.login.handler;

import client.MapleCharacterUtil;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class CheckCharNameHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    String name = slea.readMapleAsciiString();
    c.getSession().write(LoginPacket.charNameResponse(name, !MapleCharacterUtil.canCreateChar(name)
        || LoginInformationProvider.getInstance().isForbiddenName(name)));

  }

}
