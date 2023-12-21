package client.skill;

import client.MapleCharacter;
import client.MapleJob;

public class SkillHelper {

    public static void maxMastery(MapleCharacter player) {
        MapleJob job = player.getJob();
        for (ISkill skill_ : SkillFactory.getAllSkills()) {
            try {
                int skill_id = skill_.getId();
                if ((skill_id % 10000000 >= 1000000) && ((skill_id >= 9000000) && (skill_id <= 10000000))) {
                    continue;
                }
                ISkill skill = SkillFactory.getSkill(skill_id);
                boolean add = ((skill_id / 10000000 == job.getId() / 1000) && (skill.hasMastery()))
                        || (job.isCygnus());
                if ((!add) && (job.isAran())) {
                    switch (skill_id) {
                        case 21000000:
                        case 21001003:
                        case 21100000:
                        case 21100002:
                        case 21100004:
                        case 21100005:
                        case 21110002:
                            add = true;
                    }
                }
                if (add) {
                    int masterLevel = skill.getMasterLevel();
                    if (masterLevel == 0) {
                        continue;
                    }
                    player.changeSkillLevel(skill, player.getSkillLevel(skill), (byte) masterLevel);
                }
            } catch (NumberFormatException nfe) {
                continue;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }
}
