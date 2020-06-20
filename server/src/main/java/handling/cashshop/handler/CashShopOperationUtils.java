package handling.cashshop.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.world.CharacterTransfer;
import handling.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

import java.util.List;

public class CashShopOperationUtils {

  public static void LeaveCS(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
    CashShopServer.getPlayerStorage().deregisterPlayer(chr);
    c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

    try {
      World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
      c.getSession().write(MaplePacketCreator.getChannelChange(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    } finally {
      chr.saveToDB(false, true);
      c.setPlayer(null);
      c.setReceiving(false);
    }
  }

  public static void EnterCS(final int playerid, final MapleClient c) {
    CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
    if (transfer == null) {
      c.getSession().close();
      return;
    }
    MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);

    c.setPlayer(chr);
    c.setAccID(chr.getAccountID());

    if (!c.CheckIPAddress()) { // Remote hack
      c.getSession().close();
      return;
    }

    final int state = c.getLoginState();
    boolean allowLogin = false;
    if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
      if (!World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()))) {
        allowLogin = true;
      }
    }
    if (!allowLogin) {
   //   c.setPlayer(null);
      //c.getSession().close();
    //  return;
    }
    c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
    CashShopServer.getPlayerStorage().registerPlayer(chr);
    c.getSession().write(MTSCSPacket.warpCS(c));
    CSUpdate(c);
  }

  public static void CSUpdate(final MapleClient c) {
    c.getSession().write(MTSCSPacket.getCSGifts(c));
    doCSPackets(c);
    c.getSession().write(MTSCSPacket.sendWishList(c.getPlayer(), false));
  }


  public static boolean haveSpace(final MapleCharacter chr, final List<IItem> items) {
    byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
    for (IItem item : items) {
      final MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
      if (invtype == MapleInventoryType.EQUIP) {
        eq++;
      } else if (invtype == MapleInventoryType.USE) {
        use++;
      } else if (invtype == MapleInventoryType.SETUP) {
        setup++;
      } else if (invtype == MapleInventoryType.ETC) {
        etc++;
      } else if (invtype == MapleInventoryType.CASH) {
        cash++;
      }
    }
    if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq || chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use || chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup || chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc || chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
      return false;
    }
    return true;
  }


  public static void doCSPackets(final MapleClient c) {
    c.getSession().write(MTSCSPacket.getCSInventory(c));
    c.getSession().write(MTSCSPacket.showNXMapleTokens(c.getPlayer()));
    c.getSession().write(MTSCSPacket.enableCSUse());
    c.getPlayer().getCashInventory().checkExpire(c);
  }
}
