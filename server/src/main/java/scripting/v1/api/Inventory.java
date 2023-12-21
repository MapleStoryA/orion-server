package scripting.v1.api;

import tools.helper.Api;

public interface Inventory {
    @Api
    int slotCount(byte type);

    @Api
    int holdCount(byte type);

    @Api
    int itemCount(int item);

    @Api
    int exchange(int money, int id, short quantity);

    @Api
    void incSlotCount(int type, byte value);

    // Like in bms, items = item, count * n
    @Api
    int exchange(int money, int... items);
}
