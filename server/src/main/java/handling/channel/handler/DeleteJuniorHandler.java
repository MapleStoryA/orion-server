package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class DeleteJuniorHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {}
}
