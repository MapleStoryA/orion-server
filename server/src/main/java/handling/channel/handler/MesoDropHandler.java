package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class MesoDropHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.updateTick(packet.readInt());
        int meso = packet.readInt();

        if (!chr.isAlive() || (meso < 10 || meso > 50000) || (meso > chr.getMeso())) {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.gainMeso(-meso, false, true);
        chr.getMap().spawnMesoDrop(meso, chr.getPosition(), chr, chr, true, (byte) 0);
        chr.getCheatTracker().checkDrop(true);
    }
}
