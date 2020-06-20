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
import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.login.LoginServer;
import handling.mina.MapleCodecFactory;
import handling.world.CheaterData;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import scripting.EventScriptManager;
import scripting.v1.event.EventCenter;
import server.MapleSquad;
import server.ServerProperties;
import server.TimerManager;
import server.autosave.AutoSaveRunnable;
import server.events.*;
import server.gachapon.GachaponFactory;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import server.shops.HiredMerchant;
import tools.CollectionUtil;
import tools.MaplePacketCreator;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChannelServer implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  public static long serverStartTime;
  private int expRate, mesoRate, dropRate, cashRate;
  private short port = 8585;
  private static final short DEFAULT_PORT = 8585;
  private int channel, running_MerchantID = 0, flags = 0;
  private String serverMessage, ip, serverName;
  private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false, adminOnly = false;
  private PlayerStorage players;
  private MapleServerHandler serverHandler;
  private IoAcceptor acceptor;
  private final MapleMapFactory mapFactory;
  private EventScriptManager eventSM;
  private static final Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
  private final Map<String, MapleSquad> mapleSquads = new HashMap<String, MapleSquad>();
  private final Map<Integer, HiredMerchant> merchants = new HashMap<Integer, HiredMerchant>();
  private final Map<Integer, PlayerNPC> playerNPCs = new HashMap<Integer, PlayerNPC>();
  private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock(); //merchant
  private final ReentrantReadWriteLock squadLock = new ReentrantReadWriteLock(); //squad
  private int eventmap = -1;
  private final Map<MapleEventType, MapleEvent> events = new EnumMap<MapleEventType, MapleEvent>(MapleEventType.class);

  private AramiaFireWorks aramiaEvent;

  private EventCenter eventCenter;


  private ChannelServer(final int channel) {
    this.channel = channel;
    mapFactory = new MapleMapFactory();
    mapFactory.setChannel(channel);
    GachaponFactory.getInstance();
    aramiaEvent = new AramiaFireWorks();
    eventCenter = new EventCenter(channel);
    scheduleAutoSaver();
  }

  private void scheduleAutoSaver() {
    TimerManager.getInstance().register(new AutoSaveRunnable(this), 1000 * 180);
  }

  public static Set<Integer> getAllInstance() {
    return new HashSet<Integer>(instances.keySet());
  }

  public final void loadEvents() {
    if (events.size() != 0) {
      return;
    }
    events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut.mapids));
    events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness.mapids));
    events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla.mapids));
    events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz.mapids));
    events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball.mapids));
  }

  public final void run_startup_configurations() {
    setChannel(channel); //instances.put
    try {
      expRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.exp"));
      mesoRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.meso"));
      dropRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.drop"));
      cashRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.cash"));
      System.out.println("Exp:" + expRate);
      System.out.println("Meso:" + mesoRate);
      System.out.println("Drop:" + dropRate);
      System.out.println("Cash:" + cashRate);
      serverMessage = ServerProperties.getProperty("net.sf.odinms.world.serverMessage");
      serverName = ServerProperties.getProperty("net.sf.odinms.login.serverName");
      flags = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.flags", "0"));
      adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("net.sf.odinms.world.admin", "false"));
      eventSM = new EventScriptManager(this, ServerProperties.getProperty("net.sf.odinms.channel.events").split(","));
      port = Short.parseShort(ServerProperties.getProperty("net.sf.odinms.channel.net.port" + channel, String.valueOf(DEFAULT_PORT + channel)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ip = ServerProperties.getProperty("net.sf.odinms.channel.net.interface") + ":" + port;

    ByteBuffer.setUseDirectBuffers(false);
    ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

    acceptor = new SocketAcceptor();
    final SocketAcceptorConfig acceptor_config = new SocketAcceptorConfig();
    acceptor_config.getSessionConfig().setTcpNoDelay(true);
    acceptor_config.setDisconnectOnUnbind(true);
    acceptor_config.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
    players = new PlayerStorage(channel);
    loadEvents();

    try {
      this.serverHandler = new MapleServerHandler(channel, false, PacketProcessor.CHANNEL());
      acceptor.bind(new InetSocketAddress(port), serverHandler, acceptor_config);
      System.out.println("Channel " + channel + ": Listening on port " + port + "");
      eventSM.init();
    } catch (IOException e) {
      System.out.println("Binding to port " + port + " failed (ch: " + getChannel() + ")" + e);
    }
  }

  public final void shutdown() {
    if (finishedShutdown) {
      return;
    }
    broadcastPacket(MaplePacketCreator.serverNotice(0, "This channel will now shut down."));
    // dc all clients by hand so we get sessionClosed...
    shutdown = true;

    System.out.println("Channel " + channel + ", Saving hired merchants...");
    closeAllMerchant();

    System.out.println("Channel " + channel + ", Saving characters...");

    getPlayerStorage().disconnectAll();

    System.out.println("Channel " + channel + ", Unbinding...");

    acceptor.unbindAll();
    acceptor = null;

    //temporary while we dont have !addchannel
    instances.remove(channel);
    LoginServer.removeChannel(channel);
    setFinishShutdown();
  }

  public final void unbind() {
    acceptor.unbindAll();
  }

  public final boolean hasFinishedShutdown() {
    return finishedShutdown;
  }

  public final MapleMapFactory getMapFactory() {
    return mapFactory;
  }

  public static final ChannelServer newInstance(final int channel) {
    return new ChannelServer(channel);
  }

  public static final ChannelServer getInstance(final int channel) {
    return instances.get(channel);
  }

  public final void addPlayer(final MapleCharacter chr) {
    getPlayerStorage().registerPlayer(chr);
    chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
  }

  public final PlayerStorage getPlayerStorage() {
    if (players == null) { //wth
      players = new PlayerStorage(channel); //wthhhh
    }
    return players;
  }

  public final void removePlayer(final MapleCharacter chr) {
    getPlayerStorage().deregisterPlayer(chr);

  }

  public final void removePlayer(final int idz, final String namez) {
    getPlayerStorage().deregisterPlayer(idz, namez);

  }

  public final String getServerMessage() {
    return serverMessage;
  }

  public final void setServerMessage(final String newMessage) {
    serverMessage = newMessage;
    broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
  }

  public final void broadcastPacket(final byte[] data) {
    getPlayerStorage().broadcastPacket(data);
  }

  public final void broadcastSmegaPacket(final byte[] data) {
    getPlayerStorage().broadcastSmegaPacket(data);
  }

  public final void broadcastGMPacket(final byte[] data) {
    getPlayerStorage().broadcastGMPacket(data);
  }

  public final int getExpRate() {
    return expRate;
  }

  public final void setExpRate(final int expRate) {
    this.expRate = expRate;
  }

  public final int getCashRate() {
    return cashRate;
  }

  public final void setCashRate(final int cashRate) {
    this.cashRate = cashRate;
  }

  public final int getChannel() {
    return channel;
  }

  public final void setChannel(final int channel) {
    instances.put(channel, this);
    LoginServer.addChannel(channel);
  }

  public static final Collection<ChannelServer> getAllInstances() {
    return Collections.unmodifiableCollection(instances.values());
  }

  public final String getIP() {
    return ip;
  }

  public final boolean isShutdown() {
    return shutdown;
  }

  public final int getLoadedMaps() {
    return mapFactory.getLoadedMaps();
  }

  public final EventScriptManager getEventSM() {
    return eventSM;
  }

  public final void reloadEvents() {
    eventSM.cancel();
    eventSM = new EventScriptManager(this, ServerProperties.getProperty("net.sf.odinms.channel.events").split(","));
    eventSM.init();
  }

  public final int getMesoRate() {
    return mesoRate;
  }

  public final void setMesoRate(final int mesoRate) {
    this.mesoRate = mesoRate;
  }

  public final int getDropRate() {
    return dropRate;
  }

  public final void setDropRate(final int dropRate) {
    this.dropRate = dropRate;
  }

  public static final void startChannel_Main() {
    serverStartTime = System.currentTimeMillis();

    for (int i = 0; i < Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.channel.count", "0")); i++) {
      newInstance(i + 1).run_startup_configurations();
    }
  }

  public Map<String, MapleSquad> getAllSquads() {
    squadLock.readLock().lock();
    try {
      return Collections.unmodifiableMap(mapleSquads);
    } finally {
      squadLock.readLock().unlock();
    }
  }

  public final MapleSquad getMapleSquad(final String type) {
    squadLock.readLock().lock();
    try {
      return mapleSquads.get(type.toLowerCase());
    } finally {
      squadLock.readLock().unlock();
    }
  }

  public final boolean addMapleSquad(final MapleSquad squad, final String type) {
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

  public final boolean removeMapleSquad(final String type) {
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

  public final void closeAllMerchant() {
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

  public final int addMerchant(final HiredMerchant hMerchant) {
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

  public final void removeMerchant(final HiredMerchant hMerchant) {
    merchLock.writeLock().lock();

    try {
      merchants.remove(hMerchant.getStoreId());
    } finally {
      merchLock.writeLock().unlock();
    }
  }

  public final boolean containsMerchant(final int accid) {
    boolean contains = false;

    merchLock.readLock().lock();
    try {
      final Iterator<HiredMerchant> itr = merchants.values().iterator();

      while (itr.hasNext()) {
        if (((HiredMerchant) itr.next()).getOwnerAccId() == accid) {
          contains = true;
          break;
        }
      }
    } finally {
      merchLock.readLock().unlock();
    }
    return contains;
  }


  public final List<HiredMerchant> searchMerchant(final int itemSearch) {
    final List<HiredMerchant> list = new LinkedList<HiredMerchant>();
    merchLock.readLock().lock();
    try {
      final Iterator<HiredMerchant> itr = merchants.values().iterator();

      while (itr.hasNext()) {
        HiredMerchant hm = (HiredMerchant) itr.next();
        if (hm.searchItem(itemSearch).size() > 0) {
          list.add(hm);
        }
      }
    } finally {
      merchLock.readLock().unlock();
    }
    return list;
  }

  public final void toggleMegaphoneMuteState() {
    this.MegaphoneMuteState = !this.MegaphoneMuteState;
  }

  public final boolean getMegaphoneMuteState() {
    return MegaphoneMuteState;
  }

  public int getEvent() {
    return eventmap;
  }

  public final void setEvent(final int ze) {
    this.eventmap = ze;
  }

  public MapleEvent getEvent(final MapleEventType t) {
    return events.get(t);
  }

  public final Collection<PlayerNPC> getAllPlayerNPC() {
    return playerNPCs.values();
  }

  public final PlayerNPC getPlayerNPC(final int id) {
    return playerNPCs.get(id);
  }

  public final void addPlayerNPC(final PlayerNPC npc) {
    if (playerNPCs.containsKey(npc.getId())) {
      removePlayerNPC(npc);
    }
    playerNPCs.put(npc.getId(), npc);
    getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
  }

  public final void removePlayerNPC(final PlayerNPC npc) {
    if (playerNPCs.containsKey(npc.getId())) {
      playerNPCs.remove(npc.getId());
      getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
    }
  }

  public final String getServerName() {
    return serverName;
  }

  public final void setServerName(final String sn) {
    this.serverName = sn;
  }

  public final int getPort() {
    return port;
  }

  public static final Set<Integer> getChannelServer() {
    return new HashSet<Integer>(instances.keySet());
  }

  public final void setShutdown() {
    this.shutdown = true;
    System.out.println("Channel " + channel + " has set to shutdown and is closing Hired Merchants...");
  }

  public final void setFinishShutdown() {
    this.finishedShutdown = true;
    System.out.println("Channel " + channel + " has finished shutdown.");
  }

  public final boolean isAdminOnly() {
    return adminOnly;
  }

  public final static int getChannelCount() {
    return instances.size();
  }

  public final MapleServerHandler getServerHandler() {
    return serverHandler;
  }

  public final int getTempFlag() {
    return flags;
  }

  public static Map<Integer, Integer> getChannelLoad() {
    Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
    for (ChannelServer cs : instances.values()) {
      ret.put(cs.getChannel(), cs.getConnectedClients());
    }
    return ret;
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
    broadcastSmegaPacket(message);
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
