package server;

@lombok.extern.slf4j.Slf4j
public class StructRewardItem {

    private int itemId;
    private long period;
    private short prob;
    private short quantity;
    private String effect;
    private String worldMsg;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public short getProb() {
        return prob;
    }

    public void setProb(short prob) {
        this.prob = prob;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getWorldMsg() {
        return worldMsg;
    }

    public void setWorldMsg(String worldMsg) {
        this.worldMsg = worldMsg;
    }
}
