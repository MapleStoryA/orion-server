package server.gachapon.providers;

import server.gachapon.AbstractRandomEntity;
import server.gachapon.GachaponLocation;
import server.gachapon.GachaponReward;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomRewardsProvider {

  private static HashMap<Integer, List<GachaponReward>> cache = new HashMap<>();

  private DbRewardProvider provider;

  List<AbstractRandomEntity> list;

  public CustomRewardsProvider(Connection con) {
    this.provider = new DbRewardProvider(con);
    list = provider.getRewards();
  }

  public List<GachaponReward> getRewardsByLocation(GachaponLocation location) {

    if (cache.containsKey(location.getValue())) {
      return cache.get(location.getValue());
    }
    for (AbstractRandomEntity entity : list) {
      if (entity instanceof GachaponReward) {
        GachaponReward gachaEntity = ((GachaponReward) entity);
        int locationValue = gachaEntity.getLocation().getValue();
        if (!cache.containsKey(locationValue)) {
          cache.put(locationValue, new ArrayList<GachaponReward>());
        }
        cache.get(locationValue).add((GachaponReward) entity);
      }
    }

    return cache.get(location.getValue());
  }
}
