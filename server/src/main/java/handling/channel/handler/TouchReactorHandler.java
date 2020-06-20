package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.ReactorScriptManager;
import server.maps.MapleReactor;
import tools.data.input.SeekableLittleEndianAccessor;

public class TouchReactorHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final int oid = slea.readInt();
    final boolean touched = slea.readByte() > 0;
    final MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
    if (!touched || reactor == null || !reactor.isAlive() || reactor.getReactorId() < 6109013
        || reactor.getReactorId() > 6109027) {
      return;
    }
    ReactorScriptManager.getInstance().act(c, reactor); // not sure how
    // touched boolean
    // comes into play

  }

}
