package scripting;

import client.MapleCharacter;
import client.MapleQuestStatus;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.ScriptException;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import server.MapleSquad;
import server.base.timer.Timer.EventTimer;
import server.carnival.MapleCarnivalParty;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.packet.UIPacket;

@Slf4j
public class EventInstanceManager {

    private final EventManager em;
    private final int channel;
    private final String name;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock readLock = mutex.readLock();
    private final Lock writeLock = mutex.writeLock();
    private List<MapleCharacter> chars = new LinkedList<>(); // this is messy
    private List<Integer> dced = new LinkedList<>();
    private List<MapleMonster> mobs = new LinkedList<>();
    private Map<Integer, Integer> killCount = new HashMap<>();
    private Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;
    private List<Integer> mapIds = new LinkedList<>();
    private List<Boolean> isInstanced = new LinkedList<>();
    private ScheduledFuture<?> eventTimer;
    private boolean disposed = false;

    public EventInstanceManager(EventManager em, String name, int channel) {
        this.em = em;
        this.name = name;
        this.channel = channel;
    }

    public void registerPlayer(MapleCharacter chr) {
        if (disposed || chr == null) {
            return;
        }
        try {
            writeLock.lock();
            try {
                chars.add(chr);
            } finally {
                writeLock.unlock();
            }
            chr.setEventInstance(this);
            em.getIv().invokeFunction("playerEntry", this, chr);
        } catch (NullPointerException ex) {
            log.info("Log_Script_Except.rtf", ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerEntry:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerEntry:\n" + ex);
        }
    }

    public void changedMap(final MapleCharacter chr, final int mapid) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("changedMap", this, chr, mapid);
        } catch (NullPointerException npe) {
        } catch (Exception ex) {
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : changedMap:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : changedMap:\n" + ex);
        }
    }

    public void timeOut(final long delay, final EventInstanceManager eim) {
        if (disposed || eim == null) {
            return;
        }
        eventTimer = EventTimer.getInstance()
                .schedule(
                        () -> {
                            if (disposed || eim == null || em == null) {
                                return;
                            }
                            try {
                                em.getIv().invokeFunction("scheduledTimeout", eim);
                            } catch (Exception ex) {
                                final String msg = "Event name"
                                        + em.getName()
                                        + ", Instance name : "
                                        + name
                                        + ", method Name : scheduledTimeout:\n"
                                        + ex;
                                log.info("Log_Script_Except.rtf" + " : " + msg);
                                log.info("Event name"
                                        + em.getName()
                                        + ", Instance name : "
                                        + name
                                        + ", method Name : scheduledTimeout:\n"
                                        + ex);
                            }
                        },
                        delay);
    }

    public void stopEventTimer() {
        eventTime = 0;
        timeStarted = 0;
        if (eventTimer != null) {
            eventTimer.cancel(false);
        }
    }

    public void restartEventTimer(long time) {
        try {
            if (disposed) {
                return;
            }
            timeStarted = System.currentTimeMillis();
            eventTime = time;
            if (eventTimer != null) {
                eventTimer.cancel(false);
            }
            eventTimer = null;
            final int timesend = (int) time / 1000;

            for (MapleCharacter chr : getPlayers()) {
                chr.getClient().getSession().write(MaplePacketCreator.getClock(timesend));
            }
            timeOut(time, this);
        } catch (Exception ex) {
            log.info("Log_Script_Except.rtf", ex);
            log.info(
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : restartEventTimer:\n");
            ex.printStackTrace();
        }
    }

    public void startEventTimer(long time) {
        restartEventTimer(time); // just incase
    }

    public boolean isTimerStarted() {
        return eventTime > 0 && timeStarted > 0;
    }

    public long getTimeLeft() {
        return eventTime - (System.currentTimeMillis() - timeStarted);
    }

    public void registerParty(MapleParty party, MapleMap map) {
        if (disposed) {
            return;
        }
        for (MaplePartyCharacter pc : party.getMembers()) {
            MapleCharacter c = map.getCharacterById(pc.getId());
            registerPlayer(c);
        }
    }

    public void unregisterPlayer(final MapleCharacter chr) {
        if (disposed) {
            chr.setEventInstance(null);
            return;
        }
        writeLock.lock();
        try {
            unregisterPlayer_NoLock(chr);
        } finally {
            writeLock.unlock();
        }
    }

    private boolean unregisterPlayer_NoLock(final MapleCharacter chr) {
        if (name.equals("CWKPQ")) { // hard code it because i said so
            final MapleSquad squad =
                    WorldServer.getInstance().getChannel(channel).getMapleSquad("CWKPQ"); // so fkin hacky
            if (squad != null) {
                squad.removeMember(chr.getName());
                if (squad.getLeaderName().equals(chr.getName())) {
                    em.setProperty("leader", "false");
                }
            }
        }
        chr.setEventInstance(null);
        if (disposed) {
            return false;
        }
        if (chars.contains(chr)) {
            chars.remove(chr);
            return true;
        }
        return false;
    }

    public final boolean disposeIfPlayerBelow(final byte size, final int towarp) {
        if (disposed) {
            return true;
        }
        MapleMap map = null;
        if (towarp > 0) {
            map = this.getMapFactory().getMap(towarp);
        }

        writeLock.lock();
        try {
            if (chars.size() <= size) {
                final List<MapleCharacter> chrs = new LinkedList<>(chars);
                for (MapleCharacter chr : chrs) {
                    unregisterPlayer_NoLock(chr);
                    if (towarp > 0) {
                        if (towarp == 502010030) {
                            chr.cancelMorphs(true);
                        }
                        chr.changeMap(map, map.getPortal(0));
                    }
                }
                dispose_NoLock();
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    public final void saveBossQuest(final int points) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            final MapleQuestStatus record = chr.getQuestNAdd(MapleQuest.getInstance(150001));

            if (record.getCustomData() != null) {
                record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
            } else {
                record.setCustomData(String.valueOf(points)); // First time
            }
        }
    }

    public List<MapleCharacter> getPlayers() {
        if (disposed) {
            return Collections.emptyList();
        }
        readLock.lock();
        try {
            return new LinkedList<>(chars);
        } finally {
            readLock.unlock();
        }
    }

    public List<Integer> getDisconnected() {
        return dced;
    }

    public final int getPlayerCount() {
        if (disposed) {
            return 0;
        }
        return chars.size();
    }

    public void registerMonster(MapleMonster mob) {
        if (disposed) {
            return;
        }
        mobs.add(mob);
        mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) {
        mob.setEventInstance(null);
        if (disposed) {
            return;
        }
        mobs.remove(mob);
        if (mobs.size() == 0) {
            try {
                em.getIv().invokeFunction("allMonstersDead", this);
            } catch (Exception ex) {
                final String msg = "Event name"
                        + em.getName()
                        + ", Instance name : "
                        + name
                        + ", method Name : allMonstersDead:\n"
                        + ex;
                log.info("Log_Script_Except.rtf" + " : " + msg);
                log.info("Event name"
                        + em.getName()
                        + ", Instance name : "
                        + name
                        + ", method Name : allMonstersDead:\n"
                        + ex);
            }
        }
    }

    public void playerKilled(MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("playerDead", this, chr);
        } catch (Exception ex) {
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerDead:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerDead:\n" + ex);
        }
    }

    public boolean revivePlayer(MapleCharacter chr) {
        if (disposed) {
            return false;
        }
        try {
            Object b = em.getIv().invokeFunction("playerRevive", this, chr);
            if (b instanceof Boolean) {
                return (Boolean) b;
            }
        } catch (Exception ex) {
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
            log.info(
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive:\n" + ex);
        }
        return true;
    }

    public void playerDisconnected(final MapleCharacter chr, int idz) {
        if (disposed) {
            return;
        }
        byte ret;
        try {
            ret = ((Double) em.getIv().invokeFunction("playerDisconnected", this, chr)).byteValue();
        } catch (Exception e) {
            ret = 0;
        }

        writeLock.lock();
        try {
            if (disposed) {
                return;
            }
            dced.add(idz);
            if (chr != null) {
                unregisterPlayer_NoLock(chr);
            }
            if (ret == 0) {
                if (getPlayerCount() <= 0) {
                    dispose_NoLock();
                }
            } else if ((ret > 0 && getPlayerCount() < ret)
                    || (ret < 0 && (isLeader(chr) || getPlayerCount() < (ret * -1)))) {
                final List<MapleCharacter> chrs = new LinkedList<>(chars);
                for (MapleCharacter player : chrs) {
                    if (player.getId() != idz) {
                        removePlayer(player);
                    }
                }
                dispose_NoLock();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info("Log_Script_Except.rtf", ex);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @param chr
     * @param mob
     */
    public void monsterKilled(final MapleCharacter chr, final MapleMonster mob) {
        if (disposed) {
            return;
        }
        try {
            Integer kc = killCount.get(chr.getId());
            Object val = em.getIv().invokeFunction("monsterValue", this, mob.getId());
            int inc;
            if (val instanceof Double) {
                inc = ((Double) val).intValue();
            } else {
                inc = (Integer) val;
            }

            if (disposed) {
                return;
            }
            if (kc == null) {
                kc = inc;
            } else {
                kc += inc;
            }
            killCount.put(chr.getId(), kc);
            if (chr.getCarnivalParty() != null
                    && (mob.getStats().getPoint() > 0 || mob.getStats().getCP() > 0)) {
                em.getIv()
                        .invokeFunction(
                                "monsterKilled",
                                this,
                                chr,
                                mob.getStats().getCP() > 0
                                        ? mob.getStats().getCP()
                                        : mob.getStats().getPoint());
            }
        } catch (ScriptException ex) {
            log.info("Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex);
            final String msg = "Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (NoSuchMethodException ex) {
            log.info("Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex);
            final String msg = "Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info("Log_Script_Except.rtf", ex);
        }
    }

    public void monsterDamaged(final MapleCharacter chr, final MapleMonster mob, final int damage) {
        if (disposed || mob.getId() != 9700037) { // ghost PQ boss only.
            return;
        }
        try {
            em.getIv().invokeFunction("monsterDamaged", this, chr, mob.getId(), damage);
        } catch (ScriptException ex) {
            log.info("Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex);
            final String msg = "Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (NoSuchMethodException ex) {
            log.info("Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex);
            final String msg = "Event name"
                    + (em == null ? "null" : em.getName())
                    + ", Instance name : "
                    + name
                    + ", method Name : monsterValue:\n"
                    + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info("Log_Script_Except.rtf", ex);
        }
    }

    public int getKillCount(MapleCharacter chr) {
        if (disposed) {
            return 0;
        }
        Integer kc = killCount.get(chr.getId());
        if (kc == null) {
            return 0;
        } else {
            return kc;
        }
    }

    public void dispose_NoLock() {
        if (disposed || em == null) {
            return;
        }
        final String emN = em.getName();
        try {

            disposed = true;
            for (MapleCharacter chr : chars) {
                chr.setEventInstance(null);
            }
            chars.clear();
            chars = null;
            for (MapleMonster mob : mobs) {
                mob.setEventInstance(null);
            }
            mobs.clear();
            mobs = null;
            killCount.clear();
            killCount = null;
            dced.clear();
            dced = null;
            timeStarted = 0;
            eventTime = 0;
            props.clear();
            props = null;
            for (int i = 0; i < mapIds.size(); i++) {
                if (isInstanced.get(i)) {
                    this.getMapFactory().removeInstanceMap(mapIds.get(i));
                }
            }
            mapIds.clear();
            mapIds = null;
            isInstanced.clear();
            isInstanced = null;
            em.disposeInstance(name);
        } catch (Exception e) {
            log.info("Caused by : " + emN + " instance name: " + name + " method: dispose: " + e);
            log.info("Log_Script_Except.rtf"
                    + " : "
                    + ("Event name" + emN + ", Instance name : " + name + ", method Name : dispose:\n" + e));
        }
    }

    public void dispose() {
        writeLock.lock();
        try {
            dispose_NoLock();
        } finally {
            writeLock.unlock();
        }
    }

    public ChannelServer getChannelServer() {
        return WorldServer.getInstance().getChannel(channel);
    }

    public List<MapleMonster> getMobs() {
        return mobs;
    }

    public final void giveAchievement(final int type) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getFinishedAchievements().finishAchievement(chr, type);
        }
    }

    public final void dropPlayerMsg(final int type, final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.dropMessage(type, msg);
        }
    }

    public final void broadcastPlayerMsg(final int type, final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(MaplePacketCreator.serverNotice(type, msg));
        }
    }

    public final MapleMap createInstanceMap(final int mapid) {
        if (disposed) {
            return null;
        }
        final int assignedid = getChannelServer().getEventSM().getNewInstanceMapId();
        mapIds.add(assignedid);
        isInstanced.add(true);
        return this.getMapFactory().CreateInstanceMap(mapid, true, true, true, assignedid);
    }

    public final MapleMap createInstanceMapS(final int mapid) {
        if (disposed) {
            return null;
        }
        final int assignedid = getChannelServer().getEventSM().getNewInstanceMapId();
        mapIds.add(assignedid);
        isInstanced.add(true);
        return this.getMapFactory().CreateInstanceMap(mapid, false, false, false, assignedid);
    }

    public final MapleMap setInstanceMap(final int mapid) { // gets instance map from the channelserv
        if (disposed) {
            return this.getMapFactory().getMap(mapid);
        }
        mapIds.add(mapid);
        isInstanced.add(false);
        return this.getMapFactory().getMap(mapid);
    }

    public final MapleMapFactory getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    public final MapleMap getMapInstance(int args) {
        if (disposed) {
            return null;
        }
        try {
            boolean instanced = false;
            int trueMapID = -1;
            if (args >= mapIds.size()) {
                // assume real map
                trueMapID = args;
            } else {
                trueMapID = mapIds.get(args);
                instanced = isInstanced.get(args);
            }
            MapleMap map = null;
            if (!instanced) {
                map = this.getMapFactory().getMap(trueMapID);
                if (map == null) {
                    return null;
                }
                // in case reactors need shuffling and we are actually loading the map
                if (map.getCharactersSize() == 0) {
                    if (em.getProperty("shuffleReactors") != null
                            && em.getProperty("shuffleReactors").equals("true")) {
                        map.shuffleReactors();
                    }
                }
            } else {
                map = this.getMapFactory().getInstanceMap(trueMapID);
                if (map == null) {
                    return null;
                }
                // in case reactors need shuffling and we are actually loading the map
                if (map.getCharactersSize() == 0) {
                    if (em.getProperty("shuffleReactors") != null
                            && em.getProperty("shuffleReactors").equals("true")) {
                        map.shuffleReactors();
                    }
                }
            }
            return map;
        } catch (NullPointerException ex) {
            log.info("Log_Script_Except.rtf", ex);
            ex.printStackTrace();
            return null;
        }
    }

    public final void schedule(final String methodName, final long delay) {
        if (disposed) {
            return;
        }
        EventTimer.getInstance()
                .schedule(
                        () -> {
                            if (disposed || EventInstanceManager.this == null || em == null) {
                                return;
                            }
                            try {
                                em.getIv().invokeFunction(methodName, EventInstanceManager.this);
                            } catch (NullPointerException npe) {
                            } catch (Exception ex) {
                                log.info("Event name"
                                        + em.getName()
                                        + ", Instance name : "
                                        + name
                                        + ", method Name : "
                                        + methodName
                                        + ":\n"
                                        + ex);
                                final String msg = "Event name"
                                        + em.getName()
                                        + ", Instance name : "
                                        + name
                                        + ", method Name(schedule) : "
                                        + methodName
                                        + " :\n"
                                        + ex;
                                log.info("Log_Script_Except.rtf" + " : " + msg);
                            }
                        },
                        delay);
    }

    public final String getName() {
        return name;
    }

    public final void setProperty(final String key, final String value) {
        if (disposed) {
            return;
        }
        props.setProperty(key, value);
    }

    public final Object setProperty(final String key, final String value, final boolean prev) {
        if (disposed) {
            return null;
        }
        return props.setProperty(key, value);
    }

    public final String getProperty(final String key) {
        if (disposed) {
            return "";
        }
        return props.getProperty(key);
    }

    public final Properties getProperties() {
        return props;
    }

    public final void leftParty(final MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("leftParty", this, chr);
        } catch (Exception ex) {
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : leftParty:\n" + ex);
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : leftParty:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    public final void disbandParty() {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("disbandParty", this);
        } catch (Exception ex) {
            log.info(
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : disbandParty:\n" + ex);
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : disbandParty:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    // Separate function to warp players to a "finish" map, if applicable
    public final void finishPQ() {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("clearPQ", this);
        } catch (Exception ex) {
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : clearPQ:\n" + ex);
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : clearPQ:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    public final void removePlayer(final MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("playerExit", this, chr);
        } catch (Exception ex) {
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerExit:\n" + ex);
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerExit:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        }
    }

    public final void registerCarnivalParty(final MapleCharacter leader, final MapleMap map, final byte team) {
        if (disposed) {
            return;
        }
        leader.clearCarnivalRequests();
        List<MapleCharacter> characters = new LinkedList<>();
        final MapleParty party = leader.getParty();

        if (party == null) {
            return;
        }
        for (MaplePartyCharacter pc : party.getMembers()) {
            final MapleCharacter c = map.getCharacterById(pc.getId());
            if (c != null) {
                characters.add(c);
                registerPlayer(c);
                c.resetCP();
            }
        }
        final MapleCarnivalParty carnivalParty = new MapleCarnivalParty(leader, characters, team);
        try {
            em.getIv().invokeFunction("registerCarnivalParty", this, carnivalParty);
        } catch (ScriptException ex) {
            log.info("Event name"
                    + em.getName()
                    + ", Instance name : "
                    + name
                    + ", method Name : registerCarnivalParty:\n"
                    + ex);
            final String msg = "Event name"
                    + em.getName()
                    + ", Instance name : "
                    + name
                    + ", method Name : registerCarnivalParty:\n"
                    + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (NoSuchMethodException ex) {
            // ignore
        }
    }

    public void onMapLoad(final MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("onMapLoad", this, chr);
        } catch (ScriptException ex) {
            log.info("Event name" + em.getName() + ", Instance name : " + name + ", method Name : onMapLoad:\n" + ex);
            final String msg =
                    "Event name" + em.getName() + ", Instance name : " + name + ", method Name : onMapLoad:\n" + ex;
            log.info("Log_Script_Except.rtf" + " : " + msg);
        } catch (NoSuchMethodException ex) {
            // Ignore, we don't want to update this for all events.
        }
    }

    public boolean isLeader(final MapleCharacter chr) {
        return (chr != null
                && chr.getParty() != null
                && chr.getParty().getLeader().getId() == chr.getId());
    }

    public void registerSquad(MapleSquad squad, MapleMap map) {
        if (disposed) {
            return;
        }
        final int mapid = map.getId();

        for (String chr : squad.getMembers()) {
            MapleCharacter player = squad.getChar(chr);
            if (player != null && player.getMapId() == mapid) {
                registerPlayer(player);
            }
        }
        squad.setStatus((byte) 2);
        squad.getBeginMap().broadcastMessage(MaplePacketCreator.stopClock());
    }

    public boolean isDisconnected(final MapleCharacter chr) {
        if (disposed) {
            return false;
        }
        return (dced.contains(chr.getId()));
    }

    public void removeDisconnected(final int id) {
        if (disposed) {
            return;
        }
        dced.remove(id);
    }

    public EventManager getEventManager() {
        return em;
    }

    public void applyBuff(final MapleCharacter chr, final int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(chr);
        chr.getClient().getSession().write(UIPacket.getStatusMsg(id));
    }

    public final void broadcastPacket(byte[] p) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(p);
        }
    }

    public void debug(Object... args) {
        for (Object str : args) {
            log.info("," + str);
        }
    }
}
