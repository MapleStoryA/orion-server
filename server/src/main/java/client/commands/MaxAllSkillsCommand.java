package client.commands;

import client.MapleClient;
import scripting.v1.game.helper.ApiClass;

@ApiClass
public class MaxAllSkillsCommand implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) {
        c.getPlayer().maxMastery();
        c.getPlayer().maxAllSkills();
        c.getPlayer().dropMessage(5, "It's done.");
    }

    @Override
    public String getTrigger() {
        return "maxallskills";
    }
}
