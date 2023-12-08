package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.CashShopOperationHandlers;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class CSUpdateHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        CashShopOperationHandlers.CSUpdate(c);
    }
}
