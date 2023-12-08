package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class CWVsContextPackets {


    public static byte[] onSetClaimSvrAvailableTime(int nClaimSvrOpenTime, int m_nClaimSvrCloseTime) {
        final COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.CLAIM_SERVER_AVAILABLE_TIME.getValue());
        packet.write(m_nClaimSvrCloseTime);
        packet.write(m_nClaimSvrCloseTime);

        return packet.getPacket();
    }


}
