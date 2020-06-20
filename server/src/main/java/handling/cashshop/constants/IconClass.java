package handling.cashshop.constants;

public enum IconClass {
  ID_ICON_CLASS_NEW(0x0),
  ID_ICON_CLASS_SALE(0x1),
  ID_ICON_CLASS_HOT(0x2),
  ID_ICON_CLASS_EVENT(0x3),
  ID_ICON_CLASS_LIMIT(0x4);

  private int type;

  IconClass(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
