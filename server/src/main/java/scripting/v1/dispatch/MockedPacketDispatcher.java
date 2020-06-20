package scripting.v1.dispatch;

import client.MapleClient;

public class MockedPacketDispatcher implements PacketDispatcher {

  @Override
  public void dispatch(MapleClient client, byte[] packet) {
    System.out.println("Dispatching packet");

  }

}
