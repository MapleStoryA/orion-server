package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
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
