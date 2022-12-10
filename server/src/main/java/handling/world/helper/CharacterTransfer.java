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

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.anticheat.ReportType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SkillEntry;
import handling.world.buddy.BuddyListEntry;
import lombok.extern.slf4j.Slf4j;
import server.quest.MapleQuest;
import tools.Triple;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CharacterTransfer implements Externalizable {

    private static final long serialVersionUID = 3189097216195936864L;
    private final List<Integer> finishedAchievements = new ArrayList<>();
    private final List<Integer> famedCharacters = new ArrayList<>();
    private final List<BuddyListEntry> buddies = new ArrayList<>();
    private final Map<Integer, Object> quest = new LinkedHashMap<>();

    private final Map<Integer, Object> customQuests = new LinkedHashMap<>();
    private final Map<Integer, SkillEntry> Skills = new LinkedHashMap<>();
    private int character_id;
    private int account_id;
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
    private int maxMp;
    private int maxmp;
    private int hp;
    private int mp;
    private byte channel;
    private byte dojoRecord;
    private byte gender;
    private byte gmLevel;
    private byte guildrank;
    private byte alliancerank;
    private byte fairyExp;
    private byte buddysize;
    private byte world;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte mount_level;
    private byte mount_Fatigue;
    private byte subcategory;
    private byte morphId;
    private long lastfametime;
    private long TranferTime;
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
    private Object inventorys;
    private Object skillmacro;
    private Object storage;
    private Object cs;
    private int[] savedlocation;
    private int[] wishlist;
    private int[] vipTeleportRocks;
    private int[] regularTeleportRocks;
    private byte[] petStore;
    private Map<Integer, Integer> mbook = new LinkedHashMap<>();
    private Map<Byte, Integer> reports = new LinkedHashMap<>();
    private Map<Integer, Triple<Byte, Integer, Byte>> keymap = new LinkedHashMap<>();
    private Map<Integer, String> InfoQuest = new LinkedHashMap<>();

    private int remainingSp;
    private EvanSkillPoints evanSP;

    public CharacterTransfer() {
    }

    public CharacterTransfer(final MapleCharacter chr) {
        this.setCharacter_id(chr.getId());
        this.setAccount_id(chr.getAccountID());
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
        this.setMaxMp(chr.getStat().getMaxHp());
        this.setMaxmp(chr.getStat().getMaxMp());
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
        this.setGuildrank(chr.getGuildRank());
        this.setAlliancerank(chr.getAllianceRank());
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
                this.getPetStore()[i] = (byte) Math.max(this.getPetStore()[i], pet.getInventoryPosition());
            }

        }
        if (uneq) {
            chr.unequipAllPets();
        }
        for (final BuddyListEntry qs : chr.getBuddyList().getBuddies()) {
            this.getBuddies().add(new BuddyListEntry(qs.getName(), qs.getCharacterId(), qs.getGroup(), -1));
        }
        for (Map.Entry<ReportType, Integer> ss : chr.getReports().entrySet()) {
            this.getReports().put(ss.getKey().i, ss.getValue());
        }
        this.setBuddysize(chr.getBuddyCapacity());

        this.setParty_id(chr.getPartyId());

        if (chr.getMessenger() != null) {
            this.setMessenger_id(chr.getMessenger().getId());
        } else {
            this.setMessenger_id(0);
        }

        for (final Integer zz : chr.getFinishedAchievements()) {
            this.getFinishedAchievements().add(zz);
        }

        this.setmBookCover(chr.getMonsterBookCover());
        this.setDojo(chr.getDojo());
        this.setDojoRecord((byte) chr.getDojoRecord());

        this.setInfoQuest(chr.getInfoQuest_Map());

        for (final Map.Entry<MapleQuest, MapleQuestStatus> qs : chr.getQuest_Map().entrySet()) {
            this.getQuest().put(qs.getKey().getId(), qs.getValue());
        }

        this.setMbook(chr.getMonsterBook().getCards());
        this.setInventorys(chr.getInventorys());

        for (final Map.Entry<ISkill, SkillEntry> qs : chr.getSkills().entrySet()) {
            this.getSkills().put(qs.getKey().getId(), qs.getValue());
        }

        this.setBlessOfFairy(chr.getBlessOfFairyOrigin());
        this.setChalkboard(chr.getChalkboard());
        this.setSkillmacro(chr.getMacros());
        this.setKeymap(chr.getKeyLayout().Layout());
        this.setSavedlocation(chr.getSavedLocations());
        this.setWishlist(chr.getWishlist());
        this.setVipTeleportRocks(chr.getVipTeleportRock().toArray());
        this.setRegularTeleportRocks(chr.getRegTeleportRock().toArray());
        for (final Integer zz : chr.getFamedCharacters()) {
            this.getFamedcharacters().add(zz);
        }
        this.setLastfametime(chr.getLastFameTime());
        this.setLoginTime(chr.getLoginTime());
        this.setLastRecoveryTime(chr.getLastRecoveryTime());
        this.setLastDragonBloodTime(chr.getLastDragonBloodTime());
        this.setLastBerserkTime(chr.getLastBerserkTime());
        this.setLastHPTime(chr.getLastHPTime());
        this.setLastMPTime(chr.getLastMPTime());
        this.setLastFairyTime(chr.getLastFairyTime());
        this.setStorage(chr.getStorage());
        this.setCs(chr.getCashInventory());

        final MapleMount mount = chr.getMount();
        this.setMount_item_id(mount.getItemId());
        this.setMount_Fatigue(mount.getFatigue());
        this.setMount_level(mount.getLevel());
        this.setMount_exp(mount.getExp());
        setTranferTime(System.currentTimeMillis());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        this.setCharacter_id(in.readInt());
        this.setAccount_id(in.readInt());
        this.setAccountName(in.readUTF());
        this.setChannel(in.readByte());
        this.setNxCredit(in.readInt());
        this.setMaplePoints(in.readInt());
        this.setName(in.readUTF());
        this.setFame(in.readShort());
        this.setGender(in.readByte());
        this.setLevel(in.readShort());
        this.setStr(in.readShort());
        this.setDex(in.readShort());
        this.setInt_(in.readShort());
        this.setLuk(in.readShort());
        this.setHp(in.readShort());
        this.setMp(in.readShort());
        this.setMaxMp(in.readShort());
        this.setMaxmp(in.readShort());
        this.setExp(in.readInt());
        this.setHpApUsed(in.readShort());
        this.setRemainingAp(in.readInt());
        this.setMeso(in.readInt());
        this.setSkinColor(in.readByte());
        this.setJob(in.readShort());
        this.setHair(in.readInt());
        this.setFace(in.readInt());
        this.setMap_id(in.readInt());
        this.setInitialSpawnPoint(in.readByte());
        this.setWorld(in.readByte());
        this.setGuild_id(in.readInt());
        this.setGuildrank(in.readByte());
        this.setAlliancerank(in.readByte());
        this.setGmLevel(in.readByte());
        this.setPoints(in.readInt());
        if (in.readByte() == 1) {
            this.setBlessOfFairy(in.readUTF());
        } else {
            this.setBlessOfFairy(null);
        }
        if (in.readByte() == 1) {
            this.setChalkboard(in.readUTF());
        } else {
            this.setChalkboard(null);
        }
        this.setSkillmacro(in.readObject());
        this.setLastfametime(in.readLong());
        this.setLoginTime(in.readLong());
        this.setLastRecoveryTime(in.readLong());
        this.setLastDragonBloodTime(in.readLong());
        this.setLastBerserkTime(in.readLong());
        this.setLastHPTime(in.readLong());
        this.setLastMPTime(in.readLong());
        this.setLastFairyTime(in.readLong());
        this.setStorage(in.readObject());
        this.setCs(in.readObject());
        this.setMount_item_id(in.readInt());
        this.setMount_Fatigue(in.readByte());
        this.setMount_level(in.readByte());
        this.setMount_exp(in.readInt());
        this.setParty_id(in.readInt());
        this.setMessenger_id(in.readInt());
        this.setmBookCover(in.readInt());
        this.setDojo(in.readInt());
        this.setDojoRecord(in.readByte());
        this.setInventorys(in.readObject());
        this.setFairyExp(in.readByte());
        this.setSubcategory(in.readByte());
        this.setMorphId(in.readByte());
        this.setMarriageId(in.readInt());
        this.setOccupationId(in.readShort());
        this.setOccupationEXP(in.readShort());
        this.setBattleshipHP(in.readInt());
        this.setReborns(in.readInt());

        final int mbooksize = in.readShort();
        for (int i = 0; i < mbooksize; i++) {
            this.getMbook().put(in.readInt(), in.readInt());
        }

        final int skillsize = in.readShort();
        for (int i = 0; i < skillsize; i++) {
            this.getSkills().put(in.readInt(), new SkillEntry(in.readByte(), in.readByte(), in.readLong()));
        }

        this.setBuddysize(in.readByte());
        final short addedbuddysize = in.readShort();
        for (int i = 0; i < addedbuddysize; i++) {
            getBuddies().add(new BuddyListEntry(in.readUTF(), in.readInt(), in.readUTF(), in.readInt()));
        }

        final int questsize = in.readShort();
        for (int i = 0; i < questsize; i++) {
            this.getQuest().put(in.readInt(), in.readObject());
        }

        final int cquestsize = in.readShort();
        for (int i = 0; i < cquestsize; i++) {
            this.getCustomQuests().put(in.readInt(), in.readObject());
        }

        final int rzsize = in.readByte();
        for (int i = 0; i < rzsize; i++) {
            this.getReports().put(in.readByte(), in.readInt());
        }

        final int achievesize = in.readShort();
        for (int i = 0; i < achievesize; i++) {
            this.getFinishedAchievements().add(in.readInt());
        }

        final int famesize = in.readInt();
        for (int i = 0; i < famesize; i++) {
            this.getFamedcharacters().add(in.readInt());
        }

        final int savesize = in.readShort();
        setSavedlocation(new int[savesize]);
        for (int i = 0; i < savesize; i++) {
            getSavedlocation()[i] = in.readInt();
        }

        final int wsize = in.readShort();
        setWishlist(new int[wsize]);
        for (int i = 0; i < wsize; i++) {
            getWishlist()[i] = in.readInt();
        }

        final int rsize = in.readShort();
        setVipTeleportRocks(new int[rsize]);
        for (int i = 0; i < rsize; i++) {
            getVipTeleportRocks()[i] = in.readInt();
        }

        final int resize = in.readShort();
        setRegularTeleportRocks(new int[resize]);
        for (int i = 0; i < resize; i++) {
            getRegularTeleportRocks()[i] = in.readInt();
        }

        final int infosize = in.readShort();
        for (int i = 0; i < infosize; i++) {
            this.getInfoQuest().put(in.readInt(), in.readUTF());
        }

        final int keysize = in.readInt();
        for (int i = 0; i < keysize; i++) {
            this.getKeymap().put(in.readInt(), new Triple<>(in.readByte(), in.readInt(), in.readByte()));
        }
        this.setPetStore(new byte[in.readByte()]);
        for (int i = 0; i < 3; i++) {
            this.getPetStore()[i] = in.readByte();
        }
        setTranferTime(System.currentTimeMillis());
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(this.getCharacter_id());
        out.writeInt(this.getAccount_id());
        out.writeUTF(this.getAccountName());
        out.writeByte(this.getChannel());
        out.writeInt(this.getNxCredit());
        out.writeInt(this.getMaplePoints());
        out.writeUTF(this.getName());
        out.writeShort(this.getFame());
        out.writeByte(this.getGender());
        out.writeShort(this.getLevel());
        out.writeShort(this.getStr());
        out.writeShort(this.getDex());
        out.writeShort(this.getInt_());
        out.writeShort(this.getLuk());
        out.writeShort(this.getHp());
        out.writeShort(this.getMp());
        out.writeShort(this.getMaxMp());
        out.writeShort(this.getMaxmp());
        out.writeInt(this.getExp());
        out.writeShort(this.getHpApUsed());
        out.writeInt(this.getRemainingAp());
        out.writeInt(this.getMeso());
        out.writeByte(this.getSkinColor());
        out.writeShort(this.getJob());
        out.writeInt(this.getHair());
        out.writeInt(this.getFace());
        out.writeInt(this.getMap_id());
        out.writeByte(this.getInitialSpawnPoint());
        out.writeByte(this.getWorld());
        out.writeInt(this.getGuild_id());
        out.writeByte(this.getGuildrank());
        out.writeByte(this.getAlliancerank());
        out.writeByte(this.getGmLevel());
        out.writeInt(this.getPoints());
        out.writeByte(this.getBlessOfFairy() == null ? 0 : 1);
        if (this.getBlessOfFairy() != null) {
            out.writeUTF(this.getBlessOfFairy());
        }
        out.writeByte(this.getChalkboard() == null ? 0 : 1);
        if (this.getChalkboard() != null) {
            out.writeUTF(this.getChalkboard());
        }
        out.writeByte(0);

        out.writeObject(this.getSkillmacro());
        out.writeLong(this.getLastfametime());
        out.writeLong(this.getLoginTime());
        out.writeLong(this.getLastRecoveryTime());
        out.writeLong(this.getLastDragonBloodTime());
        out.writeLong(this.getLastBerserkTime());
        out.writeLong(this.getLastHPTime());
        out.writeLong(this.getLastMPTime());
        out.writeLong(this.getLastFairyTime());
        out.writeObject(this.getStorage());
        out.writeObject(this.getCs());
        out.writeInt(this.getMount_item_id());
        out.writeByte(this.getMount_Fatigue());
        out.writeByte(this.getMount_level());
        out.writeInt(this.getMount_exp());
        out.writeInt(this.getParty_id());
        out.writeInt(this.getMessenger_id());
        out.writeInt(this.getmBookCover());
        out.writeInt(this.getDojo());
        out.writeByte(this.getDojoRecord());
        out.writeObject(this.getInventorys());
        out.writeByte(this.getFairyExp());
        out.writeByte(this.getSubcategory());
        out.writeByte(this.getMorphId());
        out.writeInt(this.getMarriageId());
        out.writeShort(this.getOccupationId());
        out.writeShort(this.getOccupationEXP());
        out.writeInt(this.getBattleshipHP());
        out.writeInt(this.getReborns());

        out.writeShort(this.getMbook().size());
        for (Map.Entry<Integer, Integer> ms : this.getMbook().entrySet()) {
            out.writeInt(ms.getKey());
            out.writeInt(ms.getValue());
        }

        out.writeShort(this.getSkills().size());
        for (final Map.Entry<Integer, SkillEntry> qs : this.getSkills().entrySet()) {
            out.writeInt(qs.getKey()); // Questid instead of Skill, as it's huge
            // :(
            out.writeByte(qs.getValue().skillevel);
            out.writeByte(qs.getValue().masterlevel);
            out.writeLong(qs.getValue().expiration);
            // Bless of fairy is transported here too.
        }

        out.writeByte(this.getBuddysize());
        out.writeShort(this.getBuddies().size());
        for (final BuddyListEntry qs : this.getBuddies()) {
            out.writeUTF(qs.getName());
            out.writeInt(qs.getCharacterId());
            out.writeUTF(qs.getGroup());
            out.writeInt(-1); // channel
        }

        out.writeShort(this.getQuest().size());
        for (final Map.Entry<Integer, Object> qs : this.getQuest().entrySet()) {
            out.writeInt(qs.getKey()); // Questid instead of MapleQuest, as it's
            // huge :(
            out.writeObject(qs.getValue());
        }
        out.writeShort(this.getCustomQuests().size());
        for (final Map.Entry<Integer, Object> qs : this.getCustomQuests().entrySet()) {
            out.writeInt(qs.getKey());
            out.writeObject(qs.getValue());
        }

        out.writeByte(this.getReports().size());
        for (final Map.Entry<Byte, Integer> qs : this.getReports().entrySet()) {
            out.writeByte(qs.getKey());
            out.writeInt(qs.getValue());
        }

        out.writeShort(this.getFinishedAchievements().size());
        for (final Integer zz : getFinishedAchievements()) {
            out.writeInt(zz.intValue());
        }

        out.writeInt(this.getFamedcharacters().size());
        for (final Integer zz : getFamedcharacters()) {
            out.writeInt(zz.intValue());
        }

        out.writeShort(this.getSavedlocation().length);
        for (int zz : getSavedlocation()) {
            out.writeInt(zz);
        }

        out.writeShort(this.getWishlist().length);
        for (int zz : getWishlist()) {
            out.writeInt(zz);
        }

        out.writeShort(this.getVipTeleportRocks().length);
        for (int zz : getVipTeleportRocks()) {
            out.writeInt(zz);
        }

        out.writeShort(this.getRegularTeleportRocks().length);
        for (int zz : getRegularTeleportRocks()) {
            out.writeInt(zz);
        }

        out.writeShort(this.getInfoQuest().size());
        for (final Map.Entry<Integer, String> qs : this.getInfoQuest().entrySet()) {
            out.writeInt(qs.getKey());
            out.writeUTF(qs.getValue());
        }

        out.writeInt(this.getKeymap().size());
        for (final Map.Entry<Integer, Triple<Byte, Integer, Byte>> qs : this.getKeymap().entrySet()) {
            out.writeInt(qs.getKey());
            out.writeByte(qs.getValue().getLeft());
            out.writeInt(qs.getValue().getMid());
            out.writeByte(qs.getValue().getRight());
        }
        out.writeByte(getPetStore().length);
        for (int i = 0; i < getPetStore().length; i++) {
            out.writeByte(getPetStore()[i]);
        }
    }

    public long getLastfametime() {
        return lastfametime;
    }

    public void setLastfametime(long lastfametime) {
        this.lastfametime = lastfametime;
    }

    public byte getAlliancerank() {
        return alliancerank;
    }

    public void setAlliancerank(byte alliancerank) {
        this.alliancerank = alliancerank;
    }

    public int getMaplePoints() {
        return MaplePoints;
    }

    public void setMaplePoints(int maplePoints) {
        MaplePoints = maplePoints;
    }

    public Map<Integer, Triple<Byte, Integer, Byte>> getKeymap() {
        return keymap;
    }

    public void setKeymap(Map<Integer, Triple<Byte, Integer, Byte>> keymap) {
        this.keymap = keymap;
    }

    public short getFame() {
        return fame;
    }

    public void setFame(short fame) {
        this.fame = fame;
    }

    public List<Integer> getFinishedAchievements() {
        return finishedAchievements;
    }

    public List<Integer> getFamedcharacters() {
        return famedCharacters;
    }

    public List<BuddyListEntry> getBuddies() {
        return buddies;
    }

    public Map<Integer, Object> getQuest() {
        return quest;
    }

    public Map<Integer, Object> getCustomQuests() {
        return customQuests;
    }

    public Map<Integer, SkillEntry> getSkills() {
        return Skills;
    }

    public int getCharacter_id() {
        return character_id;
    }

    public void setCharacter_id(int character_id) {
        this.character_id = character_id;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getMeso() {
        return meso;
    }

    public void setMeso(int meso) {
        this.meso = meso;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getMap_id() {
        return map_id;
    }

    public void setMap_id(int map_id) {
        this.map_id = map_id;
    }

    public int getGuild_id() {
        return guild_id;
    }

    public void setGuild_id(int guild_id) {
        this.guild_id = guild_id;
    }

    public int getParty_id() {
        return party_id;
    }

    public void setParty_id(int party_id) {
        this.party_id = party_id;
    }

    public int getMessenger_id() {
        return messenger_id;
    }

    public void setMessenger_id(int messenger_id) {
        this.messenger_id = messenger_id;
    }

    public int getmBookCover() {
        return mBookCover;
    }

    public void setmBookCover(int mBookCover) {
        this.mBookCover = mBookCover;
    }

    public int getDojo() {
        return dojo;
    }

    public void setDojo(int dojo) {
        this.dojo = dojo;
    }

    public int getNxCredit() {
        return nxCredit;
    }

    public void setNxCredit(int nxCredit) {
        this.nxCredit = nxCredit;
    }

    public int getMount_item_id() {
        return mount_item_id;
    }

    public void setMount_item_id(int mount_item_id) {
        this.mount_item_id = mount_item_id;
    }

    public int getMount_exp() {
        return mount_exp;
    }

    public void setMount_exp(int mount_exp) {
        this.mount_exp = mount_exp;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(int marriageId) {
        this.marriageId = marriageId;
    }

    public int getBattleshipHP() {
        return battleshipHP;
    }

    public void setBattleshipHP(int battleshipHP) {
        this.battleshipHP = battleshipHP;
    }

    public int getReborns() {
        return reborns;
    }

    public void setReborns(int reborns) {
        this.reborns = reborns;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = maxMp;
    }

    public int getMaxmp() {
        return maxmp;
    }

    public void setMaxmp(int maxmp) {
        this.maxmp = maxmp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getDojoRecord() {
        return dojoRecord;
    }

    public void setDojoRecord(byte dojoRecord) {
        this.dojoRecord = dojoRecord;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public byte getGmLevel() {
        return gmLevel;
    }

    public void setGmLevel(byte gmLevel) {
        this.gmLevel = gmLevel;
    }

    public byte getGuildrank() {
        return guildrank;
    }

    public void setGuildrank(byte guildrank) {
        this.guildrank = guildrank;
    }

    public byte getFairyExp() {
        return fairyExp;
    }

    public void setFairyExp(byte fairyExp) {
        this.fairyExp = fairyExp;
    }

    public byte getBuddysize() {
        return buddysize;
    }

    public void setBuddysize(byte buddysize) {
        this.buddysize = buddysize;
    }

    public byte getWorld() {
        return world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public byte getInitialSpawnPoint() {
        return initialSpawnPoint;
    }

    public void setInitialSpawnPoint(byte initialSpawnPoint) {
        this.initialSpawnPoint = initialSpawnPoint;
    }

    public byte getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    public byte getMount_level() {
        return mount_level;
    }

    public void setMount_level(byte mount_level) {
        this.mount_level = mount_level;
    }

    public byte getMount_Fatigue() {
        return mount_Fatigue;
    }

    public void setMount_Fatigue(byte mount_Fatigue) {
        this.mount_Fatigue = mount_Fatigue;
    }

    public byte getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(byte subcategory) {
        this.subcategory = subcategory;
    }

    public byte getMorphId() {
        return morphId;
    }

    public void setMorphId(byte morphId) {
        this.morphId = morphId;
    }

    public long getTranferTime() {
        return TranferTime;
    }

    public void setTranferTime(long tranferTime) {
        TranferTime = tranferTime;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getLastRecoveryTime() {
        return lastRecoveryTime;
    }

    public void setLastRecoveryTime(long lastRecoveryTime) {
        this.lastRecoveryTime = lastRecoveryTime;
    }

    public long getLastDragonBloodTime() {
        return lastDragonBloodTime;
    }

    public void setLastDragonBloodTime(long lastDragonBloodTime) {
        this.lastDragonBloodTime = lastDragonBloodTime;
    }

    public long getLastBerserkTime() {
        return lastBerserkTime;
    }

    public void setLastBerserkTime(long lastBerserkTime) {
        this.lastBerserkTime = lastBerserkTime;
    }

    public long getLastHPTime() {
        return lastHPTime;
    }

    public void setLastHPTime(long lastHPTime) {
        this.lastHPTime = lastHPTime;
    }

    public long getLastMPTime() {
        return lastMPTime;
    }

    public void setLastMPTime(long lastMPTime) {
        this.lastMPTime = lastMPTime;
    }

    public long getLastFairyTime() {
        return lastFairyTime;
    }

    public void setLastFairyTime(long lastFairyTime) {
        this.lastFairyTime = lastFairyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBlessOfFairy() {
        return BlessOfFairy;
    }

    public void setBlessOfFairy(String blessOfFairy) {
        BlessOfFairy = blessOfFairy;
    }

    public String getChalkboard() {
        return chalkboard;
    }

    public void setChalkboard(String chalkboard) {
        this.chalkboard = chalkboard;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public short getStr() {
        return str;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public short getDex() {
        return dex;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public short getInt_() {
        return int_;
    }

    public void setInt_(short int_) {
        this.int_ = int_;
    }

    public short getLuk() {
        return luk;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public short getHpApUsed() {
        return hpApUsed;
    }

    public void setHpApUsed(short hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public short getJob() {
        return job;
    }

    public void setJob(short job) {
        this.job = job;
    }

    public short getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(short occupationId) {
        this.occupationId = occupationId;
    }

    public short getOccupationEXP() {
        return occupationEXP;
    }

    public void setOccupationEXP(short occupationEXP) {
        this.occupationEXP = occupationEXP;
    }

    public Object getInventorys() {
        return inventorys;
    }

    public void setInventorys(Object inventorys) {
        this.inventorys = inventorys;
    }

    public Object getSkillmacro() {
        return skillmacro;
    }

    public void setSkillmacro(Object skillmacro) {
        this.skillmacro = skillmacro;
    }

    public Object getStorage() {
        return storage;
    }

    public void setStorage(Object storage) {
        this.storage = storage;
    }

    public Object getCs() {
        return cs;
    }

    public void setCs(Object cs) {
        this.cs = cs;
    }

    public int[] getSavedlocation() {
        return savedlocation;
    }

    public void setSavedlocation(int[] savedlocation) {
        this.savedlocation = savedlocation;
    }

    public int[] getWishlist() {
        return wishlist;
    }

    public void setWishlist(int[] wishlist) {
        this.wishlist = wishlist;
    }

    public int[] getVipTeleportRocks() {
        return vipTeleportRocks;
    }

    public void setVipTeleportRocks(int[] vipTeleportRocks) {
        this.vipTeleportRocks = vipTeleportRocks;
    }

    public int[] getRegularTeleportRocks() {
        return regularTeleportRocks;
    }

    public void setRegularTeleportRocks(int[] regularTeleportRocks) {
        this.regularTeleportRocks = regularTeleportRocks;
    }

    public byte[] getPetStore() {
        return petStore;
    }

    public void setPetStore(byte[] petStore) {
        this.petStore = petStore;
    }

    public Map<Integer, Integer> getMbook() {
        return mbook;
    }

    public void setMbook(Map<Integer, Integer> mbook) {
        this.mbook = mbook;
    }

    public Map<Byte, Integer> getReports() {
        return reports;
    }

    public void setReports(Map<Byte, Integer> reports) {
        this.reports = reports;
    }

    public Map<Integer, String> getInfoQuest() {
        return InfoQuest;
    }

    public void setInfoQuest(Map<Integer, String> infoQuest) {
        InfoQuest = infoQuest;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public EvanSkillPoints getEvanSP() {
        return evanSP;
    }

    public void setEvanSP(EvanSkillPoints evanSP) {
        this.evanSP = evanSP;
    }
}
