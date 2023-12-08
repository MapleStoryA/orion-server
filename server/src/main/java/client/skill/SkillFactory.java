package client.skill;

import client.SummonSkillEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.config.ServerEnvironment;
import tools.StringUtil;

@lombok.extern.slf4j.Slf4j
public class SkillFactory {

    private static final Map<Integer, ISkill> skills = new HashMap<Integer, ISkill>();
    private static final Map<Integer, List<Integer>> skillsByJob =
            new HashMap<Integer, List<Integer>>();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation =
            new HashMap<Integer, SummonSkillEntry>();
    private static final MapleData stringData =
            ServerEnvironment.getConfig().getDataProvider("wz/String").getData("Skill.img");

    public static final ISkill getSkill(final int id) {
        if (!skills.isEmpty()) {
            return skills.get(Integer.valueOf(id));
        }
        // log.info("Loading SkillFactory :::");
        final MapleDataProvider datasource =
                ServerEnvironment.getConfig().getDataProvider("wz/Skill");
        final MapleDataDirectoryEntry root = datasource.getRoot();

        int skillid;
        MapleData summon_data;
        SummonSkillEntry sse;

        for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
            if (topDir.getName().length() <= 8) {
                for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) { // Loop thru each jobs
                            if (data2 != null) {
                                skillid = Integer.parseInt(data2.getName());

                                Skill skil = Skill.loadFromData(skillid, data2);
                                List<Integer> job = skillsByJob.get(skillid / 10000);
                                if (job == null) {
                                    job = new ArrayList<Integer>();
                                    skillsByJob.put(skillid / 10000, job);
                                }
                                job.add(skillid);
                                skil.setName(getName(skillid));

                                skills.put(skillid, skil);

                                summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    sse = new SummonSkillEntry();
                                    sse.attackAfter =
                                            (short)
                                                    MapleDataTool.getInt(
                                                            "attackAfter", summon_data, 999999);
                                    sse.type = (byte) MapleDataTool.getInt("type", summon_data, 0);
                                    sse.mobCount =
                                            (byte) MapleDataTool.getInt("mobCount", summon_data, 1);
                                    SummonSkillInformation.put(skillid, sse);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static final List<Integer> getSkillsByJob(final int jobId) {
        return skillsByJob.get(jobId);
    }

    public static final String getSkillName(final int id) {
        ISkill skil = getSkill(id);
        if (skil != null) {
            return skil.getName();
        }
        return null;
    }

    public static final String getName(final int id) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return null;
    }

    public static final SummonSkillEntry getSummonData(final int skillid) {
        return SummonSkillInformation.get(skillid);
    }

    public static final Collection<ISkill> getAllSkills() {
        return skills.values();
    }
}
