package client;

import client.skill.SkillMacro;
import tools.MaplePacketCreator;

public class SavedSkillMacro {
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private boolean changed;
    private int index = 0;

    public void sendMacros(MapleClient client) {
        client.getSession().write(MaplePacketCreator.getMacros(skillMacros));
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
        changed = true;
    }

    public final SkillMacro[] getSkillMacros() {
        return skillMacros;
    }

    public void add(SkillMacro macro) {
        skillMacros[index++] = macro;
    }
}
