package server;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import server.Timer.CloneTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

public class MapleSquad {


  public static enum MapleSquadType {

    bossbalrog(2), zak(2), chaoszak(3), horntail(2), chaosht(3), pinkbean(2), nmm_squad(2), vergamot(2), dunas(2), nibergen_squad(2), dunas2(2), core_blaze(2), aufheben(2), cwkpq(3), tokyo_2095(2), vonleon(3), scartar(2), cygnus(3), hilla(2), darkhilla(2), arkarium(3);

    private MapleSquadType(int i) {
      this.i = i;
    }

    public int i;
    public HashMap<Integer, ArrayList<Pair<String, String>>> queuedPlayers = new HashMap<>();
    public HashMap<Integer, ArrayList<Pair<String, Long>>> queue = new HashMap<>();
  }

  private final MapleSquadType squadType;

  private WeakReference<MapleCharacter> leader;
  private final String leaderName;
  private Map<String, String> members = new LinkedHashMap<String, String>();
  private Map<String, String> bannedMembers = new LinkedHashMap<String, String>();
  private final int ch;
  private final long startTime;
  private final int expiration;
  private final int beginMapId;
  private final String type;
  private byte status = 0;
  private ScheduledFuture<?> removal;

  public MapleSquad(final int ch, final String type, final MapleCharacter leader, final int expiration) {
    this.leader = new WeakReference<MapleCharacter>(leader);
    this.members.put(leader.getName(), MapleCarnivalChallenge.getJobBasicNameById(leader.getJob()));
    this.leaderName = leader.getName();
    this.ch = ch;
    this.type = type;
    this.squadType = MapleSquadType.valueOf(type.toLowerCase());
    if (this.squadType.queue.get(ch) == null) {
      this.squadType.queue.put(ch, new ArrayList<Pair<String, Long>>());
      this.squadType.queuedPlayers.put(ch, new ArrayList<Pair<String, String>>());
    }
    this.status = 1;
    this.beginMapId = leader.getMapId();
    leader.getMap().setSquad(type);
    this.startTime = System.currentTimeMillis();
    this.expiration = expiration;
    scheduleRemoval(expiration);
  }

  public MapleMap getBeginMap() {
    return ChannelServer.getInstance(ch).getMapFactory().getMap(beginMapId);
  }

  public void clear() {
    if (removal != null) {
      getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
      removal.cancel(false);
      removal = null;
    }
    members.clear();
    bannedMembers.clear();
    leader = null;
    ChannelServer.getInstance(ch).removeMapleSquad(type);
    this.status = 0;

  }

  public MapleCharacter getChar(String name) {
    return ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(name);
  }

  public long getTimeLeft() {
    return expiration - (System.currentTimeMillis() - startTime);
  }

  private void scheduleRemoval(final int time) {
    removal = CloneTimer.getInstance().schedule(new Runnable() {

      @Override
      public void run() {
        if (status != 0 && leader != null && (getLeader() == null || status == 1)) { //leader itself = null means we're already cleared
          clear();
        }
      }
    }, time);
  }

  public String getLeaderName() {
    return leaderName;
  }

  public MapleCharacter getLeader() {
    if (leader == null || leader.get() == null) {
      if (members.size() > 0 && getChar(leaderName) != null) {
        leader = new WeakReference<MapleCharacter>(getChar(leaderName));
      } else {
        if (status != 0) {
          clear();
        }
        return null;
      }
    }
    return leader.get();
  }

  public boolean containsMember(MapleCharacter member) {
    for (String mmbr : members.keySet()) {
      if (mmbr.equalsIgnoreCase(member.getName())) {
        return true;
      }
    }
    return false;
  }

  public List<String> getMembers() {
    return new LinkedList<String>(members.keySet());
  }

  public List<String> getBannedMembers() {
    return new LinkedList<String>(bannedMembers.keySet());
  }

  public int getSquadSize() {
    return members.size();
  }

  public boolean isBanned(MapleCharacter member) {
    return bannedMembers.containsKey(member.getName());
  }

  public int addMember(MapleCharacter member, boolean join) {
    final String job = MapleCarnivalChallenge.getJobBasicNameById(member.getJob());
    if (join) {
      if (!members.containsKey(member.getName())) {
        if (members.size() <= 30) {
          members.put(member.getName(), job);
          getLeader().dropMessage(5, member.getName() + " (" + job + ") has joined the fight!");
          return 1;
        }
        return 2;
      }
      return -1;
    } else {
      if (members.containsKey(member.getName())) {
        members.remove(member.getName());
        getLeader().dropMessage(5, member.getName() + " (" + job + ") have withdrawed from the fight.");
        return 1;
      }
      return -1;
    }
  }

  public void acceptMember(int pos) {
    if (pos < 0 || pos >= bannedMembers.size()) {
      return;
    }
    final List<String> membersAsList = getBannedMembers();
    final String toadd = membersAsList.get(pos);
    if (toadd != null && getChar(toadd) != null) {
      members.put(toadd, bannedMembers.get(toadd));
      bannedMembers.remove(toadd);

      getChar(toadd).dropMessage(5, getLeaderName() + " has decided to add you back to the squad.");
    }
  }

  public void reAddMember(MapleCharacter chr) {
    removeMember(chr);
    members.put(chr.getName(), MapleCarnivalChallenge.getJobBasicNameById(chr.getJob()));
  }

