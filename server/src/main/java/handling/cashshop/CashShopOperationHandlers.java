package handling.cashshop;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.CharacterService;
import database.LoginState;
import handling.world.WorldServer;
import handling.world.helper.CharacterTransfer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;

import java.util.List;

@lombok.extern.slf4j.Slf4j
public class CashShopOperationHandlers {

    public static void onLeaveCashShop(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        CashShopServer.getInstance().getPlayerStorage().deregisterPlayer(chr);
        c.updateLoginState(LoginState.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

        try {
            WorldServer.getInstance().getChangeChannelData(new CharacterTransfer(chr), chr.getId(), c.getChannel());
            c.getSession().write(MaplePacketCreator.getChannelChange(Integer.parseInt(WorldServer.getInstance().getChannel(c.getChannel()).getPublicAddress().split(":")[1])));
        } catch (Exception ex) {
            log.info(ex.getMessage());
        } finally {
            chr.saveToDB(false, true);
            c.setPlayer(null);
        }
    }

    public static void enterCashShop(final int playerid, final MapleClient c) {
        CharacterTransfer transfer = CashShopServer.getInstance().getPlayerStorage().getPendingCharacter(playerid);
        if (transfer == null) {
            c.getSession().close();
            return;
        }
        MapleCharacter chr = MapleCharacter.reconstructChr(transfer, c, false);

        c.setPlayer(chr);
        c.loadAccountData(chr.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }

        final LoginState state = c.getAccountData().getLoginState();
        boolean allowLogin = false;
        if (LoginState.LOGIN_SERVER_TRANSITION.equals(state) || LoginState.CHANGE_CHANNEL.equals(state)) {
            if (!WorldServer.getInstance().isCharacterListConnected(CharacterService.loadCharacterNames(c.getWorld(), c.getAccountData().getId()))) {
                allowLogin = true;
            }
        }
        if (!allowLogin) {
            //   c.setPlayer(null);
            //c.getSession().close();
            //  return;
        }
        c.updateLoginState(LoginState.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        CashShopServer.getInstance().getPlayerStorage().registerPlayer(chr);
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
        return chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= eq && chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() >= use && chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= setup && chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= etc && chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= cash;
    }


    public static void doCSPackets(final MapleClient c) {
        c.getSession().write(MTSCSPacket.getCSInventory(c));
        c.getSession().write(MTSCSPacket.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MTSCSPacket.enableCSUse());
        c.getPlayer().getCashInventory().checkExpire(c);
    }
}
