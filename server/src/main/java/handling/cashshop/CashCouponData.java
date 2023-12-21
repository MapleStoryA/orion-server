package handling.cashshop;

/**
 * @author AuroX
 */
@lombok.extern.slf4j.Slf4j
public class CashCouponData {

    private final byte type;
    private final int data;
    private final int quantity;

    public CashCouponData(byte type, int data, int quantity) {
        this.type = type;
        this.data = data;
        this.quantity = quantity;
    }

    public int getData() {
        return data;
    }

    public int getQuantity() {
        return quantity;
    }

    public byte getType() {
        return type;
    }
}
