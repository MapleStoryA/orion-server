package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.List;

public class CharlistViewAllHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    c.setWorld(0);
    c.setChannel(1);

    List<MapleCharacter> chars = c.loadCharacters(0);

    if (chars != null) {
      c.getSession().write(MaplePacketCreator.viewAllChar(1, chars.size()));
      c.getSession().write(MaplePacketCreator.viewAllCharShowChars(0, chars));
    } else {
      c.getSession().close();
    }


  }

}
