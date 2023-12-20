package handling.channel.handler.admin;

import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

/**
 * @Author Arnah @Website http://Vertisy.ca/
 *
 * @since Aug 24, 2017
 */
@lombok.extern.slf4j.Slf4j
public class AdminShopRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.enableActions();
    }
}
