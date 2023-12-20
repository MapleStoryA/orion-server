package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class DisplayNodeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(packet.readInt()); // From

        if (mob_from != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getNodeProperties(mob_from, chr.getMap()));
        }
    }
}
