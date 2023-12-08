package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import server.shops.HiredMerchant;
import tools.data.input.InPacket;
import tools.packet.PlayerShopPacket;

@lombok.extern.slf4j.Slf4j
public class RemoteStoreHandler extends AbstractMaplePacketHandler {

    public void handlePacket(InPacket packet, MapleClient c) {
        ChannelServer ch = WorldServer.getInstance().getChannel(c.getChannel());
        if (ch == null || c == null || c.getPlayer() == null) {
            return;
        }

        final HiredMerchant merchant = ch.getMerchant(c.getPlayer());
        if (merchant == null) {
            return;
        }
        if (merchant.isOwner(c.getPlayer())) {
            merchant.setOpen(false);
            merchant.removeAllVisitors((byte) 16, (byte) 0);
            c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
            return;
        }
        c.enableActions();
    }
}
