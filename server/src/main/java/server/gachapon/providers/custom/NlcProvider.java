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
public class NlcProvider implements RewardsProvider {

    private final GachaponLocation location = GachaponLocation.NLC;
    private final List<AbstractRandomEntity> reward = new ArrayList<>();
    int[] ids = {
        1472016, 1472006, 1442013, 1332011, 1472029, 1472023, 1472019, 1442017, 1322023, 1152005, 1452023, 1032008,
        1372007, 1462001, 1302004, 1402017, 1442016, 1332079, 1472074, 1402012, 1332031, 1372038, 1372040, 1372041,
        1382005, 1382007, 1332006, 1322051, 1322027, 1332014, 1302049, 1472003, 1462012, 1332019, 1432017, 1442025,
        1372031, 1452001, 1332010, 4000585, 1322016, 1332018, 1302028, 1152007, 1302006, 2022195, 2022117, 1472007,
        1312003, 1472009, 1462000, 1482006, 1302024, 1482003, 1452008, 1432016, 2000005, 2022113, 1322024, 1332005,
        1322022, 1452006, 1302013, 1462007, 1452005, 1332016, 1032002, 1402002, 2041303, 2041014, 2044702, 2040302,
        2040805, 2044902, 2044418, 2044312, 2043702, 1432004, 3010119, 1332003, 1442014, 1422008, 1432000, 1472008,
        3010133, 1442005, 1382006, 1422007, 1302002, 1032000, 2340000, 1372001, 1402009, 1422000, 1442021, 1032031,
        1122011, 1122012, 1002776, 1002777, 2044713, 2044817, 2044910, 2046204, 2046205, 2046206, 5200010
    };

    public NlcProvider() {

        for (int id : ids) {
            reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
        }
    }

    @Override
    public List<AbstractRandomEntity> getRewards() {
        return reward;
    }
}
