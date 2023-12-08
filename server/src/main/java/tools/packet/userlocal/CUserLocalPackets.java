package tools.packet.userlocal;

import handling.SendPacketOpcode;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class CUserLocalPackets {

    public static byte[] onGoToCommoditySN(int sn) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.GO_TO_CS_BY_SN.getValue());
        packet.writeInt(sn);

        return packet.getPacket();
    }

    // Shows a effect given the use item id.
    public static byte[] onBuffzoneEffect(int itemId) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.BUFFED_ZONE_EFFECT.getValue());
        packet.writeInt(itemId);

        return packet.getPacket();
    }
}
