package handling.channel.handler;

import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class ViewAllCharPacket extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        // c.enableActions();

    }
}
