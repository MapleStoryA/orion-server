package scripting.v1.binding;

import client.MapleCharacter;
import scripting.v1.dispatch.RealPacketDispatcher;

import java.util.ArrayList;
import java.util.Collection;

public class BindingWrapper {

  public static TargetScript wrapCharacter(MapleCharacter player) {
    return new TargetScript(player.getClient(), new RealPacketDispatcher());
  }

  public static Collection<TargetScript> wrapCharacter(Collection<MapleCharacter> players) {
    ArrayList<TargetScript> list = new ArrayList<>();
    for (MapleCharacter chr : players) {
      list.add(wrapCharacter(chr));
    }
    return list;
  }


}
