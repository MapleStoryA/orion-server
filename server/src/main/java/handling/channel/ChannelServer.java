package handling.channel;

import client.MapleCharacter;
import handling.GameServer;
import handling.PacketProcessor;
import handling.login.LoginServer;
import handling.world.WorldServer;
import handling.world.helper.CheaterData;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import scripting.EventScriptManager;
import scripting.v1.event.GameEventManager;
import server.MapleSquad;
import server.TimerManager;
import server.autosave.AutoSaveRunnable;
import server.config.Config;
import server.config.ServerConfig;
import server.events.MapleCoconut;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import server.shops.HiredMerchant;
import tools.CollectionUtil;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class ChannelServer extends GameServer {

    public static final short DEFAULT_PORT = 8585;
    private final String publicAddress;
    private final MapleMapFactory mapFactory;
    private final Map<String, MapleSquad> mapleSquads = new HashMap<>();
    private final Map<Integer, HiredMerchant> merchants = new HashMap<>();
    private final Map<Integer, PlayerNPC> playerNPCs = new HashMap<>();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock squadLock = new ReentrantReadWriteLock();
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    private final AramiaFireWorks aramiaEvent;
    public long serverStartTime;
    private int expRate, mesoRate, dropRate, cashRate;
    private int running_MerchantID = 0;
    private int flags = 0;
    private String serverMessage;
    private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false, adminOnly = false;
    private PlayerStorage players;
    private EventScriptManager eventSM;
    private int eventmap = -1;
    private final GameEventManager gameEventManager;

    public ChannelServer(int channel, int port) {
        super(channel, port, PacketProcessor.Mode.CHANNELSERVER);
        Config.World wordConfig = ServerConfig.serverConfig().getConfig().getWorld();
        this.expRate = wordConfig.getExp();
        this.mesoRate = wordConfig.getMeso();
        this.dropRate = wordConfig.getDrop();
        this.cashRate = wordConfig.getCash();
        this.serverMessage = wordConfig.getServerMessage();
        this.flags = wordConfig.getFlags();
        this.adminOnly = wordConfig.isAdminOnly();

        Config.Channel channelConfig = ServerConfig.serverConfig().getConfig().getChannel();
        this.publicAddress = channelConfig.getHost() + ":" + port;
        this.mapFactory = new MapleMapFactory();
        this.aramiaEvent = new AramiaFireWorks();
        this.eventSM = new EventScriptManager(this, channelConfig.getEvents());
        this.mapFactory.setChannel(channel);
        this.players = new PlayerStorage(channel);
        this.serverStartTime = System.currentTimeMillis();
        this.gameEventManager = new GameEventManager(this);
    }

    @Override
    public void onStart() {
        LoginServer.getInstance().addChannel(channel);
        scheduleAutoSaver();
        loadEvents();
        eventSM.init();
        log.info("Exp:" + expRate);
        log.info("Meso:" + mesoRate);
        log.info("Drop:" + dropRate);
        log.info("Cash:" + cashRate);
    }

    public void loadEvents() {
        if (events.size() != 0) {
            return;
        }
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut.getMapIds()));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness.getMapIds()));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla.getMapIds()));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz.getMapIds()));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball.getMapIds()));
    }

    @Override
    public void shutdown() {
        if (finishedShutdown) {
            return;
        }
        broadcastPacket(MaplePacketCreator.serverNotice(0, "This channel will now shut down."));
        // dc all clients by hand so we get sessionClosed...
        shutdown = true;

        log.info("Channel " + channel + ", Saving hired merchants...");
        closeAllMerchant();

        log.info("Channel " + channel + ", Saving characters...");

        getPlayerStorage().disconnectAll();

        log.info("Channel " + channel + ", Unbinding...");

        super.shutdown();

        WorldServer.getInstance().removeChannel(channel);
        LoginServer.getInstance().removeChannel(channel);
        setFinishShutdown();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public void addPlayer(final MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
        chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
    }

    public PlayerStorage getPlayerStorage() {
        if (players == null) {
            players = new PlayerStorage(channel);
        }
        return players;
    }

    public void removePlayer(final MapleCharacter chr) {
        getPlayerStorage().deregisterPlayer(chr);
    }

    public void removePlayer(final int idz, final String namez) {
        getPlayerStorage().deregisterPlayer(idz, namez);
    }

    public void setServerMessage(final String newMessage) {
        serverMessage = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
    }

    public void broadcastPacket(final byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public void broadcastSuperMegaPacket(final byte[] data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public void broadcastGMPacket(final byte[] data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public int getExpRate() {
        return expRate;
    }

    public void setExpRate(final int expRate) {
        this.expRate = expRate;
    }

    public int getCashRate() {
        return cashRate;
    }

    public void setCashRate(final int cashRate) {
        this.cashRate = cashRate;
    }

    public int getChannel() {
        return channel;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void reloadEvents() {
        Config.Channel channelConfig = ServerConfig.serverConfig().getConfig().getChannel();
        eventSM.cancel();
        eventSM = new EventScriptManager(this, channelConfig.getEvents());
        eventSM.init();
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public void setMesoRate(final int mesoRate) {
        this.mesoRate = mesoRate;
    }

    public int getDropRate() {
        return dropRate;
    }

    public void setDropRate(final int dropRate) {
        this.dropRate = dropRate;
    }

    public Map<String, MapleSquad> getAllSquads() {
        squadLock.readLock().lock();
        try {
            return Collections.unmodifiableMap(mapleSquads);
        } finally {
            squadLock.readLock().unlock();
        }
    }

    public MapleSquad getMapleSquad(final String type) {
        squadLock.readLock().lock();
        try {
            return mapleSquads.get(type.toLowerCase());
        } finally {
            squadLock.readLock().unlock();
        }
    }

    public boolean addMapleSquad(final MapleSquad squad, final String type) {
        squadLock.writeLock().lock();
        try {
            if (!mapleSquads.containsKey(type.toLowerCase())) {
                mapleSquads.put(type.toLowerCase(), squad);
                return true;
            }
        } finally {
            squadLock.writeLock().unlock();
        }
        return false;
    }

    public boolean removeMapleSquad(final String type) {
        squadLock.writeLock().lock();
        try {
            if (mapleSquads.containsKey(type.toLowerCase())) {
                mapleSquads.remove(type.toLowerCase());
                return true;
            }
        } finally {
            squadLock.writeLock().unlock();
        }
        return false;
    }

    public void closeAllMerchant() {
        merchLock.writeLock().lock();
        try {
            final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
            while (merchants_.hasNext()) {
                merchants_.next().closeShop(true, false);
                merchants_.remove();
            }
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public int addMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();

        int runningMerchantId = 0;
        try {
            runningMerchantId = running_MerchantID;
            merchants.put(running_MerchantID, hMerchant);
            running_MerchantID++;
        } finally {
            merchLock.writeLock().unlock();
        }
        return runningMerchantId;
    }

    public void removeMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();

        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public boolean containsMerchant(final int accountId) {
        boolean contains = false;

        merchLock.readLock().lock();
        try {
            final Iterator<HiredMerchant> itr = merchants.values().iterator();

            while (itr.hasNext()) {
                if (itr.next().getOwnerAccId() == accountId) {
                    contains = true;
                    break;
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return contains;
    }

    public List<HiredMerchant> searchMerchant(final int itemSearch) {
        final List<HiredMerchant> list = new LinkedList<HiredMerchant>();
        merchLock.readLock().lock();
        try {
            final Iterator<HiredMerchant> itr = merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = itr.next();
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return list;
    }

    public void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }

    public boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }

    public int getEvent() {
        return eventmap;
    }

    public void setEvent(final int ze) {
        this.eventmap = ze;
    }

    public MapleEvent getEvent(final MapleEventType t) {
        return events.get(t);
    }

    public Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs.values();
    }

    public void addPlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.containsKey(npc.getId())) {
            removePlayerNPC(npc);
        }
        playerNPCs.put(npc.getId(), npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public void removePlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.containsKey(npc.getId())) {
            playerNPCs.remove(npc.getId());
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public void setShutdown() {
        this.shutdown = true;
        super.shutdown();
        log.info("Channel " + channel + " has set to shutdown and is closing Hired Merchants...");
    }

    public void setFinishShutdown() {
        this.finishedShutdown = true;
        log.info("Channel " + channel + " has finished shutdown.");
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public int getTempFlag() {
        return flags;
    }

    public int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = getPlayerStorage().getCheaters();

        Collections.sort(cheaters);
        return CollectionUtil.copyFirst(cheaters, 20);
    }

    public List<CheaterData> getReports() {
        List<CheaterData> cheaters = getPlayerStorage().getReports();

        Collections.sort(cheaters);
        return CollectionUtil.copyFirst(cheaters, 20);
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSuperMegaPacket(message);
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(message);
    }

    public void broadcastYellowMsg(String msg) {
        for (@SuppressWarnings("unused") MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            broadcastPacket(MaplePacketCreator.yellowChat(msg));
        }
    }

    public AramiaFireWorks getAramiaEvent() {
        return this.aramiaEvent;
    }

    public HiredMerchant getMerchant(MapleCharacter p) {
        for (Entry<Integer, HiredMerchant> m : merchants.entrySet()) {
            if (m.getValue().getOwnerId() == p.getId()) {
                return m.getValue();
            }
        }
        return null;
    }

    private void scheduleAutoSaver() {
        TimerManager.getInstance().register(new AutoSaveRunnable(this), 1000 * 180);
    }

    public GameEventManager getGameEventManager() {
        return gameEventManager;
    }
}
