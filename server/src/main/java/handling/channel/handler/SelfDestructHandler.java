package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class SelfDestructHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int oid = packet.readInt();
        final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

        if (monster == null || !chr.isAlive() || chr.isHidden()) {
            return;
        }
        final byte selfd = monster.getStats().getSelfD();
        if (selfd != -1) {
            chr.getMap().killMonster(monster, chr, false, false, selfd);
        }
    }
}
