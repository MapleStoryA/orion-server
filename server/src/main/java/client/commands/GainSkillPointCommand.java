package client.commands;

import client.MapleClient;
import tools.helper.Api;

@Api
class GainSkillPointCommand implements Command {

    @Override
    public int execute(MapleClient c, String[] args) {
        if (args.length < 1) {
            c.getPlayer().dropMessage(5, "Missing sp quantity.");
            return 0;
        }
        var first_argument = args[0];
        c.getPlayer().gainSp(Integer.valueOf(first_argument));
        return 1;
    }

    @Override
    public String getTrigger() {
        return "gainsp";
    }
}
