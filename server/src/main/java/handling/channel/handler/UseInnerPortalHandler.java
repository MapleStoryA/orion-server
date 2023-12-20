package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import java.awt.*;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.MaplePortal;

@lombok.extern.slf4j.Slf4j
public class UseInnerPortalHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        packet.skip(1);
        if (chr == null) {
            return;
        }
        final MaplePortal portal = chr.getMap().getPortal(packet.readMapleAsciiString());
        final int toX = packet.readShort();
        final int toY = packet.readShort();

        if (portal == null) {
            return;
        } else if (portal.getPosition().distanceSq(chr.getPosition()) > 22500) {
            chr.getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL);
        }
        chr.getMap().movePlayer(chr, new Point(toX, toY));
        chr.checkFollow();
    }
}
