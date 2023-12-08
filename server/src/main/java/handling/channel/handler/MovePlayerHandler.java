package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import java.awt.*;
import server.maps.MapleMap;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class MovePlayerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        packet.skip(29);
        final MovePath path = new MovePath();
        path.decode(packet);
        if (path != null && c.getPlayer().getMap() != null) {
            final MapleMap map = c.getPlayer().getMap();
            if (chr.isHidden()) {
                c.getPlayer()
                        .getMap()
                        .broadcastGMMessage(
                                chr, MaplePacketCreator.movePlayer(chr.getId(), path), false);
            } else {
                c.getPlayer()
                        .getMap()
                        .broadcastMessage(
                                c.getPlayer(),
                                MaplePacketCreator.movePlayer(chr.getId(), path),
                                false);
            }
            updatePosition(path, chr, 0);
            final Point pos = chr.getPosition();
            map.movePlayer(chr, pos);
        }
    }
}
