package server.cashShop;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.cashShop.CashItemInfo.CashModInfo;

import java.io.File;
import java.util.*;

public class CashItemFactory {

  private final static CashItemFactory instance = new CashItemFactory();
  private final static int[] bestItems = new int[] {10002819, 50100010, 50200001, 10002147, 60000073};
  private boolean initialized = false;
  private final Map<Integer, Integer> itemSn = new HashMap<>(); // itemid, sn
  private final Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
  private final Map<Integer, List<CashItemInfo>> itemPackage = new HashMap<Integer, List<CashItemInfo>>();
  private final Map<Integer, CashModInfo> itemMods = new HashMap<Integer, CashModInfo>();
  public final static MapleDataProvider data = MapleDataProviderFactory
      .getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc"));

  public static final CashItemFactory getInstance() {
    return instance;
  }

  protected CashItemFactory() {
  }

  private void initialize() {
    long start = System.currentTimeMillis();
    System.out.println("Loading CashItemFactory :::");
    final List<Integer> itemids = new ArrayList<Integer>();
    for (MapleData field : data.getData("Commodity.img").getChildren()) {

      final int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);
      final int SN = MapleDataTool.getIntConvert("SN", field, 0);

      final CashItemInfo stats = new CashItemInfo(itemId, MapleDataTool.getIntConvert("Count", field, 1),
          MapleDataTool.getIntConvert("Price", field, 0), SN, MapleDataTool.getIntConvert("Period", field, 0),
          MapleDataTool.getIntConvert("Gender", field, 2),
          MapleDataTool.getIntConvert("OnSale", field, 0) > 0);

      if (SN > 0) {
        itemStats.put(SN, stats);
      }

      if (itemId > 0) {
        itemids.add(itemId);
      }
      if (itemId > 0 && SN > 0) {
        if (!itemSn.containsKey(itemId)) {
          itemSn.put(itemId, SN);
        }
      }
    }

    final MapleData rootNode = data.getData("CashPackage.img");
    List<MapleData> children = rootNode.getChildren();
    for (int i : itemids) {
      getPackageItems(i, children);
    }


    long finish = System.currentTimeMillis();
    System.out.println("Finished loading basic cashshop in : " + (finish - start) / 1000);

    initialized = true;
  }

  public final CashItemInfo getItem(int sn) {
    return getItem(sn, false);
  }

  public final CashItemInfo getItem(int sn, boolean exception) {
    final CashItemInfo stats = itemStats.get(Integer.valueOf(sn));
    if (exception && stats != null) {
      return stats;
    }
    if (stats == null || !stats.onSale()) {
      return null;
    }
    return stats;
  }

  public final int getSNFromItemId(int itemid) {
    final int sn = itemSn.get(Integer.valueOf(itemid));
    if (sn <= 0) {
      return 0;
    }
    return sn;
  }

  public final List<CashItemInfo> getPackageItems(int itemId, List<MapleData> nodes) {
    if (itemPackage.get(itemId) != null) {
      return itemPackage.get(itemId);
    }
    final List<CashItemInfo> packageItems = new ArrayList<CashItemInfo>();

    if (nodes == null) {
      return null;
    }
    for (MapleData currentNode : nodes) {
      MapleData children = currentNode.getChildByPath("SN");
      int value = Integer.valueOf(currentNode.getName());
      if (value == itemId) {
        for (MapleData c : children.getChildren()) {
          int currentNodeValue = Integer.valueOf(MapleDataTool.getIntConvert(c));
          packageItems.add(itemStats.get(currentNodeValue));
        }
      }
    }
    itemPackage.put(itemId, packageItems);
    return packageItems;
  }

  public final Collection<CashModInfo> getAllModInfo() {

    return itemMods.values();
  }

  public void loadCashShopData() {
    if (!initialized) {
      initialize();
    }
  }

  public final int[] getBestItems() {
    return bestItems;
  }
}
