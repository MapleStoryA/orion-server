package scripting.v1.event;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripting.v1.binding.BindingWrapper;
import scripting.v1.binding.TargetScript;
import scripting.v1.dispatch.RealPacketDispatcher;
import scripting.v1.event.engine.EventEngine;
import scripting.v1.event.engine.RhinoEventEngine;
import server.life.MapleMonster;
import server.maps.MapleMap;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventInstance {
  private static final Logger LOG = LoggerFactory.getLogger(EventInteraction.class);
  private static final String SCRIPT_PATH = "dist/scripts/instances";
  private final int channel;
  private final String name;
  private boolean isActive;
  private final EventEngine engine;
  private MapleCharacter leader;
  private final EventCenter eventCenter;
  private final Map<String, String> attributes = new HashMap<>();
  private final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();
  private final Map<Integer, MapleMap> maps = new HashMap<>();
  private final Map<String, MapleCharacter> members = new HashMap<>();
  private final ScheduledThreadPoolExecutor pool;
  private int currentEventMap;
  private final String key;

  public EventInstance(String key, int channel, String name, EventCenter eventCenter) {
    super();
    this.key = key;
    this.channel = channel;
    this.name = name;
    this.engine = new RhinoEventEngine(name);
    this.eventCenter = eventCenter;
    this.pool = new ScheduledThreadPoolExecutor(4);
    engine.loadScript();
    engine.addToContext("event", new EventInteraction(name, channel, eventCenter, this));
    engine.addToContext("instance", this);
    engine.addToContext("channel", channel);
    engine.addToContext("name", name);
    engine.addToContext("field", name);
  }

  public void setLeader(MapleCharacter player) {
    this.leader = player;
    this.members.put(player.getName(), player);
    engine.removeFromContext(key);
    engine.addToContext("leader", getLeader());
  }

  public boolean isActive() {
    return isActive;
  }

  public void addEventMap(int eventMap) {
    MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(eventMap);
    map.setNewEventInstance(this);
    maps.put(eventMap, map);
  }

  public void onEventStart() {
    isActive = true;
    engine.onEventStart();
  }

  public void setCurrentMap(int eventMap) {
    if (!maps.containsKey(eventMap)) {
      addEventMap(eventMap);
    }
    MapleMap oldMap = ChannelServer.getInstance(channel).getMapFactory().getMap(currentEventMap);
    oldMap.setNewEventInstance(null);
    currentEventMap = eventMap;
    MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(eventMap);
    engine.addToContext("field", map.getField());


  }

  public void scheduleTask(String method, int seconds) {
    pool.schedule(new Runnable() {
      @Override
      public void run() {
        try {
          engine.invokeAction(method);

        } catch (Exception ex) {
          System.out.println(ex.getMessage());
        }
      }
    }, seconds, TimeUnit.SECONDS);

  }

  public void scheduleTaskAt(String method, Date date) {

  }

  public void onPlayerDisconnected(MapleCharacter player) {
    if (leader == player && members.size() > 1) {
      leader = members.get(0);
      engine.onPlayerDisconnected(BindingWrapper.wrapCharacter(leader));
    } else if (members.size() >= 1) {
      engine.onPlayerDisconnected(BindingWrapper.wrapCharacter(leader));
    } else {
      this.clear();
    }
  }

  public void onPlayerJoinParty(MapleCharacter player) {
    engine.onPlayerDisconnected(BindingWrapper.wrapCharacter(player));
  }

  public void onMobKilled(MapleCharacter killer, MapleMonster monster) {
    engine.onMobKilled(BindingWrapper.wrapCharacter(killer), monster);
  }

  public void onPlayerLeaveParty(MapleCharacter player) {
    engine.onPlayerLeave(BindingWrapper.wrapCharacter(player));
  }

  public void onPlayerDied(MapleCharacter player) {
    engine.onPlayerDied(BindingWrapper.wrapCharacter(player));
  }

  public void onPartyDisband(MapleCharacter player) {
    engine.onPlayerDied(BindingWrapper.wrapCharacter(player));
  }

  public void broadcastPacket(byte[] packet) {
    for (MapleCharacter player : members.values()) {
      player.getClient().sendPacket(packet);
    }
  }

  public void set(String key, String value) {
    this.attributes.put(key, value);
  }

  public String get(String key) {
    return attributes.get(key);
  }

  public String getProperty(String key) {
    return engine.getProperty(key);
  }

  public void clear() {
    pool.purge();
    pool.shutdownNow();
    for (Entry<String, ScheduledFuture<?>> entry : schedules.entrySet()) {
      entry.getValue().cancel(true);
    }
    for (Entry<String, MapleCharacter> entry : members.entrySet()) {
      entry.getValue().setNewEventInstance(null);
    }
    for (Entry<Integer, MapleMap> entry : maps.entrySet()) {
      entry.getValue().setNewEventInstance(null);
    }
    schedules.clear();
    eventCenter.unregister(key);

  }

  public Collection<MapleCharacter> getMembers() {
    return members.values();
  }

  public void addMember(MapleCharacter player) {
    members.put(player.getName(), player);
  }

  public MapleCharacter getMemberByName(String name) {
    return members.get(name);
  }

  public TargetScript getLeader() {
    return new TargetScript(leader.getClient(), new RealPacketDispatcher());
  }

  public void onPlayerExitMap(MapleCharacter mapleCharacter, MapleMap map) {
    engine.onPlayerExitMap(BindingWrapper.wrapCharacter(mapleCharacter), map);
  }


}
