package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.skills.BladeMaster;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.base.timer.TimerManager;
import tools.MaplePacketCreator;

@Slf4j
public class SkillEffectHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final int skill_id = packet.readInt();
        final byte level = packet.readByte();
        final byte flags = packet.readByte();
        final byte speed = packet.readByte();
        final byte unk = packet.readByte(); // Added on v.82

        final ISkill skill = SkillFactory.getSkill(skill_id);
        if (chr == null) {
            return;
        }
        if (!chr.getJob().isSkillBelongToJob(skill_id, chr.isGameMaster())) {
            chr.dropMessage(5, "This skill cannot be used with the current job.");
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final int skilllevel_serv = chr.getSkillLevel(skill);

        if (c.getPlayer().isActiveBuffedValue(BladeMaster.FINAL_CUT)) {
            c.enableActions();
            return;
        }

        if (skilllevel_serv > 0 && skilllevel_serv == level && skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(System.currentTimeMillis());
            chr.getMap()
                    .broadcastMessage(
                            chr, MaplePacketCreator.skillEffect(chr, skill_id, level, flags, speed, unk), false);
        }

        if (skill_id == BladeMaster.FINAL_CUT) {
            TimerManager.getInstance()
                    .schedule(
                            () -> c.getPlayer()
                                    .getMap()
                                    .broadcastMessage(MaplePacketCreator.skillCancel(c.getPlayer(), skill_id)),
                            1000);
        }
    }
}
