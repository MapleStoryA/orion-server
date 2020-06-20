package server.gachapon;

import java.util.HashMap;
import java.util.List;

public class SimpleGachaponMachine implements GachaponMachine {


  private HashMap<GachaponLocation, List<? extends AbstractRandomEntity>> rewards = new HashMap<>();


  protected SimpleGachaponMachine() {

  }

  @Override
  public GachaponReward getReward(GachaponLocation location) {
    List<? extends AbstractRandomEntity> rew = rewards.get(location);
    GachaponReward reward = (GachaponReward) new SimpleGachaRandomizer(rew).next();
    return reward;
  }


  @Override
  public void setRewards(GachaponLocation location, List<? extends AbstractRandomEntity> rewards) {
    this.rewards.put(location, rewards);

  }

}
