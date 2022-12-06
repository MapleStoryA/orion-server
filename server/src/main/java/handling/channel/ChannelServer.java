/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.channel;

import client.MapleCharacter;
import handling.PacketProcessor;
import handling.login.GameServer;
import handling.login.LoginServer;
import handling.world.CheaterData;
import handling.world.WorldServer;
import scripting.EventScriptManager;
import scripting.v1.event.EventCenter;
import server.MapleSquad;
import server.ServerProperties;
import server.TimerManager;
import server.autosave.AutoSaveRunnable;
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

@lombok.extern.slf4j.Slf4j
public class ChannelServer extends GameServer {

    public static final short DEFAULT_PORT = 8585;
    private final String ip;
    private final MapleMapFactory mapFactory;
    private final Map<String, MapleSquad> mapleSquads = new HashMap<>();
    private final Map<Integer, HiredMerchant> merchants = new HashMap<>();
    private final Map<Integer, PlayerNPC> playerNPCs = new HashMap<>();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock squadLock = new ReentrantReadWriteLock();
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    private final AramiaFireWorks aramiaEvent;
    private final EventCenter eventCenter;
    public long serverStartTime;
    private int expRate, mesoRate, dropRate, cashRate;
    private int running_MerchantID = 0;
    private int flags = 0;
    private String serverMessage;
    private String serverName;
    private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false, adminOnly = false;
    private PlayerStorage players;
    private EventScriptManager eventSM;
    private int eventmap = -1;


    public ChannelServer(int channel, int port) {
        super(channel, port, PacketProcessor.Mode.CHANNELSERVER);
        this.expRate = Integer.parseInt(ServerProperties.getProperty("world.exp"));
        this.mesoRate = Integer.parseInt(ServerProperties.getProperty("world.meso"));
        this.dropRate = Integer.parseInt(ServerProperties.getProperty("world.drop"));
        this.cashRate = Integer.parseInt(ServerProperties.getProperty("world.cash"));
        this.serverMessage = ServerProperties.getProperty("world.serverMessage");
        this.serverName = ServerProperties.getProperty("login.serverName");
        this.flags = Integer.parseInt(ServerProperties.getProperty("world.flags", "0"));
        this.adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("world.admin", "false"));
        this.ip = ServerProperties.getProperty("channel.net.interface") + ":" + port;
        this.mapFactory = new MapleMapFactory();
        this.aramiaEvent = new AramiaFireWorks();
        this.eventCenter = new EventCenter(channel);
        this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("channel.events").split(","));
        this.mapFactory.setChannel(channel);
        this.players = new PlayerStorage(channel);
        this.serverStartTime = System.currentTimeMillis();
        LoginServer.getInstance().addChannel(channel);
        scheduleAutoSaver();
        loadEvents();
        eventSM.init();
        log.info("Exp:" + expRate);
        log.info("Meso:" + mesoRate);
        log.info("Drop:" + dropRate);
        log.info("Cash:" + cashRate);
    }

    private void scheduleAutoSaver() {
        TimerManager.getInstance().register(new AutoSaveRunnable(this), 1000 * 180);
    }


    public void loadEvents() {
        if (events.size() != 0) {
            return;
        }
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut.mapids));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness.mapids));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla.mapids));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz.mapids));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball.mapids));
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

        unbindAcceptor();

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

    public String getIP() {
        return ip;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void reloadEvents() {
        eventSM.cancel();
        eventSM = new EventScriptManager(this, ServerProperties.getProperty("channel.events").split(","));
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

        int runningmer = 0;
        try {
            runningmer = running_MerchantID;
            merchants.put(running_MerchantID, hMerchant);
            running_MerchantID++;
        } finally {
            merchLock.writeLock().unlock();
        }
        return runningmer;
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

    public PlayerNPC getPlayerNPC(final int id) {
        return playerNPCs.get(id);
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String sn) {
        this.serverName = sn;
    }

    public int getPort() {
        return port;
    }

    public void setShutdown() {
        this.shutdown = true;
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


    public EventCenter getEventCenter() {
        return this.eventCenter;
    }
}
