package handling.world;

import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.world.helper.CharacterTransfer;
import handling.world.helper.CheaterData;
import handling.world.helper.FindCommand;
import tools.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//TODO: Will do something with this to manage the server instances
@lombok.extern.slf4j.Slf4j
public class WorldServer {

    private static WorldServer INSTANCE;
    private final Map<Integer, ChannelServer> channels = new ConcurrentHashMap<>();
    private final long serverStarTime;

    public WorldServer() {
        this.serverStarTime = System.currentTimeMillis();
    }

    public static synchronized WorldServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WorldServer();
        }
        return INSTANCE;
    }

    public long getServerStartTime() {
        return serverStarTime;
    }

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

    public Map<Integer, Integer> getConnected() {
        Map<Integer, Integer> ret = new HashMap<>();
        int total = 0;
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            int curConnected = cs.getConnectedClients();
            ret.put(cs.getChannel(), curConnected);
            total += curConnected;
        }
        ret.put(0, total);
        return ret;
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> allCheaters = new ArrayList<>();
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            allCheaters.addAll(cs.getCheaters());
        }
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 20);
    }

    public List<CheaterData> getReports() {
        List<CheaterData> allCheaters = new ArrayList<>();
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            allCheaters.addAll(cs.getReports());
        }
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 20);
    }

    public boolean hasMerchant(int accountID) {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            if (cs.containsMerchant(accountID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCharacterListConnected(List<String> charName) {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            for (final String c : charName) {
                if (cs.getPlayerStorage().getCharacterByName(c) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isConnected(String charName) {
        return FindCommand.findChannel(charName) > 0;
    }

    public void getChangeChannelData(CharacterTransfer Data, int characterid, int toChannel) {
        getStorage(toChannel).registerPendingPlayer(Data, characterid);
    }

    public PlayerStorage getStorage(int channel) {
        if (channel == -10) {
            return CashShopServer.getInstance().getPlayerStorage();
        }
        return WorldServer.getInstance().getChannel(channel).getPlayerStorage();
    }

    public void toggleMegaphoneMuteState() {
        for (ChannelServer cs : WorldServer.getInstance().getAllChannels()) {
            cs.toggleMegaphoneMuteState();
        }
    }


}
