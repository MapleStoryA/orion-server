package maplebr2.commodity;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommodityUtils {

  public final static MapleDataProvider data = MapleDataProviderFactory
      .getDataProvider(new File("dist/wz/Etc"));


  public static HashMap<Integer, Commodity> loadCommodities() {
    HashMap<Integer, Commodity> commodities = new HashMap<>();
    for (MapleData field : data.getData("Commodity.img").getChildren()) {
      Commodity commodity = new Commodity(field.getName());
      for (MapleData intField : field.getChildren()) {
        String name = intField.getName();
        String value = String.valueOf(intField.getData());
        commodity.addField(name, value);
      }
      commodities.put(commodity.getSN(), commodity);
    }
    return commodities;
  }


  public static HashMap<Integer, List<Commodity>> loadCommoditiesGrouped() {
    HashMap<Integer, List<Commodity>> commodities = new HashMap<>();
    for (MapleData field : data.getData("Commodity.img").getChildren()) {
      Commodity commodity = new Commodity(field.getName());
      for (MapleData intField : field.getChildren()) {
        String name = intField.getName();
        String value = String.valueOf(intField.getData());
        commodity.addField(name, value);
      }
      List<Commodity> c = commodities.get(commodity.getItemId());
      if (c == null) {
        c = new ArrayList<>();
        c.add(commodity);
        commodities.put(commodity.getItemId(), c);
      } else {
        c.add(commodity);
      }
    }
    return commodities;
  }

}
