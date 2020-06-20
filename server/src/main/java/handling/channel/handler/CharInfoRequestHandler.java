package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class CharInfoRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().updateTick(slea.readInt());

    int objectid = slea.readInt();
    if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
      return;
    }
    final MapleCharacter player = c.getPlayer().getMap().getCharacterById(objectid);
    c.getSession().write(MaplePacketCreator.enableActions());
    if (player != null) {
      if (!player.isGM() || c.getPlayer().isGM()) {
        if (c.getPlayer().getId() == objectid) {
          if (player.getPet(0) != null) {
            c.getSession().write(PetPacket.loadExceptionList(player.getId(), player.getPet(0).getUniqueId(), ""));
          }
        }
        c.getSession().write(MaplePacketCreator.charInfo(player, c.getPlayer().getId() == objectid));
      }
    }

  }

}
