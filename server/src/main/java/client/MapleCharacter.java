package client;

import client.anticheat.CheatTracker;
import client.anticheat.ReportType;
import client.base.BaseMapleCharacter;
import client.base.MapleCharacterHelper;
import client.base.PlayerRandomStream;
import client.events.RockPaperScissors;
import client.events.SpeedQuiz;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.ItemLoader;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.keymap.MapleKeyLayout;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SavedSkillMacro;
import client.skill.SkillEntry;
import client.skill.SkillFactory;
import client.skill.SkillMacro;
import constants.FameStatus;
import constants.GameConstants;
import constants.JobConstants;
import constants.JobUtils;
import constants.MapConstants;
import constants.ServerConstants;
import constants.skills.BladeLord;
import constants.skills.Rogue;
import database.AccountData;
import database.CashShopService;
import database.CharacterData;
import database.CharacterService;
import database.DatabaseConnection;
import database.LoginService;
import database.LoginState;
import database.TeleportRockService;
import handling.cashshop.CashShop;
import handling.channel.ChannelServer;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.world.Broadcast;
import handling.world.ServerMigration;
import handling.world.WorldServer;
import handling.world.buddy.MapleBuddyList;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.helper.CharacterTransfer;
import handling.world.messenger.MapleMessenger;
import handling.world.messenger.MapleMessengerCharacter;
import handling.world.messenger.MessengerManager;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import org.jdbi.v3.core.statement.Update;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.RandomRewards;
import server.base.config.ServerConfig;
import server.base.timer.Timer.BuffTimer;
import server.base.timer.Timer.EtcTimer;
import server.base.timer.Timer.MapTimer;
import server.carnival.MapleCarnivalChallenge;
import server.carnival.MapleCarnivalParty;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.Event_PyramidSubway;
import server.maps.FieldLimitType;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import server.shops.MapleShop;
import tools.MaplePacketCreator;
import tools.collection.ConcurrentEnumMap;
import tools.collection.Pair;
import tools.collection.Triple;
import tools.helper.Api;
import tools.helper.DateHelper;
import tools.helper.Randomizer;
import tools.helper.StringUtil;
import tools.packet.CWVsContextOnMessagePackets;
import tools.packet.MTSCSPacket;
import tools.packet.MapleUserPackets;
import tools.packet.MobPacket;
import tools.packet.MonsterCarnivalPacket;
import tools.packet.PetPacket;
import tools.packet.PlayerShopPacket;
import tools.packet.UIPacket;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Slf4j
public class MapleCharacter extends BaseMapleCharacter {

    @Getter
    private AccountData accountData;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private byte world;

    private byte dojoRecord;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte guildRank = 5;
    private byte allianceRank = 5;
    private byte fairyExp = 10;
    private byte subcategory;

    @Getter
    private short availableCP;

    private short totalCP;
    private MapleJob job;

    @Getter
    @Setter
    private int remainingAp;

    @Getter
    @Setter
    private int remainingSp;

    private short fame;
    private int meso;
    private byte gender;
    private int hair;
    private int face;
    private int bookCover;
    private int dojo;
    private int guild_id = 0;
    private int maple_points;
    private int nx_credit;
    private int marriageId;

    // end db stuff
    private int fall_counter = 0;
    private int itemEffect;
    private int chair;
    private int marriageItemId = 0;
    private int coconut_team = 0;
    private int follow_id = 0;
    private int battleshipHP = 0;
    private short hpApUsed;
    private String chalkText;
    private String blessOfFairy_Origin;
    private long lastCombo;
    private long lastFameTime;
    private long keydown_skill;
    private long loginTime;
    private long lastRecoveryTime;
    private long lastDragonBloodTime;

    private boolean superMegaEnabled;
    private boolean hidden;
    private boolean hasSummon = false;

    @Getter
    @Setter
    private long lastBerserkTime;

    private long lastHPTime;
    private long lastMPTime;
    private long lastFairyTime;
    private byte mobKilledNo;
    private byte portalCount = 0;
    private byte morphId = 0;
    private short mu_lung_energy;
    private short combo;

    private List<Integer> lastMonthFameIds;
    private List<MapleDoor> doors;
    private List<MaplePet> pets;
    private Set<MapleMonster> controlled;

    private Map<Integer, String> questInfo;
    private Map<Integer, MapleSummon> summons;
    private final Map<MapleQuest, MapleQuestStatus> quests;
    private final Map<Integer, Integer> linkMobs = new LinkedHashMap<>();
    private final Map<ISkill, SkillEntry> skills = new LinkedHashMap<>();
    private final Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new ConcurrentEnumMap<>(MapleBuffStat.class);
    private final Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<>();
    private final Map<MapleDisease, MapleDiseaseValueHolder> diseases = new ConcurrentEnumMap<>(MapleDisease.class);
    private final Map<ReportType, Integer> reports = new EnumMap<>(ReportType.class);

    private final Map<Integer, Integer> movedMobs = new HashMap<>();
    private final HashMap<String, Object> temporaryData = new HashMap<>();

    private MapleBuddyList buddyList;
    private MonsterBook monsterBook;

    @Getter
    private TeleportRock vipTeleportRock;

    @Getter
    private TeleportRock regTeleportRock;

    @Getter
    private SavedLocations savedLocations;

    @Getter
    private WishList wishlist;

    @Getter
    private SavedSkillMacro skillMacros;

    @Getter
    private FinishedAchievements finishedAchievements;

    private MapleStorage storage;
    private MapleTrade trade;
    private MapleMount mount;
    private MapleMessenger messenger;
    private byte[] petStore;
    private IMaplePlayerShop playerShop;
    private MapleParty party;
    private MapleGuildCharacter mgc;
    private MapleInventory[] inventory;

    @Getter
    private MapleKeyLayout keyLayout;

    private EvanSkillPoints evanSP;

    private boolean changed_quest_info;
    private boolean changed_skills;
    private boolean changed_reports;

    private boolean follow_initiator = false;
    private boolean follow_on = false;

    private long nextConsume = 0;

    @Getter
    @Setter
    private long pqStartTime = 0;

    private AtomicInteger conversation_status;
    private CheatTracker anti_cheat;
    private PlayerRandomStream playerRandomStream;
    private CashShop cs;
    private MapleShop shop;
    private MapleDragon dragon;
    private RockPaperScissors rps;
    private SpeedQuiz sq;
    private MapleCarnivalParty carnivalParty;
    private Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    private Event_PyramidSubway pyramidSubway = null;
    private long travelTime;
    private List<Integer> pendingExpiration = null;
    private List<Integer> pendingSkills = null;

    @Getter
    @Setter
    private List<Integer> pendingUnlock = null;

    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;

    @Getter
    @Setter
    private ScheduledFuture<?> mapTimeLimitTask;

    private ScheduledFuture<?> fishing;
    private EventInstanceManager eventInstance;

