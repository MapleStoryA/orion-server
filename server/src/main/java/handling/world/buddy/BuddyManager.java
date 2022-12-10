package handling.world.buddy;

import client.MapleCharacter;
import database.DatabaseConnection;
import handling.channel.handler.BuddyListModifyHandler;
import handling.world.WorldServer;
import handling.world.helper.FindCommand;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class BuddyManager {

    private static final List<BuddyInvitedEntry> invited = new LinkedList<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static long lastPruneTime;

    public static boolean canPrune(long now) { // Expires every 10 minutes, Checks every 20 minutes
        return (lastPruneTime + (20 * 60 * 1000)) < now;
    }

    public static void prepareRemove() {
        final long now = System.currentTimeMillis();
        lastPruneTime = now;
        Iterator<BuddyInvitedEntry> itr = invited.iterator();
        BuddyInvitedEntry inv;
        while (itr.hasNext()) {
            inv = itr.next();
            if (now >= inv.expiration) {
                itr.remove();
            }
        }
    }

    public static boolean isBuddyPending(final BuddyInvitedEntry inv) {
        lock.readLock().lock();
        try {
            if (invited.contains(inv)) {
                return true;
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public static MapleBuddyList.BuddyAddResult requestBuddyAdd(String addName, MapleCharacter inviter) {
        int ch = FindCommand.findChannel(addName);
        if (ch > 0) {
            final MapleCharacter addChar = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterByName(addName);
            if (addChar != null) {
                final MapleBuddyList buddylist = addChar.getBuddyList();
                if (buddylist.isFull()) {
                    return MapleBuddyList.BuddyAddResult.BUDDYLIST_FULL;
                }
                if (buddylist.contains(inviter.getId())) {
                    return MapleBuddyList.BuddyAddResult.ALREADY_ON_LIST;
                }
                lock.writeLock().lock();
                try {
                    invited.add(new BuddyInvitedEntry(addChar.getName(), inviter.getId()));
                } finally {
                    lock.writeLock().unlock();
                }
                addChar.getClient().getSession().write(MaplePacketCreator.requestBuddylistAdd(inviter.getId(), inviter.getName(), inviter.getLevel(), inviter.getJob().getId()));
                return MapleBuddyList.BuddyAddResult.OK;
            }
        }
        return MapleBuddyList.BuddyAddResult.NOT_FOUND;
    }

    public static Pair<MapleBuddyList.BuddyAddResult, String> acceptToInvite(MapleCharacter chr, int inviterCid) {
        Iterator<BuddyInvitedEntry> itr = invited.iterator();
        while (itr.hasNext()) {
            BuddyInvitedEntry inv = itr.next();
            if (inviterCid == inv.inviter && chr.getName().equalsIgnoreCase(inv.name)) {
                itr.remove();
                if (chr.getBuddyList().isFull()) {
                    return new Pair<>(MapleBuddyList.BuddyAddResult.BUDDYLIST_FULL, null);
                }
                final int ch = FindCommand.findChannel(inviterCid);
                if (ch > 0) { // Inviter is online
                    final MapleCharacter addChar = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(inviterCid);
                    if (addChar == null) {
                        return new Pair<>(MapleBuddyList.BuddyAddResult.NOT_FOUND, null);
                    }
                    addChar.getBuddyList().put(new BuddyListEntry(chr.getName(), chr.getId(), "Default Group", chr.getClient().getChannel()));
                    addChar.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.ADD, addChar.getBuddyList().getBuddies()));

                    chr.getBuddyList().put(new BuddyListEntry(addChar.getName(), addChar.getId(), "Default Group", ch));
                    chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.ADD, chr.getBuddyList().getBuddies()));

                    return new Pair<>(MapleBuddyList.BuddyAddResult.OK, addChar.getName());
                }
            }
        }
        return new Pair<>(MapleBuddyList.BuddyAddResult.NOT_FOUND, null);
    }

    public static String denyToInvite(MapleCharacter chr, int inviterCid) {
        Iterator<BuddyInvitedEntry> itr = invited.iterator();
        while (itr.hasNext()) {
            BuddyInvitedEntry inv = itr.next();
            if (inviterCid == inv.inviter && chr.getName().equalsIgnoreCase(inv.name)) {
                itr.remove();
                final int ch = FindCommand.findChannel(inviterCid);
                if (ch > 0) { // Inviter is online
                    final MapleCharacter addChar = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(inviterCid);
                    if (addChar == null) {
                        return "You have denied the buddy request.";
                    }
                    addChar.dropMessage(5, chr.getName() + " have denied request to be your buddy.");
                    return "You have denied the buddy request from '" + addChar.getName() + "'";
                }
            }
        }
        return "You have denied the buddy request.";// We don't know the name..
    }

    public static MapleBuddyList.BuddyDelResult DeleteBuddy(MapleCharacter chr, int deleteCid) {
        final BuddyListEntry myBlz = chr.getBuddyList().get(deleteCid);
        if (myBlz == null) {
            return MapleBuddyList.BuddyDelResult.NOT_ON_LIST;
        }
        final int ch = FindCommand.findChannel(deleteCid);
        if (ch == -20 || ch == -10) {
            return MapleBuddyList.BuddyDelResult.IN_CASH_SHOP;
        }
        if (ch > 0) { // Buddy is online
            final MapleCharacter delChar = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(deleteCid);
            if (delChar == null) {
                final int ch_ = FindCommand.findChannel(deleteCid); // Re-attempt to find again
                if (ch_ == -20 || ch_ == -10) {
                    return MapleBuddyList.BuddyDelResult.IN_CASH_SHOP;
                }
                if (ch_ <= 0) {
                    final byte result = deleteOfflineBuddy(deleteCid, chr.getId()); // Execute SQL query.
                    if (result == -1) {
                        return MapleBuddyList.BuddyDelResult.ERROR;
                    }
                    chr.getBuddyList().remove(deleteCid);
                    chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddyList().getBuddies()));
                    return MapleBuddyList.BuddyDelResult.OK;
                }
            }
            delChar.getBuddyList().remove(chr.getId());
            delChar.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, delChar.getBuddyList().getBuddies()));
            delChar.dropMessage(5, "Your buddy relationship with '" + chr.getName() + "' has ended.");

            chr.getBuddyList().remove(deleteCid);
            chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddyList().getBuddies()));
            return MapleBuddyList.BuddyDelResult.OK;
        } else { // Buddy is offline
            final byte result = deleteOfflineBuddy(deleteCid, chr.getId()); // Execute SQL query.
            if (result == -1) {
                return MapleBuddyList.BuddyDelResult.ERROR;
            }
            chr.getBuddyList().remove(deleteCid);
            chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddyList().getBuddies()));
            return MapleBuddyList.BuddyDelResult.OK;
        }
    }

    public static byte deleteOfflineBuddy(final int delId, final int myId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE from `buddyentries` WHERE `owner` = ? AND `buddyid` = ?");
            ps.setInt(1, delId);
            ps.setInt(2, myId);
            ps.executeUpdate();
            ps.close();
            // As a safe check
            ps = con.prepareStatement("DELETE from `buddyentries` WHERE `owner` = ? AND `buddyid` = ?");
            ps.setInt(1, myId);
            ps.setInt(2, delId);
            ps.executeUpdate();
            ps.close();
            return 0;
        } catch (SQLException e) {
            log.info("Error deleting buddy id " + myId + ", Owner Id " + delId + " Reason: " + e);
            return -1;
        }
    }

    public static void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) {
        for (int characterId : recipientCharacterIds) {
            int ch = FindCommand.findChannel(characterId);
            if (ch > 0) {
                MapleCharacter chr = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(characterId);
                if (chr != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
                }
            }
        }
    }

    private static void updateBuddies(int characterId, int channel, int[] buddies, boolean offline, int gmLevel, boolean isHidden) {
        for (int buddy : buddies) {
            int ch = FindCommand.findChannel(buddy);
            if (ch > 0) {
                MapleCharacter chr = WorldServer.getInstance().getChannel(ch).getPlayerStorage().getCharacterById(buddy);
                if (chr != null) {
                    BuddyListEntry ble = chr.getBuddyList().get(characterId);
                    if (ble != null) {
                        int mcChannel;
                        if (offline || (isHidden && chr.getGMLevel() < gmLevel)) {
                            ble.setChannel(-1);
                            mcChannel = -1;
                        } else {
                            ble.setChannel(channel);
                            mcChannel = channel - 1;
                        }
                        chr.getBuddyList().put(ble);
                        chr.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(ble.getCharacterId(), mcChannel));
                    }
                }
            }
        }
    }

    public static void loggedOn(String name, int characterId, int channel, int[] buddies, int gmLevel, boolean isHidden) {
        updateBuddies(characterId, channel, buddies, false, gmLevel, isHidden);
    }

    public static void loggedOff(String name, int characterId, int channel, int[] buddies, int gmLevel, boolean isHidden) {
        updateBuddies(characterId, channel, buddies, true, gmLevel, isHidden);
    }
}
