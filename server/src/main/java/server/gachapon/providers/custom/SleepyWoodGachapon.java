package server.gachapon.providers.custom;

import server.gachapon.*;

import java.util.ArrayList;
import java.util.List;

public class SleepyWoodGachapon implements RewardsProvider {

  private GachaponLocation location = GachaponLocation.SLEEPYWOOD;

  int ids[] = {

      2022130, 4000585, 2000005, 2022113, 1032032, 1442018, 2041008, 2041010, 2044701, 2044702, 2044602, 2040101,
      2040804, 2040513, 2040517, 2040902, 2040705, 2040707, 2040708, 3010119, 2340000, 2043305, 1332015, 2000004,
      1472003, 1322008, 1002005, 1002023, 2290156, 2290153, 2290159, 2290161, 1002085, 1472009, 1302021, 1322022,
      1002212, 1002117, 2430118, 1302013, 1012200, 1952003, 1972003, 2040201, 2040402, 1002169, 1002164, 1041045,
      1041082, 1372040, 1402046, 1412033, 1422037, 1432047, 1442063, 1452057, 1462050, 1382007, 1332006, 1322051,
      1322027, 1332014, 1302049, 1462012, 1332019, 1432017, 1442025, 1372031, 1452001, 1332010, 1322016, 1332018,
      1302028, 1152007, 1302006, 2022195, 5200010

  };

  private List<AbstractRandomEntity> reward = new ArrayList<>();

  public SleepyWoodGachapon() {

    for (int id : ids) {
      reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
    }

  }

  @Override
  public List<AbstractRandomEntity> getRewards() {
    return reward;
  }

}
