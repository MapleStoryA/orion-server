package server.gachapon.providers;

import server.gachapon.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbRewardProvider implements RewardsProvider {

  public final String BASIC_QUERY = "SELECT idreward, id, chance, description, location, source from reward_item where enabled = 1";

  private Connection con;

  public DbRewardProvider(Connection con) {
    this.con = con;
  }


  @Override
  public List<AbstractRandomEntity> getRewards() {
    return getRewards(BASIC_QUERY);
  }


  public List<AbstractRandomEntity> getRewards(String query) {
    try {
      PreparedStatement st = con.prepareStatement(query);
      ResultSet rs = st.executeQuery();
      List<AbstractRandomEntity> list = new ArrayList<>();
      while (rs.next()) {
        int idreward = rs.getInt("idreward");
        int id = rs.getInt("id");
        double chance = rs.getDouble("chance");
        String description = rs.getString("description");
        int location = rs.getInt("location");
        String source = rs.getString("source");
        RewardSource rw = RewardSource.valueOf(source);
        GachaponLocation locat = GachaponLocation.valueOf(location);
        GachaponReward reward = new GachaponReward(idreward, id, 1, chance, description, locat, rw);
        list.add(reward);
      }
      return list;

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }


}
