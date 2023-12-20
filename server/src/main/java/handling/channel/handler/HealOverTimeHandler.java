package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerStats;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class HealOverTimeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        chr.updateTick(packet.readInt());
        if (packet.available() >= 8) {
            packet.skip(4);
        }
        int healHP = packet.readShort();
        int healMP = packet.readShort();

        final PlayerStats stats = chr.getStat();

        if (stats.getHp() <= 0) {
            return;
        }

        if (healHP != 0) { // && chr.canHP(now + 1000)) {
            if (healHP > stats.getHealHP()) {
                healHP = (int) stats.getHealHP();
            }
            chr.addHP(healHP);
        }
        if (healMP != 0) { // && chr.canMP(now + 1000)) {
            if (healMP > stats.getHealMP()) {
                healMP = (int) stats.getHealMP();
            }
            chr.addMP(healMP);
        }
    }
}
