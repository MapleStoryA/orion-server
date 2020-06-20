package handling.world;

import client.*;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory;
import client.status.MonsterStatusEffect;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.channel.handler.BuddyListModifyHandler;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.buddy.BuddyInvitedEntry;
import handling.world.buddy.BuddyListEntry;
import handling.world.buddy.MapleBuddyList;
import handling.world.buddy.MapleBuddyList.BuddyAddResult;
import handling.world.buddy.MapleBuddyList.BuddyDelResult;
import handling.world.expedition.ExpeditionType;
import handling.world.expedition.MapleExpedition;
import handling.world.guild.*;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import server.MapleInventoryManipulator;
import server.Timer.WorldTimer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import tools.CollectionUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.packet.MapleUserPackets;
import tools.packet.PetPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class World {

  private static long startHotTime = 0;
  public static final long HOT_TIME_INTERVAL = 60000; // 1 minute
  private static final List<String> obtainedHotTime = new ArrayList<>();
  private static final int CHANNELS_PER_THREAD = 3;
  private static int playeronlinerecord;

  public static boolean startHotTime() {
    if (startHotTime > 0) { // Already started
      return false;
    }
    startHotTime = System.currentTimeMillis();
    obtainedHotTime.clear();
    return true;
  }

  public static void endHotTime() {
    startHotTime = 0;
  }

  public static String getHotTimeList() {
    final StringBuilder sb = new StringBuilder();
    for (String xx : obtainedHotTime) {
      sb.append(MapleCharacterUtil.makeMapleReadable(xx));
      sb.append(", ");
    }
    return sb.toString();
  }

  public static int getHotTimeLeft(final long now) {
    if (!isHotTimeStarted(now)) {
      return 0;
    }
    return (int) (((startHotTime + HOT_TIME_INTERVAL) - now) / 1000);
  }

  public static boolean isHotTimeStarted(final long now) {
    if (startHotTime + HOT_TIME_INTERVAL < now) {
      startHotTime = 0;
      return false;
    }
    return startHotTime > 0 && (startHotTime + HOT_TIME_INTERVAL >= now);
  }

  public static boolean canTakeHotTime(final String name, final long now) {
    if (startHotTime + HOT_TIME_INTERVAL < now) {
      startHotTime = 0;
      return false;
    }
    return !obtainedHotTime.contains(name);
  }

  public static void init() {
    World.Find.findChannel(0);
    World.Alliance.lock.toString();
    World.Messenger.getMessenger(0);
    World.Party.getParty(0);
  }

  public static int getPlayerOnlineRecord() {
    return playeronlinerecord;
  }

  public static void updatePlayerOnlineRecord(int newval) {
    playeronlinerecord = newval;
  }


  public static String getStatus() throws Exception {
    StringBuilder ret = new StringBuilder();
    int totalUsers = 0;
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      ret.append("Channel ");
      ret.append(cs.getChannel());
      ret.append(": ");
      int channelUsers = cs.getConnectedClients();
      totalUsers += channelUsers;
      ret.append(channelUsers);
      ret.append(" users\n");
    }
    ret.append("Total users online: ");
    ret.append(totalUsers);
    ret.append("\n");
    return ret.toString();
  }

  public static Map<Integer, Integer> getConnected() {
    Map<Integer, Integer> ret = new HashMap<>();
    int total = 0;
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      int curConnected = cs.getConnectedClients();
      ret.put(cs.getChannel(), curConnected);
      total += curConnected;
    }
    ret.put(0, total);
    return ret;
  }

  public static List<CheaterData> getCheaters() {
    List<CheaterData> allCheaters = new ArrayList<>();
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      allCheaters.addAll(cs.getCheaters());
    }
    Collections.sort(allCheaters);
    return CollectionUtil.copyFirst(allCheaters, 20);
  }

  public static List<CheaterData> getReports() {
    List<CheaterData> allCheaters = new ArrayList<>();
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      allCheaters.addAll(cs.getReports());
    }
    Collections.sort(allCheaters);
    return CollectionUtil.copyFirst(allCheaters, 20);
  }

  public static boolean isConnected(String charName) {
    return Find.findChannel(charName) > 0;
  }

  public static void toggleMegaphoneMuteState() {
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      cs.toggleMegaphoneMuteState();
    }
  }

  public static void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) {
    getStorage(toChannel).registerPendingPlayer(Data, characterid);
  }

  public static boolean isCharacterListConnected(List<String> charName) {
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      for (final String c : charName) {
        if (cs.getPlayerStorage().getCharacterByName(c) != null) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasMerchant(int accountID) {
    for (ChannelServer cs : ChannelServer.getAllInstances()) {
      if (cs.containsMerchant(accountID)) {
        return true;
      }
    }
    return false;
  }


  public static PlayerStorage getStorage(int channel) {
    if (channel == -10) {
      return CashShopServer.getPlayerStorage();
    }
    return ChannelServer.getInstance(channel).getPlayerStorage();
  }

  public static class Party {

    private static Map<Integer, MapleParty> parties = new HashMap<>();
    private static Map<Integer, MapleExpedition> expeditions = new HashMap<>();
    private static final AtomicInteger runningPartyId = new AtomicInteger(1);
    private static final AtomicInteger runningExpedId = new AtomicInteger(1);

    static {
      try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE `characters` SET `party` = -1")) {
        ps.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    public static void partyChat(int partyid, String chattext, String namefrom) {
      partyChat(partyid, chattext, namefrom, 1);
    }

    public static void expedChat(int expedId, String chattext, String namefrom) {
      MapleExpedition ex = getExped(expedId);
      if (ex == null) {
        return;
      }
      for (Integer i : ex.getParties()) {
        partyChat(i, chattext, namefrom, 6);
      }
    }

    public static void partyChat(int partyid, String chattext, String namefrom, int mode) {
      MapleParty party = getParty(partyid);
      if (party == null) {
        return;
      }

      for (MaplePartyCharacter partychar : party.getMembers()) {
        int ch = Find.findChannel(partychar.getName());
        if (ch > 0) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
          if (chr != null && !chr.getName().equalsIgnoreCase(namefrom)) { //Extra check just in case
            chr.getClient().getSession().write(MaplePacketCreator.multiChat(namefrom, chattext, mode));
          }
        }
      }
    }

    public static void expedPacket(int expedId, byte[] packet, MaplePartyCharacter exception) {
      MapleExpedition ex = getExped(expedId);
      if (ex == null) {
        return;
      }
      for (Integer i : ex.getParties()) {
        partyPacket(i, packet, exception);
      }
    }

    public static void partyPacket(int partyid, byte[] packet, MaplePartyCharacter exception) {
      MapleParty party = getParty(partyid);
      if (party == null) {
        return;
      }

      for (MaplePartyCharacter partychar : party.getMembers()) {
        int ch = World.Find.findChannel(partychar.getName());
        if (ch > 0 && (exception == null || partychar.getId() != exception.getId())) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
          if (chr != null) {
            chr.getClient().getSession().write(packet);
          }
        }
      }
    }

    public static void expedMessage(int expedId, String chattext) {
      MapleExpedition ex = getExped(expedId);
      if (ex == null) {
        return;
      }
      for (Integer i : ex.getParties()) {
        partyMessage(i, chattext);
      }
    }

    public static void partyMessage(int partyid, String chattext) {
      MapleParty party = getParty(partyid);
      if (party == null) {
        return;
      }

      for (MaplePartyCharacter partychar : party.getMembers()) {
        int ch = World.Find.findChannel(partychar.getName());
        if (ch > 0) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
          if (chr != null) {
            chr.dropMessage(5, chattext);
          }
        }
      }
    }

    public static void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) {
      MapleParty party = getParty(partyid);
      if (party == null) {
        return;
      }

      int oldExped = party.getExpeditionId();
      int oldInd = -1;
      if (oldExped > 0) {
        MapleExpedition exped = getExped(oldExped);
        if (exped != null) {
          oldInd = exped.getIndex(partyid);
        }
      }
      switch (operation) {
        case JOIN:
          party.addMember(target);
          break;
        case EXPEL:
        case LEAVE:
        case MOVE_MEMBER:
          party.removeMember(target);
          break;
        case DISBAND:
        case DISBAND_IN_EXPEDITION:
          disbandParty(partyid, operation == PartyOperation.DISBAND_IN_EXPEDITION);
          break;
        case SILENT_UPDATE:
        case LOG_ONOFF:
          party.updateMember(target);
          break;
        case CHANGE_LEADER:
        case CHANGE_LEADER_DC:
          party.setLeader(target);
          break;
        default:
          throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
      }

      if (operation == PartyOperation.LEAVE || operation == PartyOperation.MOVE_MEMBER || operation == PartyOperation.EXPEL) {
        int chz = World.Find.findChannel(target.getName());
        if (chz > 0) {
          MapleCharacter chr = World.getStorage(chz).getCharacterByName(target.getName());
          if (chr != null) {
            chr.setParty(null);
            chr.getClient().getSession().write(MapleUserPackets.updateParty(chr.getClient().getChannel(), party, operation, target));
            if (oldExped > 0 && operation != PartyOperation.MOVE_MEMBER) {
              // Broadcast to self
              chr.getClient().getSession().write(MapleUserPackets.removeExpedition(operation == PartyOperation.LEAVE ? 61 : 63));
              // Broadcast to remaining member
              expedPacket(oldExped, MapleUserPackets.expeditionNotice(operation == PartyOperation.LEAVE ? 60 : 62, chr.getName()), new MaplePartyCharacter(chr));
            }
          }
        }
      }
      if (party.getMembers().size() <= 0) {
        disbandParty(partyid, operation == PartyOperation.DISBAND_IN_EXPEDITION);
      }
      for (MaplePartyCharacter partychar : party.getMembers()) {
        if (partychar == null) {
          continue;
        }
        int ch = Find.findChannel(partychar.getName());
        if (ch > 0) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
          if (chr != null) {
            if (operation == PartyOperation.DISBAND || operation == PartyOperation.DISBAND_IN_EXPEDITION) {
              chr.setParty(null);
              if (oldExped > 0 && oldInd > -1) {
                // Broadcast to self ("You have left the expedition")
                chr.getClient().getSession().write(MapleUserPackets.removeExpedition(61));
                // Broadcast to others
                expedPacket(oldExped, MapleUserPackets.expeditionNotice(60, chr.getName()), new MaplePartyCharacter(chr));
              }
            } else {
              chr.setParty(party);
            }
            chr.getClient().getSession().write(MapleUserPackets.updateParty(chr.getClient().getChannel(), party, operation, target));
          }
        }
      }
      if (oldExped > 0 && oldInd > -1 /*&& operation != PartyOperation.DISBAND && operation != PartyOperation.EXPEL && operation != PartyOperation.LEAVE*/) {
        expedPacket(oldExped, MapleUserPackets.expeditionUpdate(oldInd, party), (operation == PartyOperation.LOG_ONOFF || operation == PartyOperation.SILENT_UPDATE) ? target : null);
      }
    }

    public static MapleParty createParty(MaplePartyCharacter chrfor) {
      int partyid = runningPartyId.getAndIncrement();
      MapleParty party = new MapleParty(partyid, chrfor);
      parties.put(party.getId(), party);
      return party;
    }

    public static MapleParty createParty(MaplePartyCharacter chrfor, int expedId) {
      ExpeditionType ex = ExpeditionType.getById(expedId);
      int partyid = runningPartyId.getAndIncrement();
      int expid = runningExpedId.getAndIncrement();
      MapleParty party = new MapleParty(partyid, chrfor, ex != null ? expid : -1);
      parties.put(party.getId(), party);
      if (ex != null) {
        MapleExpedition exp = new MapleExpedition(ex, chrfor.getId(), party.getExpeditionId());
        exp.getParties().add(party.getId());
        expeditions.put(party.getExpeditionId(), exp);
      }
      return party;
    }

    public static MapleParty createPartyAndAdd(MaplePartyCharacter chrfor, int expedId) {
      MapleExpedition ex = getExped(expedId);
      if (ex == null) {
        return null;
      }
      MapleParty party = new MapleParty(runningPartyId.getAndIncrement(), chrfor, expedId);
      parties.put(party.getId(), party);
      ex.getParties().add(party.getId());
      return party;
    }

    public static MapleParty getParty(int partyid) {
      return parties.get(partyid);
    }

    public static MapleExpedition getExped(int partyid) {
      return expeditions.get(partyid);
    }

    public static MapleParty disbandParty(int partyid) {
      return disbandParty(partyid, false);
    }

    public static MapleParty disbandParty(int partyid, boolean inExpedition) {
      MapleParty ret = parties.remove(partyid);
      if (ret == null) {
        return null;
      }
      if (ret.getExpeditionId() > 0) { // Below only used when leader of a party in an expedition disband his/her party(not exp ldr)
        MapleExpedition me = getExped(ret.getExpeditionId());
        if (me != null) {
          int ind = me.getIndex(partyid);
          if (ind >= 0) {
            me.getParties().remove(ind);
            //expedPacket(me.getId(), MapleUserPackets.removeExpedition(61), null);
            //expedPacket(me.getId(), MapleUserPackets.expeditionUpdate(ind, null), null);
          }
        }
      }
      ret.disband();
      return ret;
    }

    public static MapleExpedition disbandExped(int partyid) {
      final MapleExpedition ret = expeditions.remove(partyid);
      if (ret != null) {
        for (Integer i : ret.getParties()) {
          final MapleParty pp = getParty(i);
          if (pp != null) {
            updateParty(i, PartyOperation.DISBAND, pp.getLeader());
          }
        }
      }
      return ret;
    }
  }

  public static class Buddy {

    private static final List<BuddyInvitedEntry> invited = new LinkedList<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static long lastPruneTime;

    public static boolean canPrune(long now) { // Expires every 10 minutes, Checks every 20 minutes
      return (lastPruneTime + (20 * 60 * 1000)) < now;
    }

    private static void prepareRemove() {
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

    public static BuddyAddResult requestBuddyAdd(String addName, MapleCharacter inviter) {
      int ch = Find.findChannel(addName);
      if (ch > 0) {
        final MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(addName);
        if (addChar != null) {
          final MapleBuddyList buddylist = addChar.getBuddylist();
          if (buddylist.isFull()) {
            return BuddyAddResult.BUDDYLIST_FULL;
          }
          if (buddylist.contains(inviter.getId())) {
            return BuddyAddResult.ALREADY_ON_LIST;
          }
          lock.writeLock().lock();
          try {
            invited.add(new BuddyInvitedEntry(addChar.getName(), inviter.getId()));
          } finally {
            lock.writeLock().unlock();
          }
          addChar.getClient().getSession().write(MaplePacketCreator.requestBuddylistAdd(inviter.getId(), inviter.getName(), inviter.getLevel(), inviter.getJob()));
          return BuddyAddResult.OK;
        }
      }
      return BuddyAddResult.NOT_FOUND;
    }

    public static Pair<BuddyAddResult, String> acceptToInvite(MapleCharacter chr, int inviterCid) {
      Iterator<BuddyInvitedEntry> itr = invited.iterator();
      while (itr.hasNext()) {
        BuddyInvitedEntry inv = itr.next();
        if (inviterCid == inv.inviter && chr.getName().equalsIgnoreCase(inv.name)) {
          itr.remove();
          if (chr.getBuddylist().isFull()) {
            return new Pair<>(BuddyAddResult.BUDDYLIST_FULL, null);
          }
          final int ch = Find.findChannel(inviterCid);
          if (ch > 0) { // Inviter is online
            final MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(inviterCid);
            if (addChar == null) {
              return new Pair<>(BuddyAddResult.NOT_FOUND, null);
            }
            addChar.getBuddylist().put(new BuddyListEntry(chr.getName(), chr.getId(), "Default Group", chr.getClient().getChannel()));
            addChar.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.ADD, addChar.getBuddylist().getBuddies()));

            chr.getBuddylist().put(new BuddyListEntry(addChar.getName(), addChar.getId(), "Default Group", ch));
            chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.ADD, chr.getBuddylist().getBuddies()));

            return new Pair<>(BuddyAddResult.OK, addChar.getName());
          }
        }
      }
      return new Pair<>(BuddyAddResult.NOT_FOUND, null);
    }

    public static String denyToInvite(MapleCharacter chr, int inviterCid) {
      Iterator<BuddyInvitedEntry> itr = invited.iterator();
      while (itr.hasNext()) {
        BuddyInvitedEntry inv = itr.next();
        if (inviterCid == inv.inviter && chr.getName().equalsIgnoreCase(inv.name)) {
          itr.remove();
          final int ch = Find.findChannel(inviterCid);
          if (ch > 0) { // Inviter is online
            final MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(inviterCid);
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

    public static BuddyDelResult DeleteBuddy(MapleCharacter chr, int deleteCid) {
      final BuddyListEntry myBlz = chr.getBuddylist().get(deleteCid);
      if (myBlz == null) {
        return BuddyDelResult.NOT_ON_LIST;
      }
      final int ch = Find.findChannel(deleteCid);
      if (ch == -20 || ch == -10) {
        return BuddyDelResult.IN_CASH_SHOP;
      }
      if (ch > 0) { // Buddy is online
        final MapleCharacter delChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(deleteCid);
        if (delChar == null) {
          final int ch_ = Find.findChannel(deleteCid); // Re-attempt to find again
          if (ch_ == -20 || ch_ == -10) {
            return BuddyDelResult.IN_CASH_SHOP;
          }
          if (ch_ <= 0) {
            final byte result = deleteOfflineBuddy(deleteCid, chr.getId()); // Execute SQL query.
            if (result == -1) {
              return BuddyDelResult.ERROR;
            }
            chr.getBuddylist().remove(deleteCid);
            chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddylist().getBuddies()));
            return BuddyDelResult.OK;
          }
        }
        delChar.getBuddylist().remove(chr.getId());
        delChar.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, delChar.getBuddylist().getBuddies()));
        delChar.dropMessage(5, "Your buddy relationship with '" + chr.getName() + "' has ended.");

        chr.getBuddylist().remove(deleteCid);
        chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddylist().getBuddies()));
        return BuddyDelResult.OK;
      } else { // Buddy is offline
        final byte result = deleteOfflineBuddy(deleteCid, chr.getId()); // Execute SQL query.
        if (result == -1) {
          return BuddyDelResult.ERROR;
        }
        chr.getBuddylist().remove(deleteCid);
        chr.getClient().getSession().write(MaplePacketCreator.updateBuddylist(BuddyListModifyHandler.REMOVE, chr.getBuddylist().getBuddies()));
        return BuddyDelResult.OK;
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
        System.out.println("Error deleting buddy id " + myId + ", Owner Id " + delId + " Reason: " + e);
        return -1;
      }
    }

    public static void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) {
      for (int characterId : recipientCharacterIds) {
        int ch = Find.findChannel(characterId);
        if (ch > 0) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(characterId);
          if (chr != null) {
            chr.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
          }
        }
      }
    }

    private static void updateBuddies(int characterId, int channel, int[] buddies, boolean offline, int gmLevel, boolean isHidden) {
      for (int buddy : buddies) {
        int ch = Find.findChannel(buddy);
        if (ch > 0) {
          MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(buddy);
          if (chr != null) {
            BuddyListEntry ble = chr.getBuddylist().get(characterId);
            if (ble != null) {
              int mcChannel;
              if (offline || (isHidden && chr.getGMLevel() < gmLevel)) {
                ble.setChannel(-1);
                mcChannel = -1;
              } else {
                ble.setChannel(channel);
                mcChannel = channel - 1;
              }
              chr.getBuddylist().put(ble);
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

  public static class Messenger {

    private static Map<Integer, MapleMessenger> messengers = new HashMap<>();
    private static final AtomicInteger runningMessengerId = new AtomicInteger();

    static {
      runningMessengerId.set(1);
    }

    public static MapleMessenger createMessenger(MapleMessengerCharacter chrfor) {
      int messengerid = runningMessengerId.getAndIncrement();
      MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
      messengers.put(messenger.getId(), messenger);
      return messenger;
    }

    public static void declineChat(String target, String namefrom) {
      int ch = Find.findChannel(target);
      if (ch > 0) {
        ChannelServer cs = ChannelServer.getInstance(ch);
        MapleCharacter chr = cs.getPlayerStorage().getCharacterByName(target);
        if (chr != null) {
          MapleMessenger messenger = chr.getMessenger();
          if (messenger != null) {
            chr.getClient().getSession().write(MaplePacketCreator.messengerNote(namefrom, 5, 0));
          }
        }
      }
    }

    public static MapleMessenger getMessenger(int messengerid) {
      return messengers.get(messengerid);
    }

    public static void leaveMessenger(int messengerid, MapleMessengerCharacter target) {
      MapleMessenger messenger = getMessenger(messengerid);
      if (messenger == null) {
        throw new IllegalArgumentException("No messenger with the specified messengerid exists");
      }
      int position = messenger.getPositionByName(target.getName());
      messenger.removeMember(target);

      for (MapleMessengerCharacter mmc : messenger.getMembers()) {
        if (mmc != null) {
          int ch = Find.findChannel(mmc.getId());
          if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(mmc.getName());
            if (chr != null) {
              chr.getClient().getSession().write(MaplePacketCreator.removeMessengerPlayer(position));
            }
          }
        }
      }
    }

    public static void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) {
      MapleMessenger messenger = getMessenger(messengerid);
      if (messenger == null) {
        throw new IllegalArgumentException("No messenger with the specified messengerid exists");
      }
      messenger.silentRemoveMember(target);
    }

    public static void silentJoinMessenger(int messengerid, MapleMessengerCharacter target) {
      MapleMessenger messenger = getMessenger(messengerid);
      if (messenger == null) {
        throw new IllegalArgumentException("No messenger with the specified messengerid exists");
      }
      messenger.silentAddMember(target);
    }

    public static void updateMessenger(int messengerid, String namefrom, int fromchannel) {
      MapleMessenger messenger = getMessenger(messengerid);
      int position = messenger.getPositionByName(namefrom);

      for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
        if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
          int ch = Find.findChannel(messengerchar.getName());
          if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {
              MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
              chr.getClient().getSession().write(MaplePacketCreator.updateMessengerPlayer(namefrom, from, position, fromchannel - 1));
            }
          }
        }
      }
    }

    public static void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) {
      MapleMessenger messenger = getMessenger(messengerid);
      if (messenger == null) {
        throw new IllegalArgumentException("No messenger with the specified messengerid exists");
      }
      messenger.addMember(target);
      int position = messenger.getPositionByName(target.getName());
      for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
        if (messengerchar != null) {
          int mposition = messenger.getPositionByName(messengerchar.getName());
          int ch = Find.findChannel(messengerchar.getName());
          if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {
              if (!messengerchar.getName().equals(from)) {
                MapleCharacter fromCh = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(from);
                chr.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(from, fromCh, position, fromchannel - 1));
                fromCh.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, mposition, messengerchar.getChannel() - 1));
              } else {
                chr.getClient().getSession().write(MaplePacketCreator.joinMessenger(mposition));
              }
            }
          }
        }
      }
    }

    public static void messengerChat(int messengerid, String chattext, String namefrom) {
      MapleMessenger messenger = getMessenger(messengerid);
      if (messenger == null) {
        throw new IllegalArgumentException("No messenger with the specified messengerid exists");
      }

      for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
        if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
          int ch = Find.findChannel(messengerchar.getName());
          if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
            if (chr != null) {

              chr.getClient().getSession().write(MaplePacketCreator.messengerChat(chattext));
            }
          }
        } //Whisp Monitor Code
        else if (messengerchar != null) {
          int ch = Find.findChannel(messengerchar.getName());
          if (ch > 0) {
            MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
          }
        }
        //
      }
    }

    public static void messengerInvite(String sender, int messengerid, String target, int fromchannel, boolean gm) {

      if (isConnected(target)) {

        int ch = Find.findChannel(target);
        if (ch > 0) {
          MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(sender);
          MapleCharacter targeter = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(target);
          if (targeter != null && targeter.getMessenger() == null) {
            if (!targeter.isGM() || gm) {
              targeter.getClient().getSession().write(MaplePacketCreator.messengerInvite(sender, messengerid));
              from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 1));
            } else {
              from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 0));
            }
          } else {
            from.getClient().getSession().write(MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
          }
        }
      }

    }
  }

  public static class randomWorldStuff {
    private static List<String> loggedOnSinceLastRestart = new ArrayList<>();

    public static void addToLoggedOnSinceLastRestart(MapleCharacter c) {
      loggedOnSinceLastRestart.add(c.getName());
    }
  }


  public static class Guild {

    private static final Map<Integer, MapleGuild> guilds = new LinkedHashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void addLoadedGuild(MapleGuild g) {
      if (g.isProper()) {
        guilds.put(g.getId(), g);
      }
    }

    public static int createGuild(int leaderId, String name) {
      return MapleGuild.createGuild(leaderId, name);
    }

    public static MapleGuild getGuild(int id) {
      MapleGuild ret = null;
      lock.readLock().lock();
      try {
        ret = guilds.get(id);
      } finally {
        lock.readLock().unlock();
      }
      if (ret == null) {
        lock.writeLock().lock();
        try {
          ret = new MapleGuild(id);
          if (ret == null || ret.getId() <= 0 || !ret.isProper()) { //failed to load
            return null;
          }
          guilds.put(id, ret);
        } finally {
          lock.writeLock().unlock();
        }
      }
      return ret; //Guild doesn't exist?
    }

    public static MapleGuild getGuildByName(String guildName) {
      lock.readLock().lock();
      try {
        for (MapleGuild g : guilds.values()) {
          if (g.getName().equalsIgnoreCase(guildName)) {
            return g;
          }
        }
        return null;
      } finally {
        lock.readLock().unlock();
      }
    }

    public static MapleGuild getGuild(MapleCharacter mc) {
      return getGuild(mc.getGuildId());
    }

    public static void setGuildMemberOnline(MapleGuildCharacter mc, boolean bOnline, int channel) {
      MapleGuild g = getGuild(mc.getGuildId());
      if (g != null) {
        g.setOnline(mc.getId(), bOnline, channel);
      }
    }

    public static void guildPacket(int gid, byte[] message) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.broadcast(message);
      }
    }

    public static int addGuildMember(MapleGuildCharacter mc) {
      MapleGuild g = getGuild(mc.getGuildId());
      if (g != null) {
        return g.addGuildMember(mc);
      }
      return 0;
    }

    public static void leaveGuild(MapleGuildCharacter mc) {
      MapleGuild g = getGuild(mc.getGuildId());
      if (g != null) {
        g.leaveGuild(mc);
      }
    }

    public static void guildChat(int gid, String name, int cid, String msg) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.guildChat(name, cid, msg);
      }
    }

    public static void changeRank(int gid, int cid, int newRank) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.changeRank(cid, newRank);
      }
    }

    public static void expelMember(MapleGuildCharacter initiator, String name, int cid) {
      MapleGuild g = getGuild(initiator.getGuildId());
      if (g != null) {
        g.expelMember(initiator, name, cid);
      }
    }

    public static void setGuildNotice(int gid, String notice) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.setGuildNotice(notice);
      }
    }

    public static void memberLevelJobUpdate(MapleGuildCharacter mc) {
      MapleGuild g = getGuild(mc.getGuildId());
      if (g != null) {
        g.memberLevelJobUpdate(mc);
      }
    }

    public static void changeRankTitle(int gid, String[] ranks) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.changeRankTitle(ranks);
      }
    }

    public static void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.setGuildEmblem(bg, bgcolor, logo, logocolor);
      }
    }

    public static void setGuildName(int gid, String name) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.setGuildName(name);
      }
    }

    public static void disbandGuild(int gid) {
      MapleGuild g = getGuild(gid);
      lock.writeLock().lock();
      try {
        if (g != null) {
          g.disbandGuild();
          guilds.remove(gid);
        }
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static void deleteGuildCharacter(int guildid, int charid) {

      //ensure it's loaded on world server
      //setGuildMemberOnline(mc, false, -1);
      MapleGuild g = getGuild(guildid);
      if (g != null) {
        MapleGuildCharacter mc = g.getMGC(charid);
        if (mc != null) {
          if (mc.getGuildRank() > 1) //not leader
          {
            g.leaveGuild(mc);
          } else {
            g.disbandGuild();
          }
        }
      }
    }

    public static boolean increaseGuildCapacity(int gid) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        return g.increaseCapacity();
      }
      return false;
    }

    public static void gainGP(int gid, int amount) {
      MapleGuild g = getGuild(gid);
      if (g != null) {
        g.gainGP(amount);
      }
    }

    public static int getGP(final int gid) {
      final MapleGuild g = getGuild(gid);
      if (g != null) {
        return g.getGP();
      }
      return 0;
    }

    public static int getInvitedId(final int gid) {
      final MapleGuild g = getGuild(gid);
      if (g != null) {
        return g.getInvitedId();
      }
      return 0;
    }

    public static void setInvitedId(final int gid, final int inviteid) {
      final MapleGuild g = getGuild(gid);
      if (g != null) {
        g.setInvitedId(inviteid);
      }
    }

    public static int getGuildLeader(final String guildName) {
      final MapleGuild mga = getGuildByName(guildName);
      if (mga != null) {
        return mga.getLeaderId();
      }
      return 0;
    }

    public static void save() {
      System.out.println("Saving guilds...");
      lock.writeLock().lock();
      try {
        for (MapleGuild a : guilds.values()) {
          a.writeToDB(false);
        }
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static List<MapleBBSThread> getBBS(final int gid) {
      final MapleGuild g = getGuild(gid);
      if (g != null) {
        return g.getBBS();
      }
      return null;
    }

    public static int addBBSThread(final int guildid, final String title, final String text, final int icon, final boolean bNotice, final int posterID) {
      final MapleGuild g = getGuild(guildid);
      if (g != null) {
        return g.addBBSThread(title, text, icon, bNotice, posterID);
      }
      return -1;
    }

    public static void editBBSThread(final int guildid, final int localthreadid, final String title, final String text, final int icon, final int posterID, final int guildRank) {
      final MapleGuild g = getGuild(guildid);
      if (g != null) {
        g.editBBSThread(localthreadid, title, text, icon, posterID, guildRank);
      }
    }

    public static void deleteBBSThread(final int guildid, final int localthreadid, final int posterID, final int guildRank) {
      final MapleGuild g = getGuild(guildid);
      if (g != null) {
        g.deleteBBSThread(localthreadid, posterID, guildRank);
      }
    }

    public static void addBBSReply(final int guildid, final int localthreadid, final String text, final int posterID) {
      final MapleGuild g = getGuild(guildid);
      if (g != null) {
        g.addBBSReply(localthreadid, text, posterID);
      }
    }

    public static void deleteBBSReply(final int guildid, final int localthreadid, final int replyid, final int posterID, final int guildRank) {
      final MapleGuild g = getGuild(guildid);
      if (g != null) {
        g.deleteBBSReply(localthreadid, replyid, posterID, guildRank);
      }
    }

    public static void changeEmblem(int gid, int affectedPlayers, MapleGuildSummary mgs) {
      Broadcast.sendGuildPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()), -1, gid);
      setGuildAndRank(affectedPlayers, -1, -1, -1);    //respawn player
    }

    public static void changeName(int gid, int affectedPlayers, MapleGuildSummary mgs) {
      //Broadcast.sendGuildPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()), -1, gid);
      // should we reload player
      setGuildAndRank(affectedPlayers, -1, -1, -1);    //respawn player
    }

    public static void setGuildAndRank(int cid, int guildid, int rank, int alliancerank) {
      int ch = Find.findChannel(cid);
      if (ch == -1) {
        // System.out.println("ERROR: cannot find player in given channel");
        return;
      }
      MapleCharacter mc = getStorage(ch).getCharacterById(cid);
      if (mc == null) {
        return;
      }
      boolean bDifferentGuild;
      if (guildid == -1 && rank == -1) { //just need a respawn
        bDifferentGuild = true;
      } else {
        bDifferentGuild = guildid != mc.getGuildId();
        mc.setGuildId(guildid);
        mc.setGuildRank((byte) rank);
        mc.setAllianceRank((byte) alliancerank);
        mc.saveGuildStatus();
      }
      if (bDifferentGuild && ch > 0) {
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.loadGuildName(mc), false);
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.loadGuildIcon(mc), false);
      }
    }
  }

  public static class Broadcast {

    public static void broadcastSmega(byte[] message) {
      for (ChannelServer cs : ChannelServer.getAllInstances()) {
        cs.broadcastSmega(message);
      }
    }

    public static void broadcastGMMessage(byte[] message) {
      for (ChannelServer cs : ChannelServer.getAllInstances()) {
        cs.broadcastGMMessage(message);
      }
    }

    public static void broadcastMessage(byte[] message) {
      for (ChannelServer cs : ChannelServer.getAllInstances()) {
        cs.broadcastMessage(message);
      }
    }

    public static void sendPacket(List<Integer> targetIds, byte[] packet, int exception) {
      MapleCharacter c;
      for (int i : targetIds) {
        if (i == exception) {
          continue;
        }
        int ch = Find.findChannel(i);
        if (ch < 0) {
          continue;
        }
        c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(i);
        if (c != null) {
          c.getClient().getSession().write(packet);
        }
      }
    }

    public static void sendGuildPacket(int targetIds, byte[] packet, int exception, int guildid) {
      if (targetIds == exception) {
        return;
      }
      int ch = Find.findChannel(targetIds);
      if (ch < 0) {
        return;
      }
      final MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetIds);
      if (c != null && c.getGuildId() == guildid) {
        c.getClient().getSession().write(packet);
      }
    }


  }

  public static class Find {

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static HashMap<Integer, Integer> idToChannel = new HashMap<>();
    private static HashMap<String, Integer> nameToChannel = new HashMap<>();

    public static void register(int id, String name, int channel) {
      lock.writeLock().lock();
      try {
        idToChannel.put(id, channel);
        nameToChannel.put(name.toLowerCase(), channel);
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static void forceDeregister(int id) {
      lock.writeLock().lock();
      try {
        idToChannel.remove(id);
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static void forceDeregister(String id) {
      lock.writeLock().lock();
      try {
        nameToChannel.remove(id.toLowerCase());
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static void forceDeregister(int id, String name) {
      lock.writeLock().lock();
      try {
        idToChannel.remove(id);
        nameToChannel.remove(name.toLowerCase());
      } finally {
        lock.writeLock().unlock();
      }
    }

    public static int findChannel(int id) {
      Integer ret;
      lock.readLock().lock();
      try {
        ret = idToChannel.get(id);
      } finally {
        lock.readLock().unlock();
      }
      if (ret != null) {
        if (ret != -10 && ret != -20 && ChannelServer.getInstance(ret) == null) { //wha
          forceDeregister(id);
          return -1;
        }
        return ret;
      }
      return -1;
    }

    public static int findChannel(String st) {
      Integer ret;
      lock.readLock().lock();
      try {
        ret = nameToChannel.get(st.toLowerCase());
      } finally {
        lock.readLock().unlock();
      }
      if (ret != null) {
        if (ret != -10 && ret != -20 && ChannelServer.getInstance(ret) == null) { //wha
          forceDeregister(st);
          return -1;
        }
        return ret;
      }
      return -1;
    }

    public static CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
      List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
      for (int i : characterIds) {
        int channel = findChannel(i);
        if (channel > 0) {
          foundsChars.add(new CharacterIdChannelPair(i, channel));
        }
      }
      Collections.sort(foundsChars);
      return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }
  }

  public static class Alliance {

    private static final Map<Integer, MapleGuildAlliance> alliances = new LinkedHashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
      System.out.println("[MapleGuildAlliance] Loading GuildAlliances");
      Collection<MapleGuildAlliance> allGuilds = MapleGuildAlliance.loadAll();
      for (MapleGuildAlliance g : allGuilds) {
        alliances.put(g.getId(), g);
      }
    }

    public static MapleGuildAlliance getAlliance(final int allianceid) {
      MapleGuildAlliance ret = null;
      lock.readLock().lock();
      try {
        ret = alliances.get(allianceid);
      } finally {
        lock.readLock().unlock();
      }
      if (ret == null) {
        lock.writeLock().lock();
        try {
          ret = new MapleGuildAlliance(allianceid);
          if (ret == null || ret.getId() <= 0) { //failed to load
            return null;
          }
          alliances.put(allianceid, ret);
        } finally {
          lock.writeLock().unlock();
        }
      }
      return ret;
    }

    public static int getAllianceLeader(final int allianceid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.getLeaderId();
      }
      return 0;
    }

    public static void updateAllianceRanks(final int allianceid, final String[] ranks) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        mga.setRank(ranks);
      }
    }

    public static void updateAllianceNotice(final int allianceid, final String notice) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        mga.setNotice(notice);
      }
    }

    public static boolean canInvite(final int allianceid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.getCapacity() > mga.getNoGuilds();
      }
      return false;
    }

    public static boolean changeAllianceLeader(final int allianceid, final int cid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.setLeaderId(cid);
      }
      return false;
    }

    public static boolean changeAllianceRank(final int allianceid, final int cid, final int change) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.changeAllianceRank(cid, change);
      }
      return false;
    }

    public static boolean changeAllianceCapacity(final int allianceid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.setCapacity();
      }
      return false;
    }

    public static boolean disbandAlliance(final int allianceid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.disband();
      }
      return false;
    }

    public static boolean addGuildToAlliance(final int allianceid, final int gid) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.addGuild(gid);
      }
      return false;
    }

    public static boolean removeGuildFromAlliance(final int allianceid, final int gid, final boolean expelled) {
      final MapleGuildAlliance mga = getAlliance(allianceid);
      if (mga != null) {
        return mga.removeGuild(gid, expelled);
      }
      return false;
    }

    public static void sendGuild(final int allianceid) {
      final MapleGuildAlliance alliance = getAlliance(allianceid);
      if (alliance != null) {
        sendGuild(MaplePacketCreator.getAllianceUpdate(alliance), -1, allianceid);
        sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
      }
    }

    public static void sendGuild(final byte[] packet, final int exceptionId, final int allianceid) {
      final MapleGuildAlliance alliance = getAlliance(allianceid);
      if (alliance != null) {
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
          int gid = alliance.getGuildId(i);
          if (gid > 0 && gid != exceptionId) {
            Guild.guildPacket(gid, packet);
          }
        }
      }
    }

    public static boolean createAlliance(final String alliancename, final int cid, final int cid2, final int gid, final int gid2) {
      final int allianceid = MapleGuildAlliance.createToDb(cid, alliancename, gid, gid2);
      if (allianceid <= 0) {
        return false;
      }
      final MapleGuild g = Guild.getGuild(gid), g_ = Guild.getGuild(gid2);
      g.setAllianceId(allianceid);
      g_.setAllianceId(allianceid);
      g.changeARank(true);
      g_.changeARank(false);

      final MapleGuildAlliance alliance = getAlliance(allianceid);

      sendGuild(MaplePacketCreator.createGuildAlliance(alliance), -1, allianceid);
      sendGuild(MaplePacketCreator.getAllianceInfo(alliance), -1, allianceid);
      sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
      sendGuild(MaplePacketCreator.changeAlliance(alliance, true), -1, allianceid);
      return true;
    }

    public static void allianceChat(final int gid, final String name, final int cid, final String msg) {
      final MapleGuild g = Guild.getGuild(gid);
      if (g != null) {
        final MapleGuildAlliance ga = getAlliance(g.getAllianceId());
        if (ga != null) {
          for (int i = 0; i < ga.getNoGuilds(); i++) {
            final MapleGuild g_ = Guild.getGuild(ga.getGuildId(i));
            if (g_ != null) {
              g_.allianceChat(name, cid, msg);
            }
          }
        }
      }
    }

    public static void setNewAlliance(final int gid, final int allianceid) {
      final MapleGuildAlliance alliance = getAlliance(allianceid);
      final MapleGuild guild = Guild.getGuild(gid);
      if (alliance != null && guild != null) {
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
          if (gid == alliance.getGuildId(i)) {
            guild.setAllianceId(allianceid);
            guild.broadcast(MaplePacketCreator.getAllianceInfo(alliance));
            guild.broadcast(MaplePacketCreator.getGuildAlliance(alliance));
            guild.broadcast(MaplePacketCreator.changeAlliance(alliance, true));
            guild.changeARank();
            guild.writeToDB(false);
          } else {
            final MapleGuild g_ = Guild.getGuild(alliance.getGuildId(i));
            if (g_ != null) {
              g_.broadcast(MaplePacketCreator.addGuildToAlliance(alliance, guild));
              g_.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, guild, true));
            }
          }
        }
      }
    }

    public static void setOldAlliance(final int gid, final boolean expelled, final int allianceid) {
      final MapleGuildAlliance alliance = getAlliance(allianceid);
      final MapleGuild g_ = Guild.getGuild(gid);
      if (alliance != null) {
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
          final MapleGuild guild = Guild.getGuild(alliance.getGuildId(i));
          if (guild == null) {
            if (gid != alliance.getGuildId(i)) {
              alliance.removeGuild(gid, false);
            }
            continue; //just skip
          }
          if (g_ == null || gid == alliance.getGuildId(i)) {
            guild.changeARank(5);
            guild.setAllianceId(0);
            guild.broadcast(MaplePacketCreator.disbandAlliance(allianceid));
          } else if (g_ != null) {
            guild.broadcast(MaplePacketCreator.serverNotice(5, "[" + g_.getName() + "] Guild has left the alliance."));
            guild.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, g_, false));
            guild.broadcast(MaplePacketCreator.removeGuildFromAlliance(alliance, g_, expelled));
          }

        }
      }

      if (gid == -1) {
        lock.writeLock().lock();
        try {
          alliances.remove(allianceid);
        } finally {
          lock.writeLock().unlock();
        }
      }
    }

    public static List<byte[]> getAllianceInfo(final int allianceid, final boolean start) {
      List<byte[]> ret = new ArrayList<>();
      final MapleGuildAlliance alliance = getAlliance(allianceid);
      if (alliance != null) {
        if (start) {
          ret.add(MaplePacketCreator.getAllianceInfo(alliance));
          ret.add(MaplePacketCreator.getGuildAlliance(alliance));
        }
        ret.add(MaplePacketCreator.getAllianceUpdate(alliance));
      }
      return ret;
    }

    public static void save() {
      System.out.println("Saving alliances...");
      lock.writeLock().lock();
      try {
        for (MapleGuildAlliance a : alliances.values()) {
          a.saveToDb();
        }
      } finally {
        lock.writeLock().unlock();
      }
    }
  }


  public static void registerRespawn() {
    Integer[] chs = (Integer[]) ChannelServer.getAllInstance().toArray(new Integer[0]);
    for (int i = 0; i < chs.length; i += CHANNELS_PER_THREAD) {
      WorldTimer.getInstance().register(new Respawn(chs, i), 1125); //divisible by 9000 if possible.
    }
  }

  public static class Respawn implements Runnable {

    private int numTimes = 0;
    private final List<Integer> cservs = new ArrayList<>(CHANNELS_PER_THREAD);
    private ArrayList<MapleMap> maps = new ArrayList<>();
    private ArrayList<MonsterStatusEffect> effects = new ArrayList<>();
    private ArrayList<MapleMapItem> items = new ArrayList<>();
    private ArrayList<MapleCharacter> chrs = new ArrayList<>();
    private ArrayList<MapleMonster> mobs = new ArrayList<>();
    private ArrayList<MapleDiseaseValueHolder> dis = new ArrayList<>();
    private ArrayList<MapleCoolDownValueHolder> cd = new ArrayList<>();
    private ArrayList<MaplePet> pets = new ArrayList<>();

    public Respawn(Integer[] chs, int c) {
      StringBuilder s = new StringBuilder("[Respawn Worker] Registered for channels ");
      for (int i = 1; (i <= CHANNELS_PER_THREAD) && (chs.length >= c + i); i++) {
        cservs.add(Integer.valueOf(ChannelServer.getInstance(c + i).getChannel()));
        s.append(c + i).append(" ");
      }
      System.out.println(s.toString());
    }

    @Override
    public void run() {
      numTimes++;
      long now = System.currentTimeMillis();

      for (Integer cser : cservs) {
        final ChannelServer cserv = ChannelServer.getInstance(cser);
        if (cserv != null && !cserv.hasFinishedShutdown()) {
          maps = cserv.getMapFactory().getAllLoadedMaps(maps);
          for (MapleMap map : maps) {
            handleMap(map, numTimes, map.getCharactersSize(), now, effects, items, chrs, mobs, dis, cd, pets);
          }
        }
      }
      if (Buddy.canPrune(now)) {
        Buddy.prepareRemove();
      }
    }
  }

  public static void handleMap(final MapleMap map, final int numTimes, final int size, final long now, ArrayList<MonsterStatusEffect> effects, ArrayList<MapleMapItem> items, ArrayList<MapleCharacter> chrs, ArrayList<MapleMonster> monsters, ArrayList<MapleDiseaseValueHolder> dis, ArrayList<MapleCoolDownValueHolder> cd, ArrayList<MaplePet> pets) {
    if (map.getItemsSize() > 0) {
      items = map.getAllItemsThreadsafe(items);
      for (MapleMapItem item : items) {
        if (item.shouldExpire(now)) {
          item.expire(map);
        } else if (item.shouldFFA(now)) {
          item.setDropType((byte) 2);
        }
      }
    }
    if (map.characterSize() > 0) {
      map.respawn(false, now);
      boolean hurt = map.canHurt(now);
      chrs = map.getCharactersThreadsafe(chrs);
      for (MapleCharacter chr : chrs) {
        handleCooldowns(chr, numTimes, hurt, now, dis, cd, pets);
      }

      if (map.getMobsSize() > 0) {
        monsters = map.getAllMonstersThreadsafe(monsters);
        for (MapleMonster mons : monsters) {
          if (mons.isAlive() && mons.shouldKill(now)) {
            map.killMonster(mons);
          } else if (mons.isAlive() && mons.shouldDrop(now)) {
            mons.doDropItem(now);
//					} else if (mons.isAlive() && mons.getStatiSize() > 0) {
//						effects = mons.getAllBuffs(effects);
//						for (MonsterStatusEffect mse : effects) {
//							if (mse.shouldCancel(now)) {
//								mons.cancelSingleStatus(mse);
//							} 
//						}
          }
        }
      }
    }
  }

  public static void handleCooldowns(final MapleCharacter chr, final int numTimes, final boolean hurt, final long now, ArrayList<MapleDiseaseValueHolder> dis, ArrayList<MapleCoolDownValueHolder> cd, ArrayList<MaplePet> pets) {
    if (chr.getCooldownSize() > 0) {
      cd = chr.getCooldowns(cd);
      for (MapleCoolDownValueHolder m : cd) {
        if (m.startTime + m.length < now) {
          final int skil = m.skillId;
          chr.removeCooldown(skil);
          chr.getClient().getSession().write(MaplePacketCreator.skillCooldown(skil, 0));
        }
      }
    }
    if (chr.isAlive()) {
      if (/*(chr.getJob() == 131 || chr.getJob() == 132) && */chr.canBlood(now)) {
        chr.doDragonBlood();
      }
      if (chr.canRecover(now)) {
        chr.doRecovery();
      }
      if (chr.canFairy(now)) {
        chr.doFairy();
      }
      //if (chr.canFish(now)) { chr.doFish(now); }
    }
    if (chr.getDiseaseSize() > 0) {
      dis = chr.getAllDiseases(dis);
      for (MapleDiseaseValueHolder m : dis) {
        if (m.startTime + m.length < now) {
          chr.dispelDebuff(m.disease);
        }
      }
    }
    if (numTimes % 7 == 0 && chr.getMount() != null && chr.getMount().canTire(now)) {
      chr.getMount().increaseFatigue();
    }
    if (numTimes % 13 == 0) { //we're parsing through the characters anyway (:
      pets = chr.getSummonedPets(pets);
      for (MaplePet pet : pets) {
        if (pet.getPetItemId() == 5000054 && pet.getSecondsLeft() > 0) {
          pet.setSecondsLeft(pet.getSecondsLeft() - 1);
          if (pet.getSecondsLeft() <= 0) {
            chr.unequipPet(pet, true, true);
            return;
          }
        }
        int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getPetItemId());
        if (new Random().nextInt(15) > 2) {
          continue;
        }
        if (newFullness <= 5) {
          pet.setFullness(15);
          chr.unequipPet(pet, true, true);
        } else {
          pet.setFullness(newFullness);
          chr.getClient().getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition())));
        }
      }
    }
    if (hurt && chr.isAlive()) {
      if (chr.getInventory(MapleInventoryType.EQUIPPED).findById(chr.getMap().getHPDecProtect()) == null) {
        if (chr.getMapId() == 749040100 && chr.getInventory(MapleInventoryType.CASH).findById(5451000) == null) { //minidungeon
          chr.addHP(-chr.getMap().getHPDec());
        } else if (chr.getMapId() != 749040100) {
          chr.addHP(-(chr.getMap().getHPDec() - (chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD) == null ? 0 : chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD).intValue())));
        }
      }
    }
    if (isHotTimeStarted(now) && canTakeHotTime(chr.getName(), now) && chr.isAlive()) {
      if (chr.getLevel() > 30 && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1 && chr.getConversation() == 0) {
        if (!chr.haveItem(2022336, 1, false, true)) {
          obtainedHotTime.add(chr.getName());
          chr.getClient().getSession().write(MaplePacketCreator.getNPCTalk(0, (byte) 0, "You got the Secret Box, right? Click it to see what's inside. Check your Inventory now, if you're curious.", "00 01", (byte) 5, 9010010));
          MapleInventoryManipulator.addById(chr.getClient(), 2022336, (short) 1, null, null, 7);
          chr.getClient().getSession().write(MaplePacketCreator.getShowItemGain(2022336, (short) 1, true));
        }
      }
    }
  }


}
