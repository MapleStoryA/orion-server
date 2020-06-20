package maplebr2;

import maplebr2.commodity.Commodity;
import maplebr2.commodity.CommodityUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class CommodityTest {


  class ComparareCommodity implements Comparator<Commodity> {

    @Override
    public int compare(Commodity arg0, Commodity arg1) {
      return arg0.getPrice() - arg1.getPrice();
    }


  }

  @Test
  public void test() throws IOException {
    StringBuilder br = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
    br.append("<imgdir name=\"Commodity.img\">\n");

    HashMap<Integer, Commodity> loadedCommodities = CommodityUtils.loadCommodities();
    int eventItens = 0;
    int total = 0;
    int newItems = 0;
    for (Entry<Integer, Commodity> entry : loadedCommodities.entrySet()) {
      total++;
      Commodity commodity = entry.getValue();
      if (commodity.getSN().toString().startsWith("1")) {
        commodity.setOnSale(false);
        if (commodity.getItemId() == 5062000) {
          commodity.setOnSale(true);
        }
        eventItens++;
      } else {
        if (((commodity.isEquip() || commodity.isPet()) && commodity.getPrice() > 1000) || commodity.isEffect()) {
          commodity.setOnSale(true);
          newItems++;
        }
        if (commodity.getPrice() == 100 && commodity.isPet()) {
          commodity.setOnSale(false);
        }
      }
      br.append(commodity.toXmlString());
    }


    br.append("</imgdir>");
    Files.write(new File("Commodity.img.xml").toPath(), br.toString().getBytes());
    System.out.println("Removed " + eventItens);
    System.out.println("News " + newItems);
    System.out.println("Total " + total);
  }


}
