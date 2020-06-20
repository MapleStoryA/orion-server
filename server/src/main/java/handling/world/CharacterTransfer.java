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

package handling.world;

import client.*;
import client.anticheat.ReportType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import handling.world.buddy.BuddyListEntry;
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

public class CharacterTransfer implements Externalizable {

  private static final long serialVersionUID = 3189097216195936864L;
  public int characterid, accountid, exp, meso, hair, face, mapid, guildid, partyid, messengerid, mBookCover, dojo,
      nxCredit, MaplePoints, mount_itemid, mount_exp, points, marriageId, battleshipHP, reborns,
      remainingAp, maxhp, maxmp, hp, mp;
  public byte channel, dojoRecord, gender, gmLevel, guildrank, alliancerank, fairyExp, buddysize, world,
      initialSpawnPoint, skinColor, mount_level, mount_Fatigue, subcategory, morphId;
  public long lastfametime, TranferTime, loginTime, lastRecoveryTime, lastDragonBloodTime, lastBerserkTime,
      lastHPTime, lastMPTime, lastFairyTime;
  public String name, accountname, BlessOfFairy, chalkboard;
  public short level, fame, str, dex, int_, luk, hpApUsed, job, occupationId, occupationEXP;
  public Object inventorys, skillmacro, storage, cs;
  public int[] savedlocation, wishlist, rocks, regrocks;
  public byte[] petStore;
  public Map<Integer, Integer> mbook = new LinkedHashMap<>();
  public Map<Byte, Integer> reports = new LinkedHashMap<>();
  public Map<Integer, Triple<Byte, Integer, Byte>> keymap = new LinkedHashMap<>();
  public final List<Integer> finishedAchievements = new ArrayList<>(), famedcharacters = new ArrayList<>();
  public final List<BuddyListEntry> buddies = new ArrayList<>();
  public final Map<Integer, Object> Quest = new LinkedHashMap<>(); // Questid
  // instead
  // of
  // MapleQuest,
  // as
  // it's
  // huge.
  // Cant
  // be
  // transporting
  // MapleQuest.java
  public final Map<Integer, Object> customQuests = new LinkedHashMap<>();
  public Map<Integer, String> InfoQuest = new LinkedHashMap<>();
  public final Map<Integer, SkillEntry> Skills = new LinkedHashMap<>(); // Skillid
  // instead
  // of
  // Skill.java,
  // as
  // it's
  // huge.
  // Cant
  // be
  // transporting
  // Skill.java
  // and
  // MapleStatEffect.java
  public int remainingSp;
  public EvanSkillPoints evanSP;

  public CharacterTransfer() {
  }

