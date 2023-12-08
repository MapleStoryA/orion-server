package tools.packet.cfield;

import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class CFieldBattleField {

    public byte[] OnScoreUpdate(int wolves, int sheeps) {
        final COutPacket packet = new COutPacket();
        packet.writeShort(348);
        packet.write(wolves);
        packet.write(sheeps);
        return packet.getPacket();
    }
}
