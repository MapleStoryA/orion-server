package tools.packet.cfield;

import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;

@Slf4j
public class CFieldBattleField {

    public byte[] OnScoreUpdate(int wolves, int sheeps) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(348);
        packet.write(wolves);
        packet.write(sheeps);
        return packet.getPacket();
    }
}
