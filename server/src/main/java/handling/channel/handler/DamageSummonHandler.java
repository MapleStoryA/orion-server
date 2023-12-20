package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import java.util.Iterator;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class DamageSummonHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.skip(4);
        final int unkByte = packet.readByte();
        final int damage = packet.readInt();
        final int monsterIdFrom = packet.readInt();
        // slea.readByte(); // stance
        MapleCharacter chr = c.getPlayer();
        final Iterator<MapleSummon> iter = chr.getSummons().values().iterator();
        MapleSummon summon;

        while (iter.hasNext()) {
            summon = iter.next();
            if (summon.isPuppet() && summon.getOwnerId() == chr.getId()) { // We
                // can
                // only
                // have
                // one
                // puppet(AFAIK
                // O.O)
                // so
                // this
                // check
                // is
                // safe.
                summon.addHP((short) -damage);
                if (summon.getHP() <= 0) {
                    chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
                chr.getMap()
                        .broadcastMessage(
                                chr,
                                MaplePacketCreator.damageSummon(
                                        chr.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom),
                                summon.getPosition());
                break;
            }
        }
    }
}
