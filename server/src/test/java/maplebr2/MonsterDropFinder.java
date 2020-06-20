package maplebr2;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.InsertBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sin on 22/05/2017.
 */
public class MonsterDropFinder {

  public static String[] TO_FIND = new String[] {
      "Urban Fungus",
      "Freezer",
      "King Block Golem",
      "Crimson Balrog",
      "Flyeye",
      "Wooden Mask",
      "Cold Eye",
      "Mithril Mutae",
      "Tippo Blue",
      "Jr. Balrog",
      "Lorang",
      "Mask Fish",
      "Star Pixie",
      "Tortie",
      "Rombot",
      "Drake",
      "Wild Kargo",
      "Curse Eye",
      "Ice Drake",
      "Dark Drake",
      "Officer Skeleton",
      "Dual Beetle",
      "Goby",
      "Jar",
      "Dual Birk",
      "Leatty",
      "Red Kentaurus",
      "Sparker",
      "Birk",
      "Master Robo",
      "Ghost Stump",
      "Ribbon Pig",
      "Cactus",
      "Horny Mushroom",
      "Evil Eye",
      "Blue Snail",
      "Octopus",
      "Beetle",
      "Leprechaun [2]",
      "Shark",
      "Malady",
      "Jr. Lioner",
      "Jr. Grupin",
      "Master Chronos",
      "Blue Mushroom",
      "Iron Mutae",
      "Green King Goblin",
      "Jr. Cellion",
      "Lupin",
      "Robo",
      "Wraith",
      "Street Slime",
      "Boomer",
      "Mecateon",
      "Barnard Gray",
      "Seacle",
      "Trixter",
      "Retz",
      "Plateon",
      "Lunar Pixie",
      "Tippo Red",
      "Freezer [2]"
  };

  static List<String> MONSTERS = new ArrayList<>();

  static {
    for (String str : TO_FIND) {
      MONSTERS.add(str);
    }
  }


  public static final int ITEM = 4030012;


  public static void main(String[] args) {
    System.setProperty("net.sf.odinms.wzpath", "dist/wz");
    MapleDataProvider dataProvider = MapleDataProviderFactory
        .getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/" + "String"));

    MapleData data = dataProvider.getData("Mob.img");

    for (MapleData child : data.getChildren()) {
      String name = MapleDataTool.getString(child.getChildByPath("name"), "-");
      if (MONSTERS.contains(name)) {

        InsertBuilder builder = InsertBuilder.create().forTable("drop_data")
            .newIntValue("dropperid", Integer.valueOf(child.getName()))
            .newIntValue("itemid", ITEM)
            .newIntValue("minimum_quantity", 1)
            .newIntValue("maximum_quantity", 1)
            .newIntValue("questid", 0)
            .newIntValue("chance", 6000);


        System.out.println(builder.toString());

      }

    }


  }
}
