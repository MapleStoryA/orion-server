package scripting.v1.dispatch;

import client.MapleClient;

public class RealPacketDispatcher implements PacketDispatcher {

  @Override
  public void dispatch(MapleClient client, byte[] packet) {
    client.sendPacket(packet);
  }

}
