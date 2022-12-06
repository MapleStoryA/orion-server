package handling.world;

import handling.channel.ChannelServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//TODO: Will do something with this to manage the server instances
public class WorldServer {

    private Map<Integer, ChannelServer> channels = new ConcurrentHashMap<>();

    private static WorldServer INSTANCE;

    public Collection<ChannelServer> getAllChannels() {
        return channels.values();
    }

    public Set<Integer> getAllChannelIds() {
        return channels.keySet();
    }


    public void registerChannel(int channelIndex, ChannelServer ch) {
        this.channels.put(channelIndex, ch);
    }

    public void removeChannel(int channel) {
        this.channels.get(channel);
    }

    public void shutdown() {
        channels.values()
                .stream()
                .forEach(ChannelServer::shutdown);
        channels.clear();
    }

    public int getChannelCount() {
        return channels.size();
    }

    public Map<Integer, Integer> getChannelLoad() {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (ChannelServer cs : channels.values()) {
            ret.put(cs.getChannel(), cs.getConnectedClients());
        }
        return ret;
    }


    public ChannelServer getChannel(int ch) {
        return this.channels.get(ch);
    }

    public static synchronized WorldServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WorldServer();
        }
        return INSTANCE;
    }

}
