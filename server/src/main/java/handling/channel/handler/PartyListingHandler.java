package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class PartyListingHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().dropMessage(1, "The Party Listing is currently unavailable.");
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
