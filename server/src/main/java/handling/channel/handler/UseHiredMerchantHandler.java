package handling.channel.handler;

import client.MapleClient;
import handling.channel.handler.utils.HiredMerchantHandlerUtils;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.packet.PlayerShopPacket;

@Slf4j
public class UseHiredMerchantHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        //		slea.readInt(); // TimeStamp

        if (c.getPlayer().getMap().allowPersonalShop()) {
            final byte state = HiredMerchantHandlerUtils.checkExistance(
                    c.getPlayer().getAccountID(), c.getPlayer().getId());

            switch (state) {
                case 1:
                    c.getPlayer().dropMessage(1, "Please claim your items from Fredrick first.");
                    break;
                case 0:
                    boolean merch =
                            WorldServer.getInstance().hasMerchant(c.getPlayer().getAccountID());
                    if (!merch) {
                        if (c.getChannelServer().isShutdown()) {
                            c.getPlayer().dropMessage(1, "The server is about to shut down.");
                            return;
                        }
                        c.getSession().write(PlayerShopPacket.sendTitleBox());
                    } else {
                        c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
                    }
                    break;
                default:
                    c.getPlayer().dropMessage(1, "An unknown error occured.");
                    break;
            }
        } else {
            c.getSession().close();
        }
    }
}
