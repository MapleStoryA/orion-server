package constants;

import static client.MapleJob.BEGINNER;
import static client.MapleJob.BLAZEWIZARD1;
import static client.MapleJob.BOWMAN;
import static client.MapleJob.DAWNWARRIOR1;
import static client.MapleJob.LEGEND;
import static client.MapleJob.MAGICIAN;
import static client.MapleJob.NIGHTWALKER1;
import static client.MapleJob.NOBLESSE;
import static client.MapleJob.PIRATE;
import static client.MapleJob.THIEF;
import static client.MapleJob.THUNDERBREAKER1;
import static client.MapleJob.WARRIOR;
import static client.MapleJob.WINDARCHER1;

import client.MapleJob;
import client.skill.ISkill;
import client.skill.SkillEntry;
import client.skill.SkillFactory;
import database.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobConstants {

    public static final int[] evanSkills = {
        22001001, 20, // Magic Missile
        22000000, 20, // Dragon Soul
        22101000, 20, // Fire Circle
        22101001, 20, // Teleport
        22111000, 20, // Lightning bolt
        22111001, 20, // Magic guard
        22121000, 20, // Ice Breath
        22121001, 20, // Elemental Reset
        22131000, 20, // Magic Flare
        22131001, 20, // Magic Shield
        22140000, 15, // Critical Magic
        22141001, 20, // Dragon Thrust
        22141002, 15, // Magic Booster
        22141003, 15, // Slow
        22150000, 15, // Magic Amplifier
        22151001, 20, // Fire Breath
        22151002, 20, // Killer Wings
        22151003, 10, // Magic Resistance
        22160000, 10, // Dragon Fury
        22161001, 20, // Earthquake
        22161002, 20, // Phantom Imprint
        22161003, 15, // Recovery Aura
        22171000, 30, // Maple Warrior
        22170001, 30, // Magic Mastery
        22171002, 30, // Illusion
        22171003, 30, // Flame Wheel
        22171004, 5, // Hero's Will
        22181000, 30, // Blessing of onyx
        22181001, 30, // Blaze
        22181002, 30, // Dark Fog
        22181003, 30 // Soul Stone
    };
    private static final Map<ISkill, SkillEntry> evanSkillMap = new LinkedHashMap<>();
    private static final Map<ISkill, SkillEntry> gmSkillMap = new LinkedHashMap<>();
    private static final Map<ISkill, SkillEntry> normalSkillMap = new LinkedHashMap<>();
    private static final List<Integer> fixedSkill = new LinkedList<>(); // total of 515
    private static final List<Integer> evanSkillId = new LinkedList<>();

    public static void loadAllSkills() {
        ISkill skil;
        try (var con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps =
                            con.prepareStatement("SELECT skillid, skilllevel, masterlevel FROM wz_fixedskills");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skil = SkillFactory.getSkill(rs.getInt("skillid"));
                    if (skil != null && GameConstants.isApplicableSkill(rs.getInt("skillid"))) {
                        if ((rs.getInt("skillid") / 10000 == 900) || (rs.getInt("skillid") / 10000 == 910)) {
                            gmSkillMap.put(
                                    skil, new SkillEntry(rs.getByte("skilllevel"), rs.getByte("masterlevel"), -1));
                        } else {
                            if (rs.getInt("skillid") == 4341003) { // This don't even work (Monster bomb)
                                // continue;
                            }
                            normalSkillMap.put(
                                    skil, new SkillEntry(rs.getByte("skilllevel"), rs.getByte("masterlevel"), -1));
                        }
                        fixedSkill.add(rs.getInt("skillid"));
                    }
                }
            }
        } catch (SQLException e) {
            log.info("Failed to load main skills. Reason: " + e);
        }

        int lastId = 0;
        for (int i = 0; i < evanSkills.length; i++) {
            if (i % 2 == 0) { // Even
                lastId = evanSkills[i];
            } else { // Odd (Starts 1st)
                if (lastId == 0) {
                    log.info("Error in getting evan skills..."); // Shouldn't happen
                    continue;
                }
                skil = SkillFactory.getSkill(lastId);
                if (skil != null && GameConstants.isApplicableSkill(lastId)) {
                    evanSkillId.add(lastId);
                    evanSkillMap.put(skil, new SkillEntry((byte) evanSkills[i], (byte) evanSkills[i], -1));
                    fixedSkill.add(lastId);
                }
                lastId = 0;
            }
        }
        log.info("Successfully loaded "
                + (evanSkillMap.size() + normalSkillMap.size() + gmSkillMap.size())
                + " skills.");
    }

    public static boolean isFixedSkill(final int skillId) {
        boolean ret = fixedSkill.contains(skillId);
        return ret;
    }

    public static boolean isEvanSkill(final int skillId) {
        return evanSkillId.contains(skillId);
    }

    public static Map<ISkill, SkillEntry> getNormalSkills() {
        return normalSkillMap;
    }

    public static Map<ISkill, SkillEntry> getSkillsFromJob(MapleJob job) {

        List<ISkill> list = normalSkillMap.keySet().stream()
                .filter(o -> GameConstants.skillBelongToJob(o.getId(), job))
                .collect(Collectors.toList());

        Map<ISkill, SkillEntry> newEntries = new HashMap<>();

        list.stream().forEach(o -> {
            newEntries.put(o, normalSkillMap.get(o));
        });

        return newEntries;
    }

    public static Map<ISkill, SkillEntry> getGMSkills() {
        return gmSkillMap;
    }

    public static Map<ISkill, SkillEntry> getEvanSkills() {
        return evanSkillMap;
    }

    public static Map<ISkill, SkillEntry> getQuestSkills() {
        Map<ISkill, SkillEntry> map = new HashMap<>();
        return map;
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
}
