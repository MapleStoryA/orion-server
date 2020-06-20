package server.gachapon;

public class GachaponReward extends AbstractRandomEntity {

  private GachaponLocation location;

  private RewardSource source;

  public static final String DEFAULT_DESC = "";

  public GachaponReward(int idreward,
                        int id,
                        int quantity,
                        double chance,
                        String description,
                        GachaponLocation location,
                        RewardSource source
  ) {
    super(idreward, id, quantity, chance, description);
    this.location = location;
    this.source = source;
  }

  public GachaponReward(int id, int quantity, double chance, String description, GachaponLocation location) {
    super(-1, id, quantity, chance, description);
  }

  public GachaponReward(int id, double chance, String description) {
    super(-1, id, 1, chance, description);
  }

  public GachaponReward(int idreward, int id, double chance, String description) {
    super(idreward, id, 1, chance, description);
  }

  public GachaponLocation getLocation() {
    return location;
  }

  public RewardSource getSource() {
    return source;
  }

  public void setSource(RewardSource source) {
    this.source = source;
  }

  public boolean isRare() {
    return this.getChance() <= 5000;
  }
}
