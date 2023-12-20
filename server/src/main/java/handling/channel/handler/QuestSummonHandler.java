package handling.channel.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class QuestSummonHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getSession().write(MaplePacketCreator.sendHint("It supposed to talk to me, right?", 100, 100));
    }
}
