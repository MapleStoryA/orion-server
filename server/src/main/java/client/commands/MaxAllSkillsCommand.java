package client.commands;

import client.MapleClient;
import tools.ApiClass;

@ApiClass
public class MaxAllSkillsCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] args) {
        c.getPlayer().maxMastery();
        c.getPlayer().maxAllSkills();
        c.getPlayer().dropMessage(5, "It's done.");
    }

    @Override
    public String getTrigger() {
        return "maxallskills";
    }
}
