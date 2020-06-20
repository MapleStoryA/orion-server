package server.gachapon.providers.custom;

import server.gachapon.*;

import java.util.ArrayList;
import java.util.List;

public class ElliniaProvider implements RewardsProvider {

  private GachaponLocation location = GachaponLocation.ELLINIA;

  int ids[] = {2022130, 1372032, 4000585, 1452008, 2000005, 1002207, 1952003, 1382003, 2040902, 2340000, 1382050,
      1382051, 1002169, 1002164, 1041045, 1041082, 1002042, 1040092, 1050018, 1332020, 1061051, 1002214, 1102030,
      1412006, 1002167, 1002162, 1040070, 1040073, 1002211, 3010119, 1332003, 1322010, 3010133, 3010139, 1962003,
      3010108, 1012199, 1942003, 1382010, 3010069, 1152006, 1052158, 1052159, 1092057, 1092058, 1092059, 1072355,
      1382007, 1332006, 1322051, 1322027, 1332014, 1302049, 1472003, 1462012, 1332019, 1432017, 1442025, 1372031,
      1452001, 1332010, 1322016, 1332018, 1302028, 1152007, 1302006, 2022195, 5200010

  };

  private List<AbstractRandomEntity> reward = new ArrayList<>();

  public ElliniaProvider() {

    for (int id : ids) {
      reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, "", location));
    }
  }

  @Override
  public List<AbstractRandomEntity> getRewards() {
    return reward;
  }

}
