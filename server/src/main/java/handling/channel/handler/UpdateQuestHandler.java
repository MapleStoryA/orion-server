package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.quest.MapleQuest;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class UpdateQuestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        final MapleQuest quest = MapleQuest.getInstance(packet.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }
}
