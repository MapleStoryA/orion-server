package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        packet.readByte();
        String portal_name = packet.readMapleAsciiString();
        final MaplePortal portal = chr.getMap().getPortal(portal_name);
        // slea.skip(2);
        if (portal != null) {
            portal.enterPortal(c);
        }
    }
}
