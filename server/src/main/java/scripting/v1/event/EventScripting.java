package scripting.v1.event;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.Context;
import scripting.v1.game.helper.NpcTalkHelper;
import scripting.v1.game.TargetScripting;
import scripting.v1.game.helper.BindingHelper;
import tools.MaplePacketCreator;

import java.util.Collection;

@Slf4j
public class EventScripting {


    private final EventInstance instance;

    public EventScripting(EventInstance instance) {
        super();
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
        for (TargetScripting member : BindingHelper.wrapCharacter(instance.getMembers())) {
            member.registerTransferField(map, portal);
        }
    }

    public TargetScripting getMemberByName(String name) {
        return BindingHelper.wrapCharacter(instance.getMemberByName(name));
    }

    public Collection<TargetScripting> getMembers() {
        return BindingHelper.wrapCharacter(instance.getMembers());
    }

    public void startNpc(int id) {
        //Because of Rhino contexts, it must be in another thread.
        Context.exit();
        for (MapleCharacter member : instance.getMembers()) {
            NpcTalkHelper.startConversation(id, member.getClient());
        }
    }

    public void gainPartyExp(int exp) {
        for (TargetScripting member : BindingHelper.wrapCharacter(instance.getMembers())) {
            member.incEXP(exp, true);
        }
    }

    public TargetScripting getLeader() {
        return instance.getLeader();
    }


    public void log(String message) {
        log.info(message);
    }

    public void clear() {
        instance.clear();
    }

    public void setCurrentMap(int map) {
        instance.setCurrentMap(map);
    }

}
