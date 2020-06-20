package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ReportPackets {

  public static byte[] enableReport() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
    mplew.writeShort(SendPacketOpcode.ENABLE_REPORT.getValue());
    mplew.write(3);
    return mplew.getPacket();
  }

  public static byte[] reportResponse(byte mode, int remainingReports) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
    mplew.write(mode);
    if (mode == 2) {
      mplew.write(1);
      mplew.writeInt(remainingReports);
    }
    return mplew.getPacket();
  }
}
