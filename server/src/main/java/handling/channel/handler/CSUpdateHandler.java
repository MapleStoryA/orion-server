package handling.channel.handler;

import client.MapleClient;
import handling.cashshop.CashShopOperationHandlers;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class CSUpdateHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        CashShopOperationHandlers.CSUpdate(c);
    }
}
