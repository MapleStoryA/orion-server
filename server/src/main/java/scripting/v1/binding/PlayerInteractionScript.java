package scripting.v1.binding;

import client.MapleClient;
import handling.channel.ChannelServer;
import scripting.v1.dispatch.PacketDispatcher;
import tools.MaplePacketCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerInteractionScript extends AbstractScript {

  public PlayerInteractionScript(MapleClient client, PacketDispatcher dispatcher) {
    super(client, dispatcher);
  }

  protected ChannelServer getChannelServer() {
    return ChannelServer.getInstance(client.getChannel());
  }

  public void debug(String text) {
    System.out.println(text);
  }

  public void test() {
    sendPacket(MaplePacketCreator.updateQuestFinish(21015, 2005, 0));
  }

  public long currentTime() {
    return System.currentTimeMillis();
  }

  public int random(int min, int max) {
    Random random = new Random();
    int randomNumber = random.nextInt(max + 1 - min) + min;
    return randomNumber;
  }

  public String shuffle(int i, String str) {
    List<String> list = new ArrayList<>();
    for (char c : str.toCharArray()) {
      list.add(String.valueOf(c));
    }
    String ret = "";
    for (String c : list) {
      ret += c;
    }
    return ret;
  }

  public final void showBallon(final String msg, final int width, final int height) {
    sendPacket(MaplePacketCreator.sendHint(msg, width, height));
  }


}
