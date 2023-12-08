package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class ReportPackets {

    public static byte[] enableReport() {
        OutPacket packet = new OutPacket(3);
        packet.writeShort(SendPacketOpcode.ENABLE_REPORT.getValue());
        packet.write(3);
        return packet.getPacket();
    }

    public static byte[] reportResponse(byte mode, int remainingReports) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
        packet.write(mode);
        if (mode == 2) {
            packet.write(1);
            packet.writeInt(remainingReports);
        }
        return packet.getPacket();
    }
}
