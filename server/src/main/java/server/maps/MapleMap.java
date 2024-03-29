package server.maps;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.skill.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.Broadcast;
import handling.world.WorldServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import scripting.EventManager;
import scripting.NPCScriptManager;
import scripting.ReactorScriptManager;
import scripting.v1.base.FieldScripting;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleSquad;
import server.MapleStatEffect;
import server.base.timer.Timer.MapTimer;
import server.carnival.MapleCarnivalFactory;
import server.carnival.MapleCarnivalFactory.MCSkill;
import server.events.MapleEvent;
import server.events.SpeedRunner;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.Spawns;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleNodes.MonsterPoint;
import server.maps.event.Aran3thJobThiefEvent;
import server.maps.event.KentaMapUserEnterEvent;
import server.maps.event.MapEvent;
import server.maps.event.PuppeteerMapEvent;
import server.maps.event.SimpleQuestMapEvent;
import tools.MaplePacketCreator;
import tools.collection.Pair;
import tools.helper.DateHelper;
import tools.helper.Randomizer;
import tools.helper.StringUtil;
import tools.packet.MapleUserPackets;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import tools.packet.npcpool.NpcPoolPackets;

@Slf4j
public final class MapleMap {
    public static final int KENTA_MAP_ID = 923010000;
    public static final int PUPPETER_MAP_ID = 910050300;
    public static final int ARCHER_ARAN_QUEST_MAP = 910050000;
    public static final int ARAN_3TH_JOB_YET_THIEF_MAP = 108010702;
    /*
     * Holds mappings of OID -> MapleMapObject separated by MapleMapObjectType.
     * Please acquire the appropriate lock when reading and writing to the LinkedHashMaps.
     * The MapObjectType Maps themselves do not need to synchronized in any way since they should never be modified.
     */
    private final Map<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapobjects;
    private final Map<MapleMapObjectType, ReentrantReadWriteLock> mapobjectlocks;
    private final List<MapleCharacter> characters = new ArrayList<>();
    private final ReentrantReadWriteLock charactersLock = new ReentrantReadWriteLock();
    private final Lock runningOidLock = new ReentrantLock(); // running oid lock used to improve concurrency
    private final List<Spawns> monsterSpawn = new ArrayList<>();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final Map<Integer, MaplePortal> portals = new HashMap<>();
    private final float monsterRate;
    private final byte channel;
    private final int mapid;
    private final List<Integer> dced = new ArrayList<>();
    private final Map<String, Integer> environment = new LinkedHashMap<>();
    private final HashMap<String, String> properties = new HashMap<>();
    private final List<MapEvent> mapEvents = new ArrayList<>();
    private int runningOid = 1000000; // 100000 in aurasea
    private MapleFootholdTree footholds = null;
    private float recoveryRate;
    private MapleMapEffect mapEffect;
    private short decHP = 0, createMobInterval = 9000;
    private int consumeItemCoolTime = 0;
    private int protectItem = 0;
    private int decHPInterval = 10000;
    private int returnMapId;
    private int timeLimit;
    private int fieldLimit;
    private int maxRegularSpawn = 0;
    private int fixedMob;
    private int forcedReturnMap = 999999999;
    private int lvForceMove = 0;
    private int lvLimit = 0;
    private int permanentWeather = 0;
    private boolean town,
            clock,
            personalShop,
            everlast = false,
            dropsDisabled = false,
            gDropsDisabled = false,
            soaring = false,
            squadTimer = false,
            isSpawns = true;
    private String mapName, streetName, onUserEnter, onFirstUserEnter, speedRunLeader = "", squad = "";
    private ScheduledFuture<?> squadSchedule;
    private long speedRunStart = 0, lastSpawnTime = 0, lastHurtTime = 0;
    private MapleNodes nodes;
    private boolean docked;

    public MapleMap(final int mapid, final int channel, final int returnMapId, final float monsterRate) {
        this.mapid = mapid;
        this.channel = (byte) channel;
        this.returnMapId = returnMapId;
        if (this.returnMapId == 999999999) {
            this.returnMapId = mapid;
        }
        this.monsterRate = monsterRate;
        EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> objsMap =
                new EnumMap<>(MapleMapObjectType.class);
        EnumMap<MapleMapObjectType, ReentrantReadWriteLock> objlockmap = new EnumMap<>(MapleMapObjectType.class);
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            objsMap.put(type, new LinkedHashMap<>());
            objlockmap.put(type, new ReentrantReadWriteLock());
        }
        mapobjects = Collections.unmodifiableMap(objsMap);
        mapobjectlocks = Collections.unmodifiableMap(objlockmap);

