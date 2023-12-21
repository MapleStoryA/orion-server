package client.commands;

import client.MapleClient;
import tools.helper.Api;

@Api
class ChangeJobCommand implements Command {
    @Override
    public int execute(MapleClient c, String[] args) {
        c.getPlayer().changeJob(Integer.parseInt(args[0]));
        return 1;
    }

    @Override
    public String getTrigger() {
        return "job";
    }
}
