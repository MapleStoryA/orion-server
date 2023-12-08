package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import handling.world.buddy.BuddyInvitedEntry;
import handling.world.buddy.BuddyListEntry;
import handling.world.buddy.BuddyManager;
import handling.world.buddy.MapleBuddyList;
import handling.world.buddy.MapleBuddyList.BuddyAddResult;
import handling.world.buddy.MapleBuddyList.BuddyDelResult;
import handling.world.helper.FindCommand;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class BuddyListModifyHandler extends AbstractMaplePacketHandler {

    public static final byte // Buddy send Ops
            UPDATE = 0x07,
            INVITE_RECEIVED = 0x09,
            ADD = 0x0A,
            YOUR_LIST_FULL = 0x0B, // Your buddy list is full.
            THEIR_LIST_FULL = 0x0C, // The user's buddy list is full.
            ALREADY_ON_LIST = 0x0D, // That character is already registered as your buddy.
            NO_GM_INVITES = 0x0E, // Gamemaster is not available as a buddy.
            NON_EXISTANT = 0x0F, // The character is not registered
            DENY_ERROR = 0x10, // The request was denied due to an unknown error.
            REMOVE = 0x12,
            BUDDY_LOGGED_IN = 0x14,
            CAPACITY_CHANGE = 0x15,
            ALREADY_FRIEND_REQUEST = 0x17; // You have already made the Friend Request. Please try again later.

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final MapleBuddyList buddylist = c.getPlayer().getBuddyList();
        switch (packet.readByte()) {
            case 1: // Invite / Modify Buddy List
                final String addName = packet.readMapleAsciiString();
                final String groupName = packet.readMapleAsciiString();
                if (addName.length() > 13 || groupName.length() > 16) {
                    return;
                }
                final BuddyListEntry ble = buddylist.get(addName);
                if (ble != null) { // Friend is in registered list already
                    if (ble.getGroup().equals(groupName)) {
                        c.getSession().write(MaplePacketCreator.buddylistMessage(ALREADY_ON_LIST));
                    } else { // Just update it.
                        ble.setGroup(groupName);
                        c.getSession().write(MaplePacketCreator.updateBuddylist(UPDATE, buddylist.getBuddies()));
                    }
                    return;
                }
                boolean isOnPending = BuddyManager.isBuddyPending(
                        new BuddyInvitedEntry(addName, c.getPlayer().getId()));
                if (isOnPending) { // Already Added
                    c.getSession().write(MaplePacketCreator.buddylistMessage(ALREADY_FRIEND_REQUEST));
                    return;
                }
                if (buddylist.isFull()) {
                    c.getSession().write(MaplePacketCreator.buddylistMessage(YOUR_LIST_FULL));
                    return;
                }
                final int channel = FindCommand.findChannel(addName);
                if (channel <= 0) { // Not found
                    c.getPlayer().dropMessage(5, "The character is not registered or not in game.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                final MapleCharacter otherChar = WorldServer.getInstance()
                        .getChannel(channel)
                        .getPlayerStorage()
                        .getCharacterByName(addName);
                if (!otherChar.isGameMaster() || c.getPlayer().isGameMaster()) {
                    if (otherChar.getBuddyList().isFull()) {
                        c.getSession().write(MaplePacketCreator.buddylistMessage(THEIR_LIST_FULL));
                        return;
                    }
                    // This function will do the adding player into other's list + Add into pending
                    // list
                    final BuddyAddResult buddyAddResult = BuddyManager.requestBuddyAdd(addName, c.getPlayer());
                    if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                        c.getSession().write(MaplePacketCreator.buddylistMessage(THEIR_LIST_FULL));
                        return;
                    }
                    if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST) {
                        c.getPlayer()
                                .dropMessage(
                                        5,
                                        "An error has occured. Please ask the player to re-add you"
                                                + " as a buddy again.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (buddyAddResult == BuddyAddResult.NOT_FOUND) {
                        c.getPlayer().dropMessage(5, "The character is not registered or not in game.");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    c.getPlayer().dropMessage(1, "You have invited '" + addName + "' into your Buddy List.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                } else {
                    c.getSession().write(MaplePacketCreator.buddylistMessage(NO_GM_INVITES));
                }
                break;
            case 2: // Accept
                final int otherCid = packet.readInt();
                boolean isOnPending_ = BuddyManager.isBuddyPending(
                        new BuddyInvitedEntry(c.getPlayer().getName(), otherCid));
                if (!isOnPending_) {
                    c.getSession().write(MaplePacketCreator.buddylistMessage(DENY_ERROR));
                    return;
                }
                final Pair<BuddyAddResult, String> bal = BuddyManager.acceptToInvite(c.getPlayer(), otherCid);
                if (bal.getLeft() == BuddyAddResult.NOT_FOUND) {
                    c.getSession().write(MaplePacketCreator.buddylistMessage(DENY_ERROR));
                    return;
                }
                if (bal.getLeft() == BuddyAddResult.BUDDYLIST_FULL) {
                    c.getSession().write(MaplePacketCreator.buddylistMessage(YOUR_LIST_FULL));
                    return;
                }
                c.getPlayer().dropMessage(5, "Congratulations, you are now friends with '" + bal.getRight() + "'");
                if (buddylist.getBuddies().size() >= 20) {
                    c.getPlayer().getFinishedAchievements().finishAchievement(c.getPlayer(), 26);
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            case 3: // Delete / Deny
                final int otherCID = packet.readInt();
                boolean isInvited = BuddyManager.isBuddyPending(
                        new BuddyInvitedEntry(c.getPlayer().getName(), otherCID));
                if (isInvited) {
                    c.getPlayer().dropMessage(5, BuddyManager.denyToInvite(c.getPlayer(), otherCID));
                    c.getSession().write(MaplePacketCreator.updateBuddylist(REMOVE, buddylist.getBuddies()));
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                final BuddyListEntry blz = buddylist.get(otherCID);
                if (blz == null) { // Not on buddy list, but tries to delete
                    c.getPlayer().dropMessage(5, "An error has occured. The character is not on your buddy" + " list.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                final BuddyDelResult bdr = BuddyManager.DeleteBuddy(c.getPlayer(), otherCID);
                if (bdr == BuddyDelResult.NOT_ON_LIST) {
                    c.getPlayer().dropMessage(5, "An error has occured. The character is not on your buddy" + " list.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (bdr == BuddyDelResult.IN_CASH_SHOP) {
                    c.getPlayer().dropMessage(5, "The character is currently in the Cash Shop.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (bdr == BuddyDelResult.ERROR) { // SQL Exception
                    c.getPlayer().dropMessage(5, "An error has occured. Please contact one of the GameMasters.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                c.getPlayer().dropMessage(5, "Your buddy relationship with the deleted person has ended.");
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            default:
                log.info("Unknown buddylist action: " + packet);
                break;
        }
    }
}
