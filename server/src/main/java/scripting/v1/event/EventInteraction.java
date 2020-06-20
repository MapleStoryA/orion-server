package scripting.v1.event;

import client.MapleCharacter;
import org.mozilla.javascript.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripting.v1.NewNpcTalkHandler;
import scripting.v1.binding.BindingWrapper;
import scripting.v1.binding.TargetScript;
import tools.MaplePacketCreator;

import java.util.Collection;

/**
 * Implements the interaction API.
 */
public class EventInteraction {

  private static final Logger LOG = LoggerFactory.getLogger(EventInteraction.class);

  private final EventCenter manager;
  private final EventInstance instance;

  public EventInteraction(String name, int channel, EventCenter manager, EventInstance instance) {
    super();
    this.manager = manager;
    this.instance = instance;
  }

  public void schedule(String method, int seconds) {
    instance.scheduleTask(method, seconds);
  }

  public void sendClock(int seconds) {
    instance.broadcastPacket(MaplePacketCreator.getClock(seconds));
  }

  public void destroyClock() {
    instance.broadcastPacket(MaplePacketCreator.stopClock());
  }

  public String getProperty(String key) {
    return instance.getProperty(key);
  }

  public void registerTransferField(int map, String portal) {
    for (TargetScript member : BindingWrapper.wrapCharacter(instance.getMembers())) {
      member.registerTransferField(map, portal);
    }
  }

  public TargetScript getMemberByName(String name) {
    return BindingWrapper.wrapCharacter(instance.getMemberByName(name));
  }

  public Collection<TargetScript> getMembers() {
    return BindingWrapper.wrapCharacter(instance.getMembers());
  }

  public void startNpc(int id) {
    //Because of Rhino contexts, it must be in another thread.
    Context.exit();
    for (MapleCharacter member : instance.getMembers()) {
      NewNpcTalkHandler.startConversation(id, member.getClient());
    }
  }

  public void gainPartyExp(int exp) {
    for (TargetScript member : BindingWrapper.wrapCharacter(instance.getMembers())) {
      member.incEXP(exp, true);
    }
  }

  public TargetScript getLeader() {
    return instance.getLeader();
  }


  public void log(String message) {
    System.out.println(message);
  }

  public void clear() {
    instance.clear();
  }

  public void setCurrentMap(int map) {
    instance.setCurrentMap(map);
    ;
  }

}
