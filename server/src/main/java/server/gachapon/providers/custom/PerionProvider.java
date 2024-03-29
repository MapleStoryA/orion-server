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
public class PerionProvider implements RewardsProvider {

    private final GachaponLocation location = GachaponLocation.PERION;
    private final List<AbstractRandomEntity> reward = new ArrayList<>();
    int[] ids = {
        1472016, 1472006, 1442013, 1332011, 1472029, 1472023, 1472019, 1442017, 1322023, 1152005, 1452023, 1032008,
        1372007, 1462001, 1302004, 1402017, 1442016, 1332079, 1472074, 1402012, 1332031, 2022130, 1312007, 1332009,
        1422005, 1312012, 1312010, 1422001, 1442000, 2000005, 2022113, 1302012, 1942003, 1952003, 1972003, 1312015,
        2340000, 3012005, 1462003, 1432009, 1302022, 3010013, 1051063, 1002060, 1002159, 1061051, 1002214, 1102030,
        1412006, 1002167, 1002162, 1040070, 1040073, 1002211, 1002042, 1040092, 1050018, 1332020, 2430117, 1152001,
        1412004, 1102041, 1102042, 1002395, 1002393, 1002586, 1082148, 1052122, 2040200, 2040201, 2040030, 3010119,
        1060046, 1402037, 1102040, 1402007, 1312014, 1152008, 1432013, 1012106, 1012107, 1040033, 1372008, 1372036,
        1372037, 1372041, 1072356, 1072357, 1072358, 1072359, 1302081, 1312037, 2040755, 2040756, 2040757, 2040833,
        2040834, 2040922, 2040943, 2041068, 2041069, 5200010
    };

    public PerionProvider() {

        for (int id : ids) {
            reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
        }
    }

    @Override
    public List<AbstractRandomEntity> getRewards() {
        return reward;
    }
}
