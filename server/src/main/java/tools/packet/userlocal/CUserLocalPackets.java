package tools.packet.userlocal;

import handling.packet.SendPacketOpcode;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class CUserLocalPackets {

    public static byte[] onGoToCommoditySN(int sn) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.GO_TO_CS_BY_SN.getValue());
        packet.writeInt(sn);

        return packet.getPacket();
    }

    // Shows a effect given the use item id.
    public static byte[] onBuffzoneEffect(int itemId) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.BUFFED_ZONE_EFFECT.getValue());
        packet.writeInt(itemId);

        return packet.getPacket();
    }
}
