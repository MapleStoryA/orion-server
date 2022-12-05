package handling.world;

import handling.channel.ChannelServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldServer {

    private Map<Integer, ChannelServer> channels = new ConcurrentHashMap<>();

    private static WorldServer INSTANCE;


    public void registerChannel(int channel, ChannelServer ch) {
        this.channels.put(channel, ch);
    }

    public void removeChannel(int channel) {
        this.channels.get(channel);
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
