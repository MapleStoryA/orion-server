package client.commands;

import client.MapleClient;
import tools.ApiClass;

@ApiClass
class GainLevelCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        if (args[0] != null && Integer.valueOf(args[0]) < 200) {
            for (int i = 0; i < 200; i++) {
                c.getPlayer().levelUp(true);
            }
            return;
        }

        if (c.getPlayer().getLevel() < 200) {
            c.getPlayer().gainExp(500000000, true, false, true);
        }

    }

    @Override
    public String getTrigger() {
        return "level";
    }
}
