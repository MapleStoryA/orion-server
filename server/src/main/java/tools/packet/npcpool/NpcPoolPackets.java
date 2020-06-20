package tools.packet.npcpool;

import handling.SendPacketOpcode;
import server.life.MapleNPC;
import tools.data.output.MaplePacketLittleEndianWriter;

public class NpcPoolPackets {


  public static final byte[] setSpecialAction(MapleNPC npc, String action) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SET_NPC_ACTION.getValue());
    mplew.writeInt(npc.getObjectId());
    mplew.writeMapleAsciiString(action);
    return mplew.getPacket();
  }

  /**
   * @Param show - If false npc is removed, otherwise npc is visible again, with an annimation
   */
  public static final byte[] onUpdateLimitedInfo(MapleNPC npc, boolean show) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.NPC_UPDATED_LIMITED_INFO.getValue());
    mplew.writeInt(npc.getObjectId());
    mplew.writeBool(show);
    return mplew.getPacket();
  }
}
