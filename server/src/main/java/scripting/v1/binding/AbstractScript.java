package scripting.v1.binding;

import client.MapleCharacter;
import client.MapleClient;
import scripting.v1.dispatch.PacketDispatcher;

public class AbstractScript {


  protected MapleClient client;

  protected MapleCharacter player;

  protected Object continuation;

  protected PacketDispatcher dispatcher;

  public AbstractScript(MapleClient client, PacketDispatcher dispatcher) {
    super();
    this.client = client;
    this.dispatcher = dispatcher;
    this.player = client.getPlayer();
  }


  public MapleClient getClient() {
    return client;
  }

  public MapleCharacter getPlayer() {
    return this.player;
  }

  public void sendPacket(byte[] packet) {
    dispatcher.dispatch(client, packet);
  }

  public void broadcastPacket(byte[] packet) {
    player.getMap().broadcastMessage(packet);
  }
}
