package handling;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public interface MaplePacketHandler {

  void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c);

  boolean validateState(MapleClient c);

}
