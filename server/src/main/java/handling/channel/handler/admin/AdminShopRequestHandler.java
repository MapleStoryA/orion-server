package handling.channel.handler.admin;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

/**
 * @Author Arnah @Website http://Vertisy.ca/
 *
 * @since Aug 24, 2017
 */
@lombok.extern.slf4j.Slf4j
public class AdminShopRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        c.enableActions();
    }
}
