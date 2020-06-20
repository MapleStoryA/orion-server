package server.gachapon.providers.custom;

import server.gachapon.*;

import java.util.ArrayList;
import java.util.List;

public class KerningProvider implements RewardsProvider {

  private GachaponLocation location = GachaponLocation.KERNING;

  int ids[] = {12022130, 1472026, 2049100, 4130014, 2043305, 1332015, 2000004, 1472003, 1322008, 1002005, 1002023,
      2290156, 2290153, 2290159, 2290161, 1002085, 1472009, 1302021, 2000005, 2022113, 1322022, 1002212, 1002117,
      2430118, 1302013, 1012200, 1952003, 1972003, 2040201, 2040402, 1092018, 1332017, 3010119, 1332003, 1322010,
      3010133, 3010139, 2340000, 3012005, 1462003, 1432009, 1302022, 3010013, 1051063, 1002060, 1002159, 1061051,
      1002214, 1102030, 1412006, 1002167, 1002162, 1040070, 1040073, 1002211, 1002042, 1040092, 1050018, 3010128,
      1372039, 1322060, 1332073, 1332074, 1342011, 1372044, 1382057, 2043313, 2043713, 2043813, 2044320, 2044420,
      2044513, 2044613, 5200010

  };

  private List<AbstractRandomEntity> reward = new ArrayList<>();

  public KerningProvider() {

    for (int id : ids) {
      reward.add(new GachaponReward(id, 1, BasicRewardChances.AVERAGE, GachaponReward.DEFAULT_DESC, location));
    }
  }

  @Override
  public List<AbstractRandomEntity> getRewards() {
    return reward;
  }

}
