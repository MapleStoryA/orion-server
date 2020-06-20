package server.gachapon;

import database.DatabaseConnection;
import server.gachapon.providers.CustomRewardsProvider;
import server.gachapon.providers.custom.GenericGachapon;

public class GachaponFactory {
  private static SimpleGachaponMachine instance;


  public static SimpleGachaponMachine getInstance() {
    if (instance == null) {
      instance = new SimpleGachaponMachine();
      CustomRewardsProvider reward = new CustomRewardsProvider(DatabaseConnection.getConnection());
      instance.setRewards(GachaponLocation.HENESYS, reward.getRewardsByLocation(GachaponLocation.HENESYS));
      instance.setRewards(GachaponLocation.ELLINIA, reward.getRewardsByLocation(GachaponLocation.ELLINIA));
      instance.setRewards(GachaponLocation.KERNING, reward.getRewardsByLocation(GachaponLocation.KERNING));
      instance.setRewards(GachaponLocation.PERION, reward.getRewardsByLocation(GachaponLocation.PERION));
      instance.setRewards(GachaponLocation.NLC, reward.getRewardsByLocation(GachaponLocation.NLC));
      instance.setRewards(GachaponLocation.MUSHROM_SHRINE, reward.getRewardsByLocation(GachaponLocation.MUSHROM_SHRINE));
      instance.setRewards(GachaponLocation.GENERIC, new GenericGachapon().getRewards());
      instance.setRewards(GachaponLocation.SLEEPYWOOD, reward.getRewardsByLocation(GachaponLocation.SLEEPYWOOD));
      instance.setRewards(GachaponLocation.NAUTILUS, reward.getRewardsByLocation(GachaponLocation.NAUTILUS));
    }
    return instance;
  }

}
