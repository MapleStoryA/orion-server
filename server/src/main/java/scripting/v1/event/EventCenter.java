package scripting.v1.event;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EventCenter {

  private static final Logger LOG = LoggerFactory.getLogger(EventInteraction.class);

  private Map<String, EventInstance> events = new HashMap<>();

  private final int channel;

  public EventCenter(int channel) {
    this.channel = channel;
  }

  public EventInstance register(String name, MapleCharacter player) {
    String key = name + "_" + player.getName();
    EventInstance runningInstance = events.get(key);
    if (runningInstance != null && runningInstance.isActive()) {
      runningInstance.clear();
    }
    EventInstance instance = new EventInstance(key, channel, name, this);
    instance.setLeader(player);
    //instance.setCurrentMap(player.getMapId());
    player.setNewEventInstance(instance);
    events.put(key, instance);
    LOG.info("Event with id {} has been registered at channel {}", key, channel);
    return instance;
  }

  public EventInstance registerParty(String name, MapleCharacter player) {
    String key = name + "_" + player.getName();
    EventInstance runningInstance = events.get(key);
    if (runningInstance != null && runningInstance.isActive()) {
      return runningInstance;
    }
    EventInstance instance = new EventInstance(key, channel, name, this);
    MapleParty party = player.getParty();
    PlayerStorage storage = ChannelServer.getInstance(channel).getPlayerStorage();
    MapleCharacter leader = storage.getCharacterById(party.getLeader().getId());
    for (MaplePartyCharacter p : party.getMembers()) {
      MapleCharacter member = storage.getCharacterById(p.getId());
      instance.addMember(member);
      member.setNewEventInstance(instance);
    }
    leader.setNewEventInstance(instance);
    instance.setLeader(leader);
    //instance.setCurrentMap(leader.getMapId());
    LOG.info("Event with id {} has been registered at channel {}", key, channel);
    events.put(key, instance);
    return instance;
  }

  public void unregister(String name) {
    EventInstance e = events.get(name);
    events.remove(name);
  }


}
