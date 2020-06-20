package client;

import tools.MaplePacketCreator;

public class Messages {

  private final MapleClient client;

  public Messages(MapleClient client) {
    super();
    this.client = client;
  }

  public void megaphone(String message, boolean whisper) {
    client.sendPacket(MaplePacketCreator.serverMessage(2, client.getChannel(), message, whisper));
  }

  public void popup(String message) {
    client.sendPacket(MaplePacketCreator.serverMessage(1, client.getChannel(), message, false));
  }

  public void pinkText(String message) {
    client.sendPacket(MaplePacketCreator.serverMessage(5, client.getChannel(), message, false));
  }

  public void yellowSupermega(String message) {
    client.sendPacket(MaplePacketCreator.serverMessage(9, client.getChannel(), client.getPlayer().getName() + " : " + message, false));
  }

}
