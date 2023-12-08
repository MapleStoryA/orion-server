package handling.world.guild;

import tools.MaplePacketCreator;

public enum MapleGuildResponse {
    NOT_IN_CHANNEL(0x2a),
    ALREADY_IN_GUILD(0x28),
    NOT_IN_GUILD(0x2d);
    private final int value;

    MapleGuildResponse(int val) {
        value = val;
    }

    public int getValue() {
        return value;
    }

    public byte[] getPacket() {
        return MaplePacketCreator.genericGuildMessage((byte) value);
    }
}
