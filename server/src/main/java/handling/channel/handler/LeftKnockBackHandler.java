package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class LeftKnockBackHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (c.getPlayer().getMapId() / 10000 == 10906) { // must be in snowball
            // map or else its
            // like infinite FJ
            c.getSession().write(MaplePacketCreator.leftKnockBack());
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }
}
