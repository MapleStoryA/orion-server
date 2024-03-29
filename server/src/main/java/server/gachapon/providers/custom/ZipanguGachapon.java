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
public class ZipanguGachapon implements RewardsProvider {

    private final GachaponLocation location = GachaponLocation.MUSHROM_SHRINE;
    private final List<AbstractRandomEntity> reward = new ArrayList<>();
    int[] ids = {
        1002130, 1060033, 1082147, 1152005, 1002392, 1082149, 1072321, 2049100, 5010034, 2049003, 3010099, 1472017,
        1051030, 2040604, 2040605, 2040611, 2040608, 2040606, 2044505, 2041038, 2041039, 2041030, 2041041, 2044704,
        2044604, 2043304, 2040322, 2040305, 2040811, 2040814, 2040011, 2040510, 2040511, 2040519, 2040518, 2040521,
        2040531, 2040533, 2044405, 2040906, 2040907, 2040916, 2040714, 2040713, 2040716, 2044304, 2044305, 2040411,
        2040407, 1332054, 1332030, 2430119, 1072263, 4000585, 1402013, 1332053, 1332020, 2430117, 1152001, 1412004,
        1102041, 1102042, 1002395, 1002393, 1002586, 1082148, 1052122, 2040200, 2040201, 2040030, 3010119, 1060046,
        1402037, 2340000, 1102040, 1082145, 1402007, 1312014, 1152008, 1432013, 1012106, 1012107, 1312012, 1040033,
        1372008, 1322027, 1382045, 1382046, 1382047, 1372035, 3010073, 1002778, 1002779, 1002780, 1102172, 1082234,
        1082235, 2046207, 2046304, 2046305, 2046306, 2046307, 5200010, 1432018, 1432016, 1432017
    };

    public ZipanguGachapon() {

        for (int id : ids) {
            reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
        }
    }

    @Override
    public List<AbstractRandomEntity> getRewards() {
        return reward;
    }
}
