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

package tools.packet;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import tools.KoreanDateUtil;
import tools.Triple;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.*;
import java.util.Map.Entry;

public class PacketHelper {

  private static final long FT_UT_OFFSET = 116444592000000000L; // EDT
  public static final long MAX_TIME = 150842304000000000L;
  public static final long ZERO_TIME = 94354848000000000L;
  public static final long PERMANENT = 150841440000000000L;

  public static final long getKoreanTimestamp(final long realTimestamp) {
    return getTime(realTimestamp);
  }

  public static final long getTime(final long realTimestamp) {
    if (realTimestamp == -1) {
      return MAX_TIME;
    } else if (realTimestamp == -2) {
      return ZERO_TIME;
    } else if (realTimestamp == -3) {
      return PERMANENT;
    }
    long time = (realTimestamp / 1000); // convert to seconds
    return ((time * 10000000) + FT_UT_OFFSET);
  }

  public static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    final List<MapleQuestStatus> started = chr.getStartedQuests();
    mplew.writeShort(started.size());

    for (final MapleQuestStatus q : started) {
      mplew.writeShort(q.getQuest().getId());
      mplew.writeMapleAsciiString(q.getCustomData() != null ? q.getCustomData() : "");
    }
    final List<MapleQuestStatus> completed = chr.getCompletedQuests();
    int time;
    mplew.writeShort(completed.size());

