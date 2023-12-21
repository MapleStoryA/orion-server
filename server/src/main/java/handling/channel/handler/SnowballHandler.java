package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
public class SnowballHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        // B2 00
        // 01 [team]
        // 00 00 [unknown]
        // 89 [position]
        // 01 [stage]
        c.getSession().write(MaplePacketCreator.enableActions());
        // empty, we do this in closerange

    }
}
