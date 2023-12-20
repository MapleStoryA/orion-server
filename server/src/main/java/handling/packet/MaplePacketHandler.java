package handling.packet;

import client.MapleClient;
import tools.data.input.InPacket;

public interface MaplePacketHandler {

    void handlePacket(final InPacket packet, final MapleClient c);

    boolean validateState(MapleClient c);
}
