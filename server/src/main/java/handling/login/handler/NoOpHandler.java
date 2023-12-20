package handling.login.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

public class NoOpHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(InPacket packet, MapleClient c) {}
}
