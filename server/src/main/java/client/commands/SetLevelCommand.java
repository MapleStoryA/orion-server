package client.commands;

import client.MapleClient;
import client.MapleStat;
import tools.helper.Api;

@Api
class SetLevelCommand implements Command {
    @Override
    public int execute(MapleClient c, String[] args) {
        if (args.length > 1 && args[1] != null && "set".equals(args[1])) {
            short level = Short.valueOf(args[0]);
            c.getPlayer().setLevel(level);
            c.getPlayer().updateSingleStat(MapleStat.LEVEL, level);
            return 0;
        }
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
        return 1;
    }

    @Override
    public String getTrigger() {
        return "level";
    }
}
