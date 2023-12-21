package client.commands;

import client.MapleClient;
import client.skill.SkillFactory;
import tools.helper.Api;

@Api
class HideCommand implements Command {
    @Override
    public int execute(MapleClient c, String[] args) {
        SkillFactory.getSkill(9101004).getEffect(1).applyTo(c.getPlayer());
        return 1;
    }

    @Override
    public String getTrigger() {
        return "hide";
    }
}
