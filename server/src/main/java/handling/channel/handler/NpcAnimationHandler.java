package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.SendPacketOpcode;
import tools.data.input.CInPacket;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class NpcAnimationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket slea, MapleClient c) {
        COutPacket packet = new COutPacket();
        packet.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
        final int length = (int) slea.available();
        if (length == 6) { // NPC Talk
            packet.writeInt(slea.readInt());
            packet.writeShort(slea.readShort());
        } else if (length > 6) { // NPC Move
            packet.write(slea.read(length - 9));
        } else {
            return;
        }
        c.sendPacket(packet.getPacket());
    }

}
