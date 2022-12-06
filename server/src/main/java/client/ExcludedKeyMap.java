package client;

public enum ExcludedKeyMap {
    PartySearch(25),
    Family(26);
    private final int key;

    ExcludedKeyMap(int key) {
        this.key = key;
    }

    public static ExcludedKeyMap fromKeyValue(int key) {
        for (ExcludedKeyMap e : values()) {
            if (e.getKey() == key) {
                return e;
            }
        }
        return null;
    }

    public int getKey() {
        return this.key;
    }
}
