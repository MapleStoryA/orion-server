package handling.channel.handler;

import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import server.quest.MapleQuest;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class UpdateQuestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final MapleQuest quest = MapleQuest.getInstance(packet.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }
}
