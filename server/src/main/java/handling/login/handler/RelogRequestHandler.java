package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class RelogRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(LoginPacket.getRelogResponse());
    }

}
