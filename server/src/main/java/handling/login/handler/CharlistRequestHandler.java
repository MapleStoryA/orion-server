package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.util.List;

public class CharlistRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readByte();
    final int server = slea.readByte();
    final int channel = slea.readByte() + 1;

    c.setWorld(server);
    c.setChannel(channel);

    final List<MapleCharacter> chars = c.loadCharacters(server);
    if (chars != null) {
      c.getSession().write(LoginPacket.getCharList(c.getSecondPassword() != null, chars, c.getCharacterSlots()));
    } else {
      c.getSession().close();
    }

  }

}