  public void removeMember(MapleCharacter chr) {
    if (members.containsKey(chr.getName())) {
      members.remove(chr.getName());
    }
  }

  public void removeMember(String chr) {
    if (members.containsKey(chr)) {
      members.remove(chr);
    }
  }

  public void banMember(int pos) {
    if (pos <= 0 || pos >= members.size()) { //may not ban leader
      return;
    }
    final List<String> membersAsList = getMembers();
    final String toban = membersAsList.get(pos);
    if (toban != null && getChar(toban) != null) {
      bannedMembers.put(toban, members.get(toban));
      members.remove(toban);

      getChar(toban).dropMessage(5, getLeaderName() + " has removed you from the squad.");
    }
  }

  public void setStatus(byte status) {
    this.status = status;
    if (status == 2 && removal != null) {
      removal.cancel(false);
      removal = null;
    }
  }

  public int getStatus() {
    return status;
  }

  public int getBannedMemberSize() {
    return bannedMembers.size();
  }

  public String getSquadMemberString(byte type) {
    switch (type) {
      case 0: {
        StringBuilder sb = new StringBuilder("Squad members : ");
        sb.append("#b").append(members.size()).append(" #k ").append("List of participants : \n\r ");
        int i = 0;
        for (Entry<String, String> chr : members.entrySet()) {
          i++;
          sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
          if (chr.getKey().equals(getLeader().getName())) {
            sb.append("(Leader of the squad)");
          }
          sb.append(" \n\r ");
        }
        while (i < 30) {
          i++;
          sb.append(i).append(" : ").append(" \n\r ");
        }
        return sb.toString();
      }
      case 1: {
        StringBuilder sb = new StringBuilder("Squad members : ");
        sb.append("#b").append(members.size()).append(" #n ").append("List of participants : \n\r ");
        int i = 0, selection = 0;
        for (Entry<String, String> chr : members.entrySet()) {
          i++;
          sb.append("#b#L").append(selection).append("#");
          selection++;
          sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
          if (chr.getKey().equals(getLeader().getName())) {
            sb.append("(Leader of the squad)");
          }
          sb.append("#l").append(" \n\r ");
        }
        while (i < 30) {
          i++;
          sb.append(i).append(" : ").append(" \n\r ");
        }
        return sb.toString();
      }
      case 2: {
        StringBuilder sb = new StringBuilder("Squad members : ");
        sb.append("#b").append(members.size()).append(" #n ").append("List of participants : \n\r ");
        int i = 0, selection = 0;
        for (Entry<String, String> chr : bannedMembers.entrySet()) {
          i++;
          sb.append("#b#L").append(selection).append("#");
          selection++;
          sb.append(i).append(" : ").append(chr.getKey()).append(" (").append(chr.getValue()).append(") ");
          if (chr.getKey().equals(getLeader().getName())) {
            sb.append("(Leader of the squad)"); //WTF
          }
          sb.append("#l").append(" \n\r ");
        }
        while (i < 30) {
          i++;
          sb.append(i).append(" : ").append(" \n\r ");
        }
        return sb.toString();
      }
      case 3: { //CWKPQ
        StringBuilder sb = new StringBuilder("Jobs : ");
        final Map<String, Integer> jobs = getJobs();
        for (Entry<String, Integer> chr : jobs.entrySet()) {
          sb.append("\r\n").append(chr.getKey()).append(" : ").append(chr.getValue());
        }
        return sb.toString();
      }
    }
    return null;
  }

  public final String getType() {
    return type;
  }

  public final Map<String, Integer> getJobs() {
    final Map<String, Integer> jobs = new LinkedHashMap<String, Integer>();
    for (Entry<String, String> chr : members.entrySet()) {
      if (jobs.containsKey(chr.getValue())) {
        jobs.put(chr.getValue(), jobs.get(chr.getValue()) + 1);
      } else {
        jobs.put(chr.getValue(), 1);
      }
    }
    return jobs;
  }

  public String getNextPlayer() {
    StringBuilder sb = new StringBuilder("\nQueued members : ");
    sb.append("#b").append(squadType.queue.get(ch).size()).append(" #k ").append("List of participants : \n\r ");
    int i = 0;
    for (Pair<String, Long> chr : squadType.queue.get(ch)) {
      i++;
      sb.append(i).append(" : ").append(chr.left);
      sb.append(" \n\r ");
    }
    sb.append("Would you like to #ebe next#n in the queue, or #ebe removed#n from the queue if you are in it?");
    return sb.toString();
  }

  public void setNextPlayer(String i) {
    Pair<String, Long> toRemove = null;
    for (Pair<String, Long> s : squadType.queue.get(ch)) {
      if (s.left.equals(i)) {
        toRemove = s;
        break;
      }
    }
    if (toRemove != null) {
      squadType.queue.get(ch).remove(toRemove);
      return;
    }
    for (ArrayList<Pair<String, Long>> v : squadType.queue.values()) {
      for (Pair<String, Long> s : v) {
        if (s.left.equals(i)) {
          return;
        }
      }
    }
    squadType.queue.get(ch).add(new Pair<>(i, System.currentTimeMillis()));
  }

  public List<Pair<String, Long>> getAllNextPlayer() {
    return squadType.queue.get(ch);
  }
}