    private MapleCharacter(final boolean ChannelServer) {
        setStance(0);
        setPosition(new Point(0, 0));

        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type);
        }
        quests = new LinkedHashMap<>();
        stats = new PlayerStats(this);
        if (ChannelServer) {
            changed_reports = false;
            changed_skills = false;
            changed_quest_info = false;
            lastCombo = 0;
            mu_lung_energy = 0;
            combo = 0;
            keydown_skill = 0;
            loginTime = 0;
            lastRecoveryTime = 0;
            lastDragonBloodTime = 0;
            setLastBerserkTime(0);
            lastHPTime = 0;
            lastMPTime = 0;
            lastFairyTime = 0;
            superMegaEnabled = true;
            petStore = new byte[3];
            for (int i = 0; i < petStore.length; i++) {
                petStore[i] = (byte) -1;
            }
            wishlist = new WishList();
            vipTeleportRock = new TeleportRock(true);
            regTeleportRock = new TeleportRock(false);
            savedLocations = new SavedLocations();
            skillMacros = new SavedSkillMacro();
            finishedAchievements = new FinishedAchievements();
            keyLayout = new MapleKeyLayout(id);
            conversation_status = new AtomicInteger();
            conversation_status.set(0); // 1 = NPC/ Quest, 2 = Duey, 3 = Hired Merch store, 4 =
            // Storage
            doors = new ArrayList<>();
            controlled = new LinkedHashSet<>();
            summons = new LinkedHashMap<>();
            pendingCarnivalRequests = new LinkedList<>();
            questInfo = new LinkedHashMap<>();
            anti_cheat = new CheatTracker(this);
            pets = new ArrayList<>();
        }
    }

    /**
     * Oid of players is always = the cid
     */
    @Override
    public int getObjectId() {
        return getId();
    }

    /**
     * Throws unsupported operation exception, oid of players is read only
     */
    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getName() + " at " + getPosition() + " in map: " + map.getId();
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        CharacterService.deleteWhereCharacterId(con, sql, id);
    }

    public static MapleCharacter getDefault(final MapleClient client, final int type) {
        MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.stats.setExp(0);
        ret.job = JobUtils.mapTypeToJob(type);
        ret.meso = 0;
        ret.stats.setLevel((short) 1);
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountData = client.getAccountData();
        ret.buddyList = new MapleBuddyList((byte) 20);
        ret.stats.setStr((short) 12);
        ret.stats.setDex((short) 5);
        ret.stats.setInt((short) 4);
        ret.stats.setLuk((short) 4);
        ret.stats.setMaxHp(50);
        ret.stats.setHp(50);
        ret.stats.setMaxMp(50);
        ret.stats.setMp(50);
        ret.nx_credit = client.getAccountData().getNxCredit();
        ret.maple_points = client.getAccountData().getMPoints();
        return ret;
    }

    public static class CharacterLoader {
        public static MapleCharacter loadCharFromDB(int characterId, MapleClient client, boolean isChannelServer) {

            final MapleCharacter ret = new MapleCharacter(isChannelServer);
            CharacterData characterData = LoginService.loadCharacterData(characterId);
            ret.client = client;
            ret.id = characterId;
            ret.accountData = client.getAccountData();

            ret.setName(characterData.getName());
            ret.stats.setLevel(characterData.getLevel());
            ret.fame = characterData.getFame();
            ret.stats.setExp(characterData.getExp());
            ret.setHpApUsed((short) characterData.getHpApUsed());
            ret.remainingAp = characterData.getAp();
            ret.remainingSp = characterData.getSp();
            ret.meso = characterData.getMeso();
            ret.setSkinColor(characterData.getSkinColor());
            ret.gender = (byte) characterData.getGender();
            ret.job = MapleJob.getById(characterData.getJob());
            ret.hair = characterData.getHair();
            ret.face = characterData.getFace();
            ret.map_id = characterData.getMap();
            ret.initialSpawnPoint = (byte) characterData.getSpawnPoint();
            ret.world = (byte) characterData.getWorld();
            ret.guild_id = characterData.getGuildId();
            ret.guildRank = (byte) characterData.getGuildRank();
            ret.allianceRank = (byte) characterData.getAllianceRank();
            if (ret.guild_id > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            ret.buddyList = new MapleBuddyList((byte) characterData.getBuddyCapacity());
            ret.subcategory = (byte) characterData.getSubCategory();
            ret.mount =
                    new MapleMount(ret, 0, GameConstants.getSkillByJob(1004, ret.job.getId()), (byte) 0, (byte) 1, 0);
            ret.marriageId = characterData.getMarriageId();

            ret.stats.setStr(characterData.getStr());
            ret.stats.setDex(characterData.getDex());
            ret.stats.setInt(characterData.getInt_());
            ret.stats.setLuk(characterData.getLuk());
            ret.stats.setMaxMp(characterData.getMaxMp());
            ret.stats.setMaxHp(characterData.getMaxHp());
            ret.stats.setHp(characterData.getMaxHp());
            ret.stats.setMp(characterData.getMaxMp());

            // Evan stuff
            ret.evanSP = CharacterService.loadEvanSkills(ret.id);

            if (isChannelServer) {
                MapleMapFactory mapFactory = WorldServer.getInstance()
                        .getChannel(client.getChannel())
                        .getMapFactory();
                ret.map = mapFactory.getMap(ret.map_id);
                if (ret.map == null) { // char is on a map that doesn't exist
                    // warp it to henesys
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0); // char is on a spawnpoint
                    // that doesn't exist -
                    // select the first
                    // spawnpoint instead
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());

                int partyid = characterData.getParty();
                if (partyid >= 0) {
                    MapleParty party = PartyManager.getParty(partyid);
                    if (party != null && party.getMemberById(ret.id) != null) {
                        ret.party = party;
                    }
                }
                ret.bookCover = characterData.getMonsterBookCover();
                ret.dojo = characterData.getDojo_pts();
                ret.dojoRecord = (byte) characterData.getDojoRecord();
                String petsValue = characterData.getPets();
                final String[] petsArr = petsValue.split("\\;");
                if (!petsValue.isEmpty()) {
                    for (int i = 0; i < petsArr.length; i++) {
                        String petInventoryId = petsArr[i];
                        if (petInventoryId.isEmpty()) {
                            ret.petStore[i] = -1;
                        } else {
                            ret.petStore[i] = Byte.parseByte(petsArr[i]);
                        }
                    }
                }
            }

            PreparedStatement ps = null;
            PreparedStatement pse;
            ResultSet rs = null;
            var con = DatabaseConnection.getConnection();
            try {
                if (isChannelServer) {
                    ps = con.prepareStatement("SELECT * FROM achievements WHERE accountid = ?");
                    ps.setInt(1, client.getAccountData().getId());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.finishedAchievements.addAchievementFinished(rs.getInt("achievementid"));
                    }

                    ps = con.prepareStatement("SELECT * FROM reports WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (ReportType.getById(rs.getByte("type")) != null) {
                            ret.reports.put(ReportType.getById(rs.getByte("type")), rs.getInt("count"));
                        }
                    }
                    rs.close();
                    ps.close();
                }
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, characterId);
                rs = ps.executeQuery();
                pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                while (rs.next()) {
                    final int id = rs.getInt("quest");
                    final MapleQuest q = MapleQuest.getInstance(id);
                    final MapleQuestStatus status = new MapleQuestStatus(q, rs.getByte("status"));
                    final long cTime = rs.getLong("time");
                    if (cTime > -1) {
                        status.setCompletionTime(cTime * 1000);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    status.setCustomData(rs.getString("customData"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("queststatusid"));
                    final ResultSet rsMobs = pse.executeQuery();

                    while (rsMobs.next()) {
                        status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                    }
                    rsMobs.close();
                }
                rs.close();
                ps.close();
                pse.close();

                if (isChannelServer) {
                    ret.playerRandomStream = new PlayerRandomStream();
                    ret.monsterBook = MonsterBook.loadCards(characterId);

                    ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();

                    if (!rs.next()) {
                        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit((byte) 36);
                        ret.getInventory(MapleInventoryType.USE).setSlotLimit((byte) 36);
                        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit((byte) 36);
                        ret.getInventory(MapleInventoryType.ETC).setSlotLimit((byte) 36);
                        ret.getInventory(MapleInventoryType.CASH).setSlotLimit((byte) 36);
                    } else {
                        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                        ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                        ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
                    }
                    ps.close();
                    rs.close();

                    for (Pair<IItem, MapleInventoryType> mit :
                            ItemLoader.INVENTORY.loadInventoryItems(characterId).values()) {
                        ret.getInventory(mit.getRight()).addFromDB(mit.getLeft());
                        if (mit.getLeft().getPet() != null) {
                            ret.pets.add(mit.getLeft().getPet());
                        }
                    }

                    ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    ps.setInt(1, ret.accountData.getId());
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        AccountData accountData = LoginService.loadAccountDataById(ret.accountData.getId());
                        ret.getClient().setAccountData(accountData);
                        ret.nx_credit = accountData.getNxCredit();
                        ret.maple_points = accountData.getMPoints();

                        if (rs.getTimestamp("lastlogon") != null) {
                            final Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
                            if (cal.get(Calendar.DAY_OF_WEEK) + 1
                                    == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                                ret.nx_credit += 500;
                            }
                        }
                        rs.close();
                        ps.close();

                        ps = con.prepareStatement(
                                "UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id =" + " ?");
                        ps.setInt(1, ret.accountData.getId());
                        ps.executeUpdate();
                    } else {
                        rs.close();
                    }
                    ps.close();

                    ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        ret.questInfo.put(rs.getInt("quest"), rs.getString("customData"));
                    }
                    rs.close();
                    ps.close();

                    loadAutoSkills(ret, false);

                    // All these skills are only begginer skills (mounts, etc)
                    ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills"
                            + " WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    ISkill skil;
                    while (rs.next()) {
                        skil = SkillFactory.getSkill(rs.getInt("skillid"));
                        if (ret.skills.containsKey(skil)) {
                            continue;
                        }
                        ret.skills.put(
                                skil,
                                new SkillEntry(
                                        rs.getByte("skilllevel"), rs.getByte("masterlevel"), rs.getLong("expiration")));

                        if (ServerConfig.isSkillSavingEnabled()) {
                            log.info("Loading skill: " + skil.getName() + " Level: " + rs.getByte("skilllevel"));
                        }
                    }
                    rs.close();
                    ps.close();

                    ret.expirationTask(false, true);

                    // Bless of Fairy handling
                    ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
                    ps.setInt(1, ret.accountData.getId());
                    rs = ps.executeQuery();
                    byte maxlevel_ = 0;
                    while (rs.next()) {
                        if (rs.getInt("id") != characterId) { // Not this character
                            byte maxlevel = (byte) (rs.getShort("level") / 10);

                            if (maxlevel > 20) {
                                maxlevel = 20;
                            }
                            if (maxlevel > maxlevel_) {
                                maxlevel_ = maxlevel;
                                ret.blessOfFairy_Origin = rs.getString("name");
                            }
                        }
                    }
                    ret.skills.put(
                            SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job.getId())),
                            new SkillEntry(maxlevel_, (byte) 0, -1));
                    ps.close();
                    rs.close();
                    // END

                    ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    int position;
                    while (rs.next()) {
                        position = rs.getInt("position");
                        SkillMacro macro = new SkillMacro(
                                rs.getInt("skill1"),
                                rs.getInt("skill2"),
                                rs.getInt("skill3"),
                                rs.getString("name"),
                                rs.getInt("shout"),
                                position);
                        ret.skillMacros.add(macro);
                    }
                    rs.close();
                    ps.close();

                    ret.keyLayout = new MapleKeyLayout(ret.id);
                    ret.keyLayout.loadKeybindings();

                    ps = con.prepareStatement(
                            "SELECT `locationtype`,`map` FROM savedlocations WHERE characterid" + " = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        var locationType = SavedLocationType.fromCode(rs.getInt("locationtype"));
                        ret.savedLocations.saveLocation(locationType, rs.getInt("map"));
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ?"
                            + " AND DATEDIFF(NOW(),`when`) < 30");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    ret.lastFameTime = 0;
                    ret.lastMonthFameIds = new ArrayList<>(31);
                    while (rs.next()) {
                        ret.lastFameTime = Math.max(
                                ret.lastFameTime, rs.getTimestamp("when").getTime());
                        ret.lastMonthFameIds.add(Integer.valueOf(rs.getInt("characterid_to")));
                    }
                    rs.close();
                    ps.close();

                    ret.buddyList.loadFromDb(characterId);
                    ret.storage = MapleStorage.loadStorage(ret.accountData.getId());
                    ret.cs = new CashShop(ret.accountData.getId(), characterId, ret.getJob());

                    ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.wishlist.setItem(rs.getInt("sn"));
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        ret.vipTeleportRock.addMap(rs.getInt("mapid"));
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        ret.regTeleportRock.addMap(rs.getInt("mapid"));
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                    ps.setInt(1, characterId);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException("No mount data found on SQL column");
                    }
                    final IItem mount =
                            ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
                    ret.mount = new MapleMount(
                            ret,
                            mount != null ? mount.getItemId() : 0,
                            GameConstants.getSkillByJob(1004, ret.job.getId()),
                            rs.getByte("Fatigue"),
                            rs.getByte("Level"),
                            rs.getInt("Exp"));
                    ps.close();
                    rs.close();

                    ret.stats.recalcLocalStats(true);
                } else { // Not channel server
                    for (Pair<IItem, MapleInventoryType> mit :
                            ItemLoader.INVENTORY.loadInventoryItems(characterId).values()) {
                        ret.getInventory(mit.getRight()).addFromDB(mit.getLeft());
                    }
                }
            } catch (SQLException ess) {
                ess.printStackTrace();
                log.info("Failed to load character..");
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    con.close();
                } catch (SQLException ex) {
                    log.error("Loading character sql exception", ex);
                }
            }
            return ret;
        }

        public static final MapleCharacter reconstructChr(
                final CharacterTransfer ct, final MapleClient client, final boolean isChannel) {
            final MapleCharacter ret = new MapleCharacter(true);
            ret.client = client;
            if (!isChannel) {
                ret.client.setChannel(ct.getChannel());
            }
            ret.id = ct.getCharacterId();
            ret.setName(ct.getName());
            ret.stats.setLevel(ct.getLevel());
            ret.fame = ct.getFame();

            ret.playerRandomStream = new PlayerRandomStream();

            ret.chalkText = ct.getChalkboard();
            ret.stats.setExp(ct.getExp());
            ret.setHpApUsed(ct.getHpApUsed());
            ret.remainingAp = ct.getRemainingAp();
            ret.remainingSp = ct.getRemainingSp();
            ret.meso = ct.getMeso();
            ret.setSkinColor(ct.getSkinColor());
            ret.gender = ct.getGender();
            ret.job = MapleJob.getById(ct.getJob());
            ret.hair = ct.getHair();
            ret.setFace(ct.getFace());
            ret.accountData = ct.getAccountData();
            ret.map_id = ct.getMap_id();
            ret.initialSpawnPoint = ct.getInitialSpawnPoint();
            ret.world = ct.getWorld();
            ret.bookCover = ct.getMBookCover();
            ret.dojo = ct.getDojo();
            ret.dojoRecord = ct.getDojoRecord();
            ret.guild_id = ct.getGuild_id();
            ret.guildRank = ct.getGuildRank();
            ret.allianceRank = ct.getAllianceRank();
            ret.fairyExp = ct.getFairyExp();
            ret.marriageId = ct.getMarriageId();
            ret.evanSP = ct.getEvanSP();
            ret.stats.setStr(ct.getStr());
            ret.stats.setDex(ct.getDex());
            ret.stats.setInt(ct.getInt_());
            ret.stats.setLuk(ct.getLuk());
            ret.stats.setMaxHp(ct.getMaxHp());
            ret.stats.setMaxMp(ct.getMaxMp());
            ret.stats.setHp(ct.getHp());
            ret.stats.setMp(ct.getMp());

            if (ret.guild_id > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            ret.buddyList = new MapleBuddyList(ct.getBuddySize());
            ret.subcategory = ct.getSubcategory();

            if (isChannel) {
                final MapleMapFactory mapFactory = WorldServer.getInstance()
                        .getChannel(client.getChannel())
                        .getMapFactory();
                ret.map = mapFactory.getMap(ret.map_id);
                if (ret.map == null) { // char is on a map that doesn't exist warp
                    // it to henesys
                    ret.map = mapFactory.getMap(100000000);
                } else {
                    if (ret.map.getForcedReturnId() != 999999999) {
                        ret.map = ret.map.getForcedReturnMap();
                    }
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());

                final int messengerId = ct.getMessenger_id();
                if (messengerId > 0) {
                    ret.messenger = MessengerManager.getMessenger(messengerId);
                }
            } else {
                ret.messenger = null;
            }
            int partyId = ct.getParty_id();
            if (partyId >= 0) {
                MapleParty party = PartyManager.getParty(partyId);
                if (party != null && party.getMemberById(ret.id) != null) {
                    ret.party = party;
                }
            }

            MapleQuestStatus queststatus;
            MapleQuestStatus queststatus_from;
            MapleQuest quest;
            for (final Map.Entry<Integer, Object> qs : ct.getQuest().entrySet()) {
                quest = MapleQuest.getInstance(qs.getKey());
                queststatus_from = (MapleQuestStatus) qs.getValue();

                queststatus = new MapleQuestStatus(quest, queststatus_from.getStatus());
                queststatus.setForfeited(queststatus_from.getForfeited());
                queststatus.setCustomData(queststatus_from.getCustomData());
                queststatus.setCompletionTime(queststatus_from.getCompletionTime());

                if (queststatus_from.getMobKills() != null) {
                    for (final Map.Entry<Integer, Integer> mobkills :
                            queststatus_from.getMobKills().entrySet()) {
                        queststatus.setMobKills(mobkills.getKey(), mobkills.getValue());
                    }
                }
                ret.quests.put(quest, queststatus);
            }

            for (final Map.Entry<Integer, SkillEntry> qs : ct.getSkills().entrySet()) {
                ret.skills.put(SkillFactory.getSkill(qs.getKey()), qs.getValue());
            }

            ret.finishedAchievements = ct.getFinishedAchievements();

            for (final Map.Entry<Byte, Integer> qs : ct.getReports().entrySet()) {
                ret.reports.put(ReportType.getById(qs.getKey()), qs.getValue());
            }
            ret.monsterBook = new MonsterBook(ct.getMapleBookCards());
            ret.inventory = ct.getInventories();
            ret.blessOfFairy_Origin = ct.getBlessOfFairy();
            ret.skillMacros = ct.getSkillMacros();
            ret.keyLayout = ct.getKeyMap();
            ret.petStore = ct.getPetStore();
            ret.questInfo = ct.getInfoQuest();
            ret.savedLocations = ct.getSavedLocations();
            ret.wishlist = ct.getWishlist();
            ret.vipTeleportRock.initMaps(ct.getVipTeleportRocks());
            ret.regTeleportRock.initMaps(ct.getRegularTeleportRocks());
            ret.buddyList.loadFromTransfer(ct.getBuddies());
            ret.keydown_skill = 0; // Keydown skill can't be brought over
            ret.lastFameTime = ct.getLastFameTime();
            ret.loginTime = ct.getLoginTime();
            ret.lastRecoveryTime = ct.getLastRecoveryTime();
            ret.lastDragonBloodTime = ct.getLastDragonBloodTime();
            ret.setLastBerserkTime(ct.getLastBerserkTime());
            ret.lastHPTime = ct.getLastHPTime();
            ret.lastMPTime = ct.getLastMPTime();
            ret.lastFairyTime = ct.getLastFairyTime();
            ret.lastMonthFameIds = ct.getFamedCharacters();
            ret.morphId = ct.getMorphId();
            ret.storage = ct.getStorage();
            ret.cs = ct.getCashInventory();

            ret.nx_credit = ct.getNxCredit();
            ret.maple_points = ct.getMaplePoints();
            ret.mount = new MapleMount(
                    ret,
                    ct.getMount_item_id(),
                    GameConstants.getSkillByJob(1004, ret.job.getId()),
                    ct.getMount_Fatigue(),
                    ct.getMount_level(),
                    ct.getMount_exp());
            ret.expirationTask(false, false);
            ret.stats.recalcLocalStats(true);

            return ret;
        }
    }

    private static void loadAutoSkills(final MapleCharacter ret, boolean autoSkill) {
        if (autoSkill) {
            ret.skills.putAll(JobConstants.getSkillsFromJob(ret.getJob()));
        }
        if (ret.getJob().isEvan() && autoSkill) {
            ret.skills.putAll(JobConstants.getEvanSkills());
        }

        if (ret.getGMLevel() > 0) {
            ret.skills.putAll(JobConstants.getGMSkills());
        }
    }

    public static void saveNewCharToDB(final MapleCharacter chr, final int type, final boolean db) {
        try (var handle = DatabaseConnection.getConnector().open()) {
            String insertCharacterQuery = "INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp,"
                    + " mp, maxhp, maxmp, ap, skincolor, gender, job, hair, face,"
                    + " map, meso, hpApUsed, spawnpoint, party, buddyCapacity,"
                    + " monsterbookcover, dojo_pts, dojoRecord, pets, subcategory,"
                    + " marriageId, accountid, name, world) VALUES (:level, :fame, :str, :dex, :luk, :int,"
                    + " :exp, :hp, :mp, :maxhp, :maxmp, :ap, :skincolor, :gender, :job, :hair, :face, :map, :meso, :hpApUsed,"
                    + " :spawnpoint, :party, :buddyCapacity, :monsterbookcover, :dojo_pts, :dojoRecord, :pets, :subcategory,"
                    + " :marriageId, :accountid, :name, :world)";

            handle.inTransaction(h -> {
                final PlayerStats stat = chr.stats;
                Update update = h.createUpdate(insertCharacterQuery);
                Integer generatedCharacterId = update.bind("level", stat.getLevel())
                        .bind("fame", 0)
                        .bind("str", stat.getStr())
                        .bind("dex", stat.getDex())
                        .bind("luk", stat.getLuk())
                        .bind("int", stat.getInt())
                        .bind("exp", stat.getExp())
                        .bind("hp", stat.getHp())
                        .bind("mp", stat.getMp())
                        .bind("maxhp", stat.getMaxHp())
                        .bind("maxmp", stat.getMaxMp())
                        .bind("ap", chr.getRemainingAp())
                        .bind("skincolor", chr.getSkinColor())
                        .bind("gender", chr.getGender())
                        .bind("job", chr.getJob().getId())
                        .bind("hair", chr.getHair())
                        .bind("face", chr.getFace())
                        .bind("map", type == 1 ? 0 : (type == 0 ? 130030000 : (type == 3 ? 900090000 : 914000000)))
                        .bind("meso", chr.getMeso())
                        .bind("hpApUsed", chr.getHpApUsed())
                        .bind("spawnpoint", 0)
                        .bind("party", -1)
                        .bind("buddyCapacity", chr.buddyList.getCapacity())
                        .bind("monsterbookcover", 0)
                        .bind("dojo_pts", 0)
                        .bind("dojoRecord", 0)
                        .bind("pets", "-1;-1;-1")
                        .bind("subcategory", db ? 1 : 0)
                        .bind("marriageId", 0)
                        .bind("accountid", chr.getAccountID())
                        .bind("name", chr.getName())
                        .bind("sp", chr.getRemainingSp())
                        .bind("world", chr.getWorld())
                        .executeAndReturnGeneratedKeys(insertCharacterQuery)
                        .mapTo(Integer.class)
                        .one();

                chr.id = generatedCharacterId;
                return true;
            });
        } catch (Exception ex) {
            log.error("Error saving character", ex);
        }

        LoginService.updateQuestStatus(chr.getId(), chr.getQuests());
        LoginService.saveDefaultInventorySlot(chr.getId());
        LoginService.saveDefaultMountData(chr.getId());
        var keyLayout = new MapleKeyLayout(chr.id);
        keyLayout.setDefaultKeys();
        keyLayout.saveKeys();
        chr.saveInventory();
    }

    public void saveToDB(boolean dc, boolean fromcs) {
        String petPosition = "";
        for (MaplePet pet : pets) {
            petPosition += pet.getInventoryPosition() + ";";
        }

        try {

            try (var handle = DatabaseConnection.getConnector().open()) {
                String saveCharacterQuery =
                        "UPDATE characters SET level = :level, fame = :fame, str = :str, dex = :dex, luk = :luk, "
                                + "`int` = :int, exp = :exp, hp = :hp, mp = :mp, maxhp = :maxhp, maxmp = :maxmp, ap = :ap, "
                                + "skincolor = :skincolor, gender = :gender, job = :job, hair = :hair, face = :face, "
                                + "map = :map, meso = :meso, hpApUsed = :hpApUsed, spawnpoint = :spawnpoint, party = :party, "
                                + "buddyCapacity = :buddyCapacity, monsterbookcover = :monsterbookcover, dojo_pts = :dojo_pts, "
                                + "dojoRecord = :dojoRecord, pets = :pets, subcategory = :subcategory, marriageId = :marriageId, name = :name, "
                                + "sp = :sp WHERE id = :id";

                String finalPetPosition = petPosition;
                handle.inTransaction(h -> {
                    Update update = h.createUpdate(saveCharacterQuery);
                    int returnMapId = getReturnMapId(fromcs);
                    int nearestSpawnPoint = getNearestSpawnPoint();
                    return update.bind("level", stats.getLevel())
                            .bind("fame", fame)
                            .bind("str", stats.getStr())
                            .bind("dex", stats.getDex())
                            .bind("luk", stats.getLuk())
                            .bind("int", stats.getInt())
                            .bind("exp", stats.getExp())
                            .bind("hp", stats.getHp())
                            .bind("mp", stats.getMp())
                            .bind("maxhp", stats.getMaxHp())
                            .bind("maxmp", stats.getMaxMp())
                            .bind("ap", remainingAp)
                            .bind("skincolor", getSkinColor())
                            .bind("gender", gender)
                            .bind("job", job.getId())
                            .bind("hair", hair)
                            .bind("face", getFace())
                            .bind("map", returnMapId)
                            .bind("meso", meso)
                            .bind("hpApUsed", getHpApUsed())
                            .bind("spawnpoint", nearestSpawnPoint)
                            .bind("party", party != null ? party.getId() : -1)
                            .bind("buddyCapacity", buddyList.getCapacity())
                            .bind("monsterbookcover", bookCover)
                            .bind("dojo_pts", dojo)
                            .bind("dojoRecord", dojoRecord)
                            .bind("pets", finalPetPosition)
                            .bind("subcategory", subcategory)
                            .bind("marriageId", marriageId)
                            .bind("name", getName())
                            .bind("sp", remainingSp)
                            .bind("id", id)
                            .execute();
                });
            } catch (Exception ex) {
                log.error("Error saving character", ex);
            }

            CharacterService.saveSkillMacro(skillMacros, id);

            LoginService.saveInventorySlot(id, inventory);

            saveInventory();

            if (changed_quest_info) {
                LoginService.updateQuestInfo(id, questInfo);
            }

            LoginService.updateQuestStatus(id, quests);

            if (changed_skills) {
                LoginService.updateSkills(id, skills);
            }
            if (job.isEvan()) {
                LoginService.saveEvanSkills(getId(), this.evanSP);
            }
            LoginService.saveSkillCoolDowns(getId(), getCooldowns());

            CharacterService.saveLocation(savedLocations, id);
            CharacterService.saveAchievement(finishedAchievements, this.accountData.getId(), this.id);

            if (changed_reports) {
                LoginService.saveReports(id, reports);
            }

            LoginService.saveBuddyEntries(id, buddyList.getBuddies());

            LoginService.updateAccountCash(client.getAccountData().getId(), nx_credit, maple_points);

            if (storage != null) {
                storage.saveToDB();
            }
            if (cs != null) {
                cs.save();
            }
            keyLayout.saveKeys();
            mount.saveMount(id);
            monsterBook.saveCards(id);

            CashShopService.saveWishList(wishlist, id);
            TeleportRockService.save(vipTeleportRock, id);
            TeleportRockService.save(regTeleportRock, id);

            changed_quest_info = false;
            changed_skills = false;
            changed_reports = false;
        } catch (Exception e) {
            log.error("Error saving character", e);
        }
    }

    public void saveInventory() {
        List<Pair<IItem, MapleInventoryType>> listing = new ArrayList<>();
        for (final MapleInventory iv : inventory) {
            for (final IItem item : iv.list()) {
                listing.add(new Pair<>(item, iv.getType()));
            }
        }
        ItemLoader.INVENTORY.saveItems(listing, id);
    }

    public final PlayerStats getStat() {
        return stats;
    }

    public final PlayerRandomStream CRand() {
        return playerRandomStream;
    }

    public final void QuestInfoPacket(final OutPacket packet) {
        packet.writeShort(questInfo.size());

        for (final Entry<Integer, String> q : questInfo.entrySet()) {
            packet.writeShort(q.getKey());
            packet.writeMapleAsciiString(q.getValue() == null ? "" : q.getValue());
        }
    }

    public final void updateInfoQuest(final int questid, final String data) {
        questInfo.put(questid, data);
        changed_quest_info = true;
        client.getSession().write(MaplePacketCreator.updateInfoQuest(questid, data));
    }

    public final String getInfoQuest(final int questid) {
        if (questInfo.containsKey(questid)) {
            return questInfo.get(questid);
        }
        return "";
    }

    public final int getNumQuest() {
        int i = 0;
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2) {
                i++;
            }
        }
        return i;
    }

    public final byte getQuestStatus(final int quest) {
        return getQuest(MapleQuest.getInstance(quest)).getStatus();
    }

    public final MapleQuestStatus getQuest(final MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            MapleQuestStatus newQuest = new MapleQuestStatus(quest, (byte) 0);
            quests.put(quest, newQuest);
            return newQuest;
        }
        return quests.get(quest);
    }

    public final void completeQuest(int id, int npc) {
        MapleQuest.getInstance(id).complete(this, npc);
    }

    public final void setQuestAdd(final MapleQuest quest, final int status, final String customData) {
        if (!quests.containsKey(quest)) {
            final MapleQuestStatus stat = new MapleQuestStatus(quest, (byte) status);
            stat.setCustomData(customData);
            quests.put(quest, stat);
        }
    }

    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            final MapleQuestStatus status = new MapleQuestStatus(quest, (byte) 0);
            quests.put(quest, status);
            return status;
        }
        return quests.get(quest);
    }

    public final MapleQuestStatus getQuestNoAdd(final MapleQuest quest) {
        return quests.get(quest);
    }

    public final void updateQuest(final MapleQuestStatus quest) {
        updateQuest(quest, false);
    }

    public final void updateQuest(final MapleQuestStatus quest, final boolean update) {
        quests.put(quest.getQuest(), quest);
        client.getSession().write(CWVsContextOnMessagePackets.onQuestRecordMessage(quest));
        if (quest.getStatus() == 1 && !update) {
            client.getSession()
                    .write(MaplePacketCreator.updateQuestInfo(
                            this, quest.getQuest().getId(), quest.getNpc(), (byte) 8));
        }
    }

    public final Map<Integer, String> getInfoQuest_Map() {
        return questInfo;
    }

    public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
        return quests;
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.getEffect().isSkill() && mbsvh.getEffect().getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        if (effect == MapleBuffStat.MORPH && morphId > 0) {
            return (int) morphId;
        }
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        return mbsvh == null ? null : Integer.valueOf(mbsvh.getValue());
    }

    public final Integer getBuffedSkill_X(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.getEffect().getX();
    }

    public final Integer getBuffedSkill_Y(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.getEffect().getY();
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.getEffect().isSkill() && mbsvh.getEffect().getSourceId() == skill.getId();
    }

    public int getBuffSource(MapleBuffStat stat) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(stat);
        return mbsvh == null ? -1 : mbsvh.getEffect().getSourceId();
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.setValue(value);
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        return mbsvh == null ? null : Long.valueOf(mbsvh.getStartTime());
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = getEffects().get(effect);
        return mbsvh == null ? null : mbsvh.getEffect();
    }

    public void startMapTimeLimitTask(int time, final MapleMap to) {
        client.getSession().write(MaplePacketCreator.getClock(time));

        time *= 1000;
        setMapTimeLimitTask(MapTimer.getInstance().register(() -> changeMap(to, to.getPortal(0)), time, time));
    }

    public void startFishingTask(final boolean VIP) {
        final int time = GameConstants.getFishingTime(VIP, isGameMaster());
        cancelFishingTask();

        fishing = EtcTimer.getInstance()
                .register(
                        () -> {
                            final boolean expMulti = haveItem(2300001, 1, false, true);
                            if (!expMulti && !haveItem(2300000, 1, false, true)) {
                                cancelFishingTask();
                                return;
                            }
                            MapleInventoryManipulator.removeById(
                                    client, MapleInventoryType.USE, expMulti ? 2300001 : 2300000, 1, false, false);

                            final int randval = RandomRewards.getInstance().getFishingReward();

                            switch (randval) {
                                case 0: // Meso
                                    final int money = Randomizer.rand(expMulti ? 15 : 10, expMulti ? 75000 : 50000);
                                    gainMeso(money, true);
                                    // client.getSession().write(UIPacket.fishingUpdate((byte)
                                    // 1, money));
                                    break;
                                case 1: // EXP
                                    final int experi = Randomizer.nextInt(
                                            Math.abs(GameConstants.getExpNeededForLevel(stats.getLevel()) / 200) + 1);
                                    gainExp(expMulti ? (experi * 3 / 2) : experi, true, false, true);
                                    break;
                                default:
                                    MapleInventoryManipulator.addById(client, randval, (short) 1, "");
                                    break;
                            }
                        },
                        time,
                        time);
    }

    public void cancelMapTimeLimitTask() {
        if (getMapTimeLimitTask() != null) {
            getMapTimeLimitTask().cancel(false);
        }
    }

    public void cancelFishingTask() {
        if (fishing != null) {
            fishing.cancel(false);
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        registerEffect(effect, starttime, schedule, effect.getStatups());
    }

    public void registerEffect(
            MapleStatEffect effect,
            long starttime,
            ScheduledFuture<?> schedule,
            List<Pair<MapleBuffStat, Integer>> statups) {
        if (effect.isHide()) {
            if (isGameMaster()) {
                this.hidden = true;
                client.getSession().write(MaplePacketCreator.GameMaster_Func(0x12, 1));
                map.broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()));
            } else {
                final String file = getName();
                log.info(file + " : " + "TRIED TO USE GM HIDE");
            }
        } else if (effect.isDragonBlood()) {
            prepareDragonBlood();
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isMonsterRiding_()) {
            getMount().startSchedule();
        } else if (effect.isRecovery()) {
            prepareRecovery();
        } else if (effect.isBeholder()) {
            prepareBeholderEffect();
        }
        int clonez = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft() == MapleBuffStat.ILLUSION) {
                clonez = statup.getRight();
            }
            int value = statup.getRight().intValue();
            if (statup.getLeft() == MapleBuffStat.MONSTER_RIDING && effect.getSourceId() == 5221006) {
                if (battleshipHP <= 0) { // quick hack
                    battleshipHP = value; // copy this as well
                }
            }
            getEffects().put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, value));
        }

        stats.recalcLocalStats();
    }

    public List<MapleBuffStat> getBuffStats(final MapleStatEffect effect, final long startTime) {
        final List<MapleBuffStat> bstats = new ArrayList<>();
        final Map<MapleBuffStat, MapleBuffStatValueHolder> allBuffs = new EnumMap<>(getEffects());
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : allBuffs.entrySet()) {
            final MapleBuffStatValueHolder mbsvh = stateffect.getValue();
            if (mbsvh.getEffect().sameSource(effect) && (startTime == -1 || startTime == mbsvh.getStartTime())) {
                bstats.add(stateffect.getKey());
            }
        }
        return bstats;
    }

    private boolean deregisterBuffStats(List<MapleBuffStat> stats) {
        boolean clonez = false;
        List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<>(stats.size());
        for (MapleBuffStat stat : stats) {
            final MapleBuffStatValueHolder mbsvh = getEffects().remove(stat);
            if (mbsvh != null) {
                boolean addMbsvh = true;
                for (MapleBuffStatValueHolder contained : effectsToCancel) {
                    if (mbsvh.getStartTime() == contained.getStartTime()
                            && contained.getEffect() == mbsvh.getEffect()) {
                        addMbsvh = false;
                        break;
                    }
                }
                if (addMbsvh) {
                    effectsToCancel.add(mbsvh);
                }
                if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET || stat == MapleBuffStat.REAPER) {
                    final int summonId = mbsvh.getEffect().getSourceId();
                    final MapleSummon summon = summons.get(summonId);
                    if (summon != null) {
                        map.broadcastMessage(MaplePacketCreator.removeSummon(summon, true));
                        map.removeMapObject(summon);
                        removeVisibleMapObject(summon);
                        summons.remove(summonId);
                        if (summon.getSkill() == 1321007) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    }
                } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                    lastDragonBloodTime = 0;
                } else if (stat == MapleBuffStat.RECOVERY || mbsvh.getEffect().getSourceId() == 35121005) {
                    lastRecoveryTime = 0;
                } else if (stat == MapleBuffStat.HOMING_BEACON) {
                    linkMobs.clear();
                } else if (stat == MapleBuffStat.ILLUSION) {
                    clonez = true;
                } else if (stat == MapleBuffStat.MORPH) {
                    if (morphId > 0) {
                        this.morphId = 0;
                    }
                }
            }
        }
        for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
            if (getBuffStats(cancelEffectCancelTasks.getEffect(), cancelEffectCancelTasks.getStartTime())
                    .size()
                    == 0) {
                if (cancelEffectCancelTasks.getSchedule() != null) {
                    cancelEffectCancelTasks.getSchedule().cancel(false);
                }
            }
        }
        return clonez;
    }

    /**
     * @param effect
     * @param overwrite when overwrite is set no data is sent and all the Buffstats in the
     *                  StatEffect are deregistered
     * @param startTime
     */
    public void cancelEffect(final MapleStatEffect effect, final boolean overwrite, final long startTime) {
        cancelEffect(effect, overwrite, startTime, effect.getStatups());
    }

    public void cancelEffect(
            final MapleStatEffect effect,
            final boolean overwrite,
            final long startTime,
            List<Pair<MapleBuffStat, Integer>> statups) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            buffstats = new ArrayList<>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        if (buffstats.size() <= 0) {
            return;
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                removeDoor();
                silentPartyUpdate();
            }
        } else if (effect.isMonsterRiding_()) {
            getMount().cancelSchedule();
        } else if (effect.isAranCombo()) {
            combo = 0;
        }
        // check if we are still logged in o.o
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
            if (client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null) {
                this.hidden = false;
                client.getSession().write(MaplePacketCreator.GameMaster_Func(0x12, 0));
                map.broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);

                for (final MaplePet pet : pets) {
                    if (pet.getSummoned()) {
                        map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
                    }
                }
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat... stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        if (getEffects().get(stat) != null) {
            cancelEffect(getEffects().get(stat).getEffect(), false, -1);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        boolean write = client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null;
        if (buffstats.contains(MapleBuffStat.MONSTER_RIDING) && job.isEvan() && job.getId() >= 2200) {
            makeDragon();
            map.spawnDragon(dragon);
            map.updateMapObjectVisibility(this, dragon);
        }
        if (buffstats.contains(MapleBuffStat.HOMING_BEACON)) {
            if (write) {
                client.getSession().write(MaplePacketCreator.cancelHoming());
            }
        } else {
            if (write) {
                stats.recalcLocalStats();
            }
            client.getSession().write(MaplePacketCreator.cancelBuff(buffstats));
            map.broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public void dispel() {
        if (!isHidden()) {
            final LinkedList<MapleBuffStatValueHolder> allBuffs =
                    new LinkedList<>(getEffects().values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.getEffect().isSkill()
                        && mbsvh.getSchedule() != null
                        && !mbsvh.getEffect().isMorph()) {
                    cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
                }
            }
        }
    }

    public void dispelSkill(int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.getEffect().isSkill()
                        && (mbsvh.getEffect().getSourceId() == 4331003
                        || mbsvh.getEffect().getSourceId() == 4331002
                        || mbsvh.getEffect().getSourceId() == 4341002
                        || mbsvh.getEffect().getSourceId() == 22131001
                        || mbsvh.getEffect().getSourceId() == 1321007
                        || mbsvh.getEffect().getSourceId() == 2121005
                        || mbsvh.getEffect().getSourceId() == 2221005
                        || mbsvh.getEffect().getSourceId() == 2311006
                        || mbsvh.getEffect().getSourceId() == 2321003
                        || mbsvh.getEffect().getSourceId() == 3111002
                        || mbsvh.getEffect().getSourceId() == 3111005
                        || mbsvh.getEffect().getSourceId() == 3211002
                        || mbsvh.getEffect().getSourceId() == 3211005
                        || mbsvh.getEffect().getSourceId() == 4111002)) {
                    cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
                    break;
                }
            } else {
                if (mbsvh.getEffect().isSkill() && mbsvh.getEffect().getSourceId() == skillid) {
                    cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
                    break;
                }
            }
        }
    }

    public void dispelBuff(int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.getEffect().getSourceId() == skillid) {
                cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
                break;
            }
        }
    }

    public void cancelAllBuffs_() {
        getEffects().clear();
    }

    public void cancelAllBuffs() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
        }
    }

    public void cancelMorphs() {
        cancelMorphs(false);
    }

    public void cancelMorphs(boolean force) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());

        boolean questBuff = false;
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.getEffect().getSourceId()) {
                case 5111005:
                case 5121003:
                case 15111002:
                case 13111005:
                    return; // Since we can't have more than 1, save up on loops
                case 2210062:
                case 2210063:
                case 2210064:
                case 2210065:
                    questBuff = true;
                    // fall through
                default:
                    if (mbsvh.getEffect().isMorph()) {
                        if (questBuff && MapConstants.isStorylineMap(getMapId()) && !force) {
                            return;
                        }
                        if (questBuff) {
                            for (int i = 1066; i <= 1067; i++) {
                                final ISkill skill =
                                        SkillFactory.getSkill(GameConstants.getSkillByJob(i, getJob().getId()));
                                changeSkillLevel_Skip(skill, (byte) -1, (byte) 0);
                            }
                        }
                        cancelEffect(mbsvh.getEffect(), false, mbsvh.getStartTime());
                        return;
                    }
            }
        }
    }

    public int getMorphState() {
        LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.getEffect().isMorph()) {
                return mbsvh.getEffect().getSourceId();
            }
        }
        return -1;
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<>();
        LinkedList<MapleBuffStatValueHolder> allBuffs =
                new LinkedList<>(getEffects().values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            ret.add(new PlayerBuffValueHolder(mbsvh.getStartTime(), mbsvh.getEffect()));
        }
        return ret;
    }

    public int getSkillLevel(int skillid) {
        return getSkillLevel(SkillFactory.getSkill(skillid));
    }

    public final void handleEnergyCharge(final int skillid, final int targets) {
        final ISkill echskill = SkillFactory.getSkill(skillid);
        final byte skilllevel = getSkillLevel(echskill);
        if (skilllevel > 0) {
            final MapleStatEffect echeff = echskill.getEffect(skilllevel);
            if (targets > 0) {
                if (getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) {
                    echeff.applyEnergyBuff(this, true); // Infinity time
                } else {
                    Integer energyLevel = getBuffedValue(MapleBuffStat.ENERGY_CHARGE);
                    // TODO: bar going down
                    if (energyLevel < 10000) {
                        energyLevel += (echeff.getX() * targets);

                        client.getSession().write(MaplePacketCreator.showOwnBuffEffect(skillid, 2));
                        map.broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, skillid, 2), false);

                        if (energyLevel >= 10000) {
                            energyLevel = 10000;
                        }
                        client.getSession()
                                .write(MaplePacketCreator.giveEnergyChargeTest(
                                        energyLevel, echeff.getDuration() / 1000));
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energyLevel);
                    } else if (energyLevel == 10000) {
                        echeff.applyEnergyBuff(this, false); // One with time
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(10001));
                    }
                }
            }
        }
    }

    public final void handleBattleshipHP(int damage) {
        if (isActiveBuffedValue(5221006)) {
            battleshipHP -= damage;
            if (battleshipHP <= 0) {
                battleshipHP = 0;
                final MapleStatEffect effect = getStatForBuff(MapleBuffStat.MONSTER_RIDING);
                client.getSession().write(MaplePacketCreator.skillCooldown(5221006, effect.getCooldown()));
                addCooldown(5221006, System.currentTimeMillis(), effect.getCooldown() * 1000L);
                dispelSkill(5221006);
            }
        }
    }

    public final void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO);
        ISkill combo;
        ISkill advcombo;

        switch (getJob().getId()) {
            case 1110:
            case 1111:
            case 1112:
                combo = SkillFactory.getSkill(11111001);
                advcombo = SkillFactory.getSkill(11110005);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
                advcombo = SkillFactory.getSkill(1120003);
                break;
        }

        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getSkillLevel(advcombo);
        if (advComboSkillLevel > 0) {
            ceffect = advcombo.getEffect(advComboSkillLevel);
        } else if (getSkillLevel(combo) > 0) {
            ceffect = combo.getEffect(getSkillLevel(combo));
        } else {
            return;
        }

        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                if (neworbcount < ceffect.getX() + 1) {
                    neworbcount++;
                }
            }
            List<Pair<MapleBuffStat, Integer>> stat =
                    Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

            client.getSession().write(MaplePacketCreator.giveBuff(combo.getId(), duration, stat, ceffect));
            map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
        }
    }

    public void handleOrbconsume() {
        ISkill combo;

        switch (getJob().getId()) {
            case 1110:
            case 1111:
                combo = SkillFactory.getSkill(11111001);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
                break;
        }
        if (getSkillLevel(combo) <= 0) {
            return;
        }
        MapleStatEffect ceffect = getStatForBuff(MapleBuffStat.COMBO);
        if (ceffect == null) {
            return;
        }
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        int duration = ceffect.getDuration();
        duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

        client.getSession().write(MaplePacketCreator.giveBuff(combo.getId(), duration, stat, ceffect));
        map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
    }

    public void silentEnforceMaxHpMp() {
        stats.setMp(stats.getMp());
        stats.setHp(stats.getHp(), true);
    }

    public void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> statups = new ArrayList<>(2);
        if (stats.getMp() > stats.getCurrentMaxMp()) {
            stats.setMp(stats.getMp());
            statups.add(new Pair<>(MapleStat.MP, Integer.valueOf(stats.getMp())));
        }
        if (stats.getHp() > stats.getCurrentMaxHp()) {
            stats.setHp(stats.getHp());
            statups.add(new Pair<>(MapleStat.HP, Integer.valueOf(stats.getHp())));
        }
        if (statups.size() > 0) {
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, getJob().getId()));
        }
    }

    public byte getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public final String getBlessOfFairyOrigin() {
        return this.blessOfFairy_Origin;
    }

    public final short getLevel() {
        return stats.getLevel();
    }

    public void setLevel(final short level) {
        this.stats.setLevel((short) (level - 1));
    }

    public void setFame(short fame) {
        this.fame = fame;
    }

    public void setDojo(final int dojo) {
        this.dojo = dojo;
    }

    public void setDojoRecord(final boolean reset) {
        if (reset) {
            dojo = 0;
            dojoRecord = 0;
        } else {
            dojoRecord++;
        }
    }

    public final int getFallCounter() {
        return fall_counter;
    }

    public void setFallCounter(int fallcounter) {
        this.fall_counter = fallcounter;
    }

    public int getExp() {
        return stats.getExp();
    }

    public void setExp(int exp) {
        if (job.isCygnus() && stats.getLevel() >= 120) {
            this.stats.setExp(0);
            return;
        }
        this.stats.setExp(exp);
    }

    public void setHpApUsed(short hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public CheatTracker getCheatTracker() {
        return anti_cheat;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
        if (this.fame >= 50) {
            this.getFinishedAchievements().finishAchievement(this, 7);
        }
    }

    public void changeMapBanish(final int mapid, final String portal, final String msg) {
        dropMessage(5, msg);
        final MapleMap map = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map, map.getPortal(portal));
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x81, this), null);
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), null);
    }

    public void changeMapPortal(final MapleMap to, final MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), pto);
    }

    private void changeMapInternal(final MapleMap to, final Point pos, byte[] warpPacket, final MaplePortal pto) {
        if (to == null) {
            return;
        }

        final int nowmapid = map.getId();
        updatePetAuto();
        if (eventInstance != null) {
            eventInstance.changedMap(this, to.getId());
        }
        final boolean pyramid = pyramidSubway != null;
        if (map.getId() == nowmapid) {

            client.getSession().write(warpPacket);
            map.removePlayer(this);
            if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                map = to;
                map_id = to.getId();
                setPosition(pos);
                to.addPlayer(this);
                stats.relocHeal();
                expirationTask(false, false); // do this when change map
            }
        }
        if (pyramid && pyramidSubway != null) { // checks if they had pyramid
            // before AND after changing
            pyramidSubway.onChangeMap(this, to.getId());
        }
    }

    public void leaveMap() {
        controlled.clear();
        super.leaveMap();
        if (chair != 0) {
            cancelFishingTask();
            chair = 0;
        }
        cancelMapTimeLimitTask();
    }

    public void changeJob(int newJob) {
        try {
            MapleJob varJob = MapleJob.getById(newJob);
            if (varJob == null) {
                varJob = MapleJob.BEGINNER;
                // Bad..
                newJob = MapleJob.BEGINNER.getId();
            }
            this.job = varJob;
            if (newJob > 0 && !isGameMaster()) {
                resetStatsByJob(true);
                if (newJob == 2200) {
                    MapleQuest.getInstance(22100).forceStart(this, 0, null);
                    MapleQuest.getInstance(22100).forceComplete(this, 0);
                    client.getSession().write(MaplePacketCreator.getEvanTutorial("UI/tutorial/evan/14/0"));
                    dropMessage(
                            5,
                            "The baby Dragon hatched and appears to have something to tell you."
                                    + " Click the baby Dragon to start a conversation.");
                }
            }
            updateSingleStat(MapleStat.JOB, newJob);

            int maxhp = stats.getMaxHp(), maxmp = stats.getMaxMp();

            switch (job) {
                case WARRIOR:
                case DAWNWARRIOR1:
                case ARAN2:
                case BattleMage1:
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case MAGICIAN:
                case EVAN2:
                case EVAN3:
                    maxmp += Randomizer.rand(100, 150);
                    break;
                case BOWMAN:
                case THIEF:
                case PIRATE:
                case WildHunter1:
                case Mechanic1:
                    maxhp += Randomizer.rand(100, 150);
                    maxmp += Randomizer.rand(25, 50);
                    break;
                case FIGHTER:
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case PAGE:
                case SPEARMAN:
                case DAWNWARRIOR2:
                case ARAN3:
                case BattleMage2:
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case FP_WIZARD:
                case IL_WIZARD:
                case CLERIC:
                    maxmp += Randomizer.rand(400, 450);
                    break;
                case HUNTER:
                case CROSSBOWMAN:
                case ASSASSIN:
                case BANDIT:
                case BLADE_RECRUIT:
                case WINDARCHER2:
                case NIGHTWALKER2:
                case WildHunter2:
                case Mechanic2:
                    maxhp += Randomizer.rand(300, 350);
                    maxhp += Randomizer.rand(150, 200);
                    break;
                case GM: // GM
                case Manager: // Manager
                    maxhp += 30000;
                    maxhp += 30000;
                    break;
            }
            if (maxhp >= 30000) {
                maxhp = 30000;
            }
            if (maxmp >= 30000) {
                maxmp = 30000;
            }
            stats.setMaxHp((short) maxhp);
            stats.setMaxMp((short) maxmp);
            stats.setHp((short) maxhp);
            stats.setMp((short) maxmp);
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
            statup.add(new Pair<>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
            statup.add(new Pair<>(MapleStat.HP, Integer.valueOf(maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Integer.valueOf(maxmp)));
            stats.recalcLocalStats();
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, getJob().getId()));
            map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
            silentPartyUpdate();
            guildUpdate();
            if (dragon != null) {
                map.broadcastMessage(MaplePacketCreator.removeDragon(this.id));
                map.removeMapObject(dragon);
                dragon = null;
            }
            sendSkills();
            if (newJob >= 2200 && newJob <= 2218) { // make new
                if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                    cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
                }
                makeDragon();
                map.spawnDragon(dragon);
                map.updateMapObjectVisibility(this, dragon);
            }
            equipChanged();
            sendServerChangeJobCongratulations();
        } catch (Exception e) {
            log.info("Log_Script_Except.rtf", e);
        }
    }

    private void sendServerChangeJobCongratulations() {
        if (this.isGameMaster()) {
            return;
        }
        String str = "[Lv. %s] Congratulations to %s on becoming a %s!";
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(
                6, String.format(str, stats.getLevel(), getName(), this.job.getName())));
    }

    public void makeDragon() {
        dragon = new MapleDragon(this);
    }

    public void setDragon(MapleDragon d) {
        this.dragon = d;
    }

    public void gainAp(int ap) {
        this.remainingAp += ap;
        updateSingleStat(MapleStat.AVAILABLEAP, Math.min(199, this.remainingAp));
    }

    public void gainSp(int sp) {
        if (job.isEvan()) {
            addEvanSP(sp);
            return;
        }
        this.remainingSp += sp;
        updateSingleStat(MapleStat.AVAILABLESP, Math.min(199, this.remainingSp));
    }

    public void resetAPSP() {
        gainAp(-this.remainingAp);
    }

    public void changeSkillLevel(final ISkill skill, byte newLevel, byte newMasterlevel) { // 1
        // month
        if (skill == null) {
            return;
        }
        changeSkillLevel(
                skill,
                newLevel,
                newMasterlevel,
                skill.isTimeLimited() ? (System.currentTimeMillis() + (30L * 24L * 60L * 60L * 1000L)) : -1);
    }

    public void changeSkillLevel(final ISkill skill, byte newLevel, byte newMasterlevel, long expiration) {
        if (skill == null
                || (!GameConstants.isApplicableSkill(skill.getId())
                && !GameConstants.isApplicableSkill_(skill.getId()))) {
            return;
        }
        client.getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, expiration));
        if (newLevel == 0 && newMasterlevel == 0) {
            if (skills.containsKey(skill)) {
                skills.remove(skill);
            } else {
                return; // nothing happen
            }
        } else {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
        }
        changed_skills = true;
        if (GameConstants.isRecoveryIncSkill(skill.getId())) {
            stats.relocHeal();
        } else if (GameConstants.isElementAmp_Skill(skill.getId())) {
            stats.recalcLocalStats();
        }
    }

    public void changeSkillLevel_Skip(final ISkill skill, byte newLevel, byte newMasterlevel) {
        if (skill == null) {
            return;
        }
        client.getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel, -1L));
        if (newLevel == 0 && newMasterlevel == 0) {
            if (skills.containsKey(skill)) {
                skills.remove(skill);
            } else {
                // nothing happen
            }
        } else {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, -1L));
        }
    }

    public void playerDead() {
        final MapleStatEffect statss = getStatForBuff(MapleBuffStat.SOUL_STONE);
        if (statss != null) {
            client.getSession().write(MaplePacketCreator.showSpecialEffect(26));
            getStat().setHp(((getStat().getMaxHp() / 100) * statss.getX()));
            setStance(0);
            changeMap(getMap(), getMap().getPortal(0));
            return;
        }
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        dispelSkill(0);
        cancelMorphs(true); // dead = cancel
        cancelBuffStats(MapleBuffStat.DRAGONBLOOD);
        cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        cancelEffectFromBuffStat(MapleBuffStat.REAPER);
        cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        checkFollow();
        if (job != MapleJob.BEGINNER && job != MapleJob.NOBLESSE && job != MapleJob.LEGEND && job != MapleJob.EVAN1) {
            int charms = getItemQuantity(5130000, false);
            if (charms > 0) {
                MapleInventoryManipulator.removeById(client, MapleInventoryType.CASH, 5130000, 1, true, false);

                charms--;
                if (charms > 0xFF) {
                    charms = 0xFF;
                }
                client.getSession().write(MTSCSPacket.useCharm((byte) charms, (byte) 0));
            } else {
                float diepercentage = 0.0f;
                int expforlevel = GameConstants.getExpNeededForLevel(stats.getLevel());
                if (map.isTown() || FieldLimitType.RegularExpLoss.check(map.getFieldLimit())) {
                    diepercentage = 0.01f;
                } else {
                    float v8 = 0.0f;
                    if (this.job.getId() / 100 == 3) {
                        v8 = 0.08f;
                    } else {
                        v8 = 0.2f;
                    }
                    diepercentage = (float) (v8 / this.stats.getLuk() + 0.05);
                }
                int v10 = (int) (this.stats.getExp() - (long) ((double) expforlevel * diepercentage));
                if (v10 < 0) {
                    v10 = 0;
                }
                this.stats.setExp(v10);
            }
        }
        this.updateSingleStat(MapleStat.EXP, this.stats.getExp());
        if (!stats.checkEquipDurabilitys(this, -100)) { // i guess this is how
            // it works ?
            dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
        } // lol
        if (pyramidSubway != null) {
            stats.setHp((short) 50);
            pyramidSubway.fail(this);
        }
    }

    public void receivePartyMemberHP() {
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            final MapleCharacter partyMate =
                    client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
            if (partyMate != null && partyMate != this) {
                log.info("Received HP from " + partyMate.getName());
                this.getClient().sendPacket(MapleUserPackets.updatePartyHpForCharacter(partyMate));
            }
        }
    }

    public void updatePartyMemberHP() {
        if (party == null) {
            return;
        }
        for (MaplePartyCharacter partychar : party.getMembers()) {
            final MapleCharacter partyMates =
                    client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
            if (partyMates != null && partyMates != this) {
                partyMates.getClient().sendPacket(MapleUserPackets.updatePartyHpForCharacter(this));
            }
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            PartyManager.updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
        }
    }

    public void healHP(int delta) {
        addHP(delta);
        client.getSession().write(MaplePacketCreator.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, MaplePacketCreator.showHpHealed(getId(), delta), false);
    }

    public void healMP(int delta) {
        addMP(delta);
        client.getSession().write(MaplePacketCreator.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, MaplePacketCreator.showHpHealed(getId(), delta), false);
    }

    /**
     * Convenience function which adds the supplied parameter to the current hp then directly does a
     * updateSingleStat.
     *
     * @param delta
     */
    public void addHP(int delta) {
        if (stats.setHp(stats.getHp() + delta)) {
            updateSingleStat(MapleStat.HP, stats.getHp());
        }
    }

    /**
     * Convenience function which adds the supplied parameter to the current mp then directly does a
     * updateSingleStat.
     *
     * @param delta
     */
    public void addMP(int delta) {
        if (stats.setMp(stats.getMp() + delta)) {
            updateSingleStat(MapleStat.MP, stats.getMp());
        }
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        List<Pair<MapleStat, Integer>> statups = new ArrayList<>();

        if (stats.setHp(stats.getHp() + hpDiff)) {
            statups.add(new Pair<>(MapleStat.HP, Integer.valueOf(stats.getHp())));
        }
        if (stats.setMp(stats.getMp() + mpDiff)) {
            statups.add(new Pair<>(MapleStat.MP, Integer.valueOf(stats.getMp())));
        }
        if (statups.size() > 0) {
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statups, getJob().getId()));
            int hp = this.getStat().getHp();
            if (hp <= 0) { // In case player die with disable actions
                getClient().enableActions();
            }
        }
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    /**
     * Updates a single stat of this MapleCharacter for the client. This method only creates and
     * sends an update packet, it does not update the stat stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     * @param itemReaction
     */
    public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        if (stat == MapleStat.AVAILABLESP) {
            client.getSession().write(MaplePacketCreator.updateSp(this, itemReaction, false));
            return;
        }
        Pair<MapleStat, Integer> statpair = new Pair<>(stat, Integer.valueOf(newval));
        client.getSession()
                .write(MaplePacketCreator.updatePlayerStats(
                        Collections.singletonList(statpair), itemReaction, getJob().getId()));
    }

    public void gainExp(final int total, final boolean show, final boolean inChat, final boolean white) {
        if (!isAlive()) {
            return;
        }

        try {
            if (job.isCygnus() && stats.getLevel() >= 120) {
                return;
            }
            int needed = GameConstants.getExpNeededForLevel(stats.getLevel());
            if (stats.getLevel() >= 200) {
                if (stats.getExp() + total > needed) {
                    setExp(needed);
                } else {
                    stats.addExp(total);
                }
            } else {
                if (stats.getExp() + total >= needed) {
                    stats.addExp(total);
                    levelUp(true);
                    needed = GameConstants.getExpNeededForLevel(stats.getLevel());
                    if (stats.getExp() > needed) {
                        setExp(needed);
                    }
                } else {
                    stats.addExp(total);
                }
            }

            if (total != 0) {
                if (stats.getExp() < 0) { // After adding, and negative
                    if (total > 0) {
                        setExp(needed);
                    } else if (total < 0) {
                        setExp(0);
                    }
                }
                updateSingleStat(MapleStat.EXP, getExp());
                if (show) { // still show the expgain even if it's not there
                    client.getSession().write(MaplePacketCreator.GainEXP_Others(total, inChat, white));
                }
                if (total > 0) {
                    stats.checkEquipLevels(this, total); // gms like
                }
            }
        } catch (Exception e) {
            log.error("Log_Script_Except.rtf", e);
        }
    }

    public void gainExpMonster(
            final int gain,
            final boolean show,
            final boolean white,
            final byte pty,
            int Class_Bonus_EXP,
            int Equipment_Bonus_EXP,
            int Premium_Bonus_EXP,
            boolean real) {
        if (!isAlive()) {
            return;
        }
        if (job.isCygnus() && stats.getLevel() >= 120) {
            return;
        }
        mobKilledNo++; // Reset back to 0 when cc

        long total = gain + Class_Bonus_EXP + Equipment_Bonus_EXP + Premium_Bonus_EXP;

        long Trio_Bonus_EXP = 0;
        short percentage = 0;
        double hoursFromLogin = 0.0;
        if (mobKilledNo == 3 && ServerConstants.TRIPLE_TRIO) { // Count begins
            // at 0
            // After 1 hour of login until 2 hours: Bonus 30% EXP at every 3rd
            // mob hunted
            // 2 hours to 3 hours: Bonus 100% EXP at every 3rd mob hunted
            // 3 hours to 4 hours: Bonus 150% EXP at every 3rd mob hunted
            // 4 hours to 5 hours: Bonus 180% EXP at every 3rd mob hunted
            // 5 hours and above: Bonus 200% EXP at every 3rd mob hunted
            hoursFromLogin = ((System.currentTimeMillis() - loginTime) / (1000 * 60 * 60));
            if (hoursFromLogin >= 1 && hoursFromLogin < 2) {
                percentage = 30;
            } else if (hoursFromLogin >= 2 && hoursFromLogin < 3) {
                percentage = 40;
            } else if (hoursFromLogin >= 3 && hoursFromLogin < 4) {
                percentage = 50;
            } else if (hoursFromLogin >= 4 && hoursFromLogin < 5) {
                percentage = 60;
            } else if (hoursFromLogin >= 5) {
                percentage = 100;
            }
            Trio_Bonus_EXP = ((gain / 100) * percentage);
            total += Trio_Bonus_EXP;
            mobKilledNo = 0;
        }

        int partyinc = 0;
        if (pty > 1) {
            partyinc = (gain / 20) * (pty + 1);
            total += partyinc;
        }
        if (gain > 0 && total < gain) { // just in case
            total = Integer.MAX_VALUE;
        }
        if (stats.getExp() < 0) { // Set first
            setExp(0);
            updateSingleStat(MapleStat.EXP, 0);
        }

        int needed = GameConstants.getExpNeededForLevel(stats.getLevel()); // Calculate
        // based on the
        // first level
        boolean leveled = false;
        if (getLevel() < 200) {
            long newexp = total + stats.getExp();
            while (newexp >= GameConstants.getExpNeededForLevel(stats.getLevel()) && stats.getLevel() < 200) {
                newexp -= GameConstants.getExpNeededForLevel(stats.getLevel());
                levelUp(false); // Don't show animation for ALL of the levels.
                leveled = true;
            }
            if (newexp >= Integer.MAX_VALUE || stats.getLevel() >= 200) {
                setExp(0);
            } else {
                setExp((int) newexp);
            }

        } else {
            return;
        }
        if (gain != 0) {
            if (stats.getExp() < 0) { // After adding, and negative
                if (gain > 0) {
                    setExp(GameConstants.getExpNeededForLevel(stats.getLevel()));
                } else if (gain < 0) {
                    setExp(0);
                }
            }
            updateSingleStat(MapleStat.EXP, getExp());
            if (leveled) {
                final List<Pair<MapleStat, Integer>> statup = new ArrayList<>(7);
                statup.add(new Pair<>(MapleStat.MAXHP, Math.min(30000, stats.getMaxHp())));
                statup.add(new Pair<>(MapleStat.MAXMP, Math.min(30000, stats.getMaxMp())));
                statup.add(new Pair<>(MapleStat.HP, Math.min(30000, stats.getMaxHp())));
                statup.add(new Pair<>(MapleStat.MP, Math.min(30000, stats.getMaxMp())));
                statup.add(new Pair<>(MapleStat.EXP, stats.getExp()));
                statup.add(new Pair<>(MapleStat.LEVEL, (int) stats.getLevel()));
                statup.add(new Pair<>(MapleStat.AVAILABLEAP, Math.min(199, remainingAp)));
                client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, getJob().getId()));
                map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
            }
            if (show) { // still show the expgain even if it's not there
                client.getSession()
                        .write(MaplePacketCreator.GainEXP_Monster(
                                Math.min(Integer.MAX_VALUE, gain),
                                white,
                                Math.min(Integer.MAX_VALUE, partyinc),
                                Class_Bonus_EXP,
                                Equipment_Bonus_EXP,
                                Premium_Bonus_EXP,
                                (byte) percentage,
                                hoursFromLogin));
            }
            stats.checkEquipLevels(this, (int) Math.min(Integer.MAX_VALUE, total));
        }
    }

    public void forceReAddItem_NoUpdate(IItem item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
    }

    public void forceReAddItem(IItem item, MapleInventoryType type) { // used
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.getSession()
                    .write(MaplePacketCreator.updateSpecialItemUse(
                            item, type == MapleInventoryType.EQUIPPED ? (byte) 1 : type.getType()));
        }
    }

    public void forceReAddItem_Flag(IItem item, MapleInventoryType type) { // used
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.getSession()
                    .write(MaplePacketCreator.updateSpecialItemUse_(
                            item, type == MapleInventoryType.EQUIPPED ? (byte) 1 : type.getType()));
        }
    }

    public boolean isGameMaster() {
        return client.getAccountData().getGMLevel() > 0;
    }

    public boolean isAdmin() {
        return client.getAccountData().getGMLevel() >= 5;
    }

    public int getGMLevel() {
        return client.getAccountData().getGMLevel();
    }

    public final MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public final MapleInventory[] getInventories() {
        return inventory;
    }

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(client, GameConstants.getInventoryType(id), id, -quantity, true, false);
    }

    public final void expirationTask(boolean pending, boolean firstLoad) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (pending) {
            if (pendingExpiration != null) {
                final List<String> replaceMsg = new ArrayList<>();
                for (Integer z : pendingExpiration) {
                    final Triple<Integer, String, Integer> replace = ii.replaceItemInfo(z.intValue());
                    if (replace == null) {
                        client.getSession().write(MaplePacketCreator.itemExpired(z.intValue()));
                    }
                    if (!firstLoad) {
                        if (replace != null
                                && replace.getLeft() > 0
                                && replace.getMid().length() > 0) {
                            replaceMsg.add(replace.getMid());
                        }
                    }
                }
                client.getSession().write(MaplePacketCreator.itemReplaced(replaceMsg));
            }
            pendingExpiration = null;
            if (pendingSkills != null) {
                for (Integer z : pendingSkills) {
                    client.getSession().write(MaplePacketCreator.updateSkill(z, 0, 0, -1));
                }
                client.getSession().write(MaplePacketCreator.skillExpired(pendingSkills));
            }
            pendingSkills = null;
            if (getPendingUnlock() != null) {
                client.getSession().write(MaplePacketCreator.sealExpired(getPendingUnlock()));
            }
            return;
        }

        final List<Integer> ret = new ArrayList<>();
        final List<Integer> retExpire = new ArrayList<>();
        final long currenttime = System.currentTimeMillis();
        final List<Pair<MapleInventoryType, IItem>> toberemove = new ArrayList<>(); // This
        // is
        // here
        // to
        // prevent
        // deadlock.
        final List<IItem> tobeunlock = new ArrayList<>(); // This is here
        // to prevent
        // deadlock.

        for (final MapleInventoryType inv : MapleInventoryType.values()) {
            for (final IItem item : getInventory(inv)) {
                long expiration = item.getExpiration();

                if ((expiration != -1 && !GameConstants.isPet(item.getItemId()) && currenttime > expiration)) {
                    if (ItemFlag.LOCK.check(item.getFlag())) {
                        tobeunlock.add(item);
                    } else if (currenttime > expiration) {
                        toberemove.add(new Pair<>(inv, item));
                    }
                } else if ((item.getItemId() == 5000054
                        && item.getPet() != null
                        && item.getPet().getSecondsLeft() <= 0)
                        || (firstLoad && ii.isLogoutExpire(item.getItemId()))) {
                    toberemove.add(new Pair<>(inv, item));
                }
            }
        }
        IItem item;
        for (final Pair<MapleInventoryType, IItem> itemz : toberemove) {
            item = itemz.getRight();
            ret.add(item.getItemId());
            getInventory(itemz.getLeft()).removeItem(item.getPosition(), item.getQuantity(), false);
            if (!firstLoad) {
                final Triple<Integer, String, Integer> replace = ii.replaceItemInfo(item.getItemId());
                if (replace != null && replace.getLeft() > 0) {
                    final int period = replace.getRight();
                    if (GameConstants.getInventoryType(replace.getLeft()) == MapleInventoryType.EQUIP) {
                        final IItem theNewItem = ii.getEquipById(replace.getLeft());
                        theNewItem.setPosition(item.getPosition());
                        theNewItem.setExpiration(System.currentTimeMillis() + ((long) period * 60 * 1000));
                        getInventory(itemz.getLeft()).addFromDB(theNewItem);
                    } else {
                        final Item newI = new Item(replace.getLeft(), item.getPosition(), (short) 1, (byte) 0, -1);
                        newI.setExpiration(System.currentTimeMillis() + ((long) period * 60 * 1000));
                        getInventory(itemz.getLeft()).addItem(newI);
                    }
                }
            }
        }
        this.pendingExpiration = ret;

        for (final IItem itemz : tobeunlock) {
            retExpire.add(itemz.getItemId());
            itemz.setExpiration(-1);
            itemz.setFlag((byte) (itemz.getFlag() - ItemFlag.LOCK.getValue()));
        }
        this.setPendingUnlock(retExpire);

        final List<Integer> skilz = new ArrayList<>();
        final List<ISkill> toberem = new ArrayList<>();
        for (Entry<ISkill, SkillEntry> skil : skills.entrySet()) {
            if (skil.getValue().expiration != -1 && currenttime > skil.getValue().expiration) {
                toberem.add(skil.getKey());
            }
        }
        for (ISkill skil : toberem) {
            skilz.add(skil.getId());
            this.skills.remove(skil);
            this.changed_skills = true;
        }
        this.pendingSkills = skilz;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions) {
        gainMeso(gain, show, enableActions, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        final int startMeso = meso;
        final long total = (long) startMeso + (long) gain;
        int realGain = gain;
        if (total >= Integer.MAX_VALUE) {
            meso = Integer.MAX_VALUE;
            realGain = Integer.MAX_VALUE - startMeso;
        } else {
            meso += gain;
        }
        updateSingleStat(MapleStat.MESO, meso, enableActions);
        if (show && realGain > 0) {
            client.getSession().write(MaplePacketCreator.showMesoGain(realGain, inChat));
        }
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {

        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MobPacket.controlMonster(monster, false, aggro));
    }

    public void stopControllingMonster(MapleMonster monster) {

        if (monster != null) {
            controlled.remove(monster);
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        } else {
            monster.switchController(this, true);
        }
    }

    public int getControlledSize() {
        return controlled.size();
    }

    public int getAccountID() {
        return accountData.getId();
    }

    public void mobKilled(final int id, final int skillID) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() != 1 || !q.hasMobKills()) {
                continue;
            }
            if (q.mobKilled(id, skillID)) {
                client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    client.getSession()
                            .write(MaplePacketCreator.getShowQuestCompletion(
                                    q.getQuest().getId()));
                }
            }
        }
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 1) {
                ret.add(q);
            }
        }
        return ret;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2) {
                ret.add(q);
            }
        }
        return ret;
    }

    public boolean hasSkill(int skill) {
        SkillEntry ret = skills.get(skill);
        return ret != null;
    }

    public byte getSkillLevel(final ISkill skill) {
        final SkillEntry ret = skills.get(skill);
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        return (byte)
                Math.min(skill.getMaxLevel(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : stats.getIncAllskill()));
    }

    public byte getMasterLevel(final int skill) {
        return getMasterLevel(SkillFactory.getSkill(skill));
    }

    public byte getMasterLevel(final ISkill skill) {
        final SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public void levelUp(boolean show) {
        if (getLevel() > 200) {
            return;
        }
        if (job.isCygnus() && getLevel() >= 120) {
            return;
        }
        if ((long) (remainingAp + 5) >= Integer.MAX_VALUE) {
            remainingAp = Integer.MAX_VALUE;
        } else {
            remainingAp += 5;
        }
        if (job.isCygnus() && getLevel() < 70) {
            remainingAp += 1;
        }

        int maxhp = stats.getMaxHp();
        int maxmp = stats.getMaxMp();

        if (job.isBeginner()) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isWarrior()) {
            final ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
            final int slevel = getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isMage()) {
            final ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
            final int slevel = getSkillLevel(improvingMaxMP);
            if (slevel > 0) {
                maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
            }
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if ((job.getId() >= 300 && job.getId() <= 322)
                || (job.getId() >= 400 && job.getId() <= 434)
                || (job.getId() >= 1300 && job.getId() <= 1311)
                || (job.getId() >= 1400 && job.getId() <= 1411)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isPirate()) { // Pirate
            final ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
            final int slevel = getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(22, 26);
            maxmp += Randomizer.rand(18, 22);
        } else if (job.getId() >= 1100 && job.getId() <= 1111) { // Soul Master
            final ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
            final int slevel = getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.getId() >= 1200 && job.getId() <= 1211) { // Flame Wizard
            final ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
            final int slevel = getSkillLevel(improvingMaxMP);
            if (slevel > 0) {
                maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
            }
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.getId() >= 1500 && job.getId() <= 1512) { // Pirate
            final ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
            final int slevel = getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(22, 26);
            maxmp += Randomizer.rand(18, 22);
        } else if (job.getId() >= 2100 && job.getId() <= 2112) { // Aran
            maxhp += Randomizer.rand(50, 52);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.getId() >= 2200 && job.getId() <= 2218) { // Evan
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(50, 52);
        } else { // GameMaster
            maxhp += Randomizer.rand(50, 100);
            maxmp += Randomizer.rand(50, 100);
        }
        maxmp += stats.getTotalInt() / 10;
        stats.setExp(0);
        stats.addLevel(1);
        int level = getLevel();

        maxhp = (short) Math.min(30000, Math.abs(maxhp));
        maxmp = (short) Math.min(30000, Math.abs(maxmp));

        stats.setMaxHp((short) maxhp);
        stats.setMaxMp((short) maxmp);
        stats.setHp((short) maxhp);
        stats.setMp((short) maxmp);

        final List<Pair<MapleStat, Integer>> statup = new ArrayList<>(7);
        statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
        statup.add(new Pair<>(MapleStat.HP, maxhp));
        statup.add(new Pair<>(MapleStat.MP, maxmp));
        statup.add(new Pair<>(MapleStat.EXP, this.stats.getExp()));
        statup.add(new Pair<>(MapleStat.LEVEL, level));
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));

        if (!job.isEvan() && level >= 10 || (level > 8 && job.equals(MapleJob.MAGICIAN.getId()))) {
            remainingSp += 3;
            statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp));
        }

        client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, getJob().getId()));
        if ((job.isEvan()) && (!MapleJob.EVAN1.equals(job))) {
            addEvanSP(3);
        }
        map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        stats.recalcLocalStats();

        checkForAchievements();
        sendDualbladeJobStarterMessage();
        sendLevel200Congratulations();
        silentPartyUpdate();
        guildUpdate();
        checkForChangeJob();
        equipChanged();
    }

    public void addEvanSP(int evanPoints) {
        this.evanSP.addSkillPoints(this.job.getId(), evanPoints);
        if (this.evanSP.getSkillPoints(this.job.getId()) < 0) {
            this.evanSP.setSkillPoints(this.job.getId(), 0);
        }
        this.client.getSession().write(MaplePacketCreator.updateExtendedSP(this.evanSP));
    }

    private void checkForAchievements() {
        int level = getLevel();
        if (level >= 30) {
            this.getFinishedAchievements().finishAchievement(this, 2);
        }
        if (level >= 70) {
            this.getFinishedAchievements().finishAchievement(this, 3);
        }
        if (level >= 120) {
            this.getFinishedAchievements().finishAchievement(this, 4);
        }
        if (level >= 200) {
            this.getFinishedAchievements().finishAchievement(this, 5);
        }
    }

    private void sendDualbladeJobStarterMessage() {
        int level = getLevel();
        if (level == 2 && this.getSubCategoryField() == 1) {
            String shortMessage = "To become a DualBlade click on the lightbulb over you head";
            String completeMessage = shortMessage + " and start the quest Dualblade: The Seal of Destity";
            dropMessage(-1, shortMessage);
            dropMessage(5, completeMessage);
        }
    }

    private void sendLevel200Congratulations() {
        int level = getLevel();
        if (level == 200 && !isGameMaster() || job.isCygnus() && level == 120) {
            final StringBuilder sb = new StringBuilder("[Congratulation] ");
            final IItem medal = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
            if (medal != null) { // Medal
                sb.append("<");
                sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
                sb.append("> ");
            }
            sb.append(getName());
            sb.append(" has achieved Level " + level + ". Let us Celebrate Maplers!");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, sb.toString()));
        }
    }

    private void checkForChangeJob() {
        int level = getLevel();
        if (GameConstants.isKOC(job.getId()) && job.getId() > 1000) {
            final String base = (String.valueOf(job).substring(0, 2)) + "00";
            if (level >= 120 && job.getId() % 10 != 2 && job.getId() % 100 != 0) {
                changeJob(Integer.valueOf(base) + 12);
            } else if ((level >= 70 && level <= 119) && job.getId() % 10 != 1 && job.getId() % 100 != 0) {
                changeJob(Integer.valueOf(base) + 11);
            } else if ((level >= 30 && level <= 69) && job.getId() % 100 == 0) {
                changeJob(Integer.valueOf(base) + 10);
            }
        } else if (GameConstants.isEvan(job.getId())) {
            if (level >= 160 && job.getId() != 2218) {
                changeJob(2218);
            } else if (level >= 120 && level <= 159 && job.getId() != 2217) {
                changeJob(2217);
            } else if (level >= 100 && level <= 119 && job.getId() != 2216) {
                changeJob(2216);
            } else if (level >= 80 && level <= 99 && job.getId() != 2215) {
                changeJob(2215);
            } else if (level >= 60 && level <= 79 && job.getId() != 2214) {
                changeJob(2214);
            } else if (level >= 50 && level <= 59 && job.getId() != 2213) {
                changeJob(2213);
            } else if (level >= 40 && level <= 49 && job.getId() != 2212) {
                changeJob(2212);
            } else if (level >= 30 && level <= 39 && job.getId() != 2211) {
                changeJob(2211);
            } else if (level >= 20 && level <= 29 && job.getId() != 2210) {
                changeJob(2210);
            } else if (level >= 10 && level <= 19 && job.getId() != 2200) {
                changeJob(2200);
            }
        }
    }

    public final boolean ban(String reason, boolean ipBan, boolean automaticBan) {
        if (lastMonthFameIds == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, automaticBan ? 2 : 1);
            ps.setString(2, reason);
            ps.setInt(3, accountData.getId());
            ps.execute();
            ps.close();

            if (ipBan) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, client.getSessionIPAddress());
                ps.execute();
                ps.close();
            }
        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
            return false;
        }
        client.getSession().close();
        return true;
    }

    public boolean isAlive() {
        return stats.getHp() > 0;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer().allowedToTarget(this)) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            if (party != null) {
                boolean isOnParty =
                        this.getParty().getMemberById(client.getPlayer().getId()) != null;
                if (isOnParty) {
                    client.sendPacket(MapleUserPackets.updatePartyHpForCharacter(this));
                    getClient().sendPacket(MapleUserPackets.updatePartyHpForCharacter(client.getPlayer()));
                }
            }

            if (dragon != null) {
                client.getSession().write(MaplePacketCreator.spawnDragon(dragon));
            }
            if (summons != null) {
                for (final MapleSummon summon : summons.values()) {
                    client.getSession().write(MaplePacketCreator.spawnSummon(summon, false));
                }
            }
            if (pets != null) {
                for (MaplePet pet : pets) {
                    if (pet.getSummoned()) {
                        client.sendPacket(PetPacket.showPet(this, pet, false, false));
                    }
                }
            }
            if (follow_id > 0 && follow_on) {
                client.getSession()
                        .write(MaplePacketCreator.followEffect(
                                follow_initiator ? follow_id : id, follow_initiator ? id : follow_id, null));
            }
        }
    }

    public final void equipChanged() {
        map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        stats.recalcLocalStats();
        if (getMessenger() != null) {
            MessengerManager.updateMessenger(getMessenger().getId(), getName(), client.getChannel());
        }
    }

    public final MaplePet getPet(final int index) {
        byte count = 0;
        if (pets == null) {
            return null;
        }
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (count == index) {
                    return pet;
                }
                count++;
            }
        }
        return null;
    }

    public final MaplePet getPetByUID(final int uid) {
        if (pets == null) {
            return null;
        }
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == uid) {
                    return pet;
                }
            }
        }
        return null;
    }

    public void removePetCS(MaplePet pet) {
        pets.remove(pet);
    }

    public void addPet(final MaplePet pet) {
        pets.remove(pet);
        pets.add(pet);
        // So that the pet will be at the last
        // Pet index logic :(
    }

    public void removePet(MaplePet pet, boolean shiftLeft) {
        pet.setSummoned(false);
    }

    public final byte getPetIndex(final MaplePet petz) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet == petz) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final ArrayList<MaplePet> getSummonedPets(ArrayList<MaplePet> ret) {
        ret.clear();
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                ret.add(pet);
            }
        }
        return ret;
    }

    public final byte getPetIndex(final int petId) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == petId) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final byte getPetById(final int petId) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getPetItemId() == petId) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final void unequipAllPets() {
        for (final MaplePet pet : pets) {
            if (pet != null) {
                unequipPet(pet, true, false);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
        if (pet.getSummoned()) {
            pet.saveToDb();
            map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
            // List<Pair<MapleStat, Integer>> stats = new
            // ArrayList<Pair<MapleStat, Integer>>();
            // stats.add(new Pair<>(MapleStat.PET,
            // Integer.valueOf(0)));
            removePet(pet, shiftLeft);
            client.getSession().write(PetPacket.petStatUpdate(this));
            client.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public final List<Integer> getFamedCharacters() {
        return lastMonthFameIds;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (lastFameTime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
            return FameStatus.NOT_TODAY;
        } else if (from == null
                || lastMonthFameIds == null
                || lastMonthFameIds.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastFameTime = System.currentTimeMillis();
        lastMonthFameIds.add(Integer.valueOf(to.getId()));
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps =
                    con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing famelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void clearDoors() {
        doors.clear();
    }

    public void setChair(int chair) {
        this.chair = chair;
        stats.relocHeal();
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public int getGuildId() {
        return guild_id;
    }

    public void setGuildId(int _id) {
        guild_id = _id;
        if (guild_id > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);

            } else {
                mgc.setGuildId(guild_id);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(byte _rank) {
        guildRank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public void setAllianceRank(byte rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public MapleGuild getGuild() {
        if (getGuildId() <= 0) {
            return null;
        }
        return GuildManager.getGuild(getGuildId());
    }

    public void guildUpdate() {
        if (guild_id <= 0) {
            return;
        }
        mgc.setLevel(stats.getLevel());
        mgc.setJobId(job.getId());
        GuildManager.memberLevelJobUpdate(mgc);
    }

    public void saveGuildStatus() {
        MapleGuild.setOfflineGuildStatus(guild_id, guildRank, allianceRank, id);
    }

    public void modifyCSPoints(int type, int quantity) {
        modifyCSPoints(type, quantity, false);
    }

    public void modifyCSPoints(int type, int quantity, boolean show) {
        switch (type) {
            case 1:
            case 4:
                if (nx_credit + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
                    }
                    return;
                }
                nx_credit += quantity;
                break;
            case 2:
                if (maple_points + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max maple points. No cash will be awarded.");
                    }
                    return;
                }
                maple_points += quantity;
                break;
        }
        if (show && quantity != 0) {
            dropMessage(
                    -1,
                    "You have "
                            + (quantity > 0 ? "gained " : "lost ")
                            + quantity
                            + (type == 1 ? " cash." : " maple points."));
            // client.getSession().write(MaplePacketCreator.showSpecialEffect(19));
        }
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
            case 4:
                return nx_credit;
            case 2:
                return maple_points;
        }
        return 0;
    }

    public final boolean hasEquipped(int itemid) {
        return inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        int possesed = inventory[type.ordinal()].countById(itemid);
        if (checkEquipped && type == MapleInventoryType.EQUIP) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= quantity;
        } else {
            return possesed == quantity;
        }
    }

    public final boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, true, true);
    }

    public final boolean haveItem(int itemid) {
        return haveItem(itemid, 1, true, true);
    }

    public byte getBuddyCapacity() {
        return buddyList.getCapacity();
    }

    public void setBuddyCapacity(byte capacity) {
        buddyList.setCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void addCooldown(int skillId, long startTime, long length) {
        coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length));
    }

    public void removeCooldown(int skillId) {
        coolDowns.remove(Integer.valueOf(skillId));
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        addCooldown(skillid, starttime, length);
    }

    public void giveCoolDowns(final List<MapleCoolDownValueHolder> cooldowns) {
        int time;
        if (cooldowns != null) {
            for (MapleCoolDownValueHolder cooldown : cooldowns) {
                coolDowns.put(cooldown.getSkillId(), cooldown);
            }
        } else {
            try (var con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                        "SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid" + " = ?");
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0) {
                        continue;
                    }
                    giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                }
                ps.close();
                rs.close();
                deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");

            } catch (SQLException e) {
                log.error("Error on giveCoolDowns", e);
            }
        }
    }

    public int getCooldownSize() {
        return coolDowns.size();
    }

    public int getDiseaseSize() {
        return diseases.size();
    }

    public final List<MapleCoolDownValueHolder> getCooldowns() {
        return getCooldowns(new ArrayList<>());
    }

    public ArrayList<MapleCoolDownValueHolder> getCooldowns(ArrayList<MapleCoolDownValueHolder> ret) {
        ret.clear();
        for (MapleCoolDownValueHolder mc : coolDowns.values()) {
            if (mc != null) {
                ret.add(mc);
            }
        }
        return ret;
    }

    public final List<MapleDiseaseValueHolder> getAllDiseases() {
        return getAllDiseases(new ArrayList<>());
    }

    public final ArrayList<MapleDiseaseValueHolder> getAllDiseases(ArrayList<MapleDiseaseValueHolder> ret) {
        ret.clear();
        for (MapleDiseaseValueHolder mc : diseases.values()) {
            if (mc != null) {
                ret.add(mc);
            }
        }
        return ret;
    }

    public final boolean hasDisease(final MapleDisease dis) {
        return diseases.containsKey(dis);
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        giveDebuff(disease, skill.getX(), skill.getDuration(), skill.getSkillId(), skill.getSkillLevel());
    }

    public void giveDebuff(final MapleDisease disease, int x, long duration, int skillid, int level) {
        final List<Pair<MapleDisease, Integer>> debuff =
                Collections.singletonList(new Pair<>(disease, Integer.valueOf(x)));

        if (!hasDisease(disease) && diseases.size() < 2) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)) {
                if (isActiveBuffedValue(2321005)) {
                    return;
                }
            }

            diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
            client.getSession().write(MaplePacketCreator.giveDebuff(debuff, skillid, level, (int) duration));
            map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, debuff, skillid, level), false);
        }
    }

    public final void giveSilentDebuff(final List<MapleDiseaseValueHolder> ld) {
        if (ld != null) {
            for (final MapleDiseaseValueHolder disease : ld) {
                diseases.put(disease.disease(), disease);
            }
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            long mask = debuff.getValue();
            boolean first = debuff.isFirst();
            client.getSession().write(MaplePacketCreator.cancelDebuff(mask, first));
            map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask, first), false);

            diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        dispelDebuff(MapleDisease.CURSE);
        dispelDebuff(MapleDisease.DARKNESS);
        dispelDebuff(MapleDisease.POISON);
        dispelDebuff(MapleDisease.SEAL);
        dispelDebuff(MapleDisease.WEAKEN);
    }

    public void cancelAllDebuffs() {
        diseases.clear();
    }

    public void sendNote(String to, String msg) {
        sendNote(to, msg, 0);
    }

    public void sendNote(String to, String msg, int fame) {
        MapleCharacterHelper.sendNote(to, getName(), msg, fame);
    }

    public void showNote() {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            int count = rs.getRow();
            rs.first();
            client.getSession().write(MTSCSPacket.showNotes(rs, count));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Unable to show note" + e);
        }
    }

    public void deleteNote(int id, int fame) {
        try (var con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT gift FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("gift") == fame && fame > 0) { // not exploited!
                    // hurray
                    addFame(fame);
                    updateSingleStat(MapleStat.FAME, getFame());
                    client.getSession().write(MaplePacketCreator.getShowFameGain(fame));
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Unable to delete note" + e);
        }
    }

    public void mulung_EnergyModify(boolean inc) {
        if (inc) {
            if (mu_lung_energy + 100 > 10000) {
                mu_lung_energy = 10000;
            } else {
                mu_lung_energy += 100;
            }
        } else {
            mu_lung_energy = 0;
        }
        client.getSession().write(MaplePacketCreator.MulungEnergy(mu_lung_energy));
    }

    public void writeMulungEnergy() {
        client.getSession().write(MaplePacketCreator.MulungEnergy(mu_lung_energy));
    }

    @Api
    public void writeEnergy(String type, String inc) {
        client.getSession().write(MaplePacketCreator.sendPyramidEnergy(type, inc));
    }

    public void writeStatus(String type, String inc) {
        client.getSession().write(MaplePacketCreator.sendGhostStatus(type, inc));
    }

    public void writePoint(String type, String inc) {
        client.getSession().write(MaplePacketCreator.sendGhostPoint(type, inc));
    }

    public void setCombo(final short combo) {
        this.combo = combo;
    }

    public void setLastCombo(final long combo) {
        this.lastCombo = combo;
    }

    public final long getKeyDownSkill_Time() {
        return keydown_skill;
    }

    public void setKeyDownSkill_Time(final long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }

    public void checkBerserk() {
        if (
            /* job != 132 || */ getLastBerserkTime() < 0 || getLastBerserkTime() + 10000 > System.currentTimeMillis()) {
            return;
        }
        final ISkill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = getSkillLevel(BerserkX);
        if (skilllevel >= 1 && map != null) {
            setLastBerserkTime(System.currentTimeMillis());
            final MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            stats.setBersek((stats.getHp() * 100) / stats.getCurrentMaxHp() <= ampStat.getX());
            client.getSession()
                    .write(MaplePacketCreator.showOwnBuffEffect(1320006, 1, (byte) (stats.isBersek() ? 1 : 0)));
            map.broadcastMessage(
                    this,
                    MaplePacketCreator.showBuffeffect(getId(), 1320006, 1, (byte) (stats.isBersek() ? 1 : 0)),
                    false);
        } else {
            setLastBerserkTime(-1);
        }
    }

    private void prepareBeholderEffect() {
        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(false);
        }
        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(false);
        }
        ISkill bHealing = SkillFactory.getSkill(1320008);
        final int bHealingLvl = getSkillLevel(bHealing);
        final int berserkLvl = getSkillLevel(SkillFactory.getSkill(1320006));

        if (bHealingLvl > 0) {
            final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
            int healInterval = healEffect.getX() * 1000;
            beholderHealingSchedule = BuffTimer.getInstance()
                    .register(
                            () -> {
                                int remhppercentage = (int) Math.ceil(
                                        (getStat().getHp() * 100.0) / getStat().getMaxHp());
                                if (berserkLvl == 0 || remhppercentage >= berserkLvl + 10) {
                                    addHP(healEffect.getHp());
                                }
                                client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
                                map.broadcastMessage(MaplePacketCreator.summonSkill(getId(), 1321007, 5));
                                map.broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.showBuffeffect(getId(), 1321007, 2),
                                        false);
                            },
                            healInterval,
                            healInterval);
        }
        ISkill bBuff = SkillFactory.getSkill(1320009);
        final int bBuffLvl = getSkillLevel(bBuff);
        if (bBuffLvl > 0) {
            final MapleStatEffect buffEffect = bBuff.getEffect(bBuffLvl);
            int buffInterval = buffEffect.getX() * 1000;
            beholderBuffSchedule = BuffTimer.getInstance()
                    .register(
                            () -> {
                                buffEffect.applyTo(MapleCharacter.this);
                                client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
                                map.broadcastMessage(
                                        MaplePacketCreator.summonSkill(getId(), 1321007, Randomizer.nextInt(3) + 6));
                                map.broadcastMessage(
                                        MapleCharacter.this,
                                        MaplePacketCreator.showBuffeffect(getId(), 1321007, 2),
                                        false);
                            },
                            buffInterval,
                            buffInterval);
        }
    }

    public String getChalkboard() {
        return chalkText;
    }

    public void setChalkboard(String text) {
        this.chalkText = text;
        map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void dropMessage(int type, String message) {
        dropMessage(type, message, false);
    }

    public void dropMessage(int type, String message, boolean mappp) {
        if (type == -1) {
            client.getSession().write(UIPacket.getTopMsg(message));
        } else if (type == -2) {
            client.getSession().write(PlayerShopPacket.shopChat(message, 0));
        } else if (type == -3) {
            if (mappp) {
                map.broadcastMessage(MaplePacketCreator.getChatText(getId(), message, false, 1));
            } else {
                client.getSession().write(MaplePacketCreator.getChatText(getId(), message, false, 1));
            }
        } else {
            client.getSession().write(MaplePacketCreator.serverNotice(type, message));
        }
    }

    public void setPlayerShop(IMaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public int getConversation() {
        return conversation_status.get();
    }

    public void setConversation(int inst) {
        this.conversation_status.set(inst);
    }

    public void setCarnivalParty(MapleCarnivalParty party) {
        carnivalParty = party;
    }

    public void addCP(int ammount) {
        totalCP += ammount;
        availableCP += ammount;
    }

    public void useCP(int ammount) {
        availableCP -= ammount;
    }

    public void resetCP() {
        totalCP = 0;
        availableCP = 0;
    }

    public void addCarnivalRequest(MapleCarnivalChallenge request) {
        pendingCarnivalRequests.add(request);
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return pendingCarnivalRequests.pollLast();
    }

    public void clearCarnivalRequests() {
        pendingCarnivalRequests = new LinkedList<>();
    }

    public void startMonsterCarnival(final int enemyavailable, final int enemytotal) {
        client.getSession().write(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
    }

    public void CPUpdate(final boolean party, final int available, final int total, final int team) {
        client.getSession().write(MonsterCarnivalPacket.CPUpdate(party, available, total, team));
    }

    public void playerDiedCPQ(final String name, final int lostCP, final int team) {
        client.getSession().write(MonsterCarnivalPacket.playerDiedMessage(name, lostCP, team));
    }

    public void modifyAchievementCSPoints(int type, int quantity) {
        switch (type) {
            case 1:
            case 4:
                nx_credit += quantity;
                break;
            case 2:
                maple_points += quantity;
                break;
        }
    }

    public int getEXPMod() {
        return stats.getExpMod();
    }

    public int getDropMod() {
        return stats.getDropMod();
    }

    public int getCashMod() {
        return stats.getCashMod();
    }

    public CashShop getCashInventory() {
        return cs;
    }

    public void removeAll(int id) {
        MapleInventoryType type = GameConstants.getInventoryType(id);
        int possessed = getInventory(type).countById(id);

        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, id, possessed, true, false);
            getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }

    public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        List<MapleRing> crings = new ArrayList<>();
        List<MapleRing> frings = new ArrayList<>();
        List<MapleRing> mrings = new ArrayList<>();
        MapleRing ring;
        for (Item item : equipped) {
            if (item.getRing() != null) {
                ring = item.getRing();
                ring.setEquipped(true);
                if (GameConstants.isFriendshipRing(item.getItemId()) || GameConstants.isCrushRing(item.getItemId())) {
                    if (equip) {
                        if (GameConstants.isCrushRing(item.getItemId())) {
                            crings.add(ring);
                        } else if (GameConstants.isFriendshipRing(item.getItemId())) {
                            frings.add(ring);
                        } else if (GameConstants.isMarriageRing(item.getItemId())) {
                            mrings.add(ring);
                        }
                    } else {
                        if (crings.isEmpty() && GameConstants.isCrushRing(item.getItemId())) {
                            crings.add(ring);
                        } else if (frings.isEmpty() && GameConstants.isFriendshipRing(item.getItemId())) {
                            frings.add(ring);
                        } else if (mrings.isEmpty() && GameConstants.isMarriageRing(item.getItemId())) {
                            mrings.add(ring);
                        } // for 3rd person the actual slot doesnt matter, so
                        // we'll use this to have both shirt/ring same?
                        // however there seems to be something else behind
                        // this, will have to sniff someone with shirt and
                        // ring, or more conveniently 3-4 of those
                    }
                }
            }
        }
        if (equip) {
            iv = getInventory(MapleInventoryType.EQUIP);
            for (IItem item : iv.list()) {
                if (item.getRing() != null && GameConstants.isCrushRing(item.getItemId())) {
                    ring = item.getRing();
                    ring.setEquipped(false);
                    if (GameConstants.isFriendshipRing(item.getItemId())) {
                        frings.add(ring);
                    } else if (GameConstants.isCrushRing(item.getItemId())) {
                        crings.add(ring);
                    } else if (GameConstants.isMarriageRing(item.getItemId())) {
                        mrings.add(ring);
                    }
                }
            }
        }
        frings.sort(new MapleRing.RingComparator());
        crings.sort(new MapleRing.RingComparator());
        mrings.sort(new MapleRing.RingComparator());
        return new Triple<>(crings, frings, mrings);
    }

    public int getFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(getPosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public int getCoconutTeam() {
        return coconut_team;
    }

    public void setCoconutTeam(int team) {
        coconut_team = team;
    }

    public void spawnPet(byte slot) {
        spawnPet(slot, false, true);
    }

    public void spawnPet(byte slot, boolean lead) {
        spawnPet(slot, lead, true);
    }

    public void spawnPet(byte slot, boolean lead, boolean broadcast) {
        final IItem item = getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item == null || item.getItemId() > 5000100 || item.getItemId() < 5000000) {
            return;
        }
        switch (item.getItemId()) {
            case 5000047:
            case 5000028: {
                final MaplePet pet = MaplePet.createPet(item.getItemId() + 1, MapleInventoryIdentifier.getInstance());
                if (pet != null) {
                    MapleInventoryManipulator.addById(
                            client, item.getItemId() + 1, (short) 1, item.getOwner(), pet, 45);
                    MapleInventoryManipulator.removeFromSlot(client, MapleInventoryType.CASH, slot, (short) 1, false);
                }
                break;
            }
            default: {
                final MaplePet pet = item.getPet();
                if (pet != null
                        && (item.getItemId() != 5000054 || pet.getSecondsLeft() > 0)
                        && (item.getExpiration() == -1 || item.getExpiration() > System.currentTimeMillis())) {

                    int leadid = 8;
                    if (GameConstants.isKOC(getJob().getId())) {
                        leadid = 10000018;
                    } else if (GameConstants.isAran(getJob().getId())) {
                        leadid = 20000024;
                    } else if (GameConstants.isEvan(getJob().getId())) {
                        leadid = 20011024;
                    } else if (GameConstants.isResist(getJob().getId())) {
                        leadid = 30000024;
                    }
                    if (getSkillLevel(SkillFactory.getSkill(leadid)) == 0 && getPet(0) != null) {
                        unequipPet(getPet(0), false, false);
                    }

                    pet.setPos(getPosition());
                    try {
                        pet.setFh(this.getFH());
                    } catch (NullPointerException e) {
                        pet.setFh(0); // lol, it can be fixed by movement
                    }
                    pet.setStance(0);
                    pet.setSummoned(true);

                    addPet(pet);
                    if (broadcast) {
                        getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, false), true);
                        final List<Pair<MapleStat, Integer>> stats = new ArrayList<>(1);
                        stats.add(new Pair<>(MapleStat.PET, Integer.valueOf(pet.getUniqueId())));
                        client.sendPacket(PetPacket.petStatUpdate(this));
                    }
                }
                break;
            }
        }
        client.getSession().write(PetPacket.emptyStatUpdate());
    }

    public void addMoveMob(int mobid) {
        if (movedMobs.containsKey(mobid)) {
            movedMobs.put(mobid, movedMobs.get(mobid) + 1);
            if (movedMobs.get(mobid) > 30) { // trying to move not null monster
                // = broadcast dead
                for (MapleCharacter chr : getMap().getCharactersThreadsafe()) { // also
                    // broadcast
                    // to
                    // others
                    if (chr.getMoveMobs().containsKey(mobid)) { // they also
                        // tried to move
                        // this mob
                        chr.getClient().getSession().write(MobPacket.killMonster(mobid, 1));
                        chr.getMoveMobs().remove(mobid);
                    }
                }
            }
        } else {
            movedMobs.put(mobid, 1);
        }
    }

    public Map<Integer, Integer> getMoveMobs() {
        return movedMobs;
    }

    public int getFirstLinkMid() {
        for (Integer lm : linkMobs.keySet()) {
            return lm.intValue();
        }
        return 0;
    }

    public void setLinkMid(int lm, int x) {
        linkMobs.put(lm, x);
    }

    public int getDamageIncrease(int lm) {
        if (linkMobs.containsKey(lm)) {
            return linkMobs.get(lm);
        }
        return 0;
    }

    public final void spawnSavedPets() {
        for (int i = 0; i < petStore.length; i++) {
            if (petStore[i] > -1) {
                spawnPet(petStore[i], false, true);
            }
        }
        client.getSession().write(PetPacket.petStatUpdate(this));
        petStore = new byte[]{-1, -1, -1};
    }

    public final byte[] getPetStores() {
        return petStore;
    }

    public void resetStats(final int str, final int dex, final int int_, final int luk) {
        List<Pair<MapleStat, Integer>> stat = new ArrayList<>(2);
        int total = stats.getStr() + stats.getDex() + stats.getLuk() + stats.getInt() + getRemainingAp();

        total -= str;
        stats.setStr((short) str);

        total -= dex;
        stats.setDex((short) dex);

        total -= int_;
        stats.setInt((short) int_);

        total -= luk;
        stats.setLuk((short) luk);

        setRemainingAp(total);

        stat.add(new Pair<>(MapleStat.STR, str));
        stat.add(new Pair<>(MapleStat.DEX, dex));
        stat.add(new Pair<>(MapleStat.INT, int_));
        stat.add(new Pair<>(MapleStat.LUK, luk));
        stat.add(new Pair<>(MapleStat.AVAILABLEAP, Math.min(199, total)));
        client.getSession().write(MaplePacketCreator.updatePlayerStats(stat, false, getJob().getId()));
    }

    public void setPyramidSubway(Event_PyramidSubway ps) {
        this.pyramidSubway = ps;
    }

    public byte getSubCategoryField() {
        return this.subcategory;
    }

    public int itemQuantity(final int itemid) {
        return getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
    }

    public RockPaperScissors getRPS() {
        return rps;
    }

    public void setRPS(RockPaperScissors rps) {
        this.rps = rps;
    }

    public void setNextConsume(long nc) {
        this.nextConsume = nc;
    }

    public void changeChannel(final int channel) {
        final ChannelServer toch = WorldServer.getInstance().getChannel(channel);

        if (channel == client.getChannel() || toch == null || toch.isShutdown()) {
            client.getSession().write(MaplePacketCreator.serverBlocked(1));
            return;
        }
        changeRemoval();

        final ChannelServer ch = WorldServer.getInstance().getChannel(client.getChannel());
        if (getMessenger() != null) {
            MessengerManager.silentLeaveMessenger(getMessenger().getId(), new MapleMessengerCharacter(this));
        }

        WorldServer.getInstance().getChangeChannelData(new CharacterTransfer(this), getId(), channel);
        ch.removePlayer(this);
        client.updateLoginState(LoginState.CHANGE_CHANNEL, client.getSessionIPAddress());
        ServerMigration entry = new ServerMigration(id, client.getAccountData(), client.getSessionIPAddress());
        entry.setCharacterTransfer(new CharacterTransfer(this));

        entry.addBuffsToStorage(getId(), getAllBuffs());
        entry.addCooldownsToStorage(getId(), getCooldowns());
        entry.addDiseaseToStorage(getId(), getAllDiseases());

        WorldServer.getInstance().getMigrationService().putMigrationEntry(entry);
        client.getSession()
                .write(MaplePacketCreator.getChannelChange(
                        Integer.parseInt(toch.getPublicAddress().split(":")[1])));
        getMap().removePlayer(this);
        saveToDB(false, false);

        client.setPlayer(null);
    }

    public void expandInventory(byte type, int amount) {
        final MapleInventory inv = getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) amount);
        client.getSession().write(MaplePacketCreator.getSlotUpdate(type, inv.getSlotLimit()));
    }

    public boolean allowedToTarget(MapleCharacter other) {
        return other != null && (!other.isHidden() || getGMLevel() >= other.getGMLevel());
    }

    public int getFollowId() {
        return follow_id;
    }

    public void setFollowId(int fi) {
        this.follow_id = fi;
        if (fi == 0) {
            this.follow_initiator = false;
            this.follow_on = false;
        }
    }

    public boolean isFollowOn() {
        return follow_on;
    }

    public void setFollowOn(boolean fi) {
        this.follow_on = fi;
    }

    public void setFollowInitiator(boolean fi) {
        this.follow_initiator = fi;
    }

    public void checkFollow() {
        if (follow_id <= 0) {
            return;
        }
        if (follow_on) {
            map.broadcastMessage(MaplePacketCreator.followEffect(id, 0, null));
            map.broadcastMessage(MaplePacketCreator.followEffect(follow_id, 0, null));
        }
        MapleCharacter tt = map.getCharacterById(follow_id);
        client.getSession().write(MaplePacketCreator.getFollowMessage("Follow canceled."));
        if (tt != null) {
            tt.setFollowId(0);
            tt.getClient().getSession().write(MaplePacketCreator.getFollowMessage("Follow canceled."));
        }
        setFollowId(0);
    }

    public void setMarriageId(final int mi) {
        this.marriageId = mi;
    }

    public void setMarriageItemId(final int mi) {
        this.marriageItemId = mi;
    }

    public boolean isStaff() {
        return this.getGMLevel() > ServerConstants.PlayerGMRank.NORMAL.getLevel();
    }

    public boolean startPartyQuest(final int questid) {
        boolean ret = false;
        if (!quests.containsKey(MapleQuest.getInstance(questid)) || !questInfo.containsKey(questid)) {
            final MapleQuestStatus status = getQuestNAdd(MapleQuest.getInstance(questid));
            status.setStatus((byte) 1);
            updateQuest(status);
            switch (questid) {
                case 1300:
                case 1301:
                case 1302: // carnival, ariants.
                    updateInfoQuest(
                            questid,
                            "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
                    break;
                case 1204: // herb town pq
                    updateInfoQuest(
                            questid,
                            "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                case 1206: // ellin pq
                    updateInfoQuest(
                            questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                default:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
            }
            ret = true;
        } // started the quest.
        return ret;
    }

    public String getOneInfo(final int questid, final String key) {
        if (!questInfo.containsKey(questid) || key == null) {
            return null;
        }
        final String[] split = questInfo.get(questid).split(";");
        for (String x : split) {
            final String[] split2 = x.split("="); // should be only 2
            if (split2.length == 2 && split2[0].equals(key)) {
                return split2[1];
            }
        }
        return null;
    }

    public void updateOneInfo(final int questid, final String key, final String value) {
        if (!questInfo.containsKey(questid) || key == null || value == null) {
            return;
        }
        final String[] split = questInfo.get(questid).split(";");
        boolean changed = false;
        final StringBuilder newQuest = new StringBuilder();
        for (String x : split) {
            final String[] split2 = x.split("="); // should be only 2
            if (split2.length != 2) {
                continue;
            }
            if (split2[0].equals(key)) {
                newQuest.append(key).append("=").append(value);
            } else {
                newQuest.append(x);
            }
            newQuest.append(";");
            changed = true;
        }

        updateInfoQuest(
                questid, changed ? newQuest.substring(0, newQuest.toString().length() - 1) : newQuest.toString());
    }

    public void recalcPartyQuestRank(final int questid) {
        if (!startPartyQuest(questid)) {
            final String oldRank = getOneInfo(questid, "rank");
            if (oldRank == null || oldRank.equals("S")) {
                return;
            }
            final String[] split = questInfo.get(questid).split(";");
            String newRank = null;
            if (oldRank.equals("A")) {
                newRank = "S";
            } else if (oldRank.equals("B")) {
                newRank = "A";
            } else if (oldRank.equals("C")) {
                newRank = "B";
            } else if (oldRank.equals("D")) {
                newRank = "C";
            } else if (oldRank.equals("F")) {
                newRank = "D";
            } else {
                return;
            }
            final List<Pair<String, Pair<String, Integer>>> questInfo =
                    MapleQuest.getInstance(questid).getInfoByRank(newRank);
            for (Pair<String, Pair<String, Integer>> q : questInfo) {
                boolean found = false;
                final String val = getOneInfo(questid, q.right.left);
                if (val == null) {
                    return;
                }
                int vall = 0;
                try {
                    vall = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    return;
                }
                if (q.left.equals("less")) {
                    found = vall < q.right.right;
                } else if (q.left.equals("more")) {
                    found = vall > q.right.right;
                } else if (q.left.equals("equal")) {
                    found = vall == q.right.right;
                }
                if (!found) {
                    return;
                }
            }
            // perfectly safe
            updateOneInfo(questid, "rank", newRank);
        }
    }

    @Api
    public void tryPartyQuest(final int questid) {
        try {
            startPartyQuest(questid);
            setPqStartTime(System.currentTimeMillis());
            updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(getOneInfo(questid, "try")) + 1));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("tryPartyQuest error");
        }
    }

    public void endPartyQuest(final int questid) {
        try {
            startPartyQuest(questid);
            if (getPqStartTime() > 0) {
                final long changeTime = System.currentTimeMillis() - getPqStartTime();
                final int mins = (int) (changeTime / 1000 / 60), secs = (int) (changeTime / 1000 % 60);
                final int mins2 = Integer.parseInt(getOneInfo(questid, "min")),
                        secs2 = Integer.parseInt(getOneInfo(questid, "sec"));
                if (mins2 <= 0 || mins < mins2) {
                    updateOneInfo(questid, "min", String.valueOf(mins));
                    updateOneInfo(questid, "sec", String.valueOf(secs));
                    updateOneInfo(questid, "date", DateHelper.getCurrentReadableDate());
                }
                final int newCmp = Integer.parseInt(getOneInfo(questid, "cmp")) + 1;
                updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                updateOneInfo(questid, "CR", String.valueOf((int)
                        Math.ceil((newCmp * 100.0) / Integer.parseInt(getOneInfo(questid, "try")))));
                recalcPartyQuestRank(questid);
                setPqStartTime(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("endPartyQuest error");
        }
    }

    public void havePartyQuest(final int itemId) {
        int questid = 0, index = -1;
        switch (itemId) {
            case 1002798:
                questid = 1200; // henesys
                break;
            case 1072369:
                questid = 1201; // kerning
                break;
            case 1022073:
                questid = 1202; // ludi
                break;
            case 1082232:
                questid = 1203; // orbis
                break;
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574:
                questid = 1204; // herbtown
                index = itemId - 1002571;
                break;
            case 1122010:
                questid = 1205; // magatia
                break;
            case 1032061:
            case 1032060:
                questid = 1206; // ellin
                index = itemId - 1032060;
                break;
            case 3010018:
                questid = 1300; // ariant
                break;
            case 1122007:
                questid = 1301; // carnival
                break;
            case 1122058:
                questid = 1302; // carnival2
                break;
            default:
                return;
        }
        startPartyQuest(questid);
        updateOneInfo(questid, "have" + (index == -1 ? "" : index), "1");
    }

    public void resetStatsByJob(boolean beginnerJob) {
        int baseJob = (beginnerJob ? (job.getId() % 1000) : (job.getId() % 1000 / 100 * 100)); // 1112
        if (baseJob == 100) { // first job = warrior
            resetStats(25, 4, 4, 4);
        } else if (baseJob == 200) {
            resetStats(4, 4, 20, 4);
        } else if (baseJob == 300 || baseJob == 400) {
            resetStats(4, 25, 4, 4);
        } else if (baseJob == 500) {
            resetStats(4, 20, 4, 4);
        }
    }

    public boolean hasSummon() {
        return isHasSummon();
    }

    public void setHasSummon(boolean summ) {
        this.hasSummon = summ;
    }

    public void removeDoor() {
        final MapleDoor door = getDoors().iterator().next();
        for (final MapleCharacter chr : door.getTarget().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (final MapleCharacter chr : door.getTown().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (final MapleDoor destroyDoor : getDoors()) {
            door.getTarget().removeMapObject(destroyDoor);
            door.getTown().removeMapObject(destroyDoor);
        }
        clearDoors();
    }

    public void changeRemoval() {
        changeRemoval(false);
    }

    public void changeRemoval(boolean dc) {
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), client);
        }
        if (getCheatTracker() != null) {
            getCheatTracker().dispose();
        }
        if (!dc) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
            cancelEffectFromBuffStat(MapleBuffStat.REAPER);
            cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (getPyramidSubway() != null) {
            getPyramidSubway().dispose(this);
        }
        if (playerShop != null && !dc) {
            playerShop.removeVisitor(this);
            if (playerShop.isOwner(this)) {
                playerShop.setOpen(true);
            }
        }
        if (!getDoors().isEmpty()) {
            removeDoor();
        }
        NPCScriptManager.getInstance().dispose(client);
    }

    public void updateTick(int newTick) {
        anti_cheat.updateTick(newTick);
    }

    public int currentBattleshipHP() {
        return battleshipHP;
    }

    public void setLoginTime(long login) {
        this.loginTime = login;
    }

    public final boolean canRecover(long now) {
        return lastRecoveryTime > 0 && lastRecoveryTime + 5000 < now;
    }

    private void prepareRecovery() {
        lastRecoveryTime = System.currentTimeMillis();
    }

    public void doRecovery() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.RECOVERY);
        if (bloodEffect == null) {
            lastRecoveryTime = 0;
        } else {
            prepareRecovery();
            if (stats.getHp() >= stats.getCurrentMaxHp()) {
                cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
            } else {
                healHP(bloodEffect.getX());
            }
        }
    }

    public final boolean canBlood(long now) {
        return lastDragonBloodTime > 0 && (lastDragonBloodTime + 4000 < now);
    }

    private void prepareDragonBlood() {
        lastDragonBloodTime = System.currentTimeMillis();
    }

    public void doDragonBlood() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.DRAGONBLOOD);
        if (bloodEffect == null) {
            lastDragonBloodTime = 0;
            return;
        }
        prepareDragonBlood();
        if (stats.getHp() - bloodEffect.getX() <= 1) {
            cancelBuffStats(MapleBuffStat.DRAGONBLOOD);
        } else {
            addHP(-bloodEffect.getX());
            client.getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
            map.broadcastMessage(
                    MapleCharacter.this,
                    MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5),
                    false);
        }
    }

    public List<Integer> getPetItemIgnore(final MaplePet pet) {
        List<Integer> ret = new ArrayList<>(10);
        return ret;
    }

    public void updatePetAuto() {
        String petHp = get("PET_HP");
        String petMp = get("PET_MP");
        if (petHp != null && !petHp.isEmpty()) {
            client.getSession().write(PetPacket.petAutoHP(Integer.parseInt(petHp)));
        }
        if (petMp != null && !petMp.isEmpty()) {
            client.getSession().write(PetPacket.petAutoMP(Integer.parseInt(petMp)));
        }
    }

    public final boolean canFairy(long now) {
        return lastFairyTime > 0 && (lastFairyTime + 3600000 < now);
    }

    public void doFairy() {
        // Not equipped
        // at Max for fairyExp
        // we don't reset it.
        if (stats.isEquippedFairy() && fairyExp < 30) {
            fairyExp += 10;
            lastFairyTime = System.currentTimeMillis();
            client.getSession().write(MaplePacketCreator.fairyPendantMessage(fairyExp));
            if (fairyExp == 30) {
                cancelFairySchedule(false); // Don't reset exp, just leave as
                // max
            }
        } else cancelFairySchedule(!stats.isEquippedFairy()); // Reset exp
    }

    // wear-1hr = 10%, 1hr-2hr already = 20%, 2 hrs + = 30%
    public void startFairySchedule() {
        cancelFairySchedule(true); // Reset exp
        if (stats.isEquippedFairy()) { // Used for login
            lastFairyTime = System.currentTimeMillis();
            client.getSession().write(MaplePacketCreator.fairyPendantMessage(fairyExp));
        }
    }

    public void cancelFairySchedule(boolean onStart) {
        lastFairyTime = 0;
        if (onStart) {
            fairyExp = 10;
        }
    }

    public void clearReports(ReportType type) {
        reports.remove(type);
        changed_reports = true;
    }

    public void clearReports() {
        reports.clear();
        changed_reports = true;
    }

    public final int getReportPoints() {
        int ret = 0;
        for (Integer entry : reports.values()) {
            ret += entry.intValue();
        }
        return ret;
    }

    public final String getReportSummary() {
        StringBuilder ret = new StringBuilder();
        final List<Pair<ReportType, Integer>> offenseList = new ArrayList<>();
        for (Map.Entry<ReportType, Integer> entry : reports.entrySet()) {
            offenseList.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        offenseList.sort((o1, o2) -> {
            int thisVal = o1.getRight();
            int anotherVal = o2.getRight();
            return thisVal == anotherVal ? 0 : thisVal < anotherVal ? 1 : -1;
        });
        for (int x = 0; x < offenseList.size(); x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).left.name()));
            ret.append(": ");
            ret.append(offenseList.get(x).right);
            ret.append(" ");
        }
        return ret.toString();
    }

    public void sendSkills() {
        if (job.isEvan() || getJob().getId() == 900 || getJob().getId() == 910) {
            client.getSession().write(MaplePacketCreator.updateSkill(this.getSkills()));
        }
    }

    public SpeedQuiz getSpeedQuiz() {
        return sq;
    }

    public void setSpeedQuiz(SpeedQuiz sq) {
        this.sq = sq;
    }

    public byte getPortalCount(boolean add) {
        if (add) {
            if (this.portalCount >= Byte.MAX_VALUE) {
                this.portalCount = 1; // Reset back to 1
            } else {
                this.portalCount++;
            }
        }
        return portalCount;
    }

    public void setMorphId(byte id) {
        this.morphId = id;
    }

    public void setStat(MapleStat stat, short value) {
        switch (stat) {
            case STR:
                stats.setStr(value);
                break;
            case DEX:
                stats.setDex(value);
                break;
            case INT:
                stats.setInt(value);
                break;
            case LUK:
                stats.setLuk(value);
                break;
        }
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    @Api
    public void changeMapScripting(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void dcolormsg(int color, String message) {
        client.getSession().write(MaplePacketCreator.getGameMessage(color, message));
    }

    public void checkForDarkSight() {
        if (isActiveBuffedValue(Rogue.DARK_SIGHT)) {
            int incresePercent = 10 + getSkillLevel(BladeLord.ADVANCED_DARK_SIGHT) * 2;
            int randomNumber = new Random().nextInt(100) + 1;
            if (job.isDualblade()) {
                if (incresePercent < randomNumber) {
                    cancelBuffStats(MapleBuffStat.DARKSIGHT);
                }
            } else {
                cancelBuffStats(MapleBuffStat.DARKSIGHT);
            }
        }
    }

    public void addTemporaryData(String key, Object value) {
        temporaryData.put(key, value);
    }

    public Object getTemporaryData(String key) {
        return temporaryData.get(key);
    }

    public Object removeTemporaryData(String key) {
        return temporaryData.remove(key);
    }

    public void clearTemporaryData() {
        temporaryData.clear();
    }

    public int getPossibleReports() {
        return 1;
    }

    @Api
    public boolean isRideFinished() {
        return travelTime < System.currentTimeMillis();
    }

    @Api
    public void setTravelTime(int duration) {
        travelTime = System.currentTimeMillis() + (duration * 1000L);
    }

    public boolean getSuperMegaEnabled() {
        return superMegaEnabled;
    }
}
