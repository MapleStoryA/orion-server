package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.maps.SavedLocationType;
import tools.MaplePacketCreator;

@Slf4j
public class EnterMTSHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        int map = 910000000;
        if (c.getPlayer().getLevel() < 10) {
            c.getPlayer().dropMessage(5, "Characters whose level is below Lv. 10 cannot use the market button.");
            c.enableActions();
            return;
        }
        if (c.getPlayer().getMapId() == map) {
            c.getSession().write(MaplePacketCreator.enableActions());
        } else {
            c.getPlayer()
                    .getSavedLocations()
                    .saveLocation(SavedLocationType.FREE_MARKET, c.getPlayer().getMapId());
            c.getPlayer().changeMap(map, "out00");
        }
    }
}
