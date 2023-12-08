package client.inventory;

public interface IItem extends Comparable<IItem> {

    byte getType();

    short getPosition();

    void setPosition(short position);

    byte getFlag();

    void setFlag(byte flag);

    short getQuantity();

    void setQuantity(short quantity);

    String getOwner();

    void setOwner(String owner);

    int getItemId();

    MaplePet getPet();

    int getSN();

    void setSN(int id);

    IItem copy();

    long getExpiration();

    void setExpiration(long expire);

    long getInventoryId();

    void setInventoryId(long ui);

    String getGiftFrom();

    void setGiftFrom(String gf);

    MapleRing getRing();

    default boolean isNotVisible() {
        return getPosition() < -128;
    }
}
