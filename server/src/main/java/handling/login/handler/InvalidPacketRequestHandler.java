package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class InvalidPacketRequestHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().close();
    }

    @Override
    public boolean validateState(MapleClient c) {
        return true;
    }

}
