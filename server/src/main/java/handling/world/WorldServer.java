package handling.world;

import client.anticheat.CheaterData;
import database.AccountData;
import handling.MigrationService;
import handling.MigrationServiceImpl;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.PlayerStorage;
import handling.world.helper.CharacterTransfer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import server.base.timer.Timer;
import tools.collection.CollectionUtil;

@Slf4j
public class WorldServer {

    private static WorldServer INSTANCE;
    private final Map<Integer, ChannelServer> channels = new ConcurrentHashMap<>();
    private final Map<String, AccountData> connectedAccounts = new ConcurrentHashMap<>();
    private final long serverStarTime;

    @Getter
    private final MigrationService migrationService;

    public WorldServer() {
        this.serverStarTime = System.currentTimeMillis();
        this.migrationService = new MigrationServiceImpl();
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
        channels.values().stream().forEach(ChannelServer::shutdown);
        channels.clear();
    }

    public int getChannelCount() {
        return channels.size();
    }

    public Map<Integer, Integer> getChannelLoad() {
        Map<Integer, Integer> ret = new HashMap<>();
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

    public boolean isConnectedLogin(String login) {
        return connectedAccounts.containsKey(login);
    }

    public void registerConnectedAccount(AccountData accountData) {
        connectedAccounts.put(accountData.getName(), accountData);
    }

    public void removeConnectedAccount(String name) {
        connectedAccounts.remove(name);
    }

    public static void initTimers() {
        Timer.WorldTimer.getInstance().start();
        Timer.EtcTimer.getInstance().start();
        Timer.MapTimer.getInstance().start();
        Timer.MobTimer.getInstance().start();
        Timer.CloneTimer.getInstance().start();
        Timer.EventTimer.getInstance().start();
        Timer.BuffTimer.getInstance().start();
        Timer.PingTimer.getInstance().start();
    }
}
