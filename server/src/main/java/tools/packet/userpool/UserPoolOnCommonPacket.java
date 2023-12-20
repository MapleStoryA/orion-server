package tools.packet.userpool;

import networking.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class UserPoolOnCommonPacket {

    public static byte[] onADBoard(int charId, String text) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(181);
        packet.writeInt(charId);
        packet.write(0);
        packet.writeMapleAsciiString(text);
        return packet.getPacket();
    }

    public static byte[] onADBoardDisable(int charId) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(181);
        packet.writeInt(charId);
        packet.write(0);

        return packet.getPacket();
    }
}
