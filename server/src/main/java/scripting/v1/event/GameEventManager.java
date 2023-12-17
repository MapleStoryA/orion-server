package scripting.v1.event;

import client.MapleCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static scripting.v1.event.GameEventManager.EventStatus.REQUIRES_PARTY_LEADER_PARTY;

public class GameEventManager {

    private final int channel;
    private final Map<String, Event> events = new ConcurrentHashMap<>();

    public GameEventManager(int channel) {
        this.channel = channel;
    }

    public void onCharacterDisconnect(MapleCharacter player) {
        player.unregisterEvent(player.getEvent());
    }

    public void onCharacterJoinParty(MapleCharacter player) {
        player.getEvent().onCharacterJoinParty(player);

    }

    public void onCharacterLeaveParty(MapleCharacter player) {
        player.getEvent().onCharacterLeaveParty(player);
    }

    public void onCharacterDie(MapleCharacter player) {
        player.getEvent().onCharacterDie(player);
    }

    public int create(MapleCharacter player, String eventName) {
        if (isInEvent(player)) {
            return EventStatus.ALREADY_JOINED_EVENT;
        }
        MapleParty party = player.getParty();
        if (party == null || party.isDisbanded()) {
            return EventStatus.REQUIRES_PARTY;
        }
        if (!party.containsMembers(new MaplePartyCharacter(player))) {
            return REQUIRES_PARTY_LEADER_PARTY;
        }
        Event event = new Event(player.getChannelServer().getChannel(), eventName, this);
        events.put(eventName, event);
        player.registerEvent(event);
        return EventStatus.CREATED;
    }

    public void onEventFinish(String eventName) {
        events.remove(eventName);
    }

    private boolean isInEvent(MapleCharacter player) {
        return player.getEvent() != null;
    }


    class EventStatus {
        public static final int ALREADY_JOINED_EVENT = -1;
        public static final int CREATED = 1;
        public static final int REQUIRES_PARTY = -2;
        public static final int REQUIRES_PARTY_LEADER_PARTY = -3;
    }
}
