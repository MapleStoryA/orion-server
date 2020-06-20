package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

public class DeleteCharHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readMapleAsciiString();
    final int Character_ID = slea.readInt();

    if (!c.login_Auth(Character_ID)) {
      c.getSession().close();
      return; // Attempting to delete other character
    }
    final byte state = (byte) c.deleteCharacter(Character_ID);

    c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));

  }

}
