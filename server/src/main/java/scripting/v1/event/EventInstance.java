package scripting.v1.event;

import client.MapleCharacter;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.game.TargetScripting;
import scripting.v1.game.helper.BindingHelper;
import scripting.v1.event.engine.EventEngine;
import scripting.v1.event.engine.RhinoEventEngine;
import server.life.MapleMonster;
import server.maps.MapleMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventInstance {
    private final int channel;
    private final String name;
    private final EventEngine engine;
    private final EventCenter eventCenter;
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();
    private final Map<Integer, MapleMap> maps = new HashMap<>();
    private final Map<String, MapleCharacter> members = new HashMap<>();
    private final ScheduledThreadPoolExecutor pool;
    private final String key;
    private boolean isActive;
    private MapleCharacter leader;
    private int currentEventMap;

    public EventInstance(String key, int channel, String name, EventCenter eventCenter) {
        super();
        this.key = key;
        this.channel = channel;
        this.name = name;
        this.engine = new RhinoEventEngine(name);
        this.eventCenter = eventCenter;
        this.pool = new ScheduledThreadPoolExecutor(4);
        engine.loadScript();
        engine.addToContext("event", new EventScripting(this));
        engine.addToContext("instance", this);
        engine.addToContext("channel", channel);
        engine.addToContext("name", name);
        engine.addToContext("field", name);
    }

    public boolean isActive() {
        return isActive;
    }

    public void addEventMap(int eventMap) {
        MapleMap map = WorldServer.getInstance().getChannel(channel).getMapFactory().getMap(eventMap);
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
        MapleMap oldMap = WorldServer.getInstance().getChannel(channel).getMapFactory().getMap(currentEventMap);
        oldMap.setNewEventInstance(null);
        currentEventMap = eventMap;
        MapleMap map = WorldServer.getInstance().getChannel(channel).getMapFactory().getMap(eventMap);
        engine.addToContext("field", map.getField());


    }

    public void scheduleTask(String method, int seconds) {
        pool.schedule(() -> {
            try {
                engine.invokeAction(method);

            } catch (Exception ex) {
                log.info(ex.getMessage());
            }
        }, seconds, TimeUnit.SECONDS);

    }


    public void onPlayerDisconnected(MapleCharacter player) {
        if (leader == player && members.size() > 1) {
            leader = members.get(0);
            engine.onPlayerDisconnected(BindingHelper.wrapCharacter(leader));
        } else if (members.size() >= 1) {
            engine.onPlayerDisconnected(BindingHelper.wrapCharacter(leader));
        } else {
            this.clear();
        }
    }

    public void onPlayerJoinParty(MapleCharacter player) {
        engine.onPlayerDisconnected(BindingHelper.wrapCharacter(player));
    }

    public void onMobKilled(MapleCharacter killer, MapleMonster monster) {
        engine.onMobKilled(BindingHelper.wrapCharacter(killer), monster);
    }

    public void onPlayerLeaveParty(MapleCharacter player) {
        engine.onPlayerLeave(BindingHelper.wrapCharacter(player));
    }

    public void onPlayerDied(MapleCharacter player) {
        engine.onPlayerDied(BindingHelper.wrapCharacter(player));
    }

    public void onPartyDisband(MapleCharacter player) {
        engine.onPlayerDied(BindingHelper.wrapCharacter(player));
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

    public TargetScripting getLeader() {
        return new TargetScripting(leader.getClient());
    }

    public void setLeader(MapleCharacter player) {
        this.leader = player;
        this.members.put(player.getName(), player);
        engine.removeFromContext(key);
        engine.addToContext("leader", getLeader());
    }

    public void onPlayerExitMap(MapleCharacter mapleCharacter, MapleMap map) {
        engine.onPlayerExitMap(BindingHelper.wrapCharacter(mapleCharacter), map);
    }


}
