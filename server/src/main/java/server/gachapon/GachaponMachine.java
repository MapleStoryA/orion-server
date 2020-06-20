package server.gachapon;

import java.util.List;

public interface GachaponMachine {

  AbstractRandomEntity getReward(GachaponLocation location);

  void setRewards(GachaponLocation location, List<? extends AbstractRandomEntity> rewards);

}
