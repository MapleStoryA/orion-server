package tools.packet.userlocal;

import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class CUserLocalPackets {

  public static byte[] onGoToCommoditySN(int sn) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.GO_TO_CS_BY_SN.getValue());
    mplew.writeInt(sn);

    return mplew.getPacket();
  }

  //Shows a effect given the use item id.
  public static byte[] onBuffzoneEffect(int itemId) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.BUFFED_ZONE_EFFECT.getValue());
    mplew.writeInt(itemId);

    return mplew.getPacket();
  }

}
