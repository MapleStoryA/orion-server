package handling.channel.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class ViciousHammerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.skip(8);
        c.getSession().write(MTSCSPacket.ViciousHammer(false, (byte) 0));
    }
}
