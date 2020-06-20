package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DenyGuildRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.skip(1);
    String from = slea.readMapleAsciiString();
    final MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
    if (cfrom != null) {
      cfrom.getClient().getSession().write(MaplePacketCreator.denyGuildInvitation(c.getPlayer().getName()));
    }

  }

}
