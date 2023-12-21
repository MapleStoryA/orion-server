package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;

@Slf4j
public class RequestFamilyHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {}
}
