package scripting.v1.game.helper;

import client.MapleCharacter;
import scripting.v1.game.TargetScripting;

import java.util.ArrayList;
import java.util.Collection;

@lombok.extern.slf4j.Slf4j
public class BindingHelper {

    public static TargetScripting wrapCharacter(MapleCharacter player) {
        return new TargetScripting(player.getClient());
    }

    public static Collection<TargetScripting> wrapCharacter(Collection<MapleCharacter> players) {
        ArrayList<TargetScripting> list = new ArrayList<>();
        for (MapleCharacter chr : players) {
            list.add(wrapCharacter(chr));
        }
        return list;
    }


}
