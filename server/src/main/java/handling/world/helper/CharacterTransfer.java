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

package handling.world.helper;

import client.FinishedAchievements;
import client.MapleCharacter;
import client.MapleQuestStatus;
import client.SavedLocations;
import client.SavedSkillMacro;
import client.WishList;
import client.anticheat.ReportType;
import client.inventory.MapleInventory;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.layout.MapleKeyLayout;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SkillEntry;
import database.AccountData;
import handling.world.buddy.BuddyListEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import server.MapleStorage;
import server.cashshop.CashShop;
import server.quest.MapleQuest;

@Slf4j
@Getter
@Setter
public class CharacterTransfer {

    private final List<Integer> famedCharacters = new ArrayList<>();
    private final List<BuddyListEntry> buddies = new ArrayList<>();
    private final Map<Integer, Object> quest = new LinkedHashMap<>();

    private final Map<Integer, Object> customQuests = new LinkedHashMap<>();
    private final Map<Integer, SkillEntry> Skills = new LinkedHashMap<>();
    private int character_id;
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
    private int points;
    private int marriageId;
    private int battleshipHP;
    private int reborns;
    private int remainingAp;
    private int maxHp;
    private int maxMp;
    private int hp;
    private int mp;
    private byte channel;
    private byte dojoRecord;
    private byte gender;
    private byte gmLevel;
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
        this.setCharacter_id(chr.getId());
        this.setAccountData(chr.getClient().getAccountData());
        this.accountName = chr.getClient().getAccountData().getName();
        this.setChannel((byte) chr.getClient().getChannel());
        this.setNxCredit(chr.getCSPoints(1));
        this.setMaplePoints(chr.getCSPoints(2));
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
        this.setMap_id(chr.getMapId());
        this.setInitialSpawnPoint(chr.getInitialSpawnpoint());
        this.setMarriageId(chr.getMarriageId());
        this.setWorld(chr.getWorld());
        this.setGuild_id(chr.getGuildId());
        this.setGuildRank(chr.getGuildRank());
        this.setAllianceRank(chr.getAllianceRank());
        this.setGmLevel((byte) chr.getGMLevel());
        this.setPoints(chr.getPoints());
        this.setFairyExp(chr.getFairyExp());
        this.setPetStore(chr.getPetStores());
        this.setSubcategory(chr.getSubCategoryField());
        this.setMorphId(chr.getMorphId());
        this.setBattleshipHP(chr.currentBattleshipHP());
        this.setEvanSP(chr.getEvanSP());
        boolean uneq = false;
        for (int i = 0; i < this.getPetStore().length; i++) {
            final MaplePet pet = chr.getPet(i);
            if (this.getPetStore()[i] == 0) {
                this.getPetStore()[i] = (byte) -1;
            }
            if (pet != null) {
                uneq = true;
                this.getPetStore()[i] =
                        (byte) Math.max(this.getPetStore()[i], pet.getInventoryPosition());
            }
        }
        if (uneq) {
            chr.unequipAllPets();
        }
        for (final BuddyListEntry qs : chr.getBuddyList().getBuddies()) {
            this.getBuddies()
                    .add(new BuddyListEntry(qs.getName(), qs.getCharacterId(), qs.getGroup(), -1));
        }
        for (Map.Entry<ReportType, Integer> ss : chr.getReports().entrySet()) {
            this.getReports().put(ss.getKey().i, ss.getValue());
        }
        this.setBuddySize(chr.getBuddyCapacity());

        this.setParty_id(chr.getPartyId());

        if (chr.getMessenger() != null) {
            this.setMessenger_id(chr.getMessenger().getId());
        } else {
            this.setMessenger_id(0);
        }

        this.setFinishedAchievements(chr.getFinishedAchievements());

        this.setMBookCover(chr.getMonsterBookCover());
        this.setDojo(chr.getDojo());
        this.setDojoRecord((byte) chr.getDojoRecord());

        this.setInfoQuest(chr.getInfoQuest_Map());

        for (final Map.Entry<MapleQuest, MapleQuestStatus> qs : chr.getQuest_Map().entrySet()) {
            this.getQuest().put(qs.getKey().getId(), qs.getValue());
        }

        this.setMapleBookCards(chr.getMonsterBook().getCards());
        this.setInventories(chr.getInventories());

        for (final Map.Entry<ISkill, SkillEntry> qs : chr.getSkills().entrySet()) {
            this.getSkills().put(qs.getKey().getId(), qs.getValue());
        }

        this.setBlessOfFairy(chr.getBlessOfFairyOrigin());
        this.setChalkboard(chr.getChalkboard());
        this.setSkillMacros(chr.getSkillMacros());
        this.setKeyMap(chr.getKeyLayout());
        this.setSavedLocations(chr.getSavedLocations());
        this.setWishlist(chr.getWishlist());
        this.setVipTeleportRocks(chr.getVipTeleportRock().toArray());
        this.setRegularTeleportRocks(chr.getRegTeleportRock().toArray());
        for (final Integer zz : chr.getFamedCharacters()) {
            this.getFamedCharacters().add(zz);
        }
        this.setLastFameTime(chr.getLastFameTime());
        this.setLoginTime(chr.getLoginTime());
        this.setLastRecoveryTime(chr.getLastRecoveryTime());
        this.setLastDragonBloodTime(chr.getLastDragonBloodTime());
        this.setLastBerserkTime(chr.getLastBerserkTime());
        this.setLastHPTime(chr.getLastHPTime());
        this.setLastMPTime(chr.getLastMPTime());
        this.setLastFairyTime(chr.getLastFairyTime());
        this.setStorage(chr.getStorage());
        this.setCashInventory(chr.getCashInventory());

        final MapleMount mount = chr.getMount();
        this.setMount_item_id(mount.getItemId());
        this.setMount_Fatigue(mount.getFatigue());
        this.setMount_level(mount.getLevel());
        this.setMount_exp(mount.getExp());
        setTransferTime(System.currentTimeMillis());
    }
}
