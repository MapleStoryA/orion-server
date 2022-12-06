package server;

@lombok.extern.slf4j.Slf4j
public class StructRewardItem {

    public int itemid;
    public long period;
    public short prob, quantity;
    public String effect, worldmsg;
}
