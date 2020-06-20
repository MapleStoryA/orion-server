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

package constants;

import client.MapleJob;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import client.status.MonsterStatus;
import server.Randomizer;
import server.maps.MapleMapObjectType;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class GameConstants {

  public static final List<MapleMapObjectType> rangedMapobjectTypes = Collections.unmodifiableList(Arrays.asList(
      MapleMapObjectType.ITEM,
      MapleMapObjectType.MONSTER,
      MapleMapObjectType.DOOR,
      MapleMapObjectType.REACTOR,
      MapleMapObjectType.SUMMON,
      MapleMapObjectType.NPC,
      MapleMapObjectType.MIST));
  private static final int[] exp = {0, 15, 34, 57, 92, 135, 372, 560, 840, 1242, 1144,
      1573, 2144, 2800, 3640, 4700, 5893, 7360, 9144, 11120, 13478,
      16268, 19320, 22881, 27009, 31478, 36601, 42446, 48722, 55816, 76560,
      86784, 98208, 110932, 124432, 139372, 155865, 173280, 192400, 213345, 235372,
      259392, 285532, 312928, 342624, 374760, 408336, 444544, 483532, 524160, 567772,
      598886, 631704, 666321, 702836, 741351, 781976, 824828, 870028, 917705, 967995,
      1021040, 1076993, 1136012, 1198265, 1263930, 1333193, 1406252, 1483314, 1564600, 1650340,
      1740778, 1836172, 1936794, 2042930, 2154882, 2272969, 2397528, 2528912, 2667496, 2813674,
      2967863, 3130501, 3302052, 3483004, 3673872, 3875200, 4087561, 4311559, 4547832, 4797052,
      5059931, 5337215, 5629694, 5938201, 6263614, 6606860, 6968915, 7350811, 7753635, 8178534,
      8626717, 9099461, 9598112, 10124088, 10678888, 11264090, 11881362, 12532460, 13219239, 13943652,
      14707764, 15513749, 16363902, 17260644, 18206527, 19204244, 20256636, 21366700, 22537594, 23772654,
      25075395, 26449526, 27898960, 29427822, 31040466, 32741483, 34535716, 36428272, 38424541, 40530206,
      42751261, 45094030, 47565183, 50171755, 52921167, 55821246, 58880250, 62106888, 65510344, 69100311,
      72887008, 76881216, 81094306, 85538273, 90225770, 95170142, 100385465, 105886588, 111689173, 117809740,
      124265713, 131075474, 138258409, 145834970, 153826726, 162256430, 171148082, 180526996, 190419876, 200854884,
      211861732, 223471754, 235718006, 248635352, 262260569, 276632448, 291791906, 307782102, 324648561, 342439302,
      361204976, 380999008, 401877753, 423900654, 447130409, 471633156, 497478652, 524740482, 553496260, 583827855,
      615821621, 649568646, 685165008, 722712050, 762316670, 804091623, 848155844, 894634784, 943660769, 995373379,
      1049919840, 1107455447, 1168144005, 1232158296, 1299680571, 1370903066, 1446028554, 1525270918, 1608855764, 1767659560};
  private static final int[] closeness = {0, 1, 3, 6, 14, 31, 60, 108, 181, 287, 434, 632, 891, 1224, 1642, 2161, 2793,
      3557, 4467, 5542, 6801, 8263, 9950, 11882, 14084, 16578, 19391, 22547, 26074,
      30000};
  private static final int[] mountexp = {0, 6, 25, 50, 105, 134, 196, 254, 263, 315, 367, 430, 543, 587, 679, 725, 897, 1146, 1394, 1701, 2247,
      2543, 2898, 3156, 3313, 3584, 3923, 4150, 4305, 4550};
  public static final int[] itemBlock = {2340000, 2049100, 4001129, 2040037, 2040006, 2040007, 2040303, 2040403, 2040506, 2040507, 2040603, 2040709, 2040710, 2040711, 2040806, 2040903, 2041024, 2041025, 2043003, 2043103, 2043203, 2043303, 2043703, 2043803, 2044003, 2044103, 2044203, 2044303, 2044403, 2044503, 2044603, 2044908, 2044815, 2044019, 2044703};
  public static final int[] cashBlock = {5062000, 5650000, 5431000, 5431001, 5432000, 5450000, 5550000, 5550001, 5640000, 5530013, 5150039, 5150046, 5150054, 1812006, 5650000, 5222000, 5221001, 5220014, 5220015, 5420007, 5451000,
      5210000, 5210001, 5210002, 5210003, 5210004, 5210005, 5210006, 5210007, 5210008, 5210009, 5210010, 5210011, 5211000, 5211001, 5211002, 5211003, 5211004, 5211005, 5211006, 5211007, 5211008, 5211009, 5211010, 5211011, 5211012, 5211013, 5211014, 5211015, 5211016, 5211017, 5211018,
      5211019, 5211020, 5211021, 5211022, 5211023, 5211024, 5211025, 5211026, 5211027, 5211028, 5211029, 5211030, 5211031, 5211032, 5211033, 5211034, 5211035, 5211036, 5211037, 5211038, 5211039, 5211040, 5211041, 5211042, 5211043,
      5211044, 5211045, 5211046, 5211047, 5211048, 5211049, 5211050, 5211051, 5211052, 5211053, 5211054, 5211055, 5211056, 5211057, 5211058, 5211059, 5211060, 5211061,//2x exp
      5360000, 5360001, 5360002, 5360003, 5360004, 5360005, 5360006, 5360007, 5360008, 5360009, 5360010, 5360011, 5360012, 5360013, 5360014, 5360017, 5360050, 5211050, 5360042, 5360052, 5360053, 5360050, //2x drop
      1112810, 1112811, 5530013, 4001431, 4001432, 4032605,
      5270000, 5270001, 5270002, 5270003, 5270004, 5270005, 5270006, //2x meso
      9102328, 9102329, 9102330, 9102331, 9102332, 9102333}; //miracle cube and stuff
  public static final int[] blockedSkills = {4341003};
  public static final String[] RESERVED = {};


  public static int getExpNeededForLevel(final int level) {
    if (level < 0 || level >= exp.length) {
      return Integer.MAX_VALUE;
    }
    return exp[level];
  }

  public static int getClosenessNeededForLevel(final int level) {
    return closeness[level - 1];
  }

  public static int getMountExpNeededForLevel(final int level) {
    return mountexp[level - 1];
  }

  public static int getBookLevel(final int level) {
    return (int) ((5 * level) * (level + 1));
  }

  public static int getTimelessRequiredEXP(final int level) {
    return 70 + (level * 10);
  }

  public static int getReverseRequiredEXP(final int level) {
    return 60 + (level * 5);
  }

  public static int maxViewRangeSq() {
    return 800 * 800;
  }

  public static boolean isJobFamily(final int baseJob, final int currentJob) {
    return currentJob >= baseJob && currentJob / 100 == baseJob / 100;
  }

  public static boolean isKOC(final int job) {
    return job >= 1000 && job < 2000;
  }

  public static boolean isEvan(final int job) {
    return job == 2001 || (job >= 2200 && job <= 2218);
  }

  public static boolean isAran(final int job) {
    return job >= 2000 && job <= 2112 && job != 2001;
  }

  public static boolean isResist(final int job) {
    return job >= 3000 && job <= 3512;
  }

  public static boolean isAdventurer(final int job) {
    return job >= 0 && job < 1000;
  }

  public static boolean isRecoveryIncSkill(final int id) {
    switch (id) {
      case 1110000:
      case 2000000:
      case 1210000:
      case 11110000:
      case 4100002:
      case 4200001:
        return true;
    }
    return false;
  }

  public static boolean isLinkedAranSkill(final int id) {
    return getLinkedAranSkill(id) != id;
  }

  public static int getLinkedAranSkill(final int id) {
    switch (id) {
      case 21110007:
      case 21110008:
        return 21110002;
      case 21120009:
      case 21120010:
        return 21120002;
      case 4321001:
        return 4321000;
      case 33101006:
      case 33101007:
        return 33101005;
      case 33101008:
        return 33101004;
      case 35101009:
      case 35101010:
        return 35100008;
      case 35111009:
      case 35111010:
        return 35111001;
    }
    return id;
  }

  public static int getBOF_ForJob(final int job) {
    if (isAdventurer(job)) {
      return 12;
    } else if (isKOC(job)) {
      return 10000012;
    } else if (isResist(job)) {
      return 30000012;
    } else if (isEvan(job)) {
      return 20010012;
    }
    return 20000012;
  }

  public static int getSkillByJob(final int skillID, final int job) {
    if (isKOC(job)) {
      return skillID + 10000000;
    } else if (isAran(job)) {
      return skillID + 20000000;
    } else if (isEvan(job)) {
      return skillID + 20010000;
    } else if (isResist(job)) {
      return skillID + 30000000;
    }
    return skillID;
  }

  public static boolean isElementAmp_Skill(final int skill) {
    switch (skill) {
      case 2110001:
      case 2210001:
      case 12110001:
      case 22150000:
        return true;
    }
    return false;
  }

  public static int getMPEaterForJob(final int job) {
    switch (job) {
      case 210:
      case 211:
      case 212:
        return 2100000;
      case 220:
      case 221:
      case 222:
        return 2200000;
      case 230:
      case 231:
      case 232:
        return 2300000;
    }
    return 2100000; // Default, in case GM
  }

  public static int getJobShortValue(int job) {
    if (job >= 1000) {
      job -= (job / 1000) * 1000;
    }
    job /= 100;
    if (job == 4) { // For some reason dagger/ claw is 8.. IDK
      job *= 2;
    } else if (job == 3) {
      job += 1;
    } else if (job == 5) {
      job += 11; // 16
    }
    return job;
  }

  public static boolean isPyramidSkill(final int skill) {
    switch (skill) {
      case 1020:
      case 10001020:
      case 20001020:
      case 20011020:
      case 30001020:
        return true;
    }
    return false;
  }

  public static boolean isMulungSkill(final int skill) {
    switch (skill) {
      case 1009:
      case 1010:
      case 1011:
      case 10001009:
      case 10001010:
      case 10001011:
      case 20001009:
      case 20001010:
      case 20001011:
      case 20011009:
      case 20011010:
      case 20011011:
      case 30001009:
      case 30001010:
      case 30001011:
        return true;
    }
    return false;
  }

  public static boolean isThrowingStar(final int itemId) {
    return itemId / 10000 == 207;
  }

  public static boolean isBullet(final int itemId) {
    return itemId / 10000 == 233;
  }

  public static boolean isRechargable(final int itemId) {
    return isThrowingStar(itemId) || isBullet(itemId);
  }

  public static boolean isOverall(final int itemId) {
    return itemId / 10000 == 105;
  }

  public static boolean isPet(final int itemId) {
    return itemId / 10000 == 500;
  }

  public static boolean isArrowForCrossBow(final int itemId) {
    return itemId >= 2061000 && itemId < 2062000;
  }

  public static boolean isArrowForBow(final int itemId) {
    return itemId >= 2060000 && itemId < 2061000;
  }

  public static boolean isMagicWeapon(final int itemId) {
    final int s = itemId / 10000;
    return s == 137 || s == 138;
  }

  public static boolean isWeapon(final int itemId) {
    return itemId >= 1300000 && itemId < 1500000;
  }

  public static MapleInventoryType getInventoryType(final int itemId) {
    final byte type = (byte) (itemId / 1000000);
    if (type < 1 || type > 5) {
      return MapleInventoryType.UNDEFINED;
    }
    return MapleInventoryType.getByType(type);
  }

  public static MapleWeaponType getWeaponType(final int itemId) {
    int cat = itemId / 10000;
    cat = cat % 100;
    switch (cat) {
      case 30:
        return MapleWeaponType.SWORD1H;
      case 31:
        return MapleWeaponType.AXE1H;
      case 32:
        return MapleWeaponType.BLUNT1H;
      case 33:
        return MapleWeaponType.DAGGER;
      case 34:
        return MapleWeaponType.KATARA;
      case 37:
        return MapleWeaponType.WAND;
      case 38:
        return MapleWeaponType.STAFF;
      case 40:
        return MapleWeaponType.SWORD2H;
      case 41:
        return MapleWeaponType.AXE2H;
      case 42:
        return MapleWeaponType.BLUNT2H;
      case 43:
        return MapleWeaponType.SPEAR;
      case 44:
        return MapleWeaponType.POLE_ARM;
      case 45:
        return MapleWeaponType.BOW;
      case 46:
        return MapleWeaponType.CROSSBOW;
      case 47:
        return MapleWeaponType.CLAW;
      case 48:
        return MapleWeaponType.KNUCKLE;
      case 49:
        return MapleWeaponType.GUN;
    }
    return MapleWeaponType.NOT_A_WEAPON;
  }

  public static boolean isShield(final int itemId) {
    int cat = itemId / 10000;
    cat = cat % 100;
    return cat == 9;
  }

  public static boolean isEquip(final int itemId) {
    return itemId / 1000000 == 1;
  }

  public static boolean isCleanSlate(int itemId) {
    return itemId / 100 == 20490;
  }

  public static boolean isAccessoryScroll(int itemId) {
    return (itemId / 100) == 20492 || (itemId / 100) == 20463;
  }

  public static boolean isChaosScroll(int itemId) {
    if (itemId >= 2049105 && itemId <= 2049110) {
      return false;
    }
    return itemId / 100 == 20491;
  }

  public static int getChaosNumber(int itemId) {
    return itemId == 2049116 ? 10 : 5;
  }

  public static boolean isEquipScroll(int scrollId) {
    return scrollId / 100 == 20493;
  }

  public static boolean isPotentialScroll(int scrollId) {
    return scrollId / 100 == 20494;
  }

  public static boolean isSpecialScroll(final int scrollId) {
    switch (scrollId) {
      case 2040727: // Spikes on show
      case 2041058: // Cape for Cold protection
        return true;
    }
    return false;
  }

  public static boolean isTwoHanded(final int itemId) {
    switch (getWeaponType(itemId)) {
      case AXE2H:
      case GUN:
      case KNUCKLE:
      case BLUNT2H:
      case BOW:
      case CLAW:
      case CROSSBOW:
      case POLE_ARM:
      case SPEAR:
      case SWORD2H:
        return true;
      default:
        return false;
    }
  }

  public static boolean isTownScroll(final int id) {
    return id >= 2030000 && id < 2040000;
  }

  public static boolean isUpgradeScroll(final int id) {
    return id >= 2040000 && id < 2050000;
  }

  public static boolean isGun(final int id) {
    return id >= 1492000 && id < 1500000;
  }

  public static boolean isUse(final int id) {
    return id >= 2000000 && id <= 2490000;
  }

  public static boolean isSummonSack(final int id) {
    return id / 10000 == 210;
  }

  public static boolean isMonsterCard(final int id) {
    return id / 10000 == 238;
  }

  public static boolean isSpecialCard(final int id) {
    return id / 1000 >= 2388;
  }

  public static int getCardShortId(final int id) {
    return id % 10000;
  }

  public static boolean isGem(final int id) {
    return id >= 4250000 && id <= 4251402;
  }

  public static boolean isOtherGem(final int id) {
    switch (id) {
      case 4001174:
      case 4001175:
      case 4001176:
      case 4001177:
      case 4001178:
      case 4001179:
      case 4001180:
      case 4001181:
      case 4001182:
      case 4001183:
      case 4001184:
      case 4001185:
      case 4001186:
      case 4031980:
      case 2041058:
      case 2040727:
      case 1032062:
      case 4032334:
      case 4032312:
      case 1142156:
      case 1142157:
        return true; //mostly quest items
    }
    return false;
  }

  public static int getTaxAmount(final int meso) {
    if (meso >= 100000000) {
      return (int) Math.round(0.06 * meso);
    } else if (meso >= 25000000) {
      return (int) Math.round(0.05 * meso);
    } else if (meso >= 10000000) {
      return (int) Math.round(0.04 * meso);
    } else if (meso >= 5000000) {
      return (int) Math.round(0.03 * meso);
    } else if (meso >= 1000000) {
      return (int) Math.round(0.018 * meso);
    } else if (meso >= 100000) {
      return (int) Math.round(0.008 * meso);
    }
    return 0;
  }


  public static short getSummonAttackDelay(final int id) {
    switch (id) {
      case 15001004: // Lightning
      case 14001005: // Darkness
      case 13001004: // Storm
      case 12001004: // Flame
      case 11001004: // Soul
      case 3221005: // Freezer
      case 3211005: // Golden Eagle
      case 3121006: // Phoenix
      case 3111005: // Silver Hawk
      case 2321003: // Bahamut
      case 2311006: // Summon Dragon
      case 2221005: // Infrit
      case 2121005: // Elquines
        return 3030;
      case 5211001: // Octopus
      case 5211002: // Gaviota
      case 5220002: // Support Octopus
        return 1530;
      case 3211002: // Puppet
      case 3111002: // Puppet
      case 1321007: // Beholder
      case 4341006:
      case 35121009:
      case 35121010:
      case 35111011:
      case 35111002:
        return 0;
    }
    return 0;
  }

  public static short getAttackDelay(final int id) {
    switch (id) { // Assume it's faster(2)
      case 4321001: //tornado spin
        return 40; //reason being you can spam with final assaulter
      case 3121004: // Storm of Arrow
      case 33121009:
      case 13111002: // Storm of Arrow
      case 5221004: // Rapidfire
      case 4221001: //Assassinate?
      case 5201006: // Recoil shot/ Back stab shot
        return 120;
      case 13101005: // Storm Break
        return 360;
      case 5001003: // Double Fire
      case 2301002: // Heal
        return 390;
      case 5001001: // Straight/ Flash Fist
      case 15001001: // Straight/ Flash Fist
      case 1321003: // Rush
      case 1221007: // Rush
      case 1121006: // Rush
        return 450;
      case 5211004: // Flamethrower
      case 5211005: // Ice Splitter
      case 4201005: // Savage blow
        return 480;
      case 0: // Normal Attack, TODO delay for each weapon type
      case 5111002: // Energy Blast
      case 15101005: // Energy Blast
      case 1001004: // Power Strike
      case 11001002: // Power Strike
      case 1001005: // Slash Blast
      case 11001003: // Slash Blast
      case 1311005: // Sacrifice
        return 570;
      case 2101004: // Fire Arrow
      case 12101002: // Fire Arrow
      case 2101005: // Poison Breath
      case 2121003: // Fire Demon
      case 2221003: // Ice Demon
      case 2121006: // Paralyze
      case 3111006: // Strafe
      case 311004: // Arrow Rain
      case 13111000: // Arrow Rain
      case 3111003: // Inferno
      case 3101005: // Arrow Bomb
      case 4001344: // Lucky Seven
      case 14001004: // Lucky seven
      case 4121007: // Triple Throw
      case 14111005: // Triple Throw
      case 4111004: // Shadow Meso
      case 4101005: // Drain
      case 4211004: // Band of Thieves
      case 4201004: // Steal
      case 4001334: // Double Stab
      case 5221007: // Battleship Cannon
      case 1211002: // Charged blow
      case 1311003: // Dragon Fury : Spear
      case 1311004: // Dragon Fury : Pole Arm
      case 3211006: // Strafe
      case 3211004: // Arrow Eruption
      case 3211003: // Blizzard Arrow
      case 3201005: // Iron Arrow
      case 3221001: // Piercing
      case 4111005: // Avenger
      case 14111002: // Avenger
      case 5201001: // Invisible shot
      case 5101004: // Corkscrew Blow
      case 15101003: // Corkscrew Blow
      case 1121008: // Brandish
      case 11111004: // Brandish
      case 1221009: // Blast
        return 600;
      case 5201004: // Blank Shot/ Fake shot
      case 5211000: // Burst Fire/ Triple Fire
      case 5001002: // Sommersault Kick
      case 15001002: // Sommersault Kick
      case 4221007: // Boomerang Stab
      case 1311001: // Spear Crusher, 16~30 pts = 810
      case 1311002: // PA Crusher, 16~30 pts = 810
      case 2221006: // Chain Lightning
        return 660;
      case 4121008: // Ninja Storm
      case 5211006: // Homing Beacon
      case 5221008: // Battleship Torpedo
      case 5101002: // Backspin Blow
      case 2001005: // Magic Claw
      case 12001003: // Magic Claw
      case 2001004: // Energy Bolt
      case 2301005: // Holy Arrow
      case 2121001: // Big Bang
      case 2221001: // Big Bang
      case 2321001: // Big Bang
      case 2321007: // Angel's Ray
      case 2201005: // Thunderbolt
      case 2201004: // Cold Beam
      case 4211006: // Meso Explosion
      case 5121005: // Snatch
      case 12111006: // Fire Strike
      case 11101004: // Soul Blade
        return 750;
      case 15111007: // Shark Wave
      case 2111006: // Elemental Composition
      case 2211006: // Elemental Composition
        return 810;
      case 13111006: // Wind Piercing
      case 4211002: // Assaulter
      case 5101003: // Double Uppercut
      case 2111002: // Explosion
        return 900;
      case 5121003: // Energy Orb
      case 2311004: // Shining Ray
      case 2211002: // Ice Strike
        return 930;
      case 13111007: // Wind Shot
        return 960;
      case 14101006: // Vampire
      case 4121003: // Showdown
      case 4221003: // Showdown
        return 1020;
      case 12101006: // Fire Pillar
        return 1050;
      case 5121001: // Dragon Strike
        return 1060;
      case 2211003: // Thunder Spear
      case 1311006: // Dragon Roar
        return 1140;
      case 11111006: // Soul Driver
        return 1230;
      case 12111005: // Flame Gear
        return 1260;
      case 2111003: // Poison Mist
        return 1320;
      case 5111006: // Shockwave
      case 15111003: // Shockwave
        return 1500;
      case 5121007: // Barrage
      case 15111004: // Barrage
        return 1830;
      case 5221003: // Ariel Strike
      case 5121004: // Demolition
        return 2160;
      case 2321008: // Genesis
        return 2700;
      case 2121007: // Meteor Shower
      case 10001011: // Meteo Shower
      case 2221007: // Blizzard
        return 3060;
    }
    // TODO delay for final attack, weapon type, swing,stab etc
    return 330; // Default usually
  }

  public static byte gachaponRareItem(final int id) {
    switch (id) {
      case 2340000: // White Scroll
      case 2049100: // Chaos Scroll
      case 2049000: // Reverse Scroll
      case 2049001: // Reverse Scroll
      case 2049002: // Reverse Scroll
      case 2040006: // Miracle
      case 2040007: // Miracle
      case 2040303: // Miracle
      case 2040403: // Miracle
      case 2040506: // Miracle
      case 2040507: // Miracle
      case 2040603: // Miracle
      case 2040709: // Miracle
      case 2040710: // Miracle
      case 2040711: // Miracle
      case 2040806: // Miracle
      case 2040903: // Miracle
      case 2041024: // Miracle
      case 2041025: // Miracle
      case 2043003: // Miracle
      case 2043103: // Miracle
      case 2043203: // Miracle
      case 2043303: // Miracle
      case 2043703: // Miracle
      case 2043803: // Miracle
      case 2044003: // Miracle
      case 2044103: // Miracle
      case 2044203: // Miracle
      case 2044303: // Miracle
      case 2044403: // Miracle
      case 2044503: // Miracle
      case 2044603: // Miracle
      case 2044908: // Miracle
      case 2044815: // Miracle
      case 2044019: // Miracle
      case 2044703: // Miracle
      case 1372039: // Elemental wand lvl 130
      case 1372040: // Elemental wand lvl 130
      case 1372041: // Elemental wand lvl 130
      case 1372042: // Elemental wand lvl 130
      case 1092049: // Dragon Khanjar
      case 1382037: // Blade Staff
        return 2;
      case 1102084: // Pink Gaia Cape
      case 1102041: // Pink Adventurer Cape
      case 1402044: // Pumpkin Lantern
      case 1082149: // Brown Work glove
      case 1102086: // Purple Gaia Cape
      case 1102042: // Purple Adventurer Cape

      case 3010065: // Pink Parasol
      case 3010064: // Brown Sand Bunny Cushion
      case 3010063: // Starry Moon Cushion
      case 3010068: // Teru Teru Chair
      case 3010054: // Baby Bear's Dream
      case 3012001: // Round the Campfire
      case 3012002: // Rubber Ducky Bath
      case 3010020: // Portable Meal Table
      case 3010041: // Skull Throne

      case 1082179: //yellow marker
        return 2;
      //1 = wedding msg o.o
    }
    return 0;
  }

  public final static int[] goldrewards = {
      2340000, 1, // white scroll
      2070018, 1, // balance fury
      1402037, 1, // Rigbol Sword
      2290096, 1, // Maple Warrior 20
      2290049, 1, // Genesis 30
      2290041, 1, // Meteo 30
      2290047, 1, // Blizzard 30
      2290095, 1, // Smoke 30
      2290017, 1, // Enrage 30
      2290075, 1, // Snipe 30
      2290085, 1, // Triple Throw 30
      2290116, 1, // Areal Strike
      1302059, 3, // Dragon Carabella
      2049100, 1, // Chaos Scroll
      2340000, 1, // White Scroll
      1092049, 1, // Dragon Kanjar
      1102041, 1, // Pink Cape
      1432018, 3, // Sky Ski
      1022047, 3, // Owl Mask
      3010051, 1, // Chair
      3010020, 1, // Portable meal table
      2040914, 1, // Shield for Weapon Atk

      1432011, 3, // Fair Frozen
      1442020, 3, // HellSlayer
      1382035, 3, // Blue Marine
      1372010, 3, // Dimon Wand
      1332027, 3, // Varkit
      1302056, 3, // Sparta
      1402005, 3, // Bezerker
      1472053, 3, // Red Craven
      1462018, 3, // Casa Crow
      1452017, 3, // Metus
      1422013, 3, // Lemonite
      1322029, 3, // Ruin Hammer
      1412010, 3, // Colonian Axe

      1472051, 1, // Green Dragon Sleeve
      1482013, 1, // Emperor's Claw
      1492013, 1, // Dragon fire Revlover

      1382050, 1, // Blue Dragon Staff
      1382045, 1, // Fire Staff, Level 105
      1382047, 1, // Ice Staff, Level 105
      1382048, 1, // Thunder Staff
      1382046, 1, // Poison Staff

      1332032, 4, // Christmas Tree
      1482025, 3, // Flowery Tube

      4001011, 4, // Lupin Eraser
      4001010, 4, // Mushmom Eraser
      4001009, 4, // Stump Eraser

      2030008, 5, // Bottle, return scroll
      1442018, 3, // Frozen Tuna
      2040900, 4, // Shield for DEF
      2000005, 10, // Power Elixir
      2000004, 10, // Elixir
      4280000, 4}; // Gold Box
  public final static int[] silverrewards = {
      3010041, 1, // skull throne
      1002452, 3, // Starry Bandana
      1002455, 3, // Starry Bandana
      2290084, 1, // Triple Throw 20
      2290048, 1, // Genesis 20
      2290040, 1, // Meteo 20
      2290046, 1, // Blizzard 20
      2290074, 1, // Sniping 20
      2290064, 1, // Concentration 20
      2290094, 1, // Smoke 20
      2290022, 1, // Berserk 20
      2290056, 1, // Bow Expert 30
      2290066, 1, // xBow Expert 30
      2290020, 1, // Sanc 20
      1102082, 1, // Black Raggdey Cape
      1302049, 1, // Glowing Whip
      2340000, 1, // White Scroll
      1102041, 1, // Pink Cape
      1452019, 2, // White Nisrock
      4001116, 3, // Hexagon Pend
      4001012, 3, // Wraith Eraser
      1022060, 2, // Foxy Racoon Eye

      1432011, 3, // Fair Frozen
      1442020, 3, // HellSlayer
      1382035, 3, // Blue Marine
      1372010, 3, // Dimon Wand
      1332027, 3, // Varkit
      1302056, 3, // Sparta
      1402005, 3, // Bezerker
      1472053, 3, // Red Craven
      1462018, 3, // Casa Crow
      1452017, 3, // Metus
      1422013, 3, // Lemonite
      1322029, 3, // Ruin Hammer
      1412010, 3, // Colonian Axe

      1002587, 3, // Black Wisconsin
      1402044, 1, // Pumpkin lantern
      2101013, 4, // Summoning Showa boss
      1442046, 1, // Super Snowboard
      1422031, 1, // Blue Seal Cushion
      1332054, 3, // Lonzege Dagger
      1012056, 3, // Dog Nose
      1022047, 3, // Owl Mask
      3012002, 1, // Bathtub
      1442012, 3, // Sky snowboard
      1442018, 3, // Frozen Tuna
      1432010, 3, // Omega Spear
      1432036, 1, // Fishing Pole
      2000005, 10, // Power Elixir
      2000004, 10, // Elixir
      4280001, 4}; // Silver Box
  public static int[] eventCommonReward = {
      0, 40,
      1, 10,
      5060003, 18,
      4170023, 18,
      4280000, 3,
      4280001, 4,
      5490000, 3,
      5490001, 4
  };
  public static int[] eventUncommonReward = {
      2, 4,
      3, 4,
      2022179, 5,
      5062000, 10,
      2430082, 10,
      2430092, 10,
      2430103, 1,
      2430117, 1,
      2430118, 1,
      2430201, 2,
      2430228, 2,
      2430229, 2,
      2430136, 2,
      5220000, 14,
      2022459, 5,
      2022460, 5,
      2022461, 5,
      2022462, 5,
      2022463, 5,
      5050000, 2,
      4080100, 5,
      4080000, 5
  };
  public static int[] eventRareReward = {
      2049100, 5,
      2049116, 1,
      2049401, 10,
      2049301, 20,
      2049400, 3,
      2340000, 1,
      3010130, 5,
      3010131, 5,
      3010132, 5,
      3010133, 5,
      3010136, 5,
      3010116, 5,
      3010117, 5,
      3010118, 5,
      1112405, 1,
      1022097, 1,
      2040211, 1,
      2040212, 1,
      2049000, 2,
      2049001, 2,
      2049002, 2,
      2049003, 2,
      1012058, 2,
      1012059, 2,
      1012060, 2,
      1012061, 2
  };
  public static int[] eventSuperReward = {
      2022121, 10,
      4031307, 50,
      3010127, 10,
      3010128, 10,
      3010137, 10,
      2049300, 10,
      1112427, 10,
      1112428, 10,
      1112429, 10
  };
  public static int[] fishingReward = {
      0, 90, // Meso
      1, 70, // EXP
      2022179, 1, // Onyx Apple
      1302021, 5, // Pico Pico Hammer
      1072238, 1, // Voilet Snowshoe
      1072239, 1, // Yellow Snowshoe
      2049100, 1, // Chaos Scroll
      2049301, 1, // Equip Enhancer Scroll
      2049401, 1, // Potential Scroll
      1302000, 3, // Sword
      1442011, 1, // Surfboard
      4000517, 8, // Golden Fish
      4000518, 25, // Golden Fish Egg
      4031627, 2, // White Bait (3cm)
      4031628, 1, // Sailfish (120cm)
      4031630, 1, // Carp (30cm)
      4031631, 1, // Salmon(150cm)
      4031632, 1, // Shovel
      4031633, 2, // Whitebait (3.6cm)
      4031634, 1, // Whitebait (5cm)
      4031635, 1, // Whitebait (6.5cm)
      4031636, 1, // Whitebait (10cm)
      4031637, 2, // Carp (53cm)
      4031638, 2, // Carp (60cm)
      4031639, 1, // Carp (100cm)
      4031640, 1, // Carp (113cm)
      4031641, 2, // Sailfish (128cm)
      4031642, 2, // Sailfish (131cm)
      4031643, 1, // Sailfish (140cm)
      4031644, 1, // Sailfish (148cm)
      4031645, 2, // Salmon (166cm)
      4031646, 2, // Salmon (183cm)
      4031647, 1, // Salmon (227cm)
      4031648, 1, // Salmon (288cm)
      4031629, 1 // Pot
  };

  public static boolean isDragonItem(int itemId) {
    switch (itemId) {
      case 1372032:
      case 1312031:
      case 1412026:
      case 1302059:
      case 1442045:
      case 1402036:
      case 1432038:
      case 1422028:
      case 1472051:
      case 1472052:
      case 1332049:
      case 1332050:
      case 1322052:
      case 1452044:
      case 1462039:
      case 1382036:
      case 1342010:
        return true;
      default:
        return false;
    }
  }

  public static boolean isReverseItem(int itemId) {
    switch (itemId) {
      case 1002790:
      case 1002791:
      case 1002792:
      case 1002793:
      case 1002794:
      case 1082239:
      case 1082240:
      case 1082241:
      case 1082242:
      case 1082243:
      case 1052160:
      case 1052161:
      case 1052162:
      case 1052163:
      case 1052164:
      case 1072361:
      case 1072362:
      case 1072363:
      case 1072364:
      case 1072365:

      case 1302086:
      case 1312038:
      case 1322061:
      case 1332075:
      case 1332076:
      case 1372045:
      case 1382059:
      case 1402047:
      case 1412034:
      case 1422038:
      case 1432049:
      case 1442067:
      case 1452059:
      case 1462051:
      case 1472071:
      case 1482024:
      case 1492025:

      case 1342012:
        return true;
      default:
        return false;
    }
  }

  public static boolean isTimelessItem(int itemId) {
    switch (itemId) {
      case 1032031: //shield earring, but technically
      case 1102172:
      case 1002776:
      case 1002777:
      case 1002778:
      case 1002779:
      case 1002780:
      case 1082234:
      case 1082235:
      case 1082236:
      case 1082237:
      case 1082238:
      case 1052155:
      case 1052156:
      case 1052157:
      case 1052158:
      case 1052159:
      case 1072355:
      case 1072356:
      case 1072357:
      case 1072358:
      case 1072359:
      case 1092057:
      case 1092058:
      case 1092059:

      case 1122011:
      case 1122012:

      case 1302081:
      case 1312037:
      case 1322060:
      case 1332073:
      case 1332074:
      case 1372044:
      case 1382057:
      case 1402046:
      case 1412033:
      case 1422037:
      case 1432047:
      case 1442063:
      case 1452057:
      case 1462050:
      case 1472068:
      case 1482023:
      case 1492023:
      case 1342011:
        return true;
      default:
        return false;
    }
  }

  public static boolean isRing(int itemId) {
    return itemId >= 1112000 && itemId < 1113000;
  }// 112xxxx - pendants, 113xxxx - belts

  //if only there was a way to find in wz files -.-
  public static boolean isEffectRing(int itemid) {
    return isFriendshipRing(itemid) || isCrushRing(itemid) || isMarriageRing(itemid);
  }

  public static boolean isMarriageRing(int itemId) {
    switch (itemId) {
      case 1112803:
      case 1112806:
      case 1112807:
      case 1112809:
        return true;
    }
    return false;
  }

  public static boolean isFriendshipRing(int itemId) {
    switch (itemId) {
      case 1112800:
      case 1112801:
      case 1112802:
      case 1112810: //new
      case 1112811: //new, doesnt work in friendship?
      case 1112812: //new, im ASSUMING it's friendship cuz of itemID, not sure.

      case 1049000:
        return true;
    }
    return false;
  }

  public static boolean isCrushRing(int itemId) {
    switch (itemId) {
      case 1112001:
      case 1112002:
      case 1112003:
      case 1112005: //new
      case 1112006: //new
      case 1112007:
      case 1112012:
      case 1112015: //new

      case 1048000:
        return true;
    }
    return false;
  }

  public static int[] Equipments_Bonus = {1122017};

  public static int Equipment_Bonus_EXP(final int itemid) { // TODO : Add Time for more exp increase
    switch (itemid) {
      case 1122017:
        return 10;
    }
    return 0;
  }

  public static int[] blockedMaps = {109050000, 280030000, 240060200, 280090000, 280030001, 240060201, 950101100, 950101010};
  //If you can think of more maps that could be exploitable via npc,block nao pliz!

  public static int getExpForLevel(int i, int itemId) {
    if (isReverseItem(itemId)) {
      return getReverseRequiredEXP(i);
    } else if (getMaxLevel(itemId) > 0) {
      return getTimelessRequiredEXP(i);
    }
    return 0;
  }

  public static int getMaxLevel(final int itemId) {
    if (isTimelessItem(itemId)) {
      return 5;
    } else if (isReverseItem(itemId)) {
      return 3;
    } else {
      switch (itemId) {
        case 1302109:
        case 1312041:
        case 1322067:
        case 1332083:
        case 1372048:
        case 1382064:
        case 1402055:
        case 1412037:
        case 1422041:
        case 1432052:
        case 1442073:
        case 1452064:
        case 1462058:
        case 1472079:
        case 1482035:

        case 1302108:
        case 1312040:
        case 1322066:
        case 1332082:
        case 1372047:
        case 1382063:
        case 1402054:
        case 1412036:
        case 1422040:
        case 1432051:
        case 1442072:
        case 1452063:
        case 1462057:
        case 1472078:
        case 1482036:
          return 1;

        case 1072376:
          return 2;
      }
    }
    return 0;
  }

  public static int getStatChance() {
    return 25;
  }

  public static MonsterStatus getStatFromWeapon(final int itemid) {
    switch (itemid) {
      case 1302109:
      case 1312041:
      case 1322067:
      case 1332083:
      case 1372048:
      case 1382064:
      case 1402055:
      case 1412037:
      case 1422041:
      case 1432052:
      case 1442073:
      case 1452064:
      case 1462058:
      case 1472079:
      case 1482035:
        return MonsterStatus.ACC;
      case 1302108:
      case 1312040:
      case 1322066:
      case 1332082:
      case 1372047:
      case 1382063:
      case 1402054:
      case 1412036:
      case 1422040:
      case 1432051:
      case 1442072:
      case 1452063:
      case 1462057:
      case 1472078:
      case 1482036:
        return MonsterStatus.SPEED;
    }
    return null;
  }

  public static int getXForStat(MonsterStatus stat) {
    switch (stat) {
      case ACC:
        return -70;
      case SPEED:
        return -50;
      default:
    }
    return 0;
  }

  public static int getSkillForStat(MonsterStatus stat) {
    switch (stat) {
      case ACC:
        return 3221006;
      case SPEED:
        return 3121007;
      default:
    }
    return 0;
  }

  public final static int[] normalDrops = {
      4001009, //real
      4001010,
      4001011,
      4001012,
      4001013,
      4001014, //real
      4001021,
      4001038, //fake
      4001039,
      4001040,
      4001041,
      4001042,
      4001043, //fake
      4001038, //fake
      4001039,
      4001040,
      4001041,
      4001042,
      4001043, //fake
      4001038, //fake
      4001039,
      4001040,
      4001041,
      4001042,
      4001043, //fake
      4000164, //start
      2000000,
      2000003,
      2000004,
      2000005,
      4000019,
      4000000,
      4000016,
      4000006,
      2100121,
      4000029,
      4000064,
      5110000,
      4000306,
      4032181,
      4006001,
      4006000,
      2050004,
      3994102,
      3994103,
      3994104,
      3994105,
      2430007, //end
      4000164, //start
      2000000,
      2000003,
      2000004,
      2000005,
      4000019,
      4000000,
      4000016,
      4000006,
      2100121,
      4000029,
      4000064,
      5110000,
      4000306,
      4032181,
      4006001,
      4006000,
      2050004,
      3994102,
      3994103,
      3994104,
      3994105,
      2430007, //end
      4000164, //start
      2000000,
      2000003,
      2000004,
      2000005,
      4000019,
      4000000,
      4000016,
      4000006,
      2100121,
      4000029,
      4000064,
      5110000,
      4000306,
      4032181,
      4006001,
      4006000,
      2050004,
      3994102,
      3994103,
      3994104,
      3994105,
      2430007}; //end
  public final static int[] rareDrops = {
      2022179,
      2049100,
      2049301,
      2049401,
      2022326,
      2022193,
      2049000,
      2049001,
      2049002};
  public final static int[] superDrops = {
      2040804,
      2049400,
      2049100};

  public static int getSkillBook(final int job) {
    if (job >= 2210 && job <= 2218) {
      return job - 2209;
    }
    switch (job) {
      case 3210:
      case 3310:
      case 3510:
        return 1;
      case 3211:
      case 3311:
      case 3511:
        return 2;
      case 3212:
      case 3312:
      case 3512:
        return 3;
    }
    return 0;
  }

  public static int getSkillBookForSkill(final int skillid) {
    return getSkillBook(skillid / 10000);
  }

  public static int getMountItem(final int sourceid) {
    switch (sourceid) {
      case 5221006:
        return 1932000;
      case 33001001: //temp.
        return 1932015;
      case 35001002:
      case 35120000:
        return 1932016;
      case 1013:
      case 10001013:
      case 20001013:
      case 20011013:
      case 30001013:
      case 1046:
      case 10001046:
      case 20001046:
      case 20011046:
      case 30001046:
        return 1932001;
      case 1015:
      case 10001015:
      case 20001015:
      case 20011015:
      case 30001015:
      case 1048:
      case 10001048:
      case 20001048:
      case 20011048:
      case 30001048:
        return 1932002;
      case 1016:
      case 10001016:
      case 20001016:
      case 20011016:
      case 30001016:
      case 1017:
      case 10001017:
      case 20001017:
      case 20011017:
      case 30001017:
      case 1027:
      case 10001027:
      case 20001027:
      case 20011027:
      case 30001027:
        return 1932007;
      case 1018:
      case 10001018:
      case 20001018:
      case 20011018:
      case 30001018:
        return 1932003;
      case 1019:
      case 10001019:
      case 20001019:
      case 20011019:
      case 30001019:
        return 1932005;
      case 1025:
      case 10001025:
      case 20001025:
      case 20011025:
      case 30001025:
        return 1932006;
      case 1028:
      case 10001028:
      case 20001028:
      case 20011028:
      case 30001028:
        return 1932008;
      case 1029:
      case 10001029:
      case 20001029:
      case 20011029:
      case 30001029:
        return 1932009;
      case 1030:
      case 10001030:
      case 20001030:
      case 20011030:
      case 30001030:
        return 1932011;
      case 1031:
      case 10001031:
      case 20001031:
      case 20011031:
      case 30001031:
        return 1932010;
      case 1034:
      case 10001034:
      case 20001034:
      case 20011034:
      case 30001034:
        return 1932014;
      case 1035:
      case 10001035:
      case 20001035:
      case 20011035:
      case 30001035:
        return 1932012;
      case 1036:
      case 10001036:
      case 20001036:
      case 20011036:
      case 30001036:
        return 1932017;
      case 1037:
      case 10001037:
      case 20001037:
      case 20011037:
      case 30001037:
        return 1932018;
      case 1038:
      case 10001038:
      case 20001038:
      case 20011038:
      case 30001038:
        return 1932019;
      case 1039:
      case 10001039:
      case 20001039:
      case 20011039:
      case 30001039:
        return 1932020;
      case 1040:
      case 10001040:
      case 20001040:
      case 20011040:
      case 30001040:
        return 1932021;
      case 1042:
      case 10001042:
      case 20001042:
      case 20011042:
      case 30001042:
        return 1932022;
      case 1044:
      case 10001044:
      case 20001044:
      case 20011044:
      case 30001044:
        return 1932023;
      case 1045:
      case 10001045:
      case 20001045:
      case 20011045:
      case 30001045:
        return 1932030; //wth? helicopter? i didnt see one, so we use hog
      case 1049:
      case 10001049:
      case 20001049:
      case 20011049:
      case 30001049:
        return 1932025;
      case 1050:
      case 10001050:
      case 20001050:
      case 20011050:
      case 30001050:
        return 1932004;
      case 1051:
      case 10001051:
      case 20001051:
      case 20011051:
      case 30001051:
        return 1932026;
      case 1052:
      case 10001052:
      case 20001052:
      case 20011052:
      case 30001052:
        return 1932027;
      case 1053:
      case 10001053:
      case 20001053:
      case 20011053:
      case 30001053:
        return 1932028;
      case 1054:
      case 10001054:
      case 20001054:
      case 20011054:
      case 30001054:
        return 1932029;
      case 1069:
      case 10001069:
      case 20001069:
      case 20011069:
      case 30001069:
        return 1932038;
      case 1096:
      case 10001096:
      case 20001096:
      case 20011096:
      case 30001096:
        return 1932045;
      case 1101:
      case 10001101:
      case 20001101:
      case 20011101:
      case 30001101:
        return 1932046;
      case 1102:
      case 10001102:
      case 20001102:
      case 20011102:
      case 30001102:
        return 1932047;
      case 1065:
      case 10001065:
      case 20001065:
      case 20011065:
      case 30001065:
        return 1932037;
      case 1070:
      case 10001070:
      case 20001070:
      case 20011070:
      case 30001070:
        return 1932039;
      case 1071:
      case 10001071:
      case 20001071:
      case 20011071:
      case 30001071:
        return 1932040;
      //1932013
      //1983008
      //1902049
      default:
        return 0;
    }
  }

  public static boolean isKatara(int itemId) {
    return itemId / 10000 == 134;
  }

  public static boolean isDagger(int itemId) {
    return itemId / 10000 == 133;
  }

  public static boolean isApplicableSkill(int skil) {
    return skil < 40000000 && (skil % 10000 < 8000 || skil % 10000 > 8003); //no additional/decent skills
  }

  public static boolean isApplicableSkill_(int skil) { //not applicable to saving but is more of temporary
    return skil >= 90000000 || (skil % 10000 >= 8000 && skil % 10000 <= 8003);
  }

  public static boolean isTablet(int itemId) {
    return itemId / 1000 == 2047;
  }

  public static int getSuccessTablet(final int scrollId, final int level) {
    if (scrollId % 1000 / 100 == 2) { //2047_2_00 = armor, 2047_3_00 = accessory
      switch (level) {
        case 0:
          return 70;
        case 1:
          return 55;
        case 2:
          return 43;
        case 3:
          return 33;
        case 4:
          return 26;
        case 5:
          return 20;
        case 6:
          return 16;
        case 7:
          return 12;
        case 8:
          return 10;
        default:
          return 7;
      }
    } else if (scrollId % 1000 / 100 == 3) {
      switch (level) {
        case 0:
          return 70;
        case 1:
          return 35;
        case 2:
          return 18;
        case 3:
          return 12;
        default:
          return 7;
      }
    } else {
      switch (level) {
        case 0:
          return 70;
        case 1:
          return 50; //-20
        case 2:
          return 36; //-14
        case 3:
          return 26; //-10
        case 4:
          return 19; //-7
        case 5:
          return 14; //-5
        case 6:
          return 10; //-4
        default:
          return 7;  //-3
      }
    }
  }

  public static int getCurseTablet(final int scrollId, final int level) {
    if (scrollId % 1000 / 100 == 2) { //2047_2_00 = armor, 2047_3_00 = accessory
      switch (level) {
        case 0:
          return 10;
        case 1:
          return 12;
        case 2:
          return 16;
        case 3:
          return 20;
        case 4:
          return 26;
        case 5:
          return 33;
        case 6:
          return 43;
        case 7:
          return 55;
        case 8:
          return 70;
        default:
          return 100;
      }
    } else if (scrollId % 1000 / 100 == 3) {
      switch (level) {
        case 0:
          return 12;
        case 1:
          return 18;
        case 2:
          return 35;
        case 3:
          return 70;
        default:
          return 100;
      }
    } else {
      switch (level) {
        case 0:
          return 10;
        case 1:
          return 14; //+4
        case 2:
          return 19; //+5
        case 3:
          return 26; //+7
        case 4:
          return 36; //+10
        case 5:
          return 50; //+14
        case 6:
          return 70; //+20
        default:
          return 100;  //+30
      }
    }
  }

  public static boolean isAccessory(final int itemId) {
    return (itemId >= 1010000 && itemId < 1040000) || (itemId >= 1122000 && itemId < 1153000) || (itemId >= 1112000 && itemId < 1113000);
  }

  public static boolean potentialIDFits(final int potentialID, final int newstate, final int i) {
    //first line is always the best
    //but, sometimes it is possible to get second/third line as well
    //may seem like big chance, but it's not as it grabs random potential ID anyway
    if (newstate == 7) {
      return (i == 0 || Randomizer.nextInt(10) == 0 ? potentialID >= 30000 : potentialID >= 20000 && potentialID < 30000);
    } else if (newstate == 6) {
      return (i == 0 || Randomizer.nextInt(10) == 0 ? potentialID >= 20000 && potentialID < 30000 : potentialID >= 10000 && potentialID < 20000);
    } else if (newstate == 5) {
      return (i == 0 || Randomizer.nextInt(10) == 0 ? potentialID >= 10000 && potentialID < 20000 : potentialID < 10000);
    } else {
      return false;
    }
  }

  public static boolean optionTypeFits(final int optionType, final int itemId) {
    switch (optionType) {
      case 10: //weapon
        return isWeapon(itemId);
      case 11: //any armor
        return !isWeapon(itemId);
      case 20: //shield??????????
        return itemId / 10000 == 109; //just a gues
      case 21: //pet equip?????????
        return itemId / 10000 == 180; //???LOL
      case 40: //face accessory
        return isAccessory(itemId);
      case 51: //hat
        return itemId / 10000 == 100;
      case 52: //cape
        return itemId / 10000 == 110;
      case 53: //top/bottom/overall
        return itemId / 10000 == 104 || itemId / 10000 == 105 || itemId / 10000 == 106;
      case 54: //glove
        return itemId / 10000 == 108;
      case 55: //shoe
        return itemId / 10000 == 107;
      case 90:
        return false; //half this stuff doesnt even work
      default:
        return true;
    }
  }

  public static final boolean isMountItemAvailable(final int mountid, final int jobid) {
    if (jobid != 900 && mountid / 10000 == 190) {
      if (isKOC(jobid)) {
        if (mountid < 1902005 || mountid > 1902007) {
          return false;
        }
      } else if (isAdventurer(jobid)) {
        if (mountid < 1902000 || mountid > 1902002) {
          return false;
        }
      } else if (isAran(jobid)) {
        if (mountid < 1902015 || mountid > 1902018) {
          return false;
        }
      } else if (isEvan(jobid)) {
        if (mountid < 1902040 || mountid > 1902042) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean isEvanDragonItem(final int itemId) {
    return itemId >= 1940000 && itemId < 1980000; //194 = mask, 195 = pendant, 196 = wings, 197 = tail
  }

  public static boolean canScroll(final int itemId) {
    return itemId / 100000 != 19 && itemId / 100000 != 16; //no mech/taming/dragon
  }

  public static boolean canHammer(final int itemId) {
    switch (itemId) {
      case 1122000:
      case 1122076: //ht, chaos ht
        return false;
    }
    if (!canScroll(itemId)) {
      return false;
    }
    return true;
  }

  public static int[] owlItems = new int[] {
      1082002, // work gloves
      2070005,
      2070006,
      1022047,
      1102041,
      2044705,
      2340000, // white scroll
      2040017,
      1092030,
      2040804};

  public static int getMasterySkill(final int job) {
    if (job >= 1410 && job <= 1412) {
      return 14100000;
    } else if (job >= 410 && job <= 412) {
      return 4100000;
    } else if (job >= 520 && job <= 522) {
      return 5200000;
    }
    return 0;
  }

  public static int getExpRate_Below10(final int job) {
    if (GameConstants.isEvan(job)) {
      return 1;
    } else if (GameConstants.isAran(job) || GameConstants.isKOC(job)) {
      return 1;
    }
    return 10;
  }

  public static int getExpRate_Quest(final int level) {
    if (level < 30) {
      return 1;
    }
    if (level >= 30 && level < 50) {
      return 2;
    }
    if (level >= 50 && level < 70) {
      return 4;
    }
    return 5;
  }


  public static boolean isCustomReactItem(final int rid, final int iid, final int original) {
    if (rid == 2008006) { //orbis pq LOL
      return iid == (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 4001055);
      //4001056 = sunday. 4001062 = saturday
    } else if (rid == 5022000) { // Reactor in miners map
      return iid == 4031757; // Antelion relic
    } else {
      return iid == original;
    }
  }

  public static int getJobNumber(int jobz) {
    int job = (jobz % 1000);
    if (job / 100 == 0) {
      return 0; //beginner
    } else if (job / 10 == 0) {
      return 1;
    } else {
      return 2 + (job % 10);
    }
  }

  public static boolean isForceRespawn(int mapid) {
    switch (mapid) {
      case 925100100: //crocs and stuff
        return true;
      default:
        return mapid / 100000 == 9800 && (mapid % 10 == 1 || mapid % 1000 == 100);
    }
  }

  public static int getFishingTime(boolean vip, boolean gm) {
    return gm ? 1000 : (vip ? 30000 : 60000);
  }

  public static int getCustomSpawnID(int summoner, int def) {
    switch (summoner) {
      case 9400589:
      case 9400748: //MV
        return 9400706; //jr
      default:
        return def;
    }
  }

  public static boolean canForfeit(int questid) {
    switch (questid) {
      case 20000:
      case 20010:
      case 20015: //cygnus quests
      case 20020:
        return false;
      default:
        return true;
    }
  }

  public static short getSlotMax(int itemId) {
    switch (itemId) {
      case 4030003: // Tetris
      case 4030004: // Tetris
      case 4030005: // Tetris
        return 1;
      case 4001458: // Crystanol Fragment
        return 20;
      case 4031753: // Zeta Residue
        return 2000;
      default:
        return 0;
    }
  }

  public static int getFamilyMultiplier(final int level) {
    if (level >= 1 && level <= 10) {
      return 20;
    } else if (level >= 11 && level <= 39) {
      return 22;
    } else if (level >= 40 && level <= 59) {
      return 24;
    } else if (level >= 60 && level <= 79) {
      return 26;
    } else if (level >= 80 && level <= 99) {
      return 28;
    } else if (level >= 100 && level <= 119) {
      return 30;
    } else if (level >= 120 && level <= 139) {
      return 32;
    } else if (level >= 140 && level <= 159) {
      return 34;
    } else if (level >= 160 && level <= 179) {
      return 36;
    } else if (level >= 180 && level <= 199) {
      return 38;
    }
    return 40; // lvl 200
  }

  public final static int[] cashSurpriseRewards = {
      50200004, 3,
      50200069, 3,
      50200117, 3,
      50100008, 3,
      50000047, 3,
      10002819, 3,
      50100010, 1,
      50200001, 3,
      60000073, 3
  };

  public static boolean isVisitorEquip(final int itemid) {
    switch (itemid) {
      case 1003116: // Visitor Helmet
      case 1082278: // Visitor Gloves
      case 1052278: // Visitor Suit
      case 1072450: // Visitor Boots
        return true;
    }
    return false;
  }

  public static boolean isVisitorSkill(final int skill) {
    switch (skill) {
      case 1066:
      case 10001066:
      case 20001066:
      case 20011066:
      case 30001066:
      case 1067:
      case 10001067:
      case 20001067:
      case 20011067:
      case 30001067:
        return true;
    }
    return false;
  }

  public static boolean skillBelongToJob(final int skillid, MapleJob job) {
    if (JobConstants.isFixedSkill(skillid)) {
      if (skillid >= 10000000 && skillid < 20000000) { // koc skills
        if ((skillid / 10000) <= job.getId()) {
          if (!GameConstants.isJobFamily((skillid / 10000), job.getId())) {
            return false;
          }
        }
      } else if (skillid >= 10000000 && skillid < 30000000) {
        if (GameConstants.isEvan((skillid / 10000))) {
          if ((skillid / 10000) <= job.getId()) {
            if (!GameConstants.isJobFamily((skillid / 10000), job.getId())) {
              return false;
            }
          }
        } else if (GameConstants.isAran((skillid / 10000))) {
          if ((skillid / 10000) <= job.getId()) {
            if (!GameConstants.isJobFamily((skillid / 10000), job.getId())) {
              return false;
            }
          }
        } else {
          return false;
        }
      } else { // All explorer skills
        if (skillid >= 1000000) {
          if (!GameConstants.isJobFamily((skillid / 10000), job.getId())) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
    return true;
  }


}