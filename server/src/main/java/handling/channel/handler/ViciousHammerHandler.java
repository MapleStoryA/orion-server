package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.packet.MTSCSPacket;

@Slf4j
public class ViciousHammerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.skip(8);
        c.getSession().write(MTSCSPacket.ViciousHammer(false, (byte) 0));
    }
}
