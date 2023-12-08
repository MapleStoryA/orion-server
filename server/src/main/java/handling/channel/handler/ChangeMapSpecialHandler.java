package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class ChangeMapSpecialHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
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
