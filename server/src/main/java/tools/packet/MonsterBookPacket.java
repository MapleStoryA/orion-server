package tools.packet;

import networking.data.output.OutPacket;
import networking.packet.SendPacketOpcode;

@lombok.extern.slf4j.Slf4j
public class MonsterBookPacket {

    public static byte[] addCard(boolean full, int cardid, int level) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());

        if (!full) {
            packet.write(1);
            packet.writeInt(cardid);
            packet.writeInt(level);
        } else {
            packet.write(0);
        }

        return packet.getPacket();
    }

    public static byte[] showGainCard() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(13);

        return packet.getPacket();
    }

    public static byte[] showForeginCardEffect(int id) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(id);
        packet.write(13);

        return packet.getPacket();
    }

    public static byte[] changeCover(int cardid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTERBOOK_CHANGE_COVER.getValue());
        packet.writeInt(cardid);

        return packet.getPacket();
    }
}
