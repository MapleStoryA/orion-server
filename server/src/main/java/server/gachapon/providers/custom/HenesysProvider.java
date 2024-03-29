package server.gachapon.providers.custom;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.gachapon.AbstractRandomEntity;
import server.gachapon.BasicRewardChances;
import server.gachapon.GachaponLocation;
import server.gachapon.GachaponReward;
import server.gachapon.RewardsProvider;

@Slf4j
public class HenesysProvider implements RewardsProvider {

    private final GachaponLocation location = GachaponLocation.HENESYS;
    private final List<AbstractRandomEntity> reward = new ArrayList<>();
    int[] ids = {
        3012005, 1462003, 1432009, 1302022, 3010013, 1051063, 1002060, 1002159, 1061051, 1002214, 1102030, 1412006,
        1002167, 1002162, 1040070, 1040073, 1002211, 1002042, 1040092, 1050018, 1102027, 1082147, 1152005, 1002169,
        1002164, 1041045, 1041082, 1082149, 1452023, 1462018, 2049100, 1050091, 1002170, 1002165, 2044504, 2040611,
        2041038, 2044605, 2040307, 2040809, 2040916, 1312031, 1402036, 1452044, 1462039, 1462004, 1322027, 4001434,
        1040030, 1462008, 1462016, 1051084, 1040022, 1060056, 1061050, 1002173, 1002036, 1040007, 1060063, 1051064,
        1302027, 1372006, 1462005, 1152006, 4000585, 1322007, 2430073, 1152000, 1452012, 1040085, 1422004, 4001010,
        1002418, 1452008, 3010155, 1102041, 2000005, 2022113, 1102042, 1082177, 1022058, 1102082, 1040025, 1032028,
        1102033, 1060058, 1060061, 1051017, 1040039, 1050060, 1452006, 1002723, 1462007, 1942003, 1952003, 1972003,
        1962003, 2044502, 2044602, 2040002, 2040028, 2040402, 2040412, 3010119, 1462006, 3010041, 1012076, 1022082,
        3010095, 1040089, 1452007, 1452002, 1060005, 1022060, 1041061, 1061057, 1002041, 3010152, 1402007, 2340000,
        1382048, 1382049, 1082236, 1082237, 1082238, 1052155, 1052156, 1052157, 2040045, 2040046, 2040333, 2040429,
        2040542, 2040543, 2040629, 5200010
    };

    public HenesysProvider() {

        for (int id : ids) {
            reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
        }
    }

    @Override
    public List<AbstractRandomEntity> getRewards() {
        return reward;
    }
}