  public CharacterTransfer(final MapleCharacter chr) {
    this.characterid = chr.getId();
    this.accountid = chr.getAccountID();
    this.accountname = chr.getClient().getAccountName();
    this.channel = (byte) chr.getClient().getChannel();
    this.nxCredit = chr.getCSPoints(1);
    this.MaplePoints = chr.getCSPoints(2);
    this.name = chr.getName();
    this.fame = chr.getFame();
    this.gender = (byte) chr.getGender();
    this.level = chr.getLevel();
    this.str = chr.getStat().getStr();
    this.dex = chr.getStat().getDex();
    this.int_ = chr.getStat().getInt();
    this.luk = chr.getStat().getLuk();
    this.hp = chr.getStat().getHp();
    this.mp = chr.getStat().getMp();
    this.maxhp = chr.getStat().getMaxHp();
    this.maxmp = chr.getStat().getMaxMp();
    this.exp = chr.getExp();
    this.hpApUsed = chr.getHpApUsed();
    this.remainingAp = chr.getRemainingAp();
    this.remainingSp = chr.getRemainingSp();
    this.meso = chr.getMeso();
    this.skinColor = chr.getSkinColor();
    this.job = chr.getJob();
    this.hair = chr.getHair();
    this.face = chr.getFace();
    this.mapid = chr.getMapId();
    this.initialSpawnPoint = chr.getInitialSpawnpoint();
    this.marriageId = chr.getMarriageId();
    this.world = chr.getWorld();
    this.guildid = chr.getGuildId();
    this.guildrank = (byte) chr.getGuildRank();
    this.alliancerank = (byte) chr.getAllianceRank();
    this.gmLevel = (byte) chr.getGMLevel();
    this.points = chr.getPoints();
    this.fairyExp = chr.getFairyExp();
    this.petStore = chr.getPetStores();
    this.subcategory = chr.getSubCategoryField();
    this.morphId = chr.getMorphId();
    this.battleshipHP = chr.currentBattleshipHP();
    this.evanSP = chr.getEvanSP();
    boolean uneq = false;
    for (int i = 0; i < this.petStore.length; i++) {
      final MaplePet pet = chr.getPet(i);
      if (this.petStore[i] == 0) {
        this.petStore[i] = (byte) -1;
      }
      if (pet != null) {
        uneq = true;
        this.petStore[i] = (byte) Math.max(this.petStore[i], pet.getInventoryPosition());
      }

    }
    if (uneq) {
      chr.unequipAllPets();
    }
    for (final BuddyListEntry qs : chr.getBuddylist().getBuddies()) {
      this.buddies.add(new BuddyListEntry(qs.getName(), qs.getCharacterId(), qs.getGroup(), -1));
    }
    for (Map.Entry<ReportType, Integer> ss : chr.getReports().entrySet()) {
      this.reports.put(ss.getKey().i, ss.getValue());
    }
    this.buddysize = chr.getBuddyCapacity();

    this.partyid = chr.getPartyId();

    if (chr.getMessenger() != null) {
      this.messengerid = chr.getMessenger().getId();
    } else {
      this.messengerid = 0;
    }

    for (final Integer zz : chr.getFinishedAchievements()) {
      this.finishedAchievements.add(zz);
    }

    this.mBookCover = chr.getMonsterBookCover();
    this.dojo = chr.getDojo();
    this.dojoRecord = (byte) chr.getDojoRecord();

    this.InfoQuest = chr.getInfoQuest_Map();

    for (final Map.Entry<MapleQuest, MapleQuestStatus> qs : chr.getQuest_Map().entrySet()) {
      this.Quest.put(qs.getKey().getId(), qs.getValue());
    }

    this.mbook = chr.getMonsterBook().getCards();
    this.inventorys = chr.getInventorys();

    for (final Map.Entry<ISkill, SkillEntry> qs : chr.getSkills().entrySet()) {
      this.Skills.put(qs.getKey().getId(), qs.getValue());
    }

    this.BlessOfFairy = chr.getBlessOfFairyOrigin();
    this.chalkboard = chr.getChalkboard();
    this.skillmacro = chr.getMacros();
    this.keymap = chr.getKeyLayout().Layout();
    this.savedlocation = chr.getSavedLocations();
    this.wishlist = chr.getWishlist();
    this.rocks = chr.getRocks();
    this.regrocks = chr.getRegRocks();
    for (final Integer zz : chr.getFamedCharacters()) {
      this.famedcharacters.add(zz);
    }
    this.lastfametime = chr.getLastFameTime();
    this.loginTime = chr.getLoginTime();
    this.lastRecoveryTime = chr.getLastRecoveryTime();
    this.lastDragonBloodTime = chr.getLastDragonBloodTime();
    this.lastBerserkTime = chr.getLastBerserkTime();
    this.lastHPTime = chr.getLastHPTime();
    this.lastMPTime = chr.getLastMPTime();
    this.lastFairyTime = chr.getLastFairyTime();
    this.storage = chr.getStorage();
    this.cs = chr.getCashInventory();

    final MapleMount mount = chr.getMount();
    this.mount_itemid = mount.getItemId();
    this.mount_Fatigue = mount.getFatigue();
    this.mount_level = mount.getLevel();
    this.mount_exp = mount.getExp();
    TranferTime = System.currentTimeMillis();
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    this.characterid = in.readInt();
    this.accountid = in.readInt();
    this.accountname = in.readUTF();
    this.channel = in.readByte();
    this.nxCredit = in.readInt();
    this.MaplePoints = in.readInt();
    this.name = in.readUTF();
    this.fame = in.readShort();
    this.gender = in.readByte();
    this.level = in.readShort();
    this.str = in.readShort();
    this.dex = in.readShort();
    this.int_ = in.readShort();
    this.luk = in.readShort();
    this.hp = in.readShort();
    this.mp = in.readShort();
    this.maxhp = in.readShort();
    this.maxmp = in.readShort();
    this.exp = in.readInt();
    this.hpApUsed = in.readShort();
    this.remainingAp = in.readInt();
    this.meso = in.readInt();
    this.skinColor = in.readByte();
    this.job = in.readShort();
    this.hair = in.readInt();
    this.face = in.readInt();
    this.mapid = in.readInt();
    this.initialSpawnPoint = in.readByte();
    this.world = in.readByte();
    this.guildid = in.readInt();
    this.guildrank = in.readByte();
    this.alliancerank = in.readByte();
    this.gmLevel = in.readByte();
    this.points = in.readInt();
    if (in.readByte() == 1) {
      this.BlessOfFairy = in.readUTF();
    } else {
      this.BlessOfFairy = null;
    }
    if (in.readByte() == 1) {
      this.chalkboard = in.readUTF();
    } else {
      this.chalkboard = null;
    }
    this.skillmacro = in.readObject();
    this.lastfametime = in.readLong();
    this.loginTime = in.readLong();
    this.lastRecoveryTime = in.readLong();
    this.lastDragonBloodTime = in.readLong();
    this.lastBerserkTime = in.readLong();
    this.lastHPTime = in.readLong();
    this.lastMPTime = in.readLong();
    this.lastFairyTime = in.readLong();
    this.storage = in.readObject();
    this.cs = in.readObject();
    this.mount_itemid = in.readInt();
    this.mount_Fatigue = in.readByte();
    this.mount_level = in.readByte();
    this.mount_exp = in.readInt();
    this.partyid = in.readInt();
    this.messengerid = in.readInt();
    this.mBookCover = in.readInt();
    this.dojo = in.readInt();
    this.dojoRecord = in.readByte();
    this.inventorys = in.readObject();
    this.fairyExp = in.readByte();
    this.subcategory = in.readByte();
    this.morphId = in.readByte();
    this.marriageId = in.readInt();
    this.occupationId = in.readShort();
    this.occupationEXP = in.readShort();
    this.battleshipHP = in.readInt();
    this.reborns = in.readInt();

    final int mbooksize = in.readShort();
    for (int i = 0; i < mbooksize; i++) {
      this.mbook.put(in.readInt(), in.readInt());
    }

    final int skillsize = in.readShort();
    for (int i = 0; i < skillsize; i++) {
      this.Skills.put(in.readInt(), new SkillEntry(in.readByte(), in.readByte(), in.readLong()));
    }

    this.buddysize = in.readByte();
    final short addedbuddysize = in.readShort();
    for (int i = 0; i < addedbuddysize; i++) {
      buddies.add(new BuddyListEntry(in.readUTF(), in.readInt(), in.readUTF(), in.readInt()));
    }

    final int questsize = in.readShort();
    for (int i = 0; i < questsize; i++) {
      this.Quest.put(in.readInt(), in.readObject());
    }

    final int cquestsize = in.readShort();
    for (int i = 0; i < cquestsize; i++) {
      this.customQuests.put(in.readInt(), in.readObject());
    }

    final int rzsize = in.readByte();
    for (int i = 0; i < rzsize; i++) {
      this.reports.put(in.readByte(), in.readInt());
    }

    final int achievesize = in.readShort();
    for (int i = 0; i < achievesize; i++) {
      this.finishedAchievements.add(in.readInt());
    }

    final int famesize = in.readInt();
    for (int i = 0; i < famesize; i++) {
      this.famedcharacters.add(in.readInt());
    }

    final int savesize = in.readShort();
    savedlocation = new int[savesize];
    for (int i = 0; i < savesize; i++) {
      savedlocation[i] = in.readInt();
    }

    final int wsize = in.readShort();
    wishlist = new int[wsize];
    for (int i = 0; i < wsize; i++) {
      wishlist[i] = in.readInt();
    }

    final int rsize = in.readShort();
    rocks = new int[rsize];
    for (int i = 0; i < rsize; i++) {
      rocks[i] = in.readInt();
    }

    final int resize = in.readShort();
    regrocks = new int[resize];
    for (int i = 0; i < resize; i++) {
      regrocks[i] = in.readInt();
    }

    final int infosize = in.readShort();
    for (int i = 0; i < infosize; i++) {
      this.InfoQuest.put(in.readInt(), in.readUTF());
    }

    final int keysize = in.readInt();
    for (int i = 0; i < keysize; i++) {
      this.keymap.put(in.readInt(), new Triple<>(in.readByte(), in.readInt(), in.readByte()));
    }
    this.petStore = new byte[in.readByte()];
    for (int i = 0; i < 3; i++) {
      this.petStore[i] = in.readByte();
    }
    TranferTime = System.currentTimeMillis();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeInt(this.characterid);
    out.writeInt(this.accountid);
    out.writeUTF(this.accountname);
    out.writeByte(this.channel);
    out.writeInt(this.nxCredit);
    out.writeInt(this.MaplePoints);
    out.writeUTF(this.name);
    out.writeShort(this.fame);
    out.writeByte(this.gender);
    out.writeShort(this.level);
    out.writeShort(this.str);
    out.writeShort(this.dex);
    out.writeShort(this.int_);
    out.writeShort(this.luk);
    out.writeShort(this.hp);
    out.writeShort(this.mp);
    out.writeShort(this.maxhp);
    out.writeShort(this.maxmp);
    out.writeInt(this.exp);
    out.writeShort(this.hpApUsed);
    out.writeInt(this.remainingAp);
    out.writeInt(this.meso);
    out.writeByte(this.skinColor);
    out.writeShort(this.job);
    out.writeInt(this.hair);
    out.writeInt(this.face);
    out.writeInt(this.mapid);
    out.writeByte(this.initialSpawnPoint);
    out.writeByte(this.world);
    out.writeInt(this.guildid);
    out.writeByte(this.guildrank);
    out.writeByte(this.alliancerank);
    out.writeByte(this.gmLevel);
    out.writeInt(this.points);
    out.writeByte(this.BlessOfFairy == null ? 0 : 1);
    if (this.BlessOfFairy != null) {
      out.writeUTF(this.BlessOfFairy);
    }
    out.writeByte(this.chalkboard == null ? 0 : 1);
    if (this.chalkboard != null) {
      out.writeUTF(this.chalkboard);
    }
    out.writeByte(0);

    out.writeObject(this.skillmacro);
    out.writeLong(this.lastfametime);
    out.writeLong(this.loginTime);
    out.writeLong(this.lastRecoveryTime);
    out.writeLong(this.lastDragonBloodTime);
    out.writeLong(this.lastBerserkTime);
    out.writeLong(this.lastHPTime);
    out.writeLong(this.lastMPTime);
    out.writeLong(this.lastFairyTime);
    out.writeObject(this.storage);
    out.writeObject(this.cs);
    out.writeInt(this.mount_itemid);
    out.writeByte(this.mount_Fatigue);
    out.writeByte(this.mount_level);
    out.writeInt(this.mount_exp);
    out.writeInt(this.partyid);
    out.writeInt(this.messengerid);
    out.writeInt(this.mBookCover);
    out.writeInt(this.dojo);
    out.writeByte(this.dojoRecord);
    out.writeObject(this.inventorys);
    out.writeByte(this.fairyExp);
    out.writeByte(this.subcategory);
    out.writeByte(this.morphId);
    out.writeInt(this.marriageId);
    out.writeShort(this.occupationId);
    out.writeShort(this.occupationEXP);
    out.writeInt(this.battleshipHP);
    out.writeInt(this.reborns);

    out.writeShort(this.mbook.size());
    for (Map.Entry<Integer, Integer> ms : this.mbook.entrySet()) {
      out.writeInt(ms.getKey());
      out.writeInt(ms.getValue());
    }

    out.writeShort(this.Skills.size());
    for (final Map.Entry<Integer, SkillEntry> qs : this.Skills.entrySet()) {
      out.writeInt(qs.getKey()); // Questid instead of Skill, as it's huge
      // :(
      out.writeByte(qs.getValue().skillevel);
      out.writeByte(qs.getValue().masterlevel);
      out.writeLong(qs.getValue().expiration);
      // Bless of fairy is transported here too.
    }

    out.writeByte(this.buddysize);
    out.writeShort(this.buddies.size());
    for (final BuddyListEntry qs : this.buddies) {
      out.writeUTF(qs.getName());
      out.writeInt(qs.getCharacterId());
      out.writeUTF(qs.getGroup());
      out.writeInt(-1); // channel
    }

    out.writeShort(this.Quest.size());
    for (final Map.Entry<Integer, Object> qs : this.Quest.entrySet()) {
      out.writeInt(qs.getKey()); // Questid instead of MapleQuest, as it's
      // huge :(
      out.writeObject(qs.getValue());
    }
    out.writeShort(this.customQuests.size());
    for (final Map.Entry<Integer, Object> qs : this.customQuests.entrySet()) {
      out.writeInt(qs.getKey());
      out.writeObject(qs.getValue());
    }

    out.writeByte(this.reports.size());
    for (final Map.Entry<Byte, Integer> qs : this.reports.entrySet()) {
      out.writeByte(qs.getKey());
      out.writeInt(qs.getValue());
    }

    out.writeShort(this.finishedAchievements.size());
    for (final Integer zz : finishedAchievements) {
      out.writeInt(zz.intValue());
    }

    out.writeInt(this.famedcharacters.size());
    for (final Integer zz : famedcharacters) {
      out.writeInt(zz.intValue());
    }

    out.writeShort(this.savedlocation.length);
    for (int zz : savedlocation) {
      out.writeInt(zz);
    }

    out.writeShort(this.wishlist.length);
    for (int zz : wishlist) {
      out.writeInt(zz);
    }

    out.writeShort(this.rocks.length);
    for (int zz : rocks) {
      out.writeInt(zz);
    }

    out.writeShort(this.regrocks.length);
    for (int zz : regrocks) {
      out.writeInt(zz);
    }

    out.writeShort(this.InfoQuest.size());
    for (final Map.Entry<Integer, String> qs : this.InfoQuest.entrySet()) {
      out.writeInt(qs.getKey());
      out.writeUTF(qs.getValue());
    }

    out.writeInt(this.keymap.size());
    for (final Map.Entry<Integer, Triple<Byte, Integer, Byte>> qs : this.keymap.entrySet()) {
      out.writeInt(qs.getKey());
      out.writeByte(qs.getValue().getLeft());
      out.writeInt(qs.getValue().getMid());
      out.writeByte(qs.getValue().getRight());
    }
    out.writeByte(petStore.length);
    for (int i = 0; i < petStore.length; i++) {
      out.writeByte(petStore[i]);
    }
  }
}
