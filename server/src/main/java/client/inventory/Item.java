package client.inventory;

import constants.GameConstants;

@lombok.extern.slf4j.Slf4j
public class Item implements IItem {

    private final int id;
    protected MapleRing ring = null;
    private short position;
    private short quantity;
    private byte flag;
    private long expiration = -1, inventoryitemid = 0;
    private MaplePet pet = null;
    private int uniqueid = -1;
    private String owner = "";
    private String giftFrom = "";

    public Item(final int id, final short position, final short quantity, final byte flag, final int uniqueid) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = uniqueid;
    }

    public Item(final int id, final short position, final short quantity, final byte flag) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
    }

    public Item(int id, byte position, short quantity) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
    }

    public IItem copy() {
        final Item ret = new Item(id, position, quantity, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    @Override
    public final int getItemId() {
        return id;
    }

    @Override
    public final short getPosition() {
        return position;
    }

    public final void setPosition(final short position) {
        this.position = position;

        if (pet != null) {
            pet.setInventoryPosition(position);
        }
    }

    @Override
    public final byte getFlag() {
        return flag;
    }

    public final void setFlag(final byte flag) {
        this.flag = flag;
    }

    @Override
    public final short getQuantity() {
        return quantity;
    }

    public void setQuantity(final short quantity) {
        this.quantity = quantity;
    }

    @Override
    public byte getType() {
        return 2; // An Item
    }

    @Override
    public final String getOwner() {
        return owner;
    }

    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    @Override
    public final long getExpiration() {
        return expiration;
    }

    public final void setExpiration(final long expire) {
        this.expiration = expire;
    }

    @Override
    public final int getSN() {
        return uniqueid;
    }

    @Override
    public final void setSN(final int id) {
        this.uniqueid = id;
    }

    public final MaplePet getPet() {
        return pet;
    }

    public final void setPet(final MaplePet pet) {
        this.pet = pet;
    }

    @Override
    public String getGiftFrom() {
        return giftFrom;
    }

    @Override
    public void setGiftFrom(String gf) {
        this.giftFrom = gf;
    }

    @Override
    public int compareTo(IItem other) {
        if (Math.abs(position) < Math.abs(other.getPosition())) {
            return -1;
        } else if (Math.abs(position) == Math.abs(other.getPosition())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IItem)) {
            return false;
        }
        final IItem ite = (IItem) obj;
        return uniqueid == ite.getSN()
                && id == ite.getItemId()
                && quantity == ite.getQuantity()
                && Math.abs(position) == Math.abs(ite.getPosition());
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    @Override
    public MapleRing getRing() {
        if (!GameConstants.isEffectRing(id) || getSN() <= 0) {
            return null;
        }
        if (ring == null) {
            ring = MapleRing.loadFromDb(getSN(), position < 0);
        }
        return ring;
    }

    public void setRing(MapleRing ring) {
        this.ring = ring;
    }

    @Override
    public final long getInventoryId() {
        return inventoryitemid;
    }

    @Override
    public void setInventoryId(long ui) {
        this.inventoryitemid = ui;
    }
}
