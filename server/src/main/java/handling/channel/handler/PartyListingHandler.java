package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PartyListingHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.getPlayer().dropMessage(1, "The Party Listing is currently unavailable.");
    c.getSession().write(MaplePacketCreator.enableActions());

  }

}
