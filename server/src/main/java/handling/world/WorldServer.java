package handling.world;

import handling.channel.ChannelServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO: Will do something with this to manage the server instances
public class WorldServer {

    private Map<Integer, ChannelServer> channels = new ConcurrentHashMap<>();

    private static WorldServer INSTANCE;


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
