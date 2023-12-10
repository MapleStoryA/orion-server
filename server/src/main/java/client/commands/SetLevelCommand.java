package client.commands;

import client.MapleClient;
import tools.ApiClass;

@ApiClass
class SetLevelCommand implements Command {
    @Override
    public void execute(MapleClient c, String[] args) {
        if (args[0] != null) {
            int level = Integer.valueOf(args[0]);
            if (level > 1 && level < 200 && c.getPlayer().getLevel() < level) {
                int targetLevels = level - c.getPlayer().getLevel();
                for (int i = 0; i < targetLevels; i++) {
                    c.getPlayer().levelUp(true);
                    c.getPlayer().setExp(0);
                }
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
