package tools.packet.npcpool;

import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import networking.packet.SendPacketOpcode;
import server.life.MapleNPC;

@Slf4j
public class NpcPoolPackets {

    public static final byte[] setSpecialAction(MapleNPC npc, String action) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SET_NPC_ACTION.getValue());
        packet.writeInt(npc.getObjectId());
        packet.writeMapleAsciiString(action);
        return packet.getPacket();
    }

    /** @Param show - If false npc is removed, otherwise npc is visible again, with an annimation */
    public static final byte[] onUpdateLimitedInfo(MapleNPC npc, boolean show) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.NPC_UPDATED_LIMITED_INFO.getValue());
        packet.writeInt(npc.getObjectId());
        packet.writeBool(show);
        return packet.getPacket();
    }
}
