package handling.world.helper;

import client.FinishedAchievements;
import client.MapleCharacter;
import client.MapleQuestStatus;
import client.SavedLocations;
import client.WishList;
import client.anticheat.ReportType;
import client.inventory.MapleInventory;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.keymap.MapleKeyLayout;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SavedSkillMacro;
import client.skill.SkillEntry;
import database.AccountData;
import handling.cashshop.CashShop;
import handling.world.buddy.BuddyListEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.MapleStorage;
import server.quest.MapleQuest;

@Slf4j
@Getter
@Setter
public class CharacterTransfer {

    private final List<Integer> famedCharacters = new ArrayList<>();
    private final List<BuddyListEntry> buddies = new ArrayList<>();
    private final Map<Integer, Object> quest = new LinkedHashMap<>();

    private final Map<Integer, Object> customQuests = new LinkedHashMap<>();
    private final Map<Integer, SkillEntry> skills = new LinkedHashMap<>();
    private int characterId;
    private AccountData accountData;
    private int exp;
    private int meso;
    private int hair;
    private int face;
    private int map_id;
    private int guild_id;
    private int party_id;
    private int messenger_id;
    private int mBookCover;
    private int dojo;
    private int nxCredit;
    private int MaplePoints;
    private int mount_item_id;
    private int mount_exp;
    private int marriageId;
    private int battleshipHP;
    private int remainingAp;
    private int maxHp;
    private int maxMp;
    private int hp;
    private int mp;
    private byte channel;
    private byte dojoRecord;
    private byte gender;
    private byte guildRank;
    private byte allianceRank;
    private byte fairyExp;
    private byte buddySize;
    private byte world;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte mount_level;
    private byte mount_Fatigue;
    private byte subcategory;
    private byte morphId;
    private long lastFameTime;
    private long transferTime;
    private long loginTime;
    private long lastRecoveryTime;
    private long lastDragonBloodTime;
    private long lastBerserkTime;
    private long lastHPTime;
    private long lastMPTime;
    private long lastFairyTime;
    private String name;
    private String accountName;
    private String BlessOfFairy;
    private String chalkboard;
    private short level;
    private short fame;
    private short str;
    private short dex;
    private short int_;
    private short luk;
    private short hpApUsed;
    private short job;
    private short occupationId;
    private short occupationEXP;
    private MapleInventory[] inventories;
    private SavedSkillMacro skillMacros;
    private MapleStorage storage;
    private CashShop cashInventory;
    private SavedLocations savedLocations;
    private WishList wishlist;
    private FinishedAchievements finishedAchievements;
    private int[] vipTeleportRocks;
    private int[] regularTeleportRocks;
    private byte[] petStore;
    private Map<Integer, Integer> mapleBookCards = new LinkedHashMap<>();
    private Map<Byte, Integer> reports = new LinkedHashMap<>();
    private MapleKeyLayout keyMap;
    private Map<Integer, String> infoQuest = new LinkedHashMap<>();

    private int remainingSp;
    private EvanSkillPoints evanSP;

    public CharacterTransfer(final MapleCharacter chr) {
        addWorld(chr);
        addCash(chr);
        addBasicStats(chr);
        addMapInfo(chr);
        addGuild(chr);
        addSummon(chr);
        addPets(chr);
        addBuddyList(chr);
        addReports(chr);
        addQuest(chr);
        addMessenger(chr);
        addSkills(chr);
        addLastUpdatedInfo(chr);
        addMount(chr);
        addDojo(chr);
        addMonsterBook(chr);
        this.setFinishedAchievements(chr.getFinishedAchievements());
        this.setChalkboard(chr.getChalkboard());
        this.setStorage(chr.getStorage());
        this.setInventories(chr.getInventories());
        this.setKeyMap(chr.getKeyLayout());
        this.setCashInventory(chr.getCashInventory());
        this.setSavedLocations(chr.getSavedLocations());
        this.setWishlist(chr.getWishlist());
        this.setVipTeleportRocks(chr.getVipTeleportRock().toArray());
        this.setRegularTeleportRocks(chr.getRegTeleportRock().toArray());
        for (final Integer value : chr.getFamedCharacters()) {
            this.getFamedCharacters().add(value);
        }
    }

    private void addMonsterBook(MapleCharacter chr) {
        this.setMBookCover(chr.getMonsterBookCover());
        this.setMapleBookCards(chr.getMonsterBook().getCards());
    }

    private void addDojo(MapleCharacter chr) {
        this.setDojo(chr.getDojo());
        this.setDojoRecord((byte) chr.getDojoRecord());
    }

    private void addMessenger(MapleCharacter chr) {
        if (chr.getMessenger() != null) {
            this.setMessenger_id(chr.getMessenger().getId());
        } else {
            this.setMessenger_id(0);
        }
    }

    private void addQuest(MapleCharacter chr) {
        this.setInfoQuest(chr.getInfoQuest_Map());

        for (final Map.Entry<MapleQuest, MapleQuestStatus> qs :
                chr.getQuest_Map().entrySet()) {
            this.getQuest().put(qs.getKey().getId(), qs.getValue());
        }
    }

