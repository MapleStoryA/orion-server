package tools.packet.userpool;

import tools.data.output.MaplePacketLittleEndianWriter;

public class UserPoolOnCommonPacket {

  public static byte[] onADBoard(int charId, String text) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(181);
    mplew.writeInt(charId);
    mplew.write(0);
    mplew.writeMapleAsciiString(text);
    return mplew.getPacket();
  }

  public static byte[] onADBoardDisable(int charId) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(181);
    mplew.writeInt(charId);
    mplew.write(0);

    return mplew.getPacket();
  }

}
