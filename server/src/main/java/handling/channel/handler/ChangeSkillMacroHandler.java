package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.skill.SkillMacro;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class ChangeSkillMacroHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final int num = packet.readByte();
        String name;
        int shout, skill1, skill2, skill3;
        SkillMacro macro;

        for (int i = 0; i < num; i++) {
            name = packet.readMapleAsciiString();
            shout = packet.readByte();
            skill1 = packet.readInt();
            skill2 = packet.readInt();
            skill3 = packet.readInt();

            macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
            chr.getSkillMacros().updateMacros(i, macro);
        }
    }
}
