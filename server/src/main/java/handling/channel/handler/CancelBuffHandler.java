package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.skill.ISkill;
import client.skill.SkillFactory;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class CancelBuffHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        int sourceid = packet.readInt();
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        final ISkill skill = SkillFactory.getSkill(sourceid);

        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0);
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
        }
        chr.cancelEffect(skill.getEffect(1), false, -1);
    }
}
