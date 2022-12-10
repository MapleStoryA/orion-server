package server.maps;

public enum SavedLocationType {

    FREE_MARKET(0),
    MULUNG_TC(1),
    WORLDTOUR(2),
    FLORINA(3),
    FISHING(4),
    RICHIE(5),
    DONGDONGCHIANG(6),
    EVENT(7),
    AMORIA(8),
    CHRISTMAS(9);
    private final int index;

    SavedLocationType(int index) {
        this.index = index;
    }

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }

    public int getValue() {
        return index;
    }

    public static SavedLocationType fromCode(int code) {
        for (var state : SavedLocationType.values()) {
            if (code == state.index) {
                return state;
            }
        }
        return null;
    }
}
