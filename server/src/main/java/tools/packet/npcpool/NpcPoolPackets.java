package tools.packet.npcpool;

import handling.SendPacketOpcode;
import server.life.MapleNPC;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class NpcPoolPackets {

    public static final byte[] setSpecialAction(MapleNPC npc, String action) {
        final COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.SET_NPC_ACTION.getValue());
        packet.writeInt(npc.getObjectId());
        packet.writeMapleAsciiString(action);
        return packet.getPacket();
    }

    /** @Param show - If false npc is removed, otherwise npc is visible again, with an annimation */
    public static final byte[] onUpdateLimitedInfo(MapleNPC npc, boolean show) {
        final COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.NPC_UPDATED_LIMITED_INFO.getValue());
        packet.writeInt(npc.getObjectId());
        packet.writeBool(show);
        return packet.getPacket();
    }
}
