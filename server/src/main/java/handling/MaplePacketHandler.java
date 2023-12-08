package handling;

import client.MapleClient;
import tools.data.input.CInPacket;

public interface MaplePacketHandler {

    void handlePacket(final CInPacket packet, final MapleClient c);

    boolean validateState(MapleClient c);

}
