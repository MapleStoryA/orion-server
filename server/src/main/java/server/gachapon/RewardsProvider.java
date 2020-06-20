package server.gachapon;

import java.util.List;

public interface RewardsProvider {
  List<AbstractRandomEntity> getRewards();
}
