package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;
import tools.packet.MTSCSPacket;

@lombok.extern.slf4j.Slf4j
public class ViciousHammerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        packet.skip(8);
        c.getSession().write(MTSCSPacket.ViciousHammer(false, (byte) 0));
    }
}
