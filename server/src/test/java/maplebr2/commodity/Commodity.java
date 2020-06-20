package maplebr2.commodity;

import constants.GameConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Commodity {

  public static final String IMG_DIR_OPEN = "<imgdir name=\"%s\">";
  private static final String IMG_DIR_CLOSE = "</imgdir>";
  public static final String INT_FIELD = "<int name=\"%s\" value=\"%s\"/>";

  private String name;

  private Map<String, String> fields = new HashMap<>();
  static int i = 0;

  public Commodity(String name) {
    super();
    this.name = name;
  }

  public void addField(String field, String value) {
    fields.put(field, value);
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public String getName() {
    return name;
  }

  public String toXmlString() {
    StringBuilder str = new StringBuilder();
    str.append(printOpenImgDirTag());
    str.append("\n");
    for (Entry<String, String> entry : this.getFields().entrySet()) {
      str.append(" ");
      str.append(printIntLine(entry.getKey(), entry.getValue()));
      str.append("\n");
    }

    str.append(printCloseImgDirTag());
    str.append(System.lineSeparator());
    return str.toString();
  }

  private String printIntLine(String name, String value) {
    return "\t" + String.format(INT_FIELD, name, value);

  }

  private String printOpenImgDirTag() {
    return String.format(IMG_DIR_OPEN, name);
  }

  private String printCloseImgDirTag() {
    return IMG_DIR_CLOSE;
  }

  public int getPrice() {
    String field = "Price";
    return getField(field);
  }

  private int getField(String field) {
    try {
      return Integer.valueOf(getFields().get(field));
    } catch (NumberFormatException ex) {
      System.out.println(i + " - Error formatting field: " + getFields().get(field));
      i++;
      return -1;
    }
  }

  public int getPeriod() {
    return getField("Period");
  }

  public boolean isOnSale() {
    return getField("OnSale") == 1;
  }

  public int getItemId() {
    return getField("ItemId");
  }

  public boolean isEquip() {
    return getItemId() / 1000000 == 1;
  }

  public void removeField(String string) {
    fields.remove(string);
  }

  public boolean isPet() {
    return GameConstants.isPet(getItemId());
  }

  public boolean isEffect() {
    return (getItemId() / 100000) == 505;
  }

  public Integer getSN() {
    return getField("SN");
  }

  public void setOnSale(boolean sale) {
    this.fields.put("OnSale", String.valueOf(sale ? 1 : 0));
  }

  public void setPrice(int price) {
    this.fields.put("Price", String.valueOf(price));

  }

  public void setPeriod(int period) {
    this.fields.put("Period", String.valueOf(period));

  }

}
