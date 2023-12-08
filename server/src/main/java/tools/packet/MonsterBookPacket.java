package tools.packet;

import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

@lombok.extern.slf4j.Slf4j
public class MonsterBookPacket {

    public static byte[] addCard(boolean full, int cardid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());

        if (!full) {
            mplew.write(1);
            mplew.writeInt(cardid);
            mplew.writeInt(level);
        } else {
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] showGainCard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(13);

        return mplew.getPacket();
    }

    public static byte[] showForeginCardEffect(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(13);

        return mplew.getPacket();
    }

    public static byte[] changeCover(int cardid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTERBOOK_CHANGE_COVER.getValue());
        mplew.writeInt(cardid);

        return mplew.getPacket();
    }
}
