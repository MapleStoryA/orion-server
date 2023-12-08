package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class ReportPackets {

    public static byte[] enableReport() {
        COutPacket packet = new COutPacket(3);
        packet.writeShort(SendPacketOpcode.ENABLE_REPORT.getValue());
        packet.write(3);
        return packet.getPacket();
    }

    public static byte[] reportResponse(byte mode, int remainingReports) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
        packet.write(mode);
        if (mode == 2) {
            packet.write(1);
            packet.writeInt(remainingReports);
        }
        return packet.getPacket();
    }
}
