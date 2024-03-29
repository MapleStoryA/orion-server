package tools.packet;

import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import networking.packet.SendPacketOpcode;

@Slf4j
public class CWVsContextPackets {

    public static byte[] onSetClaimSvrAvailableTime(int nClaimSvrOpenTime, int m_nClaimSvrCloseTime) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CLAIM_SERVER_AVAILABLE_TIME.getValue());
        packet.write(m_nClaimSvrCloseTime);
        packet.write(m_nClaimSvrCloseTime);

        return packet.getPacket();
    }
}
