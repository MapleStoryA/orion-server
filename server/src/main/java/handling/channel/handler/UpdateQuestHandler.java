package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

public class UpdateQuestHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    final MapleQuest quest = MapleQuest.getInstance(slea.readShort());
    if (quest != null) {
      c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
    }

  }

}
