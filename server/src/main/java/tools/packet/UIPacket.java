package tools.packet;

import networking.packet.SendPacketOpcode;
import tools.MaplePacketCreator;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class UIPacket {

    public static final byte[] EarnTitleMsg(final String msg) {
        final OutPacket packet = new OutPacket();

        // "You have acquired the Pig's Weakness skill."
        packet.writeShort(SendPacketOpcode.TOP_MSG.getValue());
        packet.writeMapleAsciiString(msg);

        return packet.getPacket();
    }

    public static byte[] getSPMsg(byte sp, short job) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(4);
        packet.writeShort(job);
        packet.write(sp);

        return packet.getPacket();
    }

    public static byte[] getGPMsg(int itemid) {
        OutPacket packet = new OutPacket();

        // Temporary transformed as a dragon, even with the skill ......
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(7);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static byte[] getTopMsg(String msg) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TOP_MSG.getValue());
        packet.writeMapleAsciiString(msg);

        return packet.getPacket();
    }

    public static byte[] getStatusMsg(int itemid) {
        OutPacket packet = new OutPacket();

        // Temporary transformed as a dragon, even with the skill ......
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(8);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static final byte[] MapEff(final String path) {
        return MaplePacketCreator.environmentChange(path, 3);
    }

    public static final byte[] MapNameDisplay(final int mapid) {
        return MaplePacketCreator.environmentChange("maplemap/enter/" + mapid, 3);
    }

    public static final byte[] Aran_Start() {
        return MaplePacketCreator.environmentChange("Aran/balloon", 4);
    }

    public static final byte[] AranTutInstructionalBalloon(final String data) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(23);
        packet.writeMapleAsciiString(data);
        packet.writeInt(1);

        return packet.getPacket();
    }

    public static final byte[] ShowWZEffect(final String data) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(18);
        packet.writeMapleAsciiString(data);

        return packet.getPacket();
    }

    public static byte[] summonHelper(boolean summon) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
        packet.write(summon ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] summonMessage(int type) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        packet.write(1);
        packet.writeInt(type);
        packet.writeInt(7000); // probably the delay

        return packet.getPacket();
    }

    public static byte[] summonMessage(String message) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        packet.write(0);
        packet.writeMapleAsciiString(message);
        packet.writeInt(200); // IDK
        packet.writeShort(0);
        packet.writeInt(10000); // Probably delay

        return packet.getPacket();
    }

    public static byte[] IntroLock(boolean enable) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
        packet.write(enable ? 1 : 0);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] IntroDisableUI(boolean enable) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
        packet.write(enable ? 1 : 0);

        return packet.getPacket();
    }
}
