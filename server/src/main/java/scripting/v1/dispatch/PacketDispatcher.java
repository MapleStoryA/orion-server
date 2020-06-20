package scripting.v1.dispatch;

import client.MapleClient;

public interface PacketDispatcher {
  void dispatch(MapleClient client, byte[] packet);
}
