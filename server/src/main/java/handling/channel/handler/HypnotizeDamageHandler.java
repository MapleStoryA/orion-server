package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.life.MapleMonster;

@lombok.extern.slf4j.Slf4j
public class HypnotizeDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(packet.readInt()); // From
        packet.skip(4); // Player ID
        final int to = packet.readInt(); // mobto
        packet.skip(1); // Same as player damage, -1 = bump, integer = skill ID
        final int damage = packet.readInt();
        // slea.skip(1); // Facing direction
        // slea.skip(4); // Some type of pos, damage display, I think

        final MapleMonster mob_to = chr.getMap().getMonsterByOid(to);

        if (mob_from != null && mob_to != null && mob_to.getStats().isFriendly()) { // temp
            // for
            // now
            if (damage > 30000) {
                return;
            }
            mob_to.damage(chr, damage, true);
            MobHandlerUtils.checkShammos(chr, mob_to, chr.getMap());
        }
    }
}
