package server.gachapon.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.gachapon.AbstractRandomEntity;
import server.gachapon.GachaponLocation;
import server.gachapon.GachaponReward;

@Slf4j
public class CustomRewardsProvider {

    private static final HashMap<Integer, List<GachaponReward>> cache = new HashMap<>();

    private final DbRewardProvider provider;

    List<AbstractRandomEntity> list;

    public CustomRewardsProvider() {
        this.provider = new DbRewardProvider();
        list = provider.getRewards();
    }

    public List<GachaponReward> getRewardsByLocation(GachaponLocation location) {

        if (cache.containsKey(location.getValue())) {
            return cache.get(location.getValue());
        }
        for (AbstractRandomEntity entity : list) {
            if (entity instanceof GachaponReward gachaEntity) {
                int locationValue = gachaEntity.getLocation().getValue();
                if (!cache.containsKey(locationValue)) {
                    cache.put(locationValue, new ArrayList<>());
                }
                cache.get(locationValue).add((GachaponReward) entity);
            }
        }

        return cache.get(location.getValue());
    }
}
