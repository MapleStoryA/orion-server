package handling.channel.handler;

import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import networking.packet.SendPacketOpcode;
import tools.data.input.InPacket;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class NpcAnimationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket slea, MapleClient c) {
        OutPacket packet = new OutPacket();
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
