package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public final class NettsPyramid {

  public enum NettsInfoType {
    COOL,
    KILL,
    MISS,
    BUFFS
  }


  private NettsPyramid() {
  }

  /**
   * Type values:
   * 0 - cool
   * 1 - kill
   * 2 - miss
   * 3 - buffs
   * ps: expedia was here, don't remove my ign pl0x
   */
  public static byte[] updatePyramidInfo(NettsInfoType type, int amount) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
    mplew.writeMapleAsciiString(getTypeValue(type));
    mplew.writeMapleAsciiString(Integer.toString(amount)); // Just like dojo, nexon sends it like this (30 + amount)
    return mplew.getPacket();
  }

  private static String getTypeValue(NettsInfoType type) {
    return "massacre_" + (type == NettsInfoType.COOL ? "cool" : type == NettsInfoType.KILL ? "hit" : "miss");
  }

}
