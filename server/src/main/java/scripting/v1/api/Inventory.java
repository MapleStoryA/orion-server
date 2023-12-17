package scripting.v1.api;

import tools.Scripting;

public interface Inventory {
    @Scripting
    int slotCount(byte type);

    @Scripting
    int holdCount(byte type);

    @Scripting
    int itemCount(int item);

    @Scripting
    int exchange(int money, int id, short quantity);

    @Scripting
    void incSlotCount(int type, byte value);

    // Like in bms, items = item, count * n
    @Scripting
    int exchange(int money, int... items);
}
