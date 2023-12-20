package networking.packet;

public interface MaplePacket extends java.io.Serializable {
    byte[] getBytes();

    Runnable getOnSend();

    void setOnSend(Runnable onSend);
}
