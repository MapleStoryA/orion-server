package server.gachapon.providers.custom;

import server.gachapon.*;

import java.util.ArrayList;
import java.util.List;

public class NautilusGachapon implements RewardsProvider {

  private GachaponLocation location = GachaponLocation.NAUTILUS;

  int ids[] = {

      2022195, 1102082, 1102081, 1052122, 1962003, 3010119, 3010108, 1012199, 1942003, 1382010, 3010069, 1152006,
      2040302, 2040005, 1002274, 2430120, 1402013, 2040811, 2043805, 1422031, 1002273, 1002529, 2040312, 2340000,
      1472068, 1482023, 1492023, 1522015, 3010106, 3010110, 3010127, 3010128, 1432009, 1302022, 3010013, 1051063,
      1002060, 1002159, 1061051, 1002214, 1102030, 1412006, 1002167, 1002162, 1040070, 1040073, 1002211, 1002042,
      1040092, 1050018, 1102027, 1002169, 1002164, 1041045, 1041082, 1082149, 1452023, 1462018, 2049100, 1050091,
      1002170, 1002165, 1082148, 2040200, 2040201, 2040030, 1382007, 1332006, 1322051, 1322027, 1332014, 1302049,
      1472003, 1462012, 1332019, 1432017, 1442025, 1372031, 1452001, 1332010, 4000585, 1322016, 1332018, 1302028,
      1152007, 1302006, 5200010

  };

  private List<AbstractRandomEntity> reward = new ArrayList<>();

  public NautilusGachapon() {

    for (int id : ids) {
      reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
    }

  }

  @Override
  public List<AbstractRandomEntity> getRewards() {
    return reward;
  }

}