    private void addWorld(MapleCharacter chr) {
        this.setChannel((byte) chr.getClient().getChannel());
        this.setWorld(chr.getWorld());
    }

    private void addLastUpdatedInfo(MapleCharacter chr) {
        this.setLoginTime(chr.getLoginTime());
        this.setLastFameTime(chr.getLastFameTime());
        this.setLastRecoveryTime(chr.getLastRecoveryTime());
        this.setLastDragonBloodTime(chr.getLastDragonBloodTime());
        this.setLastBerserkTime(chr.getLastBerserkTime());
        this.setLastHPTime(chr.getLastHPTime());
        this.setLastMPTime(chr.getLastMPTime());
        this.setLastFairyTime(chr.getLastFairyTime());
    }

    private void addReports(MapleCharacter chr) {
        for (Map.Entry<ReportType, Integer> ss : chr.getReports().entrySet()) {
            this.getReports().put(ss.getKey().i, ss.getValue());
        }
    }

    private void addBuddyList(MapleCharacter chr) {
        for (final BuddyListEntry qs : chr.getBuddyList().getBuddies()) {
            this.getBuddies().add(new BuddyListEntry(qs.getName(), qs.getCharacterId(), qs.getGroup(), -1));
        }
        this.setBuddySize(chr.getBuddyCapacity());
    }

    private void addPets(MapleCharacter chr) {
        this.setPetStore(chr.getPetStores());

        boolean uneq = false;
        for (int i = 0; i < this.getPetStore().length; i++) {
            final MaplePet pet = chr.getPet(i);
            if (this.getPetStore()[i] == 0) {
                this.getPetStore()[i] = (byte) -1;
            }
            if (pet != null) {
                uneq = true;
                this.getPetStore()[i] = (byte) Math.max(this.getPetStore()[i], pet.getInventoryPosition());
            }
        }
        if (uneq) {
            chr.unequipAllPets();
        }
    }

    private void addSummon(MapleCharacter chr) {
        this.setMorphId(chr.getMorphId());
        this.setBattleshipHP(chr.currentBattleshipHP());
    }

    private void addGuild(MapleCharacter chr) {
        this.setGuild_id(chr.getGuildId());
        this.setGuildRank(chr.getGuildRank());
        this.setAllianceRank(chr.getAllianceRank());
    }

    private void addMapInfo(MapleCharacter chr) {
        this.setMap_id(chr.getMapId());
        this.setInitialSpawnPoint(chr.getInitialSpawnpoint());
    }

    private void addBasicStats(MapleCharacter chr) {
        this.setCharacterId(chr.getId());
        this.setAccountData(chr.getClient().getAccountData());
        this.accountName = chr.getClient().getAccountData().getName();
        this.setName(chr.getName());
        this.setFame(chr.getFame());
        this.setGender(chr.getGender());
        this.setLevel(chr.getLevel());
        this.setStr(chr.getStat().getStr());
        this.setDex(chr.getStat().getDex());
        this.setInt_(chr.getStat().getInt());
        this.setLuk(chr.getStat().getLuk());
        this.setHp(chr.getStat().getHp());
        this.setMp(chr.getStat().getMp());
        this.setMaxHp(chr.getStat().getMaxHp());
        this.setMaxMp(chr.getStat().getMaxMp());
        this.setExp(chr.getExp());
        this.setHpApUsed(chr.getHpApUsed());
        this.setRemainingAp(chr.getRemainingAp());
        this.setRemainingSp(chr.getRemainingSp());
        this.setMeso(chr.getMeso());
        this.setSkinColor(chr.getSkinColor());
        this.setJob((short) chr.getJob().getId());
        this.setHair(chr.getHair());
        this.setFace(chr.getFace());
        this.setEvanSP(chr.getEvanSP());
        this.setSubcategory(chr.getSubCategoryField());
        this.setMarriageId(chr.getMarriageId());
        this.setFairyExp(chr.getFairyExp());
        this.setParty_id(chr.getPartyId());
    }

    private void addCash(MapleCharacter chr) {
        this.setNxCredit(chr.getCSPoints(1));
        this.setMaplePoints(chr.getCSPoints(2));
    }

    private void addSkills(MapleCharacter chr) {
        for (final Map.Entry<ISkill, SkillEntry> qs : chr.getSkills().entrySet()) {
            this.getSkills().put(qs.getKey().getId(), qs.getValue());
        }

        this.setBlessOfFairy(chr.getBlessOfFairyOrigin());
        this.setSkillMacros(chr.getSkillMacros());
    }

    private void addMount(MapleCharacter chr) {
        final MapleMount mount = chr.getMount();
        this.setMount_item_id(mount.getItemId());
        this.setMount_Fatigue(mount.getFatigue());
        this.setMount_level(mount.getLevel());
        this.setMount_exp(mount.getExp());
        setTransferTime(System.currentTimeMillis());
    }
}