    for (final MapleQuestStatus q : completed) {
      mplew.writeShort(q.getQuest().getId());
      time = KoreanDateUtil.getQuestTimestamp(q.getCompletionTime());
      mplew.writeInt(time); // maybe start time? no effect.
      mplew.writeInt(time); // completion time
    }
  }

  public static final void addSkillInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    final Map<ISkill, SkillEntry> skills = chr.getSkills();
    mplew.writeShort(skills.size());
    for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
      mplew.writeInt(skill.getKey().getId());
      mplew.writeInt(skill.getValue().skillevel);
      addExpirationTime(mplew, skill.getValue().expiration);
      if (skill.getKey().hasMastery()) {
        mplew.writeInt(skill.getValue().masterlevel);
      }
    }
  }

  public static final void addCoolDownInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();
    mplew.writeShort(cd.size());
    for (final MapleCoolDownValueHolder cooling : cd) {
      mplew.writeInt(cooling.skillId);
      mplew.writeShort((int) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
    }
  }

  public static final void addRocksInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    final int[] mapz = chr.getRegRocks();
    for (int i = 0; i < 5; i++) { // VIP teleport map
      mplew.writeInt(mapz[i]);
    }
    final int[] map = chr.getRocks();
    for (int i = 0; i < 10; i++) { // VIP teleport map
      mplew.writeInt(map[i]);
    }
  }

  public static final void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    mplew.writeInt(chr.getMonsterBookCover());
    mplew.write(0);
    chr.getMonsterBook().addCardPacket(mplew);
  }

  public static final void addRingInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    mplew.writeShort(0);
    //01 00 = size
    //01 00 00 00 = gametype?
    //03 00 00 00 = win
    //00 00 00 00 = tie/loss
    //01 00 00 00 = tie/loss
    //16 08 00 00 = points
    Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
    List<MapleRing> cRing = aRing.getLeft();
    mplew.writeShort(cRing.size()); // Couple
    for (MapleRing ring : cRing) {
      mplew.writeInt(ring.getPartnerChrId());
      mplew.writeAsciiString(ring.getPartnerName(), 13);
      mplew.writeLong(ring.getRingId());
      mplew.writeLong(ring.getPartnerRingId());
    }
    List<MapleRing> fRing = aRing.getMid();
    mplew.writeShort(fRing.size()); // Friends
    for (MapleRing ring : fRing) {
      mplew.writeInt(ring.getPartnerChrId());
      mplew.writeAsciiString(ring.getPartnerName(), 13);
      mplew.writeLong(ring.getRingId());
      mplew.writeLong(ring.getPartnerRingId());
      mplew.writeInt(ring.getItemId());
    }
    List<MapleRing> mRing = aRing.getRight();
    mplew.writeShort(mRing.size()); // Marriage [48]
    int marriageId = 30000;
    for (MapleRing ring : mRing) { // We only can have 1 marriage ring, so yeah..
      mplew.writeInt(marriageId); // Engagement id.
      mplew.writeInt(chr.getId());
      mplew.writeInt(ring.getPartnerChrId());
      mplew.writeShort(0/*ring.getStatus()*/);
      mplew.writeInt(ring.getItemId());
      mplew.writeInt(ring.getItemId());
      mplew.writeAsciiString(chr.getName(), 13);
      mplew.writeAsciiString(ring.getPartnerName(), 13);
    }
  }

  public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
    mplew.writeInt(chr.getMeso()); // mesos
    mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit()); // equip slots
    mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit()); // use slots
    mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit()); // set-up slots
    mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit()); // etc slots
    mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit()); // cash slots

    mplew.writeLong(getTime(-2)); // extra pendant slot
    MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
    Collection<IItem> equippedC = iv.list();
    List<Item> equipped = new ArrayList<>(equippedC.size());

    for (IItem item : equippedC) {
      equipped.add((Item) item);
    }
    Collections.sort(equipped);
    for (Item item : equipped) {
      if (item.getPosition() < 0 && item.getPosition() > -100) {
        addItemInfo(mplew, item, false, false);
      }
    }
    mplew.writeShort(0); // start of equipped nx
    for (Item item : equipped) {
      if (item.getPosition() <= -100 && item.getPosition() > -1000) {
        addItemInfo(mplew, item, false, false);
      }
    }
    mplew.writeShort(0); // start of equip inventory
    iv = chr.getInventory(MapleInventoryType.EQUIP);
    for (IItem item : iv.list()) {
      addItemInfo(mplew, item, false, false);
    }
    mplew.writeShort(0); //start of other equips
    for (Item item : equipped) {
      if (item.getPosition() <= -1000 && item.getPosition() > -1100) {
        addItemInfo(mplew, item, false, false);
      }
    }
    mplew.writeShort(0); // start of use inventory
    iv = chr.getInventory(MapleInventoryType.USE);
    for (IItem item : iv.list()) {
      addItemInfo(mplew, item, false, false);
    }
    mplew.write(0); // start of set-up inventory
    iv = chr.getInventory(MapleInventoryType.SETUP);
    for (IItem item : iv.list()) {
      addItemInfo(mplew, item, false, false);
    }
    mplew.write(0); // start of etc inventory
    iv = chr.getInventory(MapleInventoryType.ETC);
    for (IItem item : iv.list()) {
      addItemInfo(mplew, item, false, false);
    }
    mplew.write(0); // start of cash inventory
    iv = chr.getInventory(MapleInventoryType.CASH);
    for (IItem item : iv.list()) {
      addItemInfo(mplew, item, false, false);
    }
    mplew.write(0); // start of extended slots
  }

  public static final void addCharStats(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    mplew.writeInt(chr.getId()); // character id
    mplew.writeAsciiString(chr.getName(), 13);
    mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
    mplew.write(chr.getSkinColor()); // skin color
    mplew.writeInt(chr.getFace()); // face
    mplew.writeInt(chr.getHair()); // hair
    for (int i = 0; i < 3; i++) {
      if (chr.getPet(i) != null) {
        mplew.writeLong(chr.getPet(i).getUniqueId());
      } else {
        mplew.writeLong(0);
      }
    }
    mplew.write(chr.getLevel()); // level
    mplew.writeShort(chr.getJob()); // job
    chr.getStat().connectData(mplew);
    mplew.writeShort(Math.min(199, chr.getRemainingAp())); // Avoid Popup
    if (chr.isEvan() && (chr.getLevel() >= 10) && (chr.getJob() != 2001)) {
      EvanSkillPoints esp;
      esp = chr.getEvanSP();
      mplew.write(esp.getSkillPoints().keySet().size());
      for (Iterator i$ = esp.getSkillPoints().keySet().iterator(); i$.hasNext(); ) {
        int i = ((Integer) i$.next()).intValue();
        mplew.write(i == 2200 ? 1 : i - 2208);
        mplew.write(esp.getSkillPoints(i));
      }
    } else if (chr.getJob() == 2001) {
      mplew.write(0);
    } else {
      mplew.writeShort(chr.getRemainingSp()); // remaining sp
    }
    mplew.writeInt(chr.getExp()); // exp
    mplew.writeShort(chr.getFame()); // fame
    mplew.writeInt(0); // Gachapon exp
    mplew.writeInt(chr.getMapId()); // current map id
    mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
    mplew.writeInt(0);
    mplew.writeShort(chr.getSubCategoryField()); //1 here = db
  }

  public static final void addCharLook(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, final boolean mega) {
    mplew.write(chr.getGender());
    mplew.write(chr.getSkinColor());
    mplew.writeInt(chr.getFace());
    mplew.write(mega ? 0 : 1);
    mplew.writeInt(chr.getHair());

    final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
    final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
    MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);

    for (final IItem item : equip.list()) {
      if (item instanceof Equip) {
        Equip currentEquip = (Equip) item;
        if (!chr.isGameMasterJob()) {
          if (currentEquip.getRequiredStr() > chr.getStat().getTotalStr()) {
            continue;
          }
          if (currentEquip.getRequiredDex() > chr.getStat().getTotalDex()) {
            continue;
          }
          if (currentEquip.getRequiredInt() > chr.getStat().getTotalInt()) {
            continue;
          }
          if (currentEquip.getRequiredLuk() > chr.getStat().getTotalLuk()) {
            continue;
          }
          boolean isLevel0 = currentEquip.getRequiredLevel() == 0;
          if (isLevel0 == false) {
            if (currentEquip.getRequiredLevel() > chr.getLevel()) {
              continue;
            }
          }
        }
      }


      if (item.getPosition() < -128) { //not visible
        continue;
      }
      byte pos = (byte) (item.getPosition() * -1);

      if (pos < 100 && myEquip.get(pos) == null) {
        myEquip.put(pos, item.getItemId());
      } else if ((pos > 100 || pos == -128) && pos != 111) {
        pos = (byte) (pos == -128 ? 28 : pos - 100);
        if (myEquip.get(pos) != null) {
          maskedEquip.put(pos, myEquip.get(pos));
        }
        myEquip.put(pos, item.getItemId());
      } else if (myEquip.get(pos) != null) {
        maskedEquip.put(pos, item.getItemId());
      }
    }
    for (final Entry<Byte, Integer> entry : myEquip.entrySet()) {
      mplew.write(entry.getKey());
      mplew.writeInt(entry.getValue());
    }

    mplew.write(0xFF);

    // end of visible itens
    // masked itens
    for (final Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
      mplew.write(entry.getKey());
      mplew.writeInt(entry.getValue());

    }
    mplew.write(0xFF); // ending markers


    final IItem cWeapon = equip.getItem((byte) -111);
    mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
    for (int i = 0; i < 3; i++) {
      if (chr.getPet(i) != null) {
        mplew.writeInt(chr.getPet(i).getPetItemId());
      } else {
        mplew.writeInt(0);
      }
    }
  }

  public static final void addExpirationTime(final MaplePacketLittleEndianWriter mplew, final long time) {
    mplew.write(0);
    mplew.writeShort(1408); // 80 05
    if (time != -1) {
      mplew.writeInt(KoreanDateUtil.getItemTimestamp(time));
      mplew.write(1);
    } else {
      mplew.writeInt(400967355);
      mplew.write(2);
    }
  }

  public static final void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut) {
    addItemInfo(mplew, item, zeroPosition, leaveOut, false);
  }

  public static final void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut, final boolean trade) {
    short pos = item.getPosition();
    if (zeroPosition) {
      if (!leaveOut) {
        mplew.write(0);
      }
    } else {
      if (pos <= -1) {
        pos *= -1;
        if (pos > 100 && pos < 1000) {
          pos -= 100;
        }
      }
      if (!trade && item.getType() == 1) {
        mplew.writeShort(pos);
      } else {
        mplew.write(pos);
      }
    }
    mplew.write(item.getPet() != null ? 3 : item.getType());
    mplew.writeInt(item.getItemId());
    boolean hasUniqueId = item.getSN() > 0;
    //marriage rings arent cash items so dont have uniqueids, but we assign them anyway for the sake of rings
    mplew.write(hasUniqueId ? 1 : 0);
    if (hasUniqueId) {
      mplew.writeLong(item.getSN());
    }

    if (item.getPet() != null) { // Pet
      addPetItemInfo(mplew, item, item.getPet());
    } else {
      addExpirationTime(mplew, item.getExpiration());
      if (item.getType() == 1) {
        final IEquip equip = (IEquip) item;
        mplew.write(equip.getUpgradeSlots());
        mplew.write(equip.getLevel());
        mplew.writeShort(equip.getStr());
        mplew.writeShort(equip.getDex());
        mplew.writeShort(equip.getInt());
        mplew.writeShort(equip.getLuk());
        mplew.writeShort(equip.getHp());
        mplew.writeShort(equip.getMp());
        mplew.writeShort(equip.getWatk());
        mplew.writeShort(equip.getMatk());
        mplew.writeShort(equip.getWdef());
        mplew.writeShort(equip.getMdef());
        mplew.writeShort(equip.getAcc());
        mplew.writeShort(equip.getAvoid());
        mplew.writeShort(equip.getHands());
        mplew.writeShort(equip.getSpeed());
        mplew.writeShort(equip.getJump());
        mplew.writeMapleAsciiString(equip.getOwner());
        mplew.writeShort(equip.getFlag());
        mplew.write(0); // skills
        mplew.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel())); // Item level
        mplew.writeInt(equip.getExpPercentage() * 100000);
        mplew.writeInt(equip.getDurability());
        mplew.writeInt(equip.getViciousHammer());
        if (!hasUniqueId) {
          mplew.write(equip.getState()); //7 = unique for the lulz
          mplew.write(equip.getEnhance());
          mplew.writeShort(equip.getPotential1()); //potential stuff 1. total damage
          mplew.writeShort(equip.getPotential2()); //potential stuff 2. critical rate
          mplew.writeShort(equip.getPotential3()); //potential stuff 3. all stats
        }
        mplew.writeShort(equip.getHpR());
        mplew.writeShort(equip.getMpR());
        mplew.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId());
        mplew.writeLong(getTime(-2));
        mplew.writeInt(-1);
      } else {
        mplew.writeShort(item.getQuantity());
        mplew.writeMapleAsciiString(item.getOwner());
        mplew.writeShort(item.getFlag());
        if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
          mplew.writeLong(item.getInventoryId() <= 0 ? -1 : item.getInventoryId());
        }
      }
    }
  }



  public static final void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr) && chr.getPlayerShop().getShopType() != 1 && chr.getPlayerShop().isAvailable()) {
      addInteraction(mplew, chr.getPlayerShop());
    } else {
      mplew.write(0);
    }
  }

  public static final void addInteraction(final MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
    mplew.write(shop.getGameType());
    mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
    mplew.writeMapleAsciiString(shop.getDescription());
    if (shop.getShopType() != 1) {
      mplew.write(shop.getPassword().length() > 0 ? 1 : 0); //password = false
    }
    mplew.write(shop.getItemId() % 10);
    mplew.write(shop.getSize()); //current size
    mplew.write(shop.getMaxSize()); //full slots... 4 = 4-1=3 = has slots, 1-1=0 = no slots
    if (shop.getShopType() != 1) {
      mplew.write(shop.isOpen() ? 0 : 1);
    }
  }

  public static final void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
    mplew.writeLong(-1);
    mplew.write(0);
    addCharStats(mplew, chr);
    mplew.write(chr.getBuddylist().getCapacity());
    if (chr.getBlessOfFairyOrigin() != null) {
      mplew.write(1);
      mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
    } else {
      mplew.write(0);
    }
    addInventoryInfo(mplew, chr);
    addSkillInfo(mplew, chr);
    addCoolDownInfo(mplew, chr);
    addQuestInfo(mplew, chr);

    addRingInfo(mplew, chr);
    addRocksInfo(mplew, chr);
    addMonsterBookInfo(mplew, chr);
    mplew.writeShort(0);
    chr.QuestInfoPacket(mplew); // for every questinfo: int16_t questid, string questdata
    mplew.writeInt(0); // PQ rank
  }

  public static final void addPetItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final MaplePet pet) {
    if (item == null) {
      mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
    } else {
      addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1 : item.getExpiration());
    }
    mplew.writeAsciiString(pet.getName(), 13);
    mplew.write(pet.getLevel());
    mplew.writeShort(pet.getCloseness());
    mplew.write(pet.getFullness());
    if (item == null) {
      mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
    } else {
      addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1 : item.getExpiration());
    }
    mplew.writeShort(0);
    mplew.writeShort(0); // pet flags
    mplew.writeInt((pet.getPetItemId() == 5000054 && pet.getSecondsLeft() > 0) ? pet.getSecondsLeft() : 0);
    mplew.writeShort(0);
  }


}
