package client.information.drop;

import server.life.MapleMonster;

public class MapleDropData {


  private final String item;

  private final double chance;

  private final MapleMonster monster;


  public MapleDropData(String item, MapleMonster monster, double chance) {
    super();
    this.item = item;
    this.monster = monster;
    this.chance = chance;
  }


  public String getName() {
    return item;
  }

  public MapleMonster getMonster() {
    return monster;
  }


  public double getChance() {
    return chance;
  }

}
