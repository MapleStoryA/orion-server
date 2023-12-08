package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class DisplayNodeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(packet.readInt()); // From

        if (mob_from != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getNodeProperties(mob_from, chr.getMap()));
        }

    }

}
