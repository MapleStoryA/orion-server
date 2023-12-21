package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.MaplePacketCreator;

@Slf4j
public class CancelChairHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        int id = packet.readShort();
        MapleCharacter chr = c.getPlayer();
        if (id == -1) { // Cancel Chair
            if (chr.getChair() == 3011000) {
                chr.cancelFishingTask();
            }
            chr.setChair(0);
            c.getSession().write(MaplePacketCreator.cancelChair(-1));
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showChair(chr.getId(), 0), false);
        } else { // Use In-Map Chair
            chr.setChair(id);
            c.getSession().write(MaplePacketCreator.cancelChair(id));
        }
    }
}
