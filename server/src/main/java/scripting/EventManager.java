/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; w"ithout even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scripting;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import server.MapleSquad;
import server.Randomizer;
import server.Timer.EventTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.*;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class EventManager {

  private static int eventChannel = 1;
  private Invocable iv;
  private int channel;
  private Map<String, EventInstanceManager> instances = new WeakHashMap<String, EventInstanceManager>();
  private Properties props = new Properties();
  private String name;

  public EventManager(ChannelServer cserv, Invocable iv, String name) {
    this.iv = iv;
    this.channel = cserv.getChannel();
    this.name = name;
  }

  public void cancel() {
    try {
      iv.invokeFunction("cancelSchedule", (Object) null);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
    }
  }

  public ScheduledFuture<?> schedule(final String methodName, long delay) {
    return EventTimer.getInstance().schedule(new Runnable() {

      public void run() {
        try {
          iv.invokeFunction(methodName, (Object) null);
        } catch (Exception ex) {
          System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
          FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
        }
      }
    }, delay);
  }

  public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
    return EventTimer.getInstance().schedule(new Runnable() {

      public void run() {
        try {
          iv.invokeFunction(methodName, eim);
        } catch (Exception ex) {
          System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
          FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
        }
      }
    }, delay);
  }

  public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
    return EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {

      public void run() {
        try {
          iv.invokeFunction(methodName, (Object) null);
        } catch (ScriptException ex) {
          System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
        } catch (NoSuchMethodException ex) {
          System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
        }
      }
    }, timestamp);
  }

  public int getChannel() {
    return channel;
  }

  public ChannelServer getChannelServer() {
    return ChannelServer.getInstance(channel);
  }

  public EventInstanceManager getInstance(String name) {
    return instances.get(name);
  }

  public Collection<EventInstanceManager> getInstances() {
    return Collections.unmodifiableCollection(instances.values());
  }

  public EventInstanceManager newInstance(String name) {
    EventInstanceManager ret = new EventInstanceManager(this, name, channel);
    instances.put(name, ret);
    return ret;
  }

  public void disposeInstance(String name) {
    instances.remove(name);
    if (getProperty("state") != null && instances.size() == 0) {
      setProperty("state", "0");
    }
    if (getProperty("leader") != null && instances.size() == 0 && getProperty("leader").equals("false")) {
      setProperty("leader", "true");
    }
    if (this.name.equals("CWKPQ")) { //hard code it because i said so
      final MapleSquad squad = ChannelServer.getInstance(channel).getMapleSquad("CWKPQ");//so fkin hacky
      if (squad != null) {
        squad.clear();
      }
    }
  }

  public Invocable getIv() {
    return iv;
  }

  public void setProperty(String key, String value) {
    props.setProperty(key, value);
  }

  public String getProperty(String key) {
    return props.getProperty(key);
  }

  public final Properties getProperties() {
    return props;
  }

  public String getName() {
    return name;
  }

  public void startInstance() {
    try {
      iv.invokeFunction("setup", (Object) null);
    } catch (Exception ex) {
      ex.printStackTrace();
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
    }
  }

  public void startInstance(String mapid, MapleCharacter chr) {
    try {
      EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
      eim.registerCarnivalParty(chr, chr.getMap(), (byte) 0);
    } catch (Exception ex) {
      ex.printStackTrace();
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
    }
  }

  //GPQ
  public void startInstance(MapleCharacter character, String leader) {
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
      eim.registerPlayer(character);
      eim.setProperty("leader", leader);
      eim.setProperty("guildid", String.valueOf(character.getGuildId()));
      setProperty("guildid", String.valueOf(character.getGuildId()));
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-Guild:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-Guild:\n" + ex);
    }
  }

  public void startInstance_CharID(MapleCharacter character) {
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", character.getId()));
      eim.registerPlayer(character);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-CharID:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-CharID:\n" + ex);
    }
  }

  public void startInstance(MapleCharacter character) {
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
      eim.registerPlayer(character);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
    }
  }

  //PQ method: starts a PQ
  public void startInstance(MapleParty party, MapleMap map) {
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", party.getId()));
      eim.registerParty(party, map);
    } catch (ScriptException ex) {
      System.out.println("Event name : " + name + ", method Name : setup-partyid:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-partyid:\n" + ex);
    } catch (Exception ex) {
      //ignore
      startInstance_NoID(party, map, ex);
    }
  }

  public void startInstance_NoID(MapleParty party, MapleMap map) {
    startInstance_NoID(party, map, null);
  }

  public void startInstance_NoID(MapleParty party, MapleMap map, final Exception old) {
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
      eim.registerParty(party, map);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-party:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-party:\n" + ex + "\n" + (old == null ? "no old exception" : old));
    }
  }

  //non-PQ method for starting instance
  public void startInstance(EventInstanceManager eim, String leader) {
    try {
      iv.invokeFunction("setup", eim);
      eim.setProperty("leader", leader);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-leader:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-leader:\n" + ex);
    }
  }

  public void startInstance(MapleSquad squad, MapleMap map) {

    if (squad.getStatus() == 0) {
      return; //we dont like cleared squads
    }
    if (!squad.getLeader().isGM()) {
      if (squad.getMembers().size() < 1) { //less than 3
        squad.getLeader().dropMessage(5, "The squad has less than 1 people participating.");
        return;
      }
      if (name.equals("CWKPQ")) { //so fkin hacky
        if (squad.getMembers().size() < 10) {
          squad.getLeader().dropMessage(5, "The squad has less than 10 people participating.");
          return;
        }
        if (squad.getJobs().size() < 5) {
          squad.getLeader().dropMessage(5, "The squad requires members from every type of job.");
          return;
        }
      }
    }
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeaderName()));
      eim.registerSquad(squad, map);
    } catch (Exception ex) {
      System.out.println("Event name : " + name + ", method Name : setup-squad:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-squad:\n" + ex);
    }
  }

  public void startInstance_Party(String mapid, MapleCharacter chr) {
    try {
      EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
      eim.registerParty(chr.getParty(), chr.getMap());
    } catch (NoSuchMethodException | ScriptException ex) {
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\r\n" + ex);
    }
  }

  public void warpAllPlayer(int from, int to) {
    final MapleMap tomap = getMapFactory().getMap(to);
    final MapleMap frommap = getMapFactory().getMap(from);
    List<MapleCharacter> list = frommap.getCharactersThreadsafe();
    if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
      for (MapleMapObject mmo : list) {
        ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
      }
    }
  }

  public MapleMapFactory getMapFactory() {
    return getChannelServer().getMapFactory();
  }

  public OverrideMonsterStats newMonsterStats() {
    return new OverrideMonsterStats();
  }

  public List<MapleCharacter> newCharList() {
    return new ArrayList<MapleCharacter>();
  }

  public MapleMonster getMonster(final int id) {
    return MapleLifeFactory.getMonster(id);
  }

  public void broadcastShip(final int mapid, final int effect) {
    getMapFactory().getMap(mapid).broadcastMessage(MaplePacketCreator.boatPacket(effect));
  }

  public void broadcastYellowMsg(final String msg) {
    getChannelServer().broadcastPacket(MaplePacketCreator.yellowChat(msg));
  }

  public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
    if (!weather) {
      getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
    } else {
      for (MapleMap load : getMapFactory().getAllMaps()) {
        if (load.getCharactersSize() > 0) {
          load.startMapEffect(msg, type);
        }
      }
    }
  }

  public boolean scheduleRandomEvent() {
    if (getChannel() != eventChannel) {
      return false;
    }
    MapleEventType t = null;
    while (t == null) {
      for (MapleEventType x : MapleEventType.values()) {
        if (Randomizer.nextInt(MapleEventType.values().length) == 0) {
          t = x;
          break;
        }
      }
    }
    final String msg = MapleEvent.scheduleEvent(t, getChannelServer());
    if (msg.length() > 0) {
      broadcastYellowMsg(msg);
      return false;
    }
    schedule("seal", 120000);
    return true;
  }

  public void sealEvent() {
    MapleEvent.setEvent(getChannelServer(), true);
    broadcastServerMsg(0, "Entries for the event are now closed!", false);
  }

  public void setWorldEvent() {
    eventChannel = Randomizer.nextInt(7) + 2; //2-8
  }

  public void startInstance(MapleSquad squad, MapleMap map, int questID) {
    if (squad.getStatus() == 0) {
      return; //we dont like cleared squads
    }
    if (!squad.getLeader().isGM()) {
      if (name.equals("CWKPQ") && squad.getJobs().size() < 5) {
        squad.getLeader().dropMessage(5, "The squad requires members from every type of job.");
        return;
      }
    }
    try {
      EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeaderName()));
      eim.registerSquad(squad, map);
    } catch (NoSuchMethodException | ScriptException ex) {
      System.out.println("Event name : " + name + ", method Name : setup-squad:\n" + ex);
      FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-squad:\n" + ex);
    }
  }

  public void set2xExpEvent(boolean enabled) {
    for (ChannelServer channel : ChannelServer.getAllInstances()) {
      if (enabled) {
        channel.setExpRate(channel.getExpRate() * 2);
      } else {
        channel.setExpRate(channel.getExpRate() * 1);
      }
    }
  }

  public MapleReactor getReactor(final int id) {
    return new MapleReactor(MapleReactorFactory.getReactor(id), id);
  }
}
