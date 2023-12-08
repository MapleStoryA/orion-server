package client.commands;

import client.MapleClient;
import scripting.v1.game.helper.ApiClass;

@ApiClass
public class GainSkillPointCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) {
        if (splitted.length < 1) {
            c.getPlayer().dropMessage(5, "Missing sp quantity.");
            return;
        }
        var first_argument = splitted[0];
        c.getPlayer().gainSp(Integer.valueOf(first_argument));
    }

    @Override
    public String getTrigger() {
        return "gainsp";
    }
}