        registerMapEvents();
    }

    private void registerMapEvents() {
        if (mapid == KENTA_MAP_ID) {
            mapEvents.add(new KentaMapUserEnterEvent(this));
        } else if (mapid == PUPPETER_MAP_ID) {
            mapEvents.add(new PuppeteerMapEvent(this));
        } else if (mapid == ARCHER_ARAN_QUEST_MAP) {
            mapEvents.add(new SimpleQuestMapEvent(this));
        } else if (mapid == ARAN_3TH_JOB_YET_THIEF_MAP) {
            mapEvents.add(new Aran3thJobThiefEvent(this));
        } else {
            mapEvents.add(MapEvent.noOperationUserEnter);
        }
    }

    private void onUserEnter(MapleCharacter c) {
        for (MapEvent event : mapEvents) {
            event.onUserEnter(c);
        }
    }

    private void onUserExit(MapleCharacter c) {
        for (MapEvent event : mapEvents) {
            event.onUserExit(c);
        }
    }

    public boolean getSpawns() {
        return isSpawns;
    }

    public void setSpawns(final boolean fm) {
        this.isSpawns = fm;
    }

    public void setFixedMob(int fm) {
        this.fixedMob = fm;
    }

    public int getForceMove() {
        return lvForceMove;
    }

    public void setForceMove(int fm) {
        this.lvForceMove = fm;
    }

    public int getLevelLimit() {
        return lvLimit;
    }

    public void setLevelLimit(int fm) {
        this.lvLimit = fm;
    }

    public void setSoaring(boolean b) {
        this.soaring = b;
    }

    public boolean canSoar() {
        return soaring;
    }

    public void toggleDrops() {
        this.dropsDisabled = !dropsDisabled;
    }

    public void setDrops(final boolean b) {
        this.dropsDisabled = b;
    }

    public void toggleGDrops() {
        this.gDropsDisabled = !gDropsDisabled;
    }

    public int getId() {
        return mapid;
    }

    public MapleMap getReturnMap() {
        return WorldServer.getInstance().getChannel(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public void setReturnMapId(int rmi) {
        this.returnMapId = rmi;
    }

    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return WorldServer.getInstance().getChannel(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(final int map) {
        this.forcedReturnMap = map;
    }

    public float getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(final float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public int getFieldLimit() {
        return fieldLimit;
    }

    public void setFieldLimit(final int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public void setCreateMobInterval(final short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public void setTimeLimit(final int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(final String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    public void setFirstUserEnter(final String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public void setUserEnter(final String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public boolean hasClock() {
        return clock;
    }

    public void setClock(final boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean isTown() {
        return town;
    }

    public void setTown(final boolean town) {
        this.town = town;
    }

    public boolean allowPersonalShop() {
        return personalShop;
    }

    public void setPersonalShop(final boolean personalShop) {
        this.personalShop = personalShop;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public void setEverlast(final boolean everlast) {
        this.everlast = everlast;
    }

    public int getHPDec() {
        return decHP;
    }

    public void setHPDec(final int delta) {
        if (delta > 0 || mapid == 749040100) { // pmd
            lastHurtTime = System.currentTimeMillis(); // start it up
        }
        decHP = (short) delta;
    }

    public int getHPDecInterval() {
        return decHPInterval;
    }

    public void setHPDecInterval(final int delta) {
        decHPInterval = delta;
    }

    public int getHPDecProtect() {
        return protectItem;
    }

    public void setHPDecProtect(final int delta) {
        this.protectItem = delta;
    }

    public int getCurrentPartyId() {
        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (chr.getPartyId() != -1) {
                    return chr.getPartyId();
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return -1;
    }

    // aura sea way
    public void addMapObject(final MapleMapObject mapobject) {
        runningOidLock.lock();
        int newOid;
        try {
            newOid = runningOid++;
        } finally {
            runningOidLock.unlock();
        }

        mapobject.setObjectId(newOid);

        mapobjectlocks.get(mapobject.getType()).writeLock().lock();
        try {
            mapobjects.get(mapobject.getType()).put(newOid, mapobject);
        } finally {
            mapobjectlocks.get(mapobject.getType()).writeLock().unlock();
        }
    }

    private void spawnAndAddRangedMapObject(
            final MapleMapObject mapobject, final DelayedPacketCreation packetbakery, final SpawnCondition condition) {
        addMapObject(mapobject);

        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> itr = characters.iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= GameConstants.maxViewRangeSq()) {
                        packetbakery.sendPackets(chr.getClient());
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    public void removeMapObject(final MapleMapObject obj) {
        mapobjectlocks.get(obj.getType()).writeLock().lock();
        try {
            mapobjects.get(obj.getType()).remove(Integer.valueOf(obj.getObjectId()));
        } finally {
            mapobjectlocks.get(obj.getType()).writeLock().unlock();
        }
    }

    public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        return spos;
    }

    public Point calcPointBelow(final Point initial) {
        final MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            final double s1 = Math.abs(fh.getY2() - fh.getY1());
            final double s2 = Math.abs(fh.getX2() - fh.getX1());
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1()
                        - (int) (Math.cos(Math.atan(s2 / s1))
                                * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            } else {
                dropY = fh.getY1()
                        + (int) (Math.cos(Math.atan(s2 / s1))
                                * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            }
        }
        return new Point(initial.x, dropY);
    }

    public Point calcDropPos(final Point initial, final Point fallback) {
        final Point ret = calcPointBelow(new Point(initial.x, initial.y - 15));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob) {
        if (mob == null
                || chr == null
                || WorldServer.getInstance().getChannel(channel) == null
                || dropsDisabled
                || mob.dropsDisabled()
                || chr.getPyramidSubway() != null) { // no drops in pyramid ok? no cash either
            return;
        }

        // We choose not to readLock for this.
        // This will not affect the internal state, and we don't want to
        // introduce unneccessary locking, especially since this function
        // is probably used quite often.
        if (mapobjects.get(MapleMapObjectType.ITEM).size() >= ServerConstants.MAX_ITEMS) {
            removeDrops();
        }

        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        final byte droptype = (byte)
                (mob.getStats().isExplosiveReward()
                        ? 3
                        : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getPosition().x;
        final int cmServerrate = WorldServer.getInstance().getChannel(channel).getMesoRate();
        final int chServerrate = WorldServer.getInstance().getChannel(channel).getDropRate();
        final int caServerrate = WorldServer.getInstance().getChannel(channel).getCashRate();

        IItem idrop;
        byte d = 1;
        Point pos = new Point(0, mob.getPosition().y);
        double showdown = 100.0;
        final MonsterStatusEffect mse = mob.getBuff(MonsterStatus.SHOWDOWN);
        if (mse != null) {
            showdown += mse.getMultiplier();
        }

        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        final List<MonsterDropEntry> dropEntry = mi.retrieveDrop(mob.getId());
        if (dropEntry == null) {
            return;
        }
        Collections.shuffle(dropEntry);
        int j = 0;
        for (final MonsterDropEntry de : dropEntry) {

            if (de.itemId == mob.getStolen()) {
                continue;
            }

            // if(1 == 1){
            if (Randomizer.nextInt(999999)
                    < (int) (de.chance
                            * chServerrate
                            * chr.getDropMod()
                            * (chr.getStat().getDropBuff() / 100.0)
                            * (showdown / 100.0))) {
                byte questStatus = chr.getQuestStatus(de.questid);
                if (questStatus > 1) {
                    continue;
                }
                j++;
                if (droptype == 3) {
                    pos.x = (mobpos - 25 + j * 10);
                } else {
                    pos.x = (mobpos - 25 + j * 10);
                }
                if (de.itemId == 0) { // meso
                    int mesos = Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum;

                    if (mesos > 0) {
                        if (chr.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                            mesos = (int) (mesos
                                    * chr.getBuffedValue(MapleBuffStat.MESOUP).doubleValue()
                                    / 100.0);
                        }
                        spawnMesoDrop(
                                mesos * cmServerrate, calcDropPos(pos, mob.getPosition()), mob, chr, false, droptype);
                    }
                    d++;
                } else {
                    int qqu = 0;

                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        int itemCount = getItemCount(chr, de.itemId);
                        if (itemCount >= de.holdMaximum && de.holdMaximum > -1) {
                            return;
                        }
                        if (chr.getItemQuantity(de.itemId, true) >= de.holdMaximum && de.holdMaximum > -1) {
                            return;
                        }
                        final int range = Math.abs(de.Maximum - de.Minimum);
                        idrop = new Item(
                                de.itemId,
                                (byte) 0,
                                (short) (de.Maximum != 1 ? Randomizer.nextInt(range <= 0 ? 1 : range) + de.Minimum : 1),
                                (byte) 0);
                    }
                    if (qqu > 0) {
                        for (int i = 0; i < qqu; i++) {
                            if (droptype == 3) {
                                pos.x = (mobpos - 25 + j * 10);
                            } else {
                                pos.x = (mobpos - 25 + j * 10);
                            }
                            spawnMobDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
                            d++;
                        }
                    } else {
                        spawnMobDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
                        d++;
                    }
                }
            }
        }
        final List<MonsterGlobalDropEntry> globalEntry = new ArrayList<>(mi.getGlobalDrop());
        Collections.shuffle(globalEntry);
        final int cashz = (mob.getStats().isBoss() && mob.getStats().getHPDisplayType() == 0 ? 20 : 1) * caServerrate;
        final int cashModifier =
                (int) ((mob.getStats().isBoss() ? 0 : (mob.getMobExp() / 1000 + mob.getMobMaxHp() / 10000))); // no rate
        // Global Drops
        int k = 0;
        for (final MonsterGlobalDropEntry de : globalEntry) {
            // if(1 == 1){
            if (Randomizer.nextInt(999999) < de.chance
                    && (de.continent < 0
                            || (de.continent < 10 && mapid / 100000000 == de.continent)
                            || (de.continent < 100 && mapid / 10000000 == de.continent)
                            || (de.continent < 1000 && mapid / 1000000 == de.continent))) {
                if (de.questid > 0 && (chr.getQuestStatus(de.questid) != 1)) {
                    continue;
                }
                if (droptype == 3) {
                    pos.x = (mobpos - 25 + k * 10);
                } else {
                    pos.x = (mobpos - 25 + k * 10);
                }
                if (de.itemId == 0) {
                    chr.modifyCSPoints(
                            1,
                            (int) ((Randomizer.nextInt(cashz) + cashz + cashModifier)
                                    * (chr.getStat().getCashBuff() / 100.0)
                                    * chr.getCashMod()),
                            true);
                } else if (!gDropsDisabled) {
                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(
                                de.itemId,
                                (byte) 0,
                                (short)
                                        (de.Maximum != 1
                                                ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum
                                                : 1),
                                (byte) 0);
                    }
                    spawnMobDrop(
                            idrop,
                            calcDropPos(pos, mob.getPosition()),
                            mob,
                            chr,
                            de.onlySelf ? 0 : droptype,
                            de.questid);
                    d++;
                }
            }
        }
    }

    private int getItemCount(MapleCharacter chr, int itemId) {
        int count = 0;
        for (MapleMapItem item : this.getAllItemsThreadsafe()) {
            if (item.getOwner() == chr.getId() && item.getItemId() == itemId) {
                count++;
            }
        }
        return count;
    }

    public void removeMonster(final MapleMonster monster) {
        spawnedMonstersOnMap.decrementAndGet();
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
        removeMapObject(monster);
    }

    public void killMonster(final MapleMonster monster) { // For mobs with removeAfter
        if (monster == null) {
            return;
        }
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        monster.spawnRevives(this);
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
        removeMapObject(monster);
    }

    public void killMonster(
            final MapleMonster monster,
            final MapleCharacter chr,
            final boolean withDrops,
            final boolean second,
            byte animation) {
        killMonster(monster, chr, withDrops, second, animation, 0, true);
    }

    public void killMonster(
            final MapleMonster monster,
            final MapleCharacter chr,
            final boolean withDrops,
            final boolean second,
            byte animation,
            final int lastSkill,
            final boolean real) {
        if ((monster.getId() == 8810122 || monster.getId() == 8810018) && !second) {
            MapTimer.getInstance()
                    .schedule(
                            () -> {
                                killMonster(monster, chr, true, true, (byte) 1);
                                killAllMonsters(true);
                            },
                            3000);
            return;
        }
        if (monster.getId() == 8820014) { // pb sponge, kills pb(w) first before dying
            killMonster(8820000);
        } else if (monster.getId() == 9300166) { // ariant pq bomb
            animation = 4; // or is it 3?
        } else if (monster.getId() == 9300329) {
            animation = 4; // or is it 3?
        }

        spawnedMonstersOnMap.decrementAndGet();
        removeMapObject(monster);
        int dropOwner = monster.killBy(chr, lastSkill, real);
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));

        if (monster.getBuffToGive() > -1) {
            final int buffid = monster.getBuffToGive();
            final MapleStatEffect buff =
                    MapleItemInformationProvider.getInstance().getItemEffect(buffid);

            charactersLock.readLock().lock();
            try {
                for (final MapleCharacter mc : characters) {
                    if (mc.isAlive()) {
                        buff.applyTo(mc);

                        switch (monster.getId()) {
                            case 8810018:
                            case 8810122:
                            case 8820001:
                                mc.getClient()
                                        .getSession()
                                        .write(MaplePacketCreator.showOwnBuffEffect(buffid, 11)); // HT nine spirit
                                broadcastMessage(
                                        mc,
                                        MaplePacketCreator.showBuffeffect(mc.getId(), buffid, 11),
                                        false); // HT nine spirit
                                break;
                        }
                    }
                }
            } finally {
                charactersLock.readLock().unlock();
            }
        }
        final int mobid = monster.getId();
        SpeedRunType type = SpeedRunType.NULL;
        final MapleSquad sqd = getSquadByMap();
        if (mobid == 8810018 && mapid == 240060200) { // Horntail
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(
                    6,
                    "To the crew that have finally conquered Horned Tail after numerous"
                            + " attempts, I salute thee! You are the true heroes of Leafre!!"));
            for (MapleCharacter c : getCharactersThreadsafe()) {
                c.getFinishedAchievements().finishAchievement(c, 16);
            }
            final String msg = MapDebug_Log();
            log.info("Log_Horntail.rtf" + " : " + msg);
            if (speedRunStart > 0) {
                type = SpeedRunType.Horntail;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 8810122 && mapid == 240060201) { // Horntail
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(
                    6,
                    "To the crew that have finally conquered Chaos Horned Tail after"
                            + " numerous attempts, I salute thee! You are the true heroes of"
                            + " Leafre!!"));
            for (MapleCharacter c : getCharactersThreadsafe()) {
                c.getFinishedAchievements().finishAchievement(c, 24);
            }
            final String msg = MapDebug_Log();
            log.info("Log_Horntail.rtf" + " : " + msg);
            if (speedRunStart > 0) {
                type = SpeedRunType.ChaosHT;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 8500002 && mapid == 220080001) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Papulatus;
            }
        } else if (mobid == 9400266 && mapid == 802000111) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Nameless_Magic_Monster;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400265 && mapid == 802000211) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Vergamot;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400270 && mapid == 802000411) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Dunas;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400273 && mapid == 802000611) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Nibergen;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400294 && mapid == 802000711) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Dunas_2;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400296 && mapid == 802000803) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Core_Blaze;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 9400289 && mapid == 802000821) {
            if (speedRunStart > 0) {
                type = SpeedRunType.Aufhaven;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if ((mobid == 9420549 || mobid == 9420544) && mapid == 551030200) {
            if (speedRunStart > 0) {
                if (mobid == 9420549) {
                    type = SpeedRunType.Scarlion;
                } else {
                    type = SpeedRunType.Targa;
                }
            }
            // INSERT HERE: 2095_tokyo
        } else if (mobid == 8820001 && mapid == 270050100) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(
                    6,
                    "Expedition who defeated Pink Bean with invicible passion! You are the" + " true timeless hero!"));
            for (MapleCharacter c : getCharactersThreadsafe()) {
                c.getFinishedAchievements().finishAchievement(c, 17);
            }
            if (speedRunStart > 0) {
                type = SpeedRunType.Pink_Bean;
            }
            if (sqd != null) {
                doShrine(true);
            }
            final String msg = MapDebug_Log();
            log.info("Log_Pinkbean.rtf" + " : " + msg);
        } else if (mobid == 8800002 && mapid == 280030000) {
            for (MapleCharacter c : getCharactersThreadsafe()) {
                c.getFinishedAchievements().finishAchievement(c, 15);
            }
            final String msg = MapDebug_Log();
            log.info("Log_Zakum.rtf" + " : " + msg);
            if (speedRunStart > 0) {
                type = SpeedRunType.Zakum;
            }
            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid == 8800102 && mapid == 280030001) {
            for (MapleCharacter c : getCharactersThreadsafe()) {
                c.getFinishedAchievements().finishAchievement(c, 23);
            }
            final String msg = MapDebug_Log();
            log.info("Log_Zakum.rtf" + " : " + msg);
            if (speedRunStart > 0) {
                type = SpeedRunType.Chaos_Zakum;
            }

            if (sqd != null) {
                doShrine(true);
            }
        } else if (mobid >= 8800003 && mobid <= 8800010) {
            boolean makeZakReal = true;
            final Collection<MapleMonster> monsters = getAllMonstersThreadsafe();

            for (final MapleMonster mons : monsters) {
                if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (final MapleMapObject object : monsters) {
                    final MapleMonster mons = ((MapleMonster) object);
                    if (mons.getId() == 8800000) {
                        final Point pos = mons.getPosition();
                        this.killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
                        break;
                    }
                }
            }
        } else if (mobid >= 8800103 && mobid <= 8800110) {
            boolean makeZakReal = true;
            final Collection<MapleMonster> monsters = getAllMonstersThreadsafe();

            for (final MapleMonster mons : monsters) {
                if (mons.getId() >= 8800103 && mons.getId() <= 8800110) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (final MapleMonster mons : monsters) {
                    if (mons.getId() == 8800100) {
                        final Point pos = mons.getPosition();
                        this.killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100), pos);
                        break;
                    }
                }
            }
        }
        if (type != SpeedRunType.NULL) {
            if (speedRunStart > 0 && speedRunLeader.length() > 0) {
                long endTime = System.currentTimeMillis();
                String time = StringUtil.getReadableMillis(speedRunStart, endTime);
                broadcastMessage(MaplePacketCreator.serverNotice(
                        5, speedRunLeader + "'s squad has taken " + time + " to defeat " + type + "!"));
                getRankAndAdd(
                        speedRunLeader, time, type, (endTime - speedRunStart), (sqd == null ? null : sqd.getMembers()));
                endSpeedRun();
            }
        }
        if (mobid == 8820008) { // wipe out statues and respawn
            for (final MapleMapObject mmo : getAllMonstersThreadsafe()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getLinkOid() != monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if (mobid >= 8820010 && mobid <= 8820014) {
            for (final MapleMapObject mmo : getAllMonstersThreadsafe()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getId() != 8820000
                        && mons.getObjectId() != monster.getObjectId()
                        && mons.getLinkOid() != monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        }
        if (withDrops) {
            MapleCharacter drop = null;
            if (dropOwner <= 0) {
                drop = chr;
            } else {
                drop = getCharacterById(dropOwner);
                if (drop == null) {
                    drop = chr;
                }
            }
            dropFromMonster(drop, monster);
        }
    }

    public List<MapleReactor> getAllReactor() {
        return getAllReactorsThreadsafe();
    }

    public List<MapleReactor> getAllReactorsThreadsafe() {
        ArrayList<MapleReactor> ret = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ret.add((MapleReactor) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllDoor() {
        return getAllDoorsThreadsafe();
    }

    public List<MapleMapObject> getAllDoorsThreadsafe() {
        ArrayList<MapleMapObject> ret = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.DOOR).values()) {
                ret.add(mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllMerchant() {
        return getAllHiredMerchantsThreadsafe();
    }

    public List<MapleMapObject> getAllHiredMerchantsThreadsafe() {
        ArrayList<MapleMapObject> ret = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT).readLock().lock();
        try {
            for (MapleMapObject mmo :
                    mapobjects.get(MapleMapObjectType.HIRED_MERCHANT).values()) {
                ret.add(mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMonster> getAllMonster() {
        return getAllMonstersThreadsafe();
    }

    public List<MapleMonster> getAllMonstersThreadsafe() {
        return getAllMonstersThreadsafe(new ArrayList<>());
    }

    public ArrayList<MapleMonster> getAllMonstersThreadsafe(ArrayList<MapleMonster> ret) {
        ret.clear();
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MONSTER).values()) {
                ret.add((MapleMonster) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
        return ret;
    }

    public void killAllMonsters(final boolean animate) {
        for (final MapleMapObject monstermo : getAllMonstersThreadsafe()) {
            final MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
            removeMapObject(monster);
        }
    }

    public void killMonster(final int monsId) {
        for (final MapleMapObject mmo : getAllMonstersThreadsafe()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                spawnedMonstersOnMap.decrementAndGet();
                removeMapObject(mmo);
                broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
                break;
            }
        }
    }

    private String MapDebug_Log() {
        final StringBuilder sb = new StringBuilder("Defeat time : ");
        sb.append(DateHelper.getCurrentReadableTime());

        sb.append(" | Mapid : ").append(this.mapid);

        charactersLock.readLock().lock();
        try {
            sb.append(" Users [").append(characters.size()).append("] | ");
            for (MapleCharacter mc : characters) {
                sb.append(mc.getName()).append(", ");
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return sb.toString();
    }

    public void limitReactor(final int rid, final int num) {
        List<MapleReactor> toDestroy = new ArrayList<>();
        Map<Integer, Integer> contained = new LinkedHashMap<>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (contained.containsKey(mr.getReactorId())) {
                    if (contained.get(mr.getReactorId()) >= num) {
                        toDestroy.add(mr);
                    } else {
                        contained.put(mr.getReactorId(), contained.get(mr.getReactorId()) + 1);
                    }
                } else {
                    contained.put(mr.getReactorId(), 1);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public void spawnReactorOnGroundBelow(MapleReactor mob, Point pos) {
        mob.setPosition(pos);
        spawnReactor(mob);
    }

    public void destroyReactors(final int first, final int last) {
        List<MapleReactor> toDestroy = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    toDestroy.add(mr);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public void destroyReactor(final int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);

        if (reactor.getDelay() > 0) {
            MapTimer.getInstance().schedule(() -> respawnReactor(reactor), reactor.getDelay());
        }
    }

    public void reloadReactors() {
        List<MapleReactor> toSpawn = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                final MapleReactor reactor = (MapleReactor) obj;
                broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
                reactor.setAlive(false);
                reactor.setTimerActive(false);
                toSpawn.add(reactor);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        for (MapleReactor r : toSpawn) {
            removeMapObject(r);
            if (r.getReactorId() != 9980000 && r.getReactorId() != 9980001) { // guardians cpq
                respawnReactor(r);
            }
        }
    }

    /*
     * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
     * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
     */
    public void resetReactors() {
        setReactorState((byte) 0);
    }

    public void setReactorState() {
        setReactorState((byte) 1);
    }

    public void setReactorState(final byte state) {
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ((MapleReactor) obj).forceHitReactor(state);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    /*
     * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
     */
    public void shuffleReactors() {
        shuffleReactors(0, 9999999); // all
    }

    public void shuffleReactors(int first, int last) {
        List<Point> points = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    points.add(mr.getPosition());
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        Collections.shuffle(points);
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    mr.setPosition(points.remove(points.size() - 1));
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    /**
     * Automagically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
    public void updateMonsterController(final MapleMonster monster) {
        if (!monster.isAlive()) {
            return;
        }
        if (monster.getController() != null) {
            if (monster.getController().getMap() != this) {
                monster.getController().stopControllingMonster(monster);
            } else { // Everything is fine :)
                return;
            }
        }
        int mincontrolled = -1;
        MapleCharacter newController = null;

        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (!chr.isHidden() && (chr.getControlledSize() < mincontrolled || mincontrolled == -1)) {
                    mincontrolled = chr.getControlledSize();
                    newController = chr;
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        if (newController != null) {
            if (monster.isFirstAttack()) {
                newController.controlMonster(monster, true);
                monster.setControllerHasAggro(true);
                monster.setControllerKnowsAboutAggro(true);
            } else {
                newController.controlMonster(monster, false);
            }
        }
    }

    public MapleMapObject getMapObject(int oid, MapleMapObjectType type) {
        mapobjectlocks.get(type).readLock().lock();
        try {
            return mapobjects.get(type).get(oid);
        } finally {
            mapobjectlocks.get(type).readLock().unlock();
        }
    }

    public boolean containsNPC(int npcid) {
        mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.NPC).values().iterator();
            while (itr.hasNext()) {
                MapleNPC n = (MapleNPC) itr.next();
                if (n.getId() == npcid) {
                    return true;
                }
            }
            return false;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
    }

    public MapleNPC getNPCById(int id) {
        mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.NPC).values().iterator();
            while (itr.hasNext()) {
                MapleNPC n = (MapleNPC) itr.next();
                if (n.getId() == id) {
                    return n;
                }
            }
            return null;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
    }

    public MapleMonster getMonsterById(int id) {
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            MapleMonster ret = null;
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.MONSTER).values().iterator();
            while (itr.hasNext()) {
                MapleMonster n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
    }

    public int countMonsterById(int id) {
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            int ret = 0;
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.MONSTER).values().iterator();
            while (itr.hasNext()) {
                MapleMonster n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret++;
                }
            }
            return ret;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
    }

    public MapleReactor getReactorById(int id) {
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            MapleReactor ret = null;
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.REACTOR).values().iterator();
            while (itr.hasNext()) {
                MapleReactor n = (MapleReactor) itr.next();
                if (n.getReactorId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.MONSTER);
        if (mmo == null) {
            return null;
        }
        return (MapleMonster) mmo;
    }

    public MapleNPC getNPCByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.NPC);
        if (mmo == null) {
            return null;
        }
        return (MapleNPC) mmo;
    }

    public MapleReactor getReactorByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.REACTOR);
        if (mmo == null) {
            return null;
        }
        return (MapleReactor) mmo;
    }

    public MapleReactor getReactorByName(final String name) {
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = ((MapleReactor) obj);
                if (mr.getName().equalsIgnoreCase(name)) {
                    return mr;
                }
            }
            return null;
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    public void spawnNpc(final int id, final Point pos) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
    }

    public void spawnNpcWithEffect(final int id, final Point pos) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
        broadcastMessage(NpcPoolPackets.onUpdateLimitedInfo(npc, true));
    }

    public void makeNpcInvisible(int id) {
        MapleNPC npc = getNPCById(id);
        broadcastMessage(NpcPoolPackets.onUpdateLimitedInfo(npc, false));
    }

    public void makeNpcTalk(int id, String action) {
        MapleNPC npc = getNPCById(id);
        broadcastMessage(NpcPoolPackets.setSpecialAction(npc, action));
    }

    public void removeNpc(final int npcid) {
        mapobjectlocks.get(MapleMapObjectType.NPC).writeLock().lock();
        try {
            Iterator<MapleMapObject> itr =
                    mapobjects.get(MapleMapObjectType.NPC).values().iterator();
            while (itr.hasNext()) {
                MapleNPC npc = (MapleNPC) itr.next();
                if (npc.isCustom() && npc.getId() == npcid) {
                    broadcastMessage(MaplePacketCreator.removeNPC(npc.getObjectId()));
                    itr.remove();
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).writeLock().unlock();
        }
    }

    public void spawnMonster_sSack(final MapleMonster mob, final Point pos, final int spawnType) {
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mob.setPosition(spos);
        spawnMonster(mob, spawnType);
    }

    public void spawnMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
        spawnMonster_sSack(mob, pos, -2);
    }

    public int spawnMonsterWithEffectBelow(final MapleMonster mob, final Point pos, final int effect) {
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        return spawnMonsterWithEffect(mob, effect, spos);
    }

    public void spawnZakum(final int x, final int y) {
        final Point pos = new Point(x, y);
        final MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        // Might be possible to use the map object for reference in future.
        spawnFakeMonster(mainb);

        final int[] zakpart = {8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010};

        for (final int i : zakpart) {
            final MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);

            spawnMonster(part, -2);
        }
        if (squadSchedule != null) {
            cancelSquadSchedule();
            broadcastMessage(MaplePacketCreator.stopClock());
        }
    }

    public void spawnChaosZakum(final int x, final int y) {
        final Point pos = new Point(x, y);
        final MapleMonster mainb = MapleLifeFactory.getMonster(8800100);
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        // Might be possible to use the map object for reference in future.
        spawnFakeMonster(mainb);

        final int[] zakpart = {8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};

        for (final int i : zakpart) {
            final MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);

            spawnMonster(part, -2);
        }
        if (squadSchedule != null) {
            cancelSquadSchedule();
            broadcastMessage(MaplePacketCreator.stopClock());
        }
    }

    public void spawnFakeMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    private void checkRemoveAfter(final MapleMonster monster) {
        final int ra = monster.getStats().getRemoveAfter();

        if (ra > 0) {
            monster.registerKill(ra * 1000L);
        }
    }

    public void spawnRevives(final MapleMonster monster, final int oid) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        monster.setLinkOid(oid);
        spawnAndAddRangedMapObject(
                monster,
                c -> {
                    c.getSession().write(MobPacket.spawnMonster(monster, -2, 0, oid)); // TODO effect
                },
                null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster, final int spawnType) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        spawnAndAddRangedMapObject(
                monster,
                c -> {
                    c.getSession().write(MobPacket.spawnMonster(monster, spawnType, 0, 0));
                    if (monster.getId() == 9300166) {
                        MapTimer.getInstance()
                                .schedule(
                                        () -> { // will this cause an npE if
                                            // there're no chars on the map? :O
                                            killMonster(monster, null, false, false, (byte) 1);
                                        },
                                        4500 + Randomizer.nextInt(500));
                    }
                },
                null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public int spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        try {
            monster.setMap(this);
            monster.setPosition(pos);

            spawnAndAddRangedMapObject(
                    monster, c -> c.getSession().write(MobPacket.spawnMonster(monster, -2, effect, 0)), null);
            updateMonsterController(monster);
            spawnedMonstersOnMap.incrementAndGet();
            return monster.getObjectId();
        } catch (Exception e) {
            return -1;
        }
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);

        spawnAndAddRangedMapObject(
                monster,
                c -> {
                    c.getSession().write(MobPacket.spawnMonster(monster, -2, 0xfc, 0));
                    //		c.getSession().write(MobPacket.spawnFakeMonster(monster, 0));
                },
                null);
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);

        spawnAndAddRangedMapObject(reactor, c -> c.getSession().write(MaplePacketCreator.spawnReactor(reactor)), null);
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(
                door,
                c -> {
                    c.getSession()
                            .write(MaplePacketCreator.spawnDoor(
                                    door.getOwner().getId(), door.getTargetPosition(), false));
                    if (door.getOwner().getParty() != null
                            && (door.getOwner() == c.getPlayer()
                                    || door.getOwner()
                                            .getParty()
                                            .containsMembers(new MaplePartyCharacter(c.getPlayer())))) {
                        c.getSession()
                                .write(MapleUserPackets.partyPortal(
                                        door.getTown().getId(),
                                        door.getTarget().getId(),
                                        door.getSkill(),
                                        door.getTargetPosition()));
                    }
                    c.getSession()
                            .write(MaplePacketCreator.spawnPortal(
                                    door.getTown().getId(),
                                    door.getTarget().getId(),
                                    door.getSkill(),
                                    door.getTargetPosition()));
                    c.getSession().write(MaplePacketCreator.enableActions());
                },
                chr -> door.getTarget().getId() == chr.getMapId()
                        || door.getOwnerId() == chr.getId()
                        || (door.getOwner() != null
                                && door.getOwner().getParty() != null
                                && door.getOwner().getParty().getMemberById(chr.getId()) != null));
    }

    public void spawnSummon(final MapleSummon summon) {
        summon.updateMap(this);
        spawnAndAddRangedMapObject(
                summon,
                c -> {
                    if (!summon.isChangedMap()
                            || summon.getOwnerId() == c.getPlayer().getId()) {
                        c.getSession().write(MaplePacketCreator.spawnSummon(summon, true));
                    }
                },
                null);
    }

    public void spawnDragon(final MapleDragon summon) {
        spawnAndAddRangedMapObject(summon, c -> c.getSession().write(MaplePacketCreator.spawnDragon(summon)), null);
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean fake) {
        spawnAndAddRangedMapObject(mist, c -> mist.sendSpawnData(c), null);

        final MapTimer tMan = MapTimer.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        switch (mist.isPoisonMist()) {
            case 1:
                // poison: 0 = none, 1 = poisonous, 2 = recovery aura
                final MapleCharacter owner = getCharacterById(mist.getOwnerId());
                poisonSchedule = tMan.register(
                        () -> {
                            for (final MapleMapObject mo : getMapObjectsInRect(
                                    mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                                if (mist.makeChanceResult()) {
                                    ((MapleMonster) mo)
                                            .applyStatus(
                                                    owner,
                                                    new MonsterStatusEffect(
                                                            MonsterStatus.POISON,
                                                            1,
                                                            mist.getSourceSkill()
                                                                    .getId(),
                                                            null,
                                                            false),
                                                    true,
                                                    duration,
                                                    false);
                                }
                            }
                        },
                        2000,
                        2500);
                break;
            case 2:
                poisonSchedule = tMan.register(
                        () -> {
                            for (final MapleMapObject mo : getMapObjectsInRect(
                                    mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                                if (mist.makeChanceResult()) {
                                    final MapleCharacter chr = ((MapleCharacter) mo);
                                    chr.addMP((int) (mist.getSource().getX()
                                            * (chr.getStat().getMaxMp() / 100.0)));
                                }
                            }
                        },
                        2000,
                        2500);
                break;
            default:
                poisonSchedule = null;
                break;
        }
        tMan.schedule(
                () -> {
                    broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId()));
                    removeMapObject(mist);
                    if (poisonSchedule != null) {
                        poisonSchedule.cancel(false);
                    }
                },
                duration);
    }

    public void disappearingItemDrop(
            final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(
                MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 3),
                drop.getPosition());
    }

    public void spawnMesoDrop(
            final int meso,
            final Point position,
            final MapleMapObject dropper,
            final MapleCharacter owner,
            final boolean playerDrop,
            final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(
                mdrop,
                c -> c.getSession()
                        .write(MaplePacketCreator.dropItemFromMapObject(
                                mdrop, dropper.getPosition(), droppos, (byte) 1)),
                null);
        if (!everlast) {
            mdrop.registerExpire(120000);
            if (droptype == 0 || droptype == 1) {
                mdrop.registerFFA(30000);
            }
        }
    }

    public void spawnMobMesoDrop(
            final int meso,
            final Point position,
            final MapleMapObject dropper,
            final MapleCharacter owner,
            final boolean playerDrop,
            final byte droptype) {
        final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(
                mdrop,
                c -> c.getSession()
                        .write(MaplePacketCreator.dropItemFromMapObject(
                                mdrop, dropper.getPosition(), position, (byte) 1)),
                null);

        mdrop.registerExpire(120000);
        if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000);
        }
    }

    public void spawnMobDrop(
            final IItem idrop,
            final Point dropPos,
            final MapleMonster mob,
            final MapleCharacter chr,
            final byte droptype,
            final int questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);

        spawnAndAddRangedMapObject(
                mdrop,
                c -> {
                    if (mdrop.canLoot(c)) {
                        c.getSession()
                                .write(MaplePacketCreator.dropItemFromMapObject(
                                        mdrop, mob.getPosition(), dropPos, (byte) 1));
                    }
                },
                null);

        mdrop.registerExpire(120000);
        if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000);
        }
        activateItemReactors(mdrop, chr.getClient());
    }

    public void spawnRandDrop() {
        if (mapid != 910000000 || channel != 1) {
            return; // fm, ch1
        }

        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject o : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (((MapleMapItem) o).isRandDrop()) {
                    return;
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        MapTimer.getInstance()
                .schedule(
                        () -> {
                            final Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
                            final int theItem = Randomizer.nextInt(1000);
                            int itemid = 0;
                            if (theItem < 950) { // 0-949 = normal, 950-989 = rare, 990-999 = super
                                itemid =
                                        GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
                            } else if (theItem < 990) {
                                itemid = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
                            } else {
                                itemid = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
                            }
                            spawnAutoDrop(itemid, pos);
                        },
                        20000);
    }

    public void spawnAutoDrop(final int itemid, final Point pos) {
        IItem idrop = null;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip) ii.getEquipById(itemid));
        } else {
            idrop = new Item(itemid, (byte) 0, (short) 1, (byte) 0);
        }
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop);
        spawnAndAddRangedMapObject(
                mdrop,
                c -> c.getSession().write(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte) 1)),
                null);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte) 0));
        mdrop.registerExpire(120000);
    }

    public void spawnItemDrop(
            final MapleMapObject dropper,
            final MapleCharacter owner,
            final IItem item,
            Point pos,
            final boolean ffaDrop,
            final boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 2, playerDrop);

        spawnAndAddRangedMapObject(
                drop,
                c -> c.getSession()
                        .write(MaplePacketCreator.dropItemFromMapObject(
                                drop, dropper.getPosition(), droppos, (byte) 1)),
                null);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 0));

        if (!everlast) {
            drop.registerExpire(120000);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final IItem item = drop.getItem();

        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (final MapleMapObject o :
                    mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                final MapleReactor react = (MapleReactor) o;

                if (react.getReactorId() == 5022000) {
                    if (item.getItemId() == 4031757 && item.getQuantity() == 1) {
                        if (react.getArea().contains(drop.getPosition())) {
                            if (!react.isTimerActive()) {
                                MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                                react.setTimerActive(true);
                                break;
                            }
                        }
                    }
                } else if (react.getReactorType() == 100) {
                    if (GameConstants.isCustomReactItem(
                                    react.getReactorId(),
                                    item.getItemId(),
                                    react.getReactItem().getLeft())
                            && react.getReactItem().getRight() == item.getQuantity()) {
                        if (react.getArea().contains(drop.getPosition())) {
                            if (!react.isTimerActive()) {
                                MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                                react.setTimerActive(true);
                                break;
                            }
                        }
                    }
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    public int getItemsSize() {
        return mapobjects.get(MapleMapObjectType.ITEM).size();
    }

    public int getMobsSize() {
        return mapobjects.get(MapleMapObjectType.MONSTER).size();
    }

    public List<MapleMapItem> getAllItems() {
        return getAllItemsThreadsafe();
    }

    public List<MapleMapItem> getAllItemsThreadsafe() {
        return getAllItemsThreadsafe(new ArrayList<>());
    }

    public ArrayList<MapleMapItem> getAllItemsThreadsafe(ArrayList<MapleMapItem> ret) {
        ret.clear();
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                ret.add((MapleMapItem) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        return ret;
    }

    public void returnEverLastItem(final MapleCharacter chr) {
        for (final MapleMapObject o : getAllItemsThreadsafe()) {
            final MapleMapItem item = ((MapleMapItem) o);
            if (item.getOwner() == chr.getId()) {
                item.setPickedUp(true);
                broadcastMessage(
                        MaplePacketCreator.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getPosition());
                if (item.getMeso() > 0) {
                    chr.gainMeso(item.getMeso(), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
                }
                removeMapObject(item);
            }
        }
        spawnRandDrop();
    }

    public void talkMonster(final String msg, final int itemId, final int objectid) {
        if (itemId > 0) {
            startMapEffect(msg, itemId, false);
        }
        broadcastMessage(MobPacket.talkMonster(objectid, itemId, msg)); // 5120035
        broadcastMessage(MobPacket.removeTalkMonster(objectid));
    }

    public void startMapEffect(final String msg, final int itemId) {
        startMapEffect(msg, itemId, false);
    }

    public void startMapEffect(final String msg, final int itemId, final boolean jukebox) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        mapEffect.setJukebox(jukebox);
        broadcastMessage(mapEffect.makeStartData());
        MapTimer.getInstance()
                .schedule(
                        () -> {
                            broadcastMessage(mapEffect.makeDestroyData());
                            mapEffect = null;
                        },
                        jukebox ? 300000 : 30000);
    }

    public void startExtendedMapEffect(final String msg, final int itemId) {
        broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, true));
        MapTimer.getInstance()
                .schedule(
                        () -> {
                            broadcastMessage(MaplePacketCreator.removeMapEffect());
                            broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, false));
                            // dont remove mapeffect.
                        },
                        60000);
    }

    public void startJukebox(final String msg, final int itemId) {
        startMapEffect(msg, itemId, true);
    }

    public void addPlayer(final MapleCharacter chr) {
        mapobjectlocks.get(MapleMapObjectType.PLAYER).writeLock().lock();
        try {
            mapobjects.get(MapleMapObjectType.PLAYER).put(chr.getObjectId(), chr);
        } finally {
            mapobjectlocks.get(MapleMapObjectType.PLAYER).writeLock().unlock();
        }

        charactersLock.writeLock().lock();
        try {
            characters.add(chr);
        } finally {
            charactersLock.writeLock().unlock();
        }

        if (mapid == 109080000 || mapid == 109080001 || mapid == 109080002 || mapid == 109080003) {
            chr.setCoconutTeam(getAndSwitchTeam() ? 0 : 1);
        } else if (mapid == 502010300) { // In miner map
            MapleItemInformationProvider.getInstance().getItemEffect(2210063).applyTo(chr);
        } else if (mapid == 502010200 || mapid == 502040100) { // Deep Sea crash site
            MapleItemInformationProvider.getInstance().getItemEffect(2210062).applyTo(chr);
        } else if (mapid == 502030004) { // lump energy pq
            MapleItemInformationProvider.getInstance().getItemEffect(2210065).applyTo(chr);
        }

        if (!onFirstUserEnter.equals("")) {
            if (getCharactersSize() == 1) {
                MapScriptMethods.startScript_FirstUser(chr.getClient(), onFirstUserEnter);
            }
        }
        sendObjectPlacement(chr);
        if (!chr.isHidden() && !chr.isGameMaster()) {
            chr.getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnPlayerMapobject(chr));
        }

        if (!onUserEnter.equals("")) {
            MapScriptMethods.startScript_User(chr.getClient(), onUserEnter);
        }
        switch (mapid) {
            case 109080000: // coconut shit
            case 109080001:
            case 109080002:
            case 109080003:
                chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect(chr.getCoconutTeam()));
                break;
            case 809000101:
            case 809000201:
                chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect());
                break;
        }

        if (chr.getParty() != null) {
            chr.silentPartyUpdate();
            chr.getClient()
                    .getSession()
                    .write(MapleUserPackets.updateParty(
                            chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, null));
        }
        final MapleStatEffect stat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (stat != null) {
            final MapleSummon summon = chr.getSummons().get(stat.getSourceId());
            summon.setPosition(chr.getPosition());
            try {
                summon.setFh(getFootholds().findBelow(chr.getPosition()).getId());
            } catch (NullPointerException e) {
                summon.setFh(0); // lol, it can be fixed by movement
            }
            chr.addVisibleMapObject(summon);
            this.spawnSummon(summon);
        }
        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }
        if (timeLimit > 0 && getForcedReturnMap() != null) {
            chr.startMapTimeLimitTask(timeLimit, getForcedReturnMap());
        }
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            if (FieldLimitType.Mount.check(fieldLimit)) {
                chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
            }
        }
        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int)
                    (chr.getEventInstance().getTimeLeft() / 1000)));
        }
        if (chr.getEvent() != null) {
            chr.getEvent().onChangeMap(this, chr);
        }
        if (hasClock()) {
            final Calendar cal = Calendar.getInstance();
            chr.getClient()
                    .getSession()
                    .write((MaplePacketCreator.getClockTime(
                            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        if (chr.getCarnivalParty() != null && chr.getEventInstance() != null) {
            chr.getEventInstance().onMapLoad(chr);
        }
        MapleEvent.mapLoad(chr, channel);
        if (getSquadBegin() != null
                && getSquadBegin().getTimeLeft() > 0
                && getSquadBegin().getStatus() == 1) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int)
                    (getSquadBegin().getTimeLeft() / 1000)));
        }
        if (mapid / 1000 != 105100
                && mapid / 100 != 8020003
                && mapid / 100 != 8020008) { // no boss_balrog/2095/coreblaze/auf. but coreblaze/auf does
            // AFTER
            final MapleSquad sqd = getSquadByMap(); // for all squads
            if (!squadTimer && sqd != null && chr.getName().equals(sqd.getLeaderName())) {
                // leader? display
                doShrine(false);
                squadTimer = true;
            }
        }
        if (getNumMonsters() > 0
                && (mapid == 280030001
                        || mapid == 240060201
                        || mapid == 280030000
                        || mapid == 240060200
                        || mapid == 220080001
                        || mapid == 541020800
                        || mapid == 541010100)) {
            String music = "Bgm09/TimeAttack";
            switch (mapid) {
                case 240060200:
                case 240060201:
                    music = "Bgm14/HonTale";
                    break;
                case 280030000:
                case 280030001:
                    music = "Bgm06/FinalFight";
                    break;
            }
            chr.getClient().getSession().write(MaplePacketCreator.musicChange(music));
            // maybe timer too for zak/ht
        }
        if (mapid == 914000000) {
            chr.getClient().getSession().write(MaplePacketCreator.temporaryStats_Aran());
        } else if (mapid == 105100300 && chr.getLevel() >= 91) {
            chr.getClient().getSession().write(MaplePacketCreator.temporaryStats_Balrog(chr));
        } else if (mapid == 140090000 || mapid == 105100301 || mapid == 105100401 || mapid == 105100100) {
            chr.getClient().getSession().write(MaplePacketCreator.temporaryStats_Reset());
        }
        if (GameConstants.isEvan(chr.getJob().getId())
                && chr.getJob().getId() >= 2200
                && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null) {
            if (chr.getDragon() == null) {
                chr.makeDragon();
            }
            spawnDragon(chr.getDragon());
            updateMapObjectVisibility(chr, chr.getDragon());
        }
        if (mapid == 502010300) { // In miner map
            NPCScriptManager.getInstance().dispose(chr.getClient());
            NPCScriptManager.getInstance().start(chr.getClient(), 9250125);
        } else if (mapid == 1000000) {
            chr.getClient().getSession().write(MaplePacketCreator.musicChange("Bgm00/DragonDream"));
        }
        for (int i = 1066; i <= 1067; i++) {
            final int realId = GameConstants.getSkillByJob(i, chr.getJob().getId());
            if (chr.getSkillLevel(realId) > -1
                    && chr.getMorphState() != 2210062
                    && chr.getMorphState() != 2210063
                    && chr.getMorphState() != 2210064
                    && chr.getMorphState() != 2210065) {
                chr.changeSkillLevel_Skip(SkillFactory.getSkill(realId), (byte) -1, (byte) 0);
            }
        }
        if (permanentWeather > 0) {
            chr.getClient()
                    .getSession()
                    .write(MaplePacketCreator.startMapEffect("", permanentWeather, false)); // snow, no msg
        }
        if (getPlatforms().size() > 0) {
            chr.getClient().getSession().write(MaplePacketCreator.getMovingPlatforms(this));
        }
        if (environment.size() > 0) {
            chr.getClient().getSession().write(MaplePacketCreator.getUpdateEnvironment(this));
        }
        if (isTown()) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.RAINING_MINES);
        }
        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), true);
            }
        }
        chr.updatePetAuto();
        onUserEnter(chr);
    }

    public int getNumItems() {
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            return mapobjects.get(MapleMapObjectType.ITEM).size();
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
    }

    public int getNumMonsters() {
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            return mapobjects.get(MapleMapObjectType.MONSTER).size();
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
    }

    public void doShrine(final boolean spawned) { // false = entering map, true = defeated
        if (squadSchedule != null) {
            cancelSquadSchedule();
        }
        final int mode = (mapid == 280030000
                ? 1
                : (mapid == 280030001 ? 2 : (mapid == 240060200 || mapid == 240060201 ? 3 : 0)));
        // chaos_horntail message for horntail too because it looks nicer
        final MapleSquad sqd = getSquadByMap();
        final EventManager em = getEMByMap();
        if (sqd != null && em != null && getCharactersSize() > 0) {
            final String leaderName = sqd.getLeaderName();
            final String state = em.getProperty("state");
            final Runnable run;
            MapleMap returnMapa = getForcedReturnMap();
            if (returnMapa == null || returnMapa.getId() == mapid) {
                returnMapa = getReturnMap();
            }
            if (mode == 1) { // zakum
                broadcastMessage(MaplePacketCreator.showZakumShrine(spawned, 5));
            } else if (mode == 2) { // chaoszakum
                broadcastMessage(MaplePacketCreator.showChaosZakumShrine(spawned, 5));
            } else if (mode == 3) { // ht/chaosht
                broadcastMessage(MaplePacketCreator.showChaosHorntailShrine(spawned, 5));
            } else {
                broadcastMessage(MaplePacketCreator.showHorntailShrine(spawned, 5));
            }
            if (mode == 1 || spawned) { // both of these together dont go well
                broadcastMessage(MaplePacketCreator.getClock(300)); // 5 min
            }
            final MapleMap returnMapz = returnMapa;
            if (!spawned) { // no monsters yet; inforce timer to spawn it quickly
                final List<MapleMonster> monsterz = getAllMonstersThreadsafe();
                final List<Integer> monsteridz = new ArrayList<>();
                for (MapleMapObject m : monsterz) {
                    monsteridz.add(m.getObjectId());
                }
                run = () -> {
                    final MapleSquad sqnow = MapleMap.this.getSquadByMap();
                    if (MapleMap.this.getCharactersSize() > 0
                            && MapleMap.this.getNumMonsters() == monsterz.size()
                            && sqnow != null
                            && sqnow.getStatus() == 2
                            && sqnow.getLeaderName().equals(leaderName)
                            && MapleMap.this.getEMByMap().getProperty("state").equals(state)) {
                        boolean passed = monsterz.isEmpty();
                        for (MapleMapObject m : MapleMap.this.getAllMonstersThreadsafe()) {
                            for (int i : monsteridz) {
                                if (m.getObjectId() == i) {
                                    passed = true;
                                    break;
                                }
                            }
                            if (passed) {
                                break;
                            } // even one of the monsters is the same
                        }
                        if (passed) {
                            // are we still the same squad? are monsters still == 0?
                            byte[] packet;
                            if (mode == 1) { // zakum
                                packet = MaplePacketCreator.showZakumShrine(spawned, 0);
                            } else if (mode == 2) { // chaoszakum
                                packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                            } else {
                                packet = MaplePacketCreator.showHorntailShrine(
                                        spawned, 0); // chaoshorntail message is weird
                            }
                            for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) { // warp all in
                                // map
                                chr.getClient().getSession().write(packet);
                                chr.changeMap(returnMapz, returnMapz.getPortal(0)); // hopefully event will still take
                                // care of everything once warp out
                            }
                            checkStates("");
                            resetFully();
                        }
                    }
                };
            } else { // inforce timer to gtfo
                run = () -> {
                    MapleSquad sqnow = MapleMap.this.getSquadByMap();
                    // we dont need to stop clock here because they're getting warped
                    // out anyway
                    if (MapleMap.this.getCharactersSize() > 0
                            && sqnow != null
                            && sqnow.getStatus() == 2
                            && sqnow.getLeaderName().equals(leaderName)
                            && MapleMap.this.getEMByMap().getProperty("state").equals(state)) {
                        // are we still the same squad? monsters however don't count
                        byte[] packet;
                        if (mode == 1) { // zakum
                            packet = MaplePacketCreator.showZakumShrine(spawned, 0);
                        } else if (mode == 2) { // chaoszakum
                            packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                        } else {
                            packet =
                                    MaplePacketCreator.showHorntailShrine(spawned, 0); // chaoshorntail message is weird
                        }
                        for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) { // warp all in map
                            chr.getClient().getSession().write(packet);
                            chr.changeMap(returnMapz, returnMapz.getPortal(0)); // hopefully event will still take care
                            // of everything once warp out
                        }
                        checkStates("");
                        resetFully();
                    }
                };
            }
            squadSchedule = MapTimer.getInstance().schedule(run, 300000); // 5 mins
        }
    }

    public MapleSquad getSquadByMap() {
        String zz = null;
        switch (mapid) {
            case 105100400:
            case 105100300:
                zz = "BossBalrog";
                break;
            case 280030000:
                zz = "ZAK";
                break;
            case 280030001:
                zz = "ChaosZak";
                break;
            case 240060200:
                zz = "Horntail";
                break;
            case 240060201:
                zz = "ChaosHT";
                break;
            case 270050100:
                zz = "PinkBean";
                break;
            case 802000111:
                zz = "nmm_squad";
                break;
            case 802000211:
                zz = "VERGAMOT";
                break;
            case 802000311:
                zz = "2095_tokyo";
                break;
            case 802000411:
                zz = "Dunas";
                break;
            case 802000611:
                zz = "Nibergen_squad";
                break;
            case 802000711:
                zz = "dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                zz = "Core_Blaze";
                break;
            case 802000821:
                zz = "Aufheben";
                break;
            default:
                return null;
        }
        return WorldServer.getInstance().getChannel(channel).getMapleSquad(zz);
    }

    public MapleSquad getSquadBegin() {
        if (squad.length() > 0) {
            return WorldServer.getInstance().getChannel(channel).getMapleSquad(squad);
        }
        return null;
    }

    public EventManager getEMByMap() {
        String em = null;
        switch (mapid) {
            case 105100400:
                em = "BossBalrog_EASY";
                break;
            case 105100300:
                em = "BossBalrog_NORMAL";
                break;
            case 280030000:
                em = "ZakumBattle";
                break;
            case 240060200:
                em = "HorntailBattle";
                break;
            case 280030001:
                em = "ChaosZakum";
                break;
            case 240060201:
                em = "ChaosHorntail";
                break;
            case 270050100:
                em = "PinkBeanBattle";
                break;
            case 802000111:
                em = "NamelessMagicMonster";
                break;
            case 802000211:
                em = "Vergamot";
                break;
            case 802000311:
                em = "2095_tokyo";
                break;
            case 802000411:
                em = "Dunas";
                break;
            case 802000611:
                em = "Nibergen";
                break;
            case 802000711:
                em = "Dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                em = "CoreBlaze";
                break;
            case 802000821:
                em = "Aufhaven";
                break;
            default:
                return null;
        }
        return WorldServer.getInstance().getChannel(channel).getEventSM().getEventManager(em);
    }

    public void removePlayer(final MapleCharacter chr) {
        // log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });

        if (everlast) {
            returnEverLastItem(chr);
        }

        charactersLock.writeLock().lock();
        try {
            characters.remove(chr);
        } finally {
            charactersLock.writeLock().unlock();
        }
        removeMapObject(chr);
        chr.checkFollow();
        broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        final List<MapleMonster> update = new ArrayList<>();
        final Iterator<MapleMonster> controlled = chr.getControlled().iterator();

        while (controlled.hasNext()) {
            MapleMonster monster = controlled.next();
            if (monster != null) {
                monster.setController(null);
                monster.setControllerHasAggro(false);
                monster.setControllerKnowsAboutAggro(false);
                controlled.remove();
                update.add(monster);
            }
        }
        for (MapleMonster mons : update) {
            updateMonsterController(mons);
        }
        chr.leaveMap();
        checkStates(chr.getName());
        chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        chr.cancelEffectFromBuffStat(MapleBuffStat.REAPER);
        boolean cancelSummons = false;
        for (final MapleSummon summon : chr.getSummons().values()) {
            if (summon.getMovementType() == SummonMovementType.STATIONARY
                    || summon.getMovementType() == SummonMovementType.CIRCLE_STATIONARY
                    || summon.getMovementType() == SummonMovementType.WALK_STATIONARY) {
                cancelSummons = true;
            } else {
                summon.setChangedMap(true);
                removeMapObject(summon);
            }
        }
        if (cancelSummons) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        }
        if (chr.getDragon() != null) {
            removeMapObject(chr.getDragon());
        }

        onUserExit(chr);
    }

    public void broadcastMessage(final byte[] packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastMessage(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /*	public void broadcastMessage(MapleCharacter source, byte[] packet, boolean repeatToSource, boolean ranged) {
    broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition());
    }*/
    public void broadcastMessage(final byte[] packet, final Point rangedFrom) {
        broadcastMessage(null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public void broadcastMessage(final MapleCharacter source, final byte[] packet, final Point rangedFrom) {
        broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    private void broadcastMessage(
            final MapleCharacter source, final byte[] packet, final double rangeSq, final Point rangedFrom) {
        charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    private void sendObjectPlacement(final MapleCharacter c) {
        if (c == null) {
            return;
        }
        for (final MapleMapObject o : this.getAllMonstersThreadsafe()) {
            updateMonsterController((MapleMonster) o);
        }
        for (final MapleMapObject o : getMapObjectsInRange(
                c.getPosition(), GameConstants.maxViewRangeSq(), GameConstants.rangedMapobjectTypes)) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                if (!((MapleReactor) o).isAlive()) {
                    continue;
                }
            }
            o.sendSpawnData(c.getClient());
            c.addVisibleMapObject(o);
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq) {
        final List<MapleMapObject> ret = new ArrayList<>();
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator<MapleMapObject> itr = mapobjects.get(type).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = itr.next();
                    if (from.distanceSq(mmo.getPosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        return getMapObjectsInRange(from, rangeSq, Collections.singletonList(MapleMapObjectType.ITEM));
    }

    public List<MapleMapObject> getMapObjectsInRange(
            final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<>();
        for (MapleMapObjectType type : MapObject_types) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator<MapleMapObject> itr = mapobjects.get(type).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = itr.next();
                    if (from.distanceSq(mmo.getPosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInRect(
            final Rectangle box, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<>();
        for (MapleMapObjectType type : MapObject_types) {
            mapobjectlocks.get(type).readLock().lock();
            try {
                Iterator<MapleMapObject> itr = mapobjects.get(type).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = itr.next();
                    if (box.contains(mmo.getPosition())) {
                        ret.add(mmo);
                    }
                }
            } finally {
                mapobjectlocks.get(type).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleCharacter> getPlayersInRectAndInList(final Rectangle box, final List<MapleCharacter> chrList) {
        final List<MapleCharacter> character = new LinkedList<>();

        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter a;
            while (ltr.hasNext()) {
                a = ltr.next();
                if (chrList.contains(a) && box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return character;
    }

    public void addPortal(final MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(final String portalname) {
        for (final MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return portals.values().iterator().next();
    }

    public MaplePortal getPortal(final int portalid) {
        return portals.get(portalid);
    }

    public void resetPortals() {
        for (final MaplePortal port : portals.values()) {
            port.setPortalState(true);
        }
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }

    public void setFootholds(final MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public void loadMonsterRate(final boolean first) {
        final int spawnSize = monsterSpawn.size();
        maxRegularSpawn = Math.round(spawnSize * monsterRate);
        if (maxRegularSpawn < 2) {
            maxRegularSpawn = 2;
        } else if (maxRegularSpawn > spawnSize) {
            maxRegularSpawn = spawnSize - (spawnSize / 15);
        }
        if (fixedMob > 0) {
            maxRegularSpawn = fixedMob;
        }
        Collection<Spawns> newSpawn = new LinkedList<>();
        Collection<Spawns> newBossSpawn = new LinkedList<>();
        for (final Spawns s : monsterSpawn) {
            if (s.getCarnivalTeam() >= 2) {
                continue; // Remove carnival spawned mobs
            }
            if (s.getMonster().getStats().isBoss()) {
                newBossSpawn.add(s);
            } else {
                newSpawn.add(s);
            }
        }
        monsterSpawn.clear();
        monsterSpawn.addAll(newBossSpawn);
        monsterSpawn.addAll(newSpawn);

        if (first && spawnSize > 0) {
            lastSpawnTime = System.currentTimeMillis();
            if (GameConstants.isForceRespawn(mapid)) {
                createMobInterval = 15000;
            }
        }
    }

    public SpawnPoint addMonsterSpawn(
            final MapleMonster monster, final int mobTime, final byte carnivalTeam, final String msg) {
        final Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        final SpawnPoint sp = new SpawnPoint(monster, this, newpos, mobTime, carnivalTeam, msg);
        if (carnivalTeam > -1) {
            monsterSpawn.add(0, sp); // at the beginning
        } else {
            monsterSpawn.add(sp);
        }
        return sp;
    }

    public void addAreaMonsterSpawn(
            final MapleMonster monster, Point pos1, Point pos2, Point pos3, final int mobTime, final String msg) {
        pos1 = calcPointBelow(pos1);
        pos2 = calcPointBelow(pos2);
        pos3 = calcPointBelow(pos3);
        if (pos1 != null) {
            pos1.y -= 1;
        }
        if (pos2 != null) {
            pos2.y -= 1;
        }
        if (pos3 != null) {
            pos3.y -= 1;
        }
        if (pos1 == null && pos2 == null && pos3 == null) {
            log.info("WARNING: mapid " + mapid + ", monster " + monster.getId() + " could not be spawned.");

            return;
        } else if (pos1 != null) {
            if (pos2 == null) {
                pos2 = new Point(pos1);
            }
            if (pos3 == null) {
                pos3 = new Point(pos1);
            }
        } else if (pos2 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos2);
            }
            if (pos3 == null) {
                pos3 = new Point(pos2);
            }
        } else if (pos3 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos3);
            }
            if (pos2 == null) {
                pos2 = new Point(pos3);
            }
        }
        monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg));
    }

    public List<MapleCharacter> getCharacters() {
        return getCharactersThreadsafe();
    }

    public List<MapleCharacter> getCharactersThreadsafe() {
        return getCharactersThreadsafe(new ArrayList<>());
    }

    public ArrayList<MapleCharacter> getCharactersThreadsafe(ArrayList<MapleCharacter> chars) {
        chars.clear();
        charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : characters) {
                chars.add(mc);
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return chars;
    }

    public MapleCharacter getCharacterByName(final String name) {
        charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : characters) {
                if (mc.getName().equalsIgnoreCase(name)) {
                    return mc;
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return null;
    }

    public MapleCharacter getCharacterById_InMap(final int id) {
        return getCharacterById(id);
    }

    public MapleCharacter getCharacterById(final int id) {
        charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : characters) {
                if (mc.getId() == id) {
                    return mc;
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return null;
    }

    public void updateMapObjectVisibility(final MapleCharacter chr, final MapleMapObject mo) {
        if (chr == null) {
            return;
        }
        if (mo instanceof MapleCharacter) {
            if (mo == chr) {
                return;
            }
        }

        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getType() == MapleMapObjectType.SUMMON
                    || mo.getPosition().distanceSq(chr.getPosition()) <= GameConstants.maxViewRangeSq()) {
                mo.sendSpawnData(chr.getClient());
                chr.addVisibleMapObject(mo);
            }
        } else { // monster left view range
            if (mo.getType() != MapleMapObjectType.SUMMON
                    && mo.getPosition().distanceSq(chr.getPosition()) > GameConstants.maxViewRangeSq()) {
                chr.removeVisibleMapObject(mo);
                if (mo instanceof MapleCharacter) {
                    ((MapleCharacter) mo).removeVisibleMapObject(chr);
                }
                mo.sendDestroyData(chr.getClient());
            }
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);

        charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : characters) {
                updateMapObjectVisibility(mc, monster);
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    public void movePlayer(final MapleCharacter player, final Point newPosition) {
        player.setPosition(newPosition);
        try {
            Collection<MapleMapObject> visibleObjects = player.getAndWriteLockVisibleMapObjects();
            ArrayList<MapleMapObject> copy = new ArrayList<>(visibleObjects);
            Iterator<MapleMapObject> itr = copy.iterator();
            while (itr.hasNext()) {
                MapleMapObject mo = itr.next();
                if (mo != null && getMapObject(mo.getObjectId(), mo.getType()) == mo) {
                    updateMapObjectVisibility(player, mo);
                } else {
                    visibleObjects.remove(mo);
                }
            }
            for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), GameConstants.maxViewRangeSq())) {
                if (mo != null && !player.isMapObjectVisible(mo) && mo != player) {
                    mo.sendSpawnData(player.getClient());
                    visibleObjects.add(mo);
                    if (mo instanceof MapleCharacter) {
                        player.sendSpawnData(((MapleCharacter) mo).getClient());
                    }
                }
            }
        } finally {
            player.unlockWriteVisibleMapObjects();
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double distance, shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0
                    && portal.getType() <= 2
                    && distance < shortestDistance
                    && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public String spawnDebug() {
        String sb = "Mapobjects in map : "
                + this.getMapObjectSize()
                + " spawnedMonstersOnMap: "
                + spawnedMonstersOnMap
                + " spawnpoints: "
                + monsterSpawn.size()
                + " maxRegularSpawn: "
                + maxRegularSpawn
                + " actual monsters: "
                + getNumMonsters();

        return sb;
    }

    public int characterSize() {
        return characters.size();
    }

    public int getMapObjectSize() {
        return mapobjects.size() + getCharactersSize() - characters.size();
    }

    public int getCharactersSize() {
        int ret = 0;
        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            while (ltr.hasNext()) {
                ltr.next();
                ret++;
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return ret;
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    public void respawn(boolean force) {
        respawn(force, System.currentTimeMillis());
    }

    public void respawn(boolean force, long now) {
        lastSpawnTime = now;
        if (force) { // cpq quick hack
            final int numShouldSpawn =
                    (monsterSpawn.size() * ServerConstants.getRespawnRate(mapid)) - spawnedMonstersOnMap.get();
            if (numShouldSpawn > 0) {
                int spawned = 0;
                for (Spawns spawnPoint : monsterSpawn) {
                    spawnPoint.spawnMonster(this);
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } else {
            final int numShouldSpawn =
                    (maxRegularSpawn * ServerConstants.getRespawnRate(mapid)) - spawnedMonstersOnMap.get();
            if (numShouldSpawn > 0) {
                int spawned = 0;

                final List<Spawns> randomSpawn = new ArrayList<>(monsterSpawn);
                Collections.shuffle(randomSpawn);

                for (Spawns spawnPoint : randomSpawn) {
                    if (spawnPoint.shouldSpawn() || GameConstants.isForceRespawn(mapid)) {
                        spawnPoint.spawnMonster(this);
                        spawned++;
                    }
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        }
    }

    public String getSnowballPortal() {
        int[] teamss = new int[2];
        for (MapleCharacter chr : getCharactersThreadsafe()) {
            if (chr.getPosition().y > -80) {
                teamss[0]++;
            } else {
                teamss[1]++;
            }
        }
        if (teamss[0] > teamss[1]) {
            return "st01";
        } else {
            return "st00";
        }
    }

    public boolean isDisconnected(int id) {
        return dced.contains(Integer.valueOf(id));
    }

    public void addDisconnected(int id) {
        dced.add(Integer.valueOf(id));
    }

    public void resetDisconnected() {
        dced.clear();
    }

    public void startSpeedRun() {
        final MapleSquad squad = getSquadByMap();
        if (squad != null) {
            for (MapleCharacter chr : getCharactersThreadsafe()) {
                if (chr.getName().equals(squad.getLeaderName())) {
                    startSpeedRun(chr.getName());
                    return;
                }
            }
        }
    }

    public void startSpeedRun(String leader) {
        speedRunStart = System.currentTimeMillis();
        speedRunLeader = leader;
    }

    public void endSpeedRun() {
        speedRunStart = 0;
        speedRunLeader = "";
    }

    public void getRankAndAdd(String leader, String time, SpeedRunType type, long timz, Collection<String> squad) {
        try (var con = DatabaseConnection.getConnection()) {
            // Pair<String, Map<Integer, String>>
            StringBuilder rett = new StringBuilder();
            if (squad != null) {
                for (String chr : squad) {
                    rett.append(chr);
                    rett.append(",");
                }
            }
            String z = rett.toString();
            if (squad != null) {
                z = z.substring(0, z.length() - 1);
            }
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO speedruns(`type`, `leader`, `timestring`, `time`," + " `members`) VALUES (?,?,?,?,?)");
            ps.setString(1, type.name());
            ps.setString(2, leader);
            ps.setString(3, time);
            ps.setLong(4, timz);
            ps.setString(5, z);
            ps.executeUpdate();
            ps.close();

            if (SpeedRunner.getInstance().getSpeedRunData(type) == null) { // great, we just add it
                SpeedRunner.getInstance()
                        .addSpeedRunData(
                                type,
                                SpeedRunner.getInstance()
                                        .addSpeedRunData(
                                                new StringBuilder(
                                                        "#rThese are the speedrun times for " + type + ".#k\r\n\r\n"),
                                                new HashMap<>(),
                                                z,
                                                leader,
                                                1,
                                                time));
            } else {
                // i wish we had a way to get the rank
                // TODO revamp
                SpeedRunner.getInstance().removeSpeedRunData(type);
                SpeedRunner.getInstance().loadSpeedRunData(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getSpeedRunStart() {
        return speedRunStart;
    }

    public void disconnectAll() {
        for (MapleCharacter chr : getCharactersThreadsafe()) {
            if (!chr.isGameMaster()) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close();
            }
        }
    }

    public List<MapleNPC> getAllNPCs() {
        return getAllNPCsThreadsafe();
    }

    public List<MapleNPC> getAllNPCsThreadsafe() {
        ArrayList<MapleNPC> ret = new ArrayList<>();
        mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.NPC).values()) {
                ret.add((MapleNPC) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
        return ret;
    }

    public void resetNPCs() {
        List<MapleNPC> npcs = getAllNPCsThreadsafe();
        for (MapleNPC npc : npcs) {
            if (npc.isCustom()) {
                broadcastMessage(MaplePacketCreator.spawnNPC(npc, false));
                removeMapObject(npc);
            }
        }
    }

    public void resetFully() {
        resetFully(true);
    }

    public void resetFully(final boolean respawn) {
        killAllMonsters(false);
        reloadReactors();
        removeDrops();
        resetNPCs();
        resetSpawns();
        resetDisconnected();
        endSpeedRun();
        cancelSquadSchedule();
        resetPortals();
        environment.clear();
        if (respawn) {
            respawn(true);
        }
    }

    public void cancelSquadSchedule() {
        squadTimer = false;
        if (squadSchedule != null) {
            squadSchedule.cancel(false);
            squadSchedule = null;
        }
    }

    public void removeDrops() {
        List<MapleMapItem> items = this.getAllItemsThreadsafe();
        for (MapleMapItem i : items) {
            i.expire(this);
        }
    }

    public void resetAllSpawnPoint(int mobid, int mobTime) {
        Collection<Spawns> sss = new LinkedList<>(monsterSpawn);
        resetFully();
        monsterSpawn.clear();
        for (Spawns s : sss) {
            MapleMonster newMons = MapleLifeFactory.getMonster(mobid);
            MapleMonster oldMons = s.getMonster();
            newMons.setCy(oldMons.getCy());
            newMons.setF(oldMons.getF());
            newMons.setFh(oldMons.getFh());
            newMons.setRx0(oldMons.getRx0());
            newMons.setRx1(oldMons.getRx1());
            newMons.setPosition(new Point(oldMons.getPosition()));
            newMons.setHide(oldMons.isHidden());
            addMonsterSpawn(newMons, mobTime, (byte) -1, null);
        }
        loadMonsterRate(true);
    }

    public void resetSpawns() {
        boolean changed = false;
        Iterator<Spawns> sss = monsterSpawn.iterator();
        while (sss.hasNext()) {
            if (sss.next().getCarnivalId() > -1) {
                sss.remove();
                changed = true;
            }
        }
        setSpawns(true);
        if (changed) {
            loadMonsterRate(true);
        }
    }

    public boolean makeCarnivalSpawn(final int team, final MapleMonster newMons, final int num) {
        MonsterPoint ret = null;
        for (MonsterPoint mp : nodes.getMonsterPoints()) {
            if (mp.team == team || mp.team == -1) {
                final Point newpos = calcPointBelow(new Point(mp.x, mp.y));
                newpos.y -= 1;
                boolean found = false;
                for (Spawns s : monsterSpawn) {
                    if (s.getCarnivalId() > -1
                            && (mp.team == -1 || s.getCarnivalTeam() == mp.team)
                            && s.getPosition().x == newpos.x
                            && s.getPosition().y == newpos.y) {
                        found = true;
                        break; // this point has already been used.
                    }
                }
                if (!found) {
                    ret = mp; // this point is safe for use.
                    break;
                }
            }
        }
        if (ret != null) {
            newMons.setCy(ret.cy);
            newMons.setF(0); // always.
            newMons.setFh(ret.fh);
            newMons.setRx0(ret.x + 50);
            newMons.setRx1(ret.x - 50); // does this matter
            newMons.setPosition(new Point(ret.x, ret.y));
            newMons.setHide(false);
            final SpawnPoint sp = addMonsterSpawn(newMons, 1, (byte) team, null);
            sp.setCarnival(num);
        }
        return ret != null;
    }

    public boolean makeCarnivalReactor(final int team, final int num) {
        final MapleReactor old = getReactorByName(team + "" + num);
        if (old != null && old.getState() < 5) { // already exists
            return false;
        }
        Point guardz = null;
        final List<MapleReactor> react = getAllReactorsThreadsafe();
        for (Pair<Point, Integer> guard : nodes.getGuardians()) {
            if (guard.right == team || guard.right == -1) {
                boolean found = false;
                for (MapleReactor r : react) {
                    if (r.getPosition().x == guard.left.x && r.getPosition().y == guard.left.y && r.getState() < 5) {
                        found = true;
                        break; // already used
                    }
                }
                if (!found) {
                    guardz = guard.left; // this point is safe for use.
                    break;
                }
            }
        }
        if (guardz != null) {
            final MapleReactorStats stats = MapleReactorFactory.getReactor(9980000 + team);
            final MapleReactor my = new MapleReactor(stats, 9980000 + team);
            stats.setFacingDirection((byte) 0); // always
            my.setPosition(guardz);
            my.setState((byte) 1);
            my.setDelay(0);
            my.setName(team + "" + num); // lol
            // with num. -> guardians in factory
            spawnReactor(my);
            final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            for (MapleMonster mons : getAllMonstersThreadsafe()) {
                if (mons.getCarnivalTeam() == team) {
                    skil.getSkill().applyEffect(null, mons, false);
                }
            }
        }
        return guardz != null;
    }

    public void blockAllPortal() {
        for (MaplePortal p : portals.values()) {
            p.setPortalState(false);
        }
    }

    public boolean getAndSwitchTeam() {
        return getCharactersSize() % 2 != 0;
    }

    public void setSquad(String s) {
        this.squad = s;
    }

    public int getChannel() {
        return channel;
    }

    public int getConsumeItemCoolTime() {
        return consumeItemCoolTime;
    }

    public void setConsumeItemCoolTime(int ciit) {
        this.consumeItemCoolTime = ciit;
    }

    public int getPermanentWeather() {
        return permanentWeather;
    }

    public void setPermanentWeather(int pw) {
        this.permanentWeather = pw;
    }

    public void checkStates(final String chr) {
        final MapleSquad sqd = getSquadByMap();
        final EventManager em = getEMByMap();
        final int size = getCharactersSize();
        if (sqd != null) {
            sqd.removeMember(chr);
            if (em != null) {
                if (sqd.getLeaderName().equals(chr)) {
                    em.setProperty("leader", "false");
                }
                if (chr.equals("") || size == 0) {
                    sqd.clear();
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
                    cancelSquadSchedule();
                }
            }
        }
        if (em != null && em.getProperty("state") != null) {
            if (size == 0) {
                em.setProperty("state", "0");
                if (em.getProperty("leader") != null) {
                    em.setProperty("leader", "true");
                }
            }
        }
        if (speedRunStart > 0 && speedRunLeader.equalsIgnoreCase(chr)) {
            if (size > 0) {
                broadcastMessage(
                        MaplePacketCreator.serverNotice(5, "The leader is not in the map! Your speedrun has failed"));
            }
            endSpeedRun();
        }
    }

    public List<MaplePlatform> getPlatforms() {
        return nodes.getPlatforms();
    }

    public Collection<MapleNodeInfo> getNodes() {
        return nodes.getNodes();
    }

    public void setNodes(final MapleNodes mn) {
        this.nodes = mn;
    }

    public MapleNodeInfo getNode(final int index) {
        return nodes.getNode(index);
    }

    public List<Rectangle> getAreas() {
        return nodes.getAreas();
    }

    public Rectangle getArea(final int index) {
        return nodes.getArea(index);
    }

    public void changeEnvironment(final String ms, final int type) {
        broadcastMessage(MaplePacketCreator.environmentChange(ms, type));
    }

    public void toggleEnvironment(final String ms) {
        if (environment.containsKey(ms)) {
            moveEnvironment(ms, environment.get(ms) == 1 ? 2 : 1);
        } else {
            moveEnvironment(ms, 1);
        }
    }

    public void moveEnvironment(final String ms, final int type) {
        broadcastMessage(MaplePacketCreator.environmentMove(ms, type));
        environment.put(ms, type);
    }

    public Map<String, Integer> getEnvironment() {
        return environment;
    }

    public int getNumPlayersInArea(final int index) {
        int ret = 0;

        charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            while (ltr.hasNext()) {
                if (getArea(index).contains(ltr.next().getPosition())) {
                    ret++;
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return ret;
    }

    public void broadcastNONGMMessage(MapleCharacter source, byte[] packet) {
        charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isGameMaster()) {
                    chr.getClient().getSession().write(packet);
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    public void broadcastGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastGMMessage(MapleCharacter source, byte[] packet, double rangeSq, Point rangedFrom) {
        charactersLock.readLock().lock();
        try {
            if (source == null) {
                for (MapleCharacter chr : characters) {
                    if (chr.isStaff()) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            } else {
                for (MapleCharacter chr : characters) {
                    if (chr != source && (chr.getGMLevel() >= source.getGMLevel())) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            charactersLock.readLock().unlock();
        }
    }

    public List<Pair<Integer, Integer>> getMobsToSpawn() {
        return nodes.getMobsToSpawn();
    }

    public List<Integer> getSkillIds() {
        return nodes.getSkillIds();
    }

    public boolean canSpawn(final long now) {
        return lastSpawnTime > 0 && isSpawns && lastSpawnTime + createMobInterval < now;
    }

    public boolean canHurt(final long now) {
        if (lastHurtTime > 0 && lastHurtTime + decHPInterval < now) {
            lastHurtTime = now;
            return true;
        }
        return false;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }

    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnMonsterOnGroundBelow(int mobid, int x, int y) {
        MapleMonster mob = MapleLifeFactory.getMonster(mobid);
        if (mob != null) {
            Point point = new Point(x, y);
            spawnMonsterOnGroundBelow(mob, point);
        }
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public void removeProperty(String key) {
        this.properties.remove(key);
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public void killAllBoogies() {
        List<MapleMapObject> monsters = getMapObjectsInRange(
                new Point(0, 0), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.MONSTER));
        int[] boogies = new int[] {3230300, 3230301, 6130104, 9300050, 9400005, 9400006, 9400007, 9400008, 8800111};
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            for (int i = 0; i < boogies.length; i++) {
                if (monster.getId() == boogies[i]) {
                    spawnedMonstersOnMap.decrementAndGet();
                    monster.setHp(0);
                    removeMonster(monster);
                }
            }
        }
        this.broadcastMessage(MaplePacketCreator.serverNotice(
                6, "As the rock crumbled, Jr. Boogie fell in great pain and disappeared."));
    }

    public boolean hasPlayersInRectanble(Rectangle rectangle) {
        for (MapleCharacter player : this.getCharacters()) {
            if (rectangle.contains(player.getPosition())) {
                return true;
            }
        }
        return false;
    }

    public FieldScripting getField() {
        return new FieldScripting(this);
    }

    public void spawnSpecialMonsters() {
        for (Spawns spawn : this.monsterSpawn) {
            if (spawn.getMobTime() <= 0) {
                spawn.spawnMonster(this);
            }
        }
    }

    private interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    private class ActivateItemReactor implements Runnable {

        private final MapleMapItem mapitem;
        private final MapleReactor reactor;
        private final MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId(), mapitem.getType())) {
                if (mapitem.isPickedUp() || !mapitem.canLoot(c)) {
                    reactor.setTimerActive(false);
                    return;
                }
                mapitem.expire(MapleMap.this);
                if (reactor.getReactorId() == 5022000) {
                    destroyReactor(reactor.getObjectId());
                    ReactorScriptManager.getInstance().act(c, reactor);
                } else {
                    reactor.hitReactor(c);
                }
                reactor.setTimerActive(false);

                if (reactor.getDelay() > 0) {
                    MapTimer.getInstance().schedule(() -> reactor.forceHitReactor((byte) 0), reactor.getDelay());
                }
            } else {
                reactor.setTimerActive(false);
            }
        }
    }
}
