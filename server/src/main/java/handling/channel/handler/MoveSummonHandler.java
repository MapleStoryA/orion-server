package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.movement.MovePath;
import tools.MaplePacketCreator;

@Slf4j
public class MoveSummonHandler extends BaseMoveHandler {

    @Override
    public void handlePacket(InPacket slea, MapleClient c) {
        final int oid = slea.readInt();
        MapleCharacter chr = c.getPlayer();
        final MovePath path = new MovePath();
        path.decode(slea);
        if (chr == null) {
            return;
        }
        for (MapleSummon sum : chr.getSummons().values()) {
            if (sum.getObjectId() == oid && sum.getMovementType() != SummonMovementType.STATIONARY) {
                updatePosition(path, sum, 0);
                byte[] packet = MaplePacketCreator.moveSummon(chr.getId(), oid, path);
                chr.getMap().broadcastMessage(chr, packet, sum.getPosition());
                break;
            }
        }
    }
}
