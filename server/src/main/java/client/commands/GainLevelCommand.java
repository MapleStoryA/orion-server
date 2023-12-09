package client.commands;

import client.MapleClient;
import scripting.v1.game.helper.ApiClass;

@ApiClass
public class GainLevelCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        if (c.getPlayer().getLevel() < 200) {
            c.getPlayer().gainExp(500000000, true, false, true);
        }
    }

    @Override
    public String getTrigger() {
        return "level";
    }
}
