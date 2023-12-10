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
        }
    }

    @Override
    public String getTrigger() {
        return "level";
    }
}
