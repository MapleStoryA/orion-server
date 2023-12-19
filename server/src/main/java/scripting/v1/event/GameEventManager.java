package scripting.v1.event;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameEventManager {

    private final ChannelServer channelServer;
    private final Map<String, Event> events = new ConcurrentHashMap<>();

    public GameEventManager(ChannelServer channelServer) {
        this.channelServer = channelServer;
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
            return EventStatus.REQUIRES_PARTY_LEADER_PARTY;
        }
        String id = UUID.randomUUID().toString();
        Event event = new Event(id, eventName, this, channelServer);
        events.put(eventName, event);
        player.registerEvent(event);
        event.init();
        return EventStatus.CREATED;
    }

    private boolean isInEvent(MapleCharacter player) {
        return player.getEvent() != null;
    }

    public void onEventEnd(Event event) {
        events.remove(event.getName());
    }

    class EventStatus {
        public static final int ALREADY_JOINED_EVENT = -1;
        public static final int CREATED = 1;
        public static final int REQUIRES_PARTY = -2;
        public static final int REQUIRES_PARTY_LEADER_PARTY = -3;
    }

    public void onPlayerDisconnect(MapleCharacter player) {
        if (player.getEvent() != null) {}
    }

    public void onPlayerJoinParty(MapleCharacter player) {}

    public void onPlayerLeaveParty(MapleCharacter player) {}
}
