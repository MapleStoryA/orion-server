package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.CashShopOperationHandlers;
import tools.data.input.CInPacket;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class TwinDragonEggHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        final int uniqueId = (int) packet.readLong();
        c.getSession().write(MTSCSPacket.showTwinDragonEgg(uniqueId));
        CashShopOperationHandlers.doCSPackets(c);

    }

}
