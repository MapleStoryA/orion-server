package client.commands;

import client.MapleClient;
import tools.helper.Api;

@Api
public class MaxAllSkillsCommand implements Command {

    @Override
    public int execute(MapleClient c, String[] args) {
        c.getPlayer().maxMastery();
        c.getPlayer().maxAllSkills();
        c.getPlayer().dropMessage(5, "It's done.");
        return 1;
    }

    @Override
    public String getTrigger() {
        return "maxallskills";
    }
}
