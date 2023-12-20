package server.life;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.config.ServerConfig;
import tools.collection.Pair;

@lombok.extern.slf4j.Slf4j
public class MobSkillFactory {

    private static final Map<Pair<Integer, Integer>, MobSkill> mobSkills =
            new HashMap<Pair<Integer, Integer>, MobSkill>();
    private static final MapleDataProvider dataSource =
            ServerConfig.serverConfig().getDataProvider("wz/Skill");
    private static final MapleData skillRoot = dataSource.getData("MobSkill.img");

    public static MobSkill getMobSkill(int skillId, int level) {
        MobSkill ret = mobSkills.get(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)));
        if (ret != null) {
            return ret;
        }
        if (skillRoot == null
                || skillRoot.getChildren() == null
                || skillRoot.getChildByPath(String.valueOf(skillId)) == null
                || skillRoot.getChildByPath(String.valueOf(skillId)).getChildren() == null
                || skillRoot.getChildByPath(String.valueOf(skillId)).getChildByPath("level") == null) {
            return null;
        }
        final MapleData skillData = skillRoot.getChildByPath(skillId + "/level/" + level);
        if (skillData != null && skillData.getChildren() != null) {
            List<Integer> toSummon = new ArrayList<Integer>();
            for (int i = 0; i > -1; i++) {
                if (skillData.getChildByPath(String.valueOf(i)) == null) {
                    break;
                }
                toSummon.add(Integer.valueOf(MapleDataTool.getInt(skillData.getChildByPath(String.valueOf(i)), 0)));
            }
            final MapleData ltd = skillData.getChildByPath("lt");
            Point lt = null;
            Point rb = null;
            if (ltd != null) {
                lt = (Point) ltd.getData();
                rb = (Point) skillData.getChildByPath("rb").getData();
            }
            ret = new MobSkill(skillId, level);
            ret.addSummons(toSummon);
            ret.setCoolTime(MapleDataTool.getInt("interval", skillData, 0) * 1000L);
            ret.setDuration(MapleDataTool.getInt("time", skillData, 1) * 1000L);
            ret.setHp(MapleDataTool.getInt("hp", skillData, 100));
            ret.setMpCon(MapleDataTool.getInt(skillData.getChildByPath("mpCon"), 0));
            ret.setSpawnEffect(MapleDataTool.getInt("summonEffect", skillData, 0));
            ret.setX(MapleDataTool.getInt("x", skillData, 1));
            ret.setY(MapleDataTool.getInt("y", skillData, 1));
            ret.setProp(MapleDataTool.getInt("prop", skillData, 100) / 100f);
            ret.setLimit((short) MapleDataTool.getInt("limit", skillData, 0));
            ret.setLtRb(lt, rb);

            mobSkills.put(new Pair<Integer, Integer>(Integer.valueOf(skillId), Integer.valueOf(level)), ret);
        }
        return ret;
    }
}
