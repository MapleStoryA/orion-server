package tools.packet;

import client.MapleJob;
import client.MapleQuestStatus;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;
import networking.packet.SendPacketOpcode;

@Slf4j
public class CWVsContextOnMessagePackets {

    public static byte[] onQuestRecordMessage(final MapleQuestStatus quest) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeShort(quest.getQuest().getId());
        packet.write(quest.getStatus());
        switch (quest.getStatus()) {
            case 0:
                packet.writeZeroBytes(10);
                break;
            case 1:
                packet.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
                break;
            case 2:
                packet.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
                break;
        }

        return packet.getPacket();
    }

    public static byte[] onIncGPMessage(int points) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(MessageTypes.OnIncGPMessage);
        packet.writeInt(points);

        return packet.getPacket();
    }

    public static byte[] onGiveBuffMessage(int itemId) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(MessageTypes.OnOnGiveBuffMessage);
        packet.writeInt(itemId);

        return packet.getPacket();
    }

    public static byte[] onIncSpMessage(MapleJob job, int amount) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(MessageTypes.OnIncSPMessage);
        packet.writeShort(job.getId());
        packet.write(amount);
        return packet.getPacket();
    }

    /**
     * Display a message in chat log about given amount of fames.
     *
     * @param ItemId
     * @return
     */
    public static byte[] onIncPOPMessage(int amount) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(MessageTypes.OnIncPOPMessage);
        packet.writeInt(amount);
        return packet.getPacket();
    }

    static final class MessageTypes {
        public static final byte OnQuestRecordMessage = 0x01;
        public static final byte OnIncSPMessage = 0x4;
        public static final byte OnIncPOPMessage = 0x5;
        public static final byte OnIncGPMessage = 0x7;
        public static final byte OnOnGiveBuffMessage = 0x8;
    }
}
