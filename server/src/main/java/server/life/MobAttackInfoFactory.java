package server.life;

import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.config.ServerEnvironment;
import tools.Pair;
import tools.StringUtil;

@lombok.extern.slf4j.Slf4j
public class MobAttackInfoFactory {

    private static final MobAttackInfoFactory instance = new MobAttackInfoFactory();
    private static final MapleDataProvider dataSource =
            ServerEnvironment.serverConfig().getDataProvider("wz/Mob");
    private static final Map<Pair<Integer, Integer>, MobAttackInfo> mobAttacks =
            new HashMap<Pair<Integer, Integer>, MobAttackInfo>();

    public static MobAttackInfoFactory getInstance() {
        return instance;
    }

    public MobAttackInfo getMobAttackInfo(MapleMonster mob, int attack) {
        MobAttackInfo ret =
                mobAttacks.get(
                        new Pair<Integer, Integer>(
                                Integer.valueOf(mob.getId()), Integer.valueOf(attack)));
        if (ret != null) {
            return ret;
        }

        MapleData mobData =
                dataSource.getData(StringUtil.getLeftPaddedStr(mob.getId() + ".img", '0', 11));
        if (mobData != null) {
            MapleData infoData = mobData.getChildByPath("info/link");
            if (infoData != null) {
                String linkedmob = MapleDataTool.getString("info/link", mobData);
                mobData =
                        dataSource.getData(
                                StringUtil.getLeftPaddedStr(linkedmob + ".img", '0', 11));
            }
            final MapleData attackData = mobData.getChildByPath("attack" + (attack + 1) + "/info");
            if (attackData != null) {
                ret = new MobAttackInfo();
                ret.setDeadlyAttack(attackData.getChildByPath("deadlyAttack") != null);
                ret.setMpBurn(MapleDataTool.getInt("mpBurn", attackData, 0));
                ret.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
                ret.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
                ret.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
            }
        }
        mobAttacks.put(
                new Pair<Integer, Integer>(Integer.valueOf(mob.getId()), Integer.valueOf(attack)),
                ret);

        return ret;
    }
}
