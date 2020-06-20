package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.HiredMerchantHandlerUtils;
import handling.world.World;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PlayerShopPacket;

public class UseHiredMerchantHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
//		slea.readInt(); // TimeStamp

    if (c.getPlayer().getMap().allowPersonalShop()) {
      final byte state = HiredMerchantHandlerUtils.checkExistance(c.getPlayer().getAccountID(), c.getPlayer().getId());

      switch (state) {
        case 1:
          c.getPlayer().dropMessage(1, "Please claim your items from Fredrick first.");
          break;
        case 0:
          boolean merch = World.hasMerchant(c.getPlayer().getAccountID());
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
