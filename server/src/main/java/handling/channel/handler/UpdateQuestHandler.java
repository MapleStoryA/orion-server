package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import server.quest.MapleQuest;

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
