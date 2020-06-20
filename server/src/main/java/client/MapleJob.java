/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package client;

public enum MapleJob {
  BEGINNER(0, "Beginner"),
  WARRIOR(100, "Warrior"),
  FIGHTER(110, "Fighter"),
  CRUSADER(111, "Crusader"),
  HERO(112, "Hero"),
  PAGE(120, "Page"),
  WHITEKNIGHT(121, "White Knight"),
  PALADIN(122, "Paladin"),
  SPEARMAN(130, "Spearman"),
  DRAGONKNIGHT(131, "Dragon Knight"),
  DARKKNIGHT(132, "Dark Knight"),
  MAGICIAN(200, "Magician"),
  FP_WIZARD(210, "Wizard (Fire,Poison)"),
  FP_MAGE(211, "Wizard (Fire,Poison)"),
  FP_ARCHMAGE(212, "Archmage (Fire,Poison)"),
  IL_WIZARD(220, "Wizard (Ice,Lightning)"),
  IL_MAGE(221, "Wizard (Ice,Lightning)"),
  IL_ARCHMAGE(222, "Wizard (Ice,Lightning)"),
  CLERIC(230, "Cleric"),
  PRIEST(231, "Priest"),
  BISHOP(232, "Bishop"),
  BOWMAN(300, "Bowman"),
  HUNTER(310, "Hunter"),
  RANGER(311, "Ranger"),
  BOWMASTER(312, "Bowmaster"),
  CROSSBOWMAN(320, "Crossbowman"),
  SNIPER(321, "Sniper"),
  MARKSMAN(322, "Marksman"),
  THIEF(400, "Thief"),
  ASSASSIN(410, "Assasin"),
  HERMIT(411, "Hermit"),
  NIGHTLORD(412, "Nightlord"),
  BANDIT(420, "Bandit"),
  CHIEFBANDIT(421, "Chief Bandit"),
  SHADOWER(422, "Shadower"),
  BLADE_RECRUIT(430, "Blade Recruit"),
  BLADE_ACOLYTE(431, "Blade Acolyte"),
  BLADE_SPECIALIST(432, "Blade Specialist"),
  BLADE_LORD(433, "Blade Lord"),
  BLADE_MASTER(434, "Blade Master"),
  PIRATE(500, "Pirate"),
  BRAWLER(510, "Brawler"),
  MARAUDER(511, "Marauder"),
  BUCCANEER(512, "Buccaneer"),
  GUNSLINGER(520, "Gunslinger"),
  OUTLAW(521, "Outlaw"),
  CORSAIR(522, "Corsair"),
  MAPLELEAF_BRIGADIER(800, "Manager"),
  GM(900, "GameMaster"),
  SUPERGM(910, "GameMaster"),
  NOBLESSE(1000, "Dawn Warrior"),
  DAWNWARRIOR1(1100, "Dawn Warrior"),
  DAWNWARRIOR2(1110, "Dawn Warrior"),
  DAWNWARRIOR3(1111, "Dawn Warrior"),
  DAWNWARRIOR4(1112, "Dawn Warrior"),
  BLAZEWIZARD1(1200, "Blaze Wizard"),
  BLAZEWIZARD2(1210, "Blaze Wizard"),
  BLAZEWIZARD3(1211, "Blaze Wizard"),
  BLAZEWIZARD4(1212, "Blaze Wizard"),
  WINDARCHER1(1300, "Wind Archer"),
  WINDARCHER2(1310, "Wind Archer"),
  WINDARCHER3(1311, "Wind Archer"),
  WINDARCHER4(1312, "Wind Archer"),
  NIGHTWALKER1(1400, "Night Walker"),
  NIGHTWALKER2(1410, "Night Walker"),
  NIGHTWALKER3(1411, "Night Walker"),
  NIGHTWALKER4(1412, "Night Walker"),
  THUNDERBREAKER1(1500, "Thrunder Breaker"),
  THUNDERBREAKER2(1510, "Thrunder Breaker"),
  THUNDERBREAKER3(1511, "Thrunder Breaker"),
  THUNDERBREAKER4(1512, "Thrunder Breaker"),
  LEGEND(2000, "Legend"),
  EVAN1(2001, "Evan"),
  ARAN2(2100, "Aran"),
  ARAN3(2110, "Aran"),
  ARAN4(2111, "Aran"),
  ARAN5(2112, "Aran"),
  EVAN2(2200, "Evan"),
  EVAN3(2210, "Evan"),
  EVAN4(2211, "Evan"),
  EVAN5(2212, "Evan"),
  EVAN6(2213, "Evan"),
  EVAN7(2214, "Evan"),
  EVAN8(2215, "Evan"),
  EVAN9(2216, "Evan"),
  EVAN10(2217, "Evan"),
  EVAN11(2218, "Evan"),
  ADDITIONAL_SKILLS(9000, "???");
  private final int jobid;
  private final String name;

  private MapleJob(int id, String name) {
    jobid = id;
    this.name = name;
  }

  public int getId() {
    return jobid;
  }

  public static MapleJob getById(int id) {
    for (MapleJob l : MapleJob.values()) {
      if (l.getId() == id) {
        return l;
      }
    }
    return null;
  }

  public static MapleJob getBy5ByteEncoding(int encoded) {
    switch (encoded) {
      case 2:
        return WARRIOR;
      case 4:
        return MAGICIAN;
      case 8:
        return BOWMAN;
      case 16:
        return THIEF;
      case 32:
        return PIRATE;
      case 1024:
        return NOBLESSE;
      case 2048:
        return DAWNWARRIOR1;
      case 4096:
        return BLAZEWIZARD1;
      case 8192:
        return WINDARCHER1;
      case 16384:
        return NIGHTWALKER1;
      case 32768:
        return THUNDERBREAKER1;
      case 65536:
        return LEGEND;
      default:
        return BEGINNER;
    }
  }

  public boolean isA(MapleJob basejob) {
    return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
  }

  public boolean isAnEvan() {
    return getId() == 2001 || getId() / 100 == 22;
  }

  public static boolean isExtendSPJob(int jobId) {
    return jobId / 1000 == 3 || jobId / 100 == 22 || jobId == 2001;
  }

  public static boolean isExtendSPJob(MapleJob job) {
    return isExtendSPJob(job.getId());
  }

  public String getName() {
    return this.name;
  }
}
