package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.CashShopOperationHandlers;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;

@Slf4j
public class CSUpdateHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        CashShopOperationHandlers.CSUpdate(c);
    }
}
