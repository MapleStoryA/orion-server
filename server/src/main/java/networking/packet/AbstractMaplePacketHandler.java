package networking.packet;

import client.MapleClient;

public abstract class AbstractMaplePacketHandler implements MaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return c.isLoggedIn();
    }
}
