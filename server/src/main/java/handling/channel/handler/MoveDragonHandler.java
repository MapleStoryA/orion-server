package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleMap;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class MoveDragonHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MovePath path = new MovePath();
        path.decode(slea);
        if (chr != null && chr.getDragon() != null) {
            final Point pos = chr.getDragon().getPosition();
            updatePosition(path, chr.getDragon(), 0);
            MapleMap map = chr.getMap();
            if (!chr.isHidden()) {
                map.broadcastMessage(chr, MaplePacketCreator.moveDragon(chr.getDragon(), path), chr.getPosition());
            } else {
                map.broadcastGMMessage(chr, MaplePacketCreator.moveDragon(chr.getDragon(), path), false);
            }
        }

    }

}
