package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import server.MapleShop;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcShopHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final byte bmode = slea.readByte();
    if (chr == null) {
      return;
    }
    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    switch (bmode) {
      case 0: // Buy
      case 1: { // Sell
        final MapleShop shop = chr.getShop();
        if (shop == null) {
          return;
        }
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final short quantity = slea.readShort();
        if (quantity > ii.getSlotMax(c, itemId)) { // packet editing
          return;
        }

        if (bmode == 0) {
          shop.buy(c, itemId, quantity, slot);
        } else {
          shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
        }
        break;
      }
      case 2: {
        final MapleShop shop = chr.getShop();
        if (shop == null) {
          return;
        }
        final byte slot = (byte) slea.readShort();
        shop.recharge(c, slot);
        break;
      }
      default:
        chr.setConversation(0);
        break;
    }

  }

}
