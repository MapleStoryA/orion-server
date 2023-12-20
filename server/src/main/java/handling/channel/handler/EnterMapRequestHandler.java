package handling.channel.handler;

import client.MapleClient;
import java.util.Map.Entry;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.MapleDragon;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class EnterMapRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {

        if (c.getPlayer().isHidden() && c.getPlayer().isGameMaster()
                || c.getPlayer().getMap() == null) {
            c.enableActions();
            return;
        }
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnPlayerMapobject(c.getPlayer()));
        MapleDragon dragon = c.getPlayer().getDragon();
        if (dragon != null) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnDragon(dragon));
        }

        for (Entry<Integer, MapleSummon> summon : c.getPlayer().getSummons().entrySet()) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnSummon(summon.getValue(), false));
        }
    }
}
