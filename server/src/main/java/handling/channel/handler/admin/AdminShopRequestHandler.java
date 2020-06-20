package handling.channel.handler.admin;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 24, 2017
 */
public class AdminShopRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    c.enableActions();
  }
}
