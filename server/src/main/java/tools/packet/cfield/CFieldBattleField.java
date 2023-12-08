package tools.packet.cfield;

import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class CFieldBattleField {

    public byte[] OnScoreUpdate(int wolves, int sheeps) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(348);
        packet.write(wolves);
        packet.write(sheeps);
        return packet.getPacket();
    }
}
