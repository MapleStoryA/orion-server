package handling.channel.handler;

import client.MapleClient;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class UseFamilyHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {}
}
