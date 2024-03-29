package client;

import constants.GameConstants;
import constants.JobConstants;

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
    Manager(800, "Manager"),
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
    BattleMage1(3200, "BattleMage"),
    BattleMage2(3210, "WildHunter"),
    WildHunter1(3300, "WildHunter"),
    WildHunter2(3310, "WildHunter"),
    Mechanic1(3500, "WildHunter"),
    Mechanic2(3510, "WildHunter"),
    ADDITIONAL_SKILLS(9000, "???");
    private final int jobId;
    private final String name;

    MapleJob(int id, String name) {
        jobId = id;
        this.name = name;
    }

    public static MapleJob getById(int id) {
        for (var state : MapleJob.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        return null;
    }

    public static boolean isExtendSPJob(int jobId) {
        return jobId / 1000 == 3 || jobId / 100 == 22 || jobId == 2001;
    }

    public int getId() {
        return jobId;
    }

    public boolean isA(MapleJob basejob) {
        return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
    }

    public int getJobType() {
        return getId() / 1000;
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return (getId() / 100 == 21) || (getId() == 2000);
    }

    public boolean isDualblade() {
        return getId() >= 430 && getId() <= 434;
    }

    public boolean isEvan() {
        return (getId() == 2001 || getId() / 100 == 22);
    }

    public boolean isPirate() {
        return getId() >= 500 && getId() <= 522;
    }

    public boolean isMage() {
        return getId() >= 200 && getId() <= 232;
    }

    public boolean isWarrior() {
        return getId() >= 100 && getId() <= 132;
    }

    public boolean isBeginner() {
        return getId() == 0 || getId() == 1000 || getId() == 2000 || getId() == 2001 || getId() == 3000;
    }

    public int getJobCategoryForEquips() {
        if (isEvan()) {
            return 2;
        }
        if (isDualblade()) {
            return 4;
        }
        if (isAran()) {
            return 1;
        }
        return this.getId() / 100;
    }

    public boolean isSkillBelongToJob(final int skillId, boolean isGM) {
        if (isGM) {
            return true;
        }

        if (JobConstants.isFixedSkill(skillId)) {
            if (skillId >= 10000000 && skillId < 20000000) { // koc skills
                if ((skillId / 10000) <= getId()) {
                    return GameConstants.isJobFamily((skillId / 10000), getId());
                }
            } else if (skillId >= 10000000 && skillId < 30000000) {
                if (GameConstants.isEvan((skillId / 10000))) {
                    if ((skillId / 10000) <= getId()) {
                        return GameConstants.isJobFamily((skillId / 10000), getId());
                    }
                } else if (GameConstants.isAran((skillId / 10000))) {
                    if ((skillId / 10000) <= getId()) {
                        return GameConstants.isJobFamily((skillId / 10000), getId());
                    }
                } else {
                    return false;
                }
            } else { // All explorer skills
                if (skillId >= 1000000) {
                    return GameConstants.isJobFamily((skillId / 10000), getId());
                }
            }
        }
        return true;
    }

    public String getName() {
        return this.name;
    }

    public boolean isGameMasterJob() {
        return this.equals(MapleJob.GM) || this.equals(MapleJob.SUPERGM);
    }

    public static int[] get3rdJobMobMapInfo(MapleCharacter player) {
        int returnMapId = 0;
        int monsterId = 0;
        int mapId = 0;

        if (player.getJob() == MapleJob.FP_WIZARD
                || player.getJob() == MapleJob.IL_WIZARD
                || player.getJob() == MapleJob.CLERIC) {
            mapId = 108010200;
            returnMapId = 100040106;
            monsterId = 9001001;
        } else if (player.getJob() == MapleJob.FIGHTER
                || player.getJob() == MapleJob.PAGE
                || player.getJob() == MapleJob.SPEARMAN) {
            mapId = 108010300;
            returnMapId = 105070001;
            monsterId = 9001000;
        } else if (player.getJob() == MapleJob.ASSASSIN || player.getJob() == MapleJob.BANDIT) {
            mapId = 108010400;
            returnMapId = 107000402;
            monsterId = 9001003;
        } else if (player.getJob() == MapleJob.HUNTER || player.getJob() == MapleJob.CROSSBOWMAN) {
            mapId = 108010100;
            returnMapId = 105040305;
            monsterId = 9001002;
        } else if (player.getJob() == MapleJob.BRAWLER || player.getJob() == MapleJob.GUNSLINGER) {
            mapId = 108010500;
            returnMapId = 105040305;
            monsterId = 9001008;
        }

        return new int[] {mapId, returnMapId, monsterId};
    }
}
