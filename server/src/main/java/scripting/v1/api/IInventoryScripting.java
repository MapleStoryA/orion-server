package scripting.v1.game.api;

import tools.ApiClass;

public interface IInventoryScripting {
    @ApiClass
    int slotCount(byte type);

    @ApiClass
    int holdCount(byte type);

    @ApiClass
    int itemCount(int item);

    @ApiClass
    int exchange(int money, int id, short quantity);

    @ApiClass
    void incSlotCount(int type, byte value);

    // Like in bms, items = item, count * n
    @ApiClass
    int exchange(int money, int... items);
}
