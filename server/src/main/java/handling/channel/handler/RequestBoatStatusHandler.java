package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import scripting.EventManager;
import tools.MaplePacketCreator;

@Slf4j
public class RequestBoatStatusHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient client) {
        int mapId = packet.readInt();

        if (client.getPlayer().getMap().getId() == mapId) {

            EventManager manager;
            if (mapId == 260000100 || mapId == 200000151) { // ariant
                manager = client.getChannelServer().getEventSM().getEventManager("Geenie");
            } else {
                manager = client.getChannelServer().getEventSM().getEventManager("Boats");
            }

            String docked = manager.getProperty("docked");
            if (docked != null && Boolean.valueOf(docked)) {
                client.getSession().write(MaplePacketCreator.boatPacket(0));
            } else {
                client.getSession().write(MaplePacketCreator.boatPacket(2));
            }
        }
    }
}
