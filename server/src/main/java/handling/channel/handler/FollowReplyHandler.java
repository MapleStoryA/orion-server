package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FollowReplyHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getFollowId() > 0 && c.getPlayer().getFollowId() == slea.readInt()) {
      MapleCharacter tt = c.getPlayer().getMap().getCharacterById(c.getPlayer().getFollowId());
      if (tt != null && tt.getPosition().distanceSq(c.getPlayer().getPosition()) < 10000 && tt.getFollowId() == 0
          && tt.getId() != c.getPlayer().getId()) { // estimate,
        // should less
        boolean accepted = slea.readByte() > 0;
        if (accepted) {
          tt.setFollowId(c.getPlayer().getId());
          tt.setFollowOn(true);
          tt.setFollowInitiator(false);
          c.getPlayer().setFollowOn(true);
          c.getPlayer().setFollowInitiator(true);
          c.getPlayer().getMap()
              .broadcastMessage(MaplePacketCreator.followEffect(tt.getId(), c.getPlayer().getId(), null));
        } else {
          c.getPlayer().setFollowId(0);
          tt.setFollowId(0);
          tt.getClient().getSession().write(MaplePacketCreator.getFollowMsg(5));
        }
      } else {
        if (tt != null) {
          tt.setFollowId(0);
          c.getPlayer().setFollowId(0);
        }
        c.getSession().write(MaplePacketCreator.serverNotice(1, "You are too far away."));
      }
    } else {
      c.getPlayer().setFollowId(0);
    }

  }

}
