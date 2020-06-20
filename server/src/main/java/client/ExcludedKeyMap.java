package client;

public enum ExcludedKeyMap {
  PartySearch(25),
  Family(26);
  private int key;

  ExcludedKeyMap(int key) {
    this.key = key;
  }

  public int getKey() {
    return this.key;
  }

  public static ExcludedKeyMap fromKeyValue(int key) {
    for (ExcludedKeyMap e : values()) {
      if (e.getKey() == key) {
        return e;
      }
    }
    return null;
  }
}
