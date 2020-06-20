package tools.packet;

import client.MapleJob;
import client.MapleQuestStatus;
import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;


public class CWVsContextOnMessagePackets {

  static final class MessageTypes {
    public static final byte OnQuestRecordMessage = 0x01;
    public static final byte OnIncSPMessage = 0x4;
    public static final byte OnIncPOPMessage = 0x5;
    public static final byte OnIncGPMessage = 0x7;
    public static final byte OnOnGiveBuffMessage = 0x8;

  }

  public static byte[] onQuestRecordMessage(final MapleQuestStatus quest) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(1);
    mplew.writeShort(quest.getQuest().getId());
    mplew.write(quest.getStatus());
    switch (quest.getStatus()) {
      case 0:
        mplew.writeZeroBytes(10);
        break;
      case 1:
        mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
        break;
      case 2:
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        break;
    }

    return mplew.getPacket();
  }


  public static byte[] onIncGPMessage(int points) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(MessageTypes.OnIncGPMessage);
    mplew.writeInt(points);

    return mplew.getPacket();
  }


  public static byte[] onGiveBuffMessage(int itemId) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(MessageTypes.OnOnGiveBuffMessage);
    mplew.writeInt(itemId);

    return mplew.getPacket();
  }


  public static byte[] onIncSpMessage(MapleJob job, int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(MessageTypes.OnIncSPMessage);
    mplew.writeShort(job.getId());
    mplew.write(amount);
    return mplew.getPacket();
  }

  /**
   * Display a message in chat log about given amount of fames.
   *
   * @param ItemId
   * @return
   */
  public static byte[] onIncPOPMessage(int amount) {
    final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
    mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
    mplew.write(MessageTypes.OnIncPOPMessage);
    mplew.writeInt(amount);
    return mplew.getPacket();
  }


}
