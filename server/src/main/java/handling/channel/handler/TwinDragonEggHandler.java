package handling.channel.handler;

import client.MapleClient;
import handling.cashshop.CashShopOperationHandlers;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class TwinDragonEggHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final int uniqueId = (int) packet.readLong();
        c.getSession().write(MTSCSPacket.showTwinDragonEgg(uniqueId));
        CashShopOperationHandlers.doCSPackets(c);
    }
}
