package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class CWVsContextPackets {


  public static byte[] onSetClaimSvrAvailableTime(int nClaimSvrOpenTime, int m_nClaimSvrCloseTime) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.CLAIM_SERVER_AVAILABLE_TIME.getValue());
    mplew.write(m_nClaimSvrCloseTime);
    mplew.write(m_nClaimSvrCloseTime);

    return mplew.getPacket();
  }


}
