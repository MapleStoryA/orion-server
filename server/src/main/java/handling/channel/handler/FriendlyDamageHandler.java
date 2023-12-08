package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.Randomizer;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class FriendlyDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        final MapleMonster mobfrom = map.getMonsterByOid(packet.readInt());
        packet.skip(4); // Player ID
        final MapleMonster mobto = map.getMonsterByOid(packet.readInt());

        if (mobfrom != null && mobto != null && mobto.getStats().isFriendly()) {
            final int damage = (mobto.getStats().getLevel() * Randomizer.nextInt(99)) / 2; // Temp
            mobto.damage(chr, damage, true);
            MobHandlerUtils.checkShammos(chr, mobto, map);
        }
    }
}
