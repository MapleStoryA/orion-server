package maplebr2;

import constants.ServerEnvironment;
import database.DatabaseConnection;
import org.junit.Test;
import server.gachapon.AbstractRandomEntity;
import server.gachapon.GachaponLocation;
import server.gachapon.SimpleGachaRandomizer;
import server.gachapon.providers.CustomRewardsProvider;

import java.sql.SQLException;

public class GachaponTests {

  @Test
  public void testGachaReward() throws SQLException {
    ServerEnvironment.loadEnvironment();
    DatabaseConnection.setProps();
    CustomRewardsProvider reward = new CustomRewardsProvider(DatabaseConnection.getConnection());

    AbstractRandomEntity result = new SimpleGachaRandomizer(reward.getRewardsByLocation(GachaponLocation.MUSHROM_SHRINE)).next();
    int times = 0;
    while (result.getChance() != 1000) {
      result = new SimpleGachaRandomizer(reward.getRewardsByLocation(GachaponLocation.MUSHROM_SHRINE)).next();
      times++;
    }
    System.out.println(times);


  }
}
