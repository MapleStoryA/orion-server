package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FollowRequestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter tt = c.getPlayer().getMap().getCharacterById(slea.readInt());
    if (slea.readByte() > 0) {
      // 1 when changing map
      tt = c.getPlayer().getMap().getCharacterById(c.getPlayer().getFollowId());
      if (tt != null && tt.getFollowId() == c.getPlayer().getId()) {
        tt.setFollowOn(true);
        c.getPlayer().setFollowOn(true);
      } else {
        c.getPlayer().checkFollow();
      }
      return;
    }
    if (slea.readByte() > 0) { // cancelling follow
      tt = c.getPlayer().getMap().getCharacterById(c.getPlayer().getFollowId());
      if (tt != null && tt.getFollowId() == c.getPlayer().getId() && c.getPlayer().isFollowOn()) {
        c.getPlayer().checkFollow();
      }
      return;
    }
    if (tt != null && tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000 && tt.getFollowId() == 0
        && c.getPlayer().getFollowId() == 0 && tt.getId() != c.getPlayer().getId()) { // estimate,
      // should
      // less
      tt.setFollowId(c.getPlayer().getId());
      tt.setFollowOn(false);
      tt.setFollowInitiator(false);
      c.getPlayer().setFollowOn(false);
      c.getPlayer().setFollowInitiator(false);
      tt.getClient().getSession().write(MaplePacketCreator.followRequest(c.getPlayer().getId()));
    } else {
      c.getSession().write(MaplePacketCreator.serverNotice(1, "You are too far away."));
    }

  }

}
