package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import networking.data.input.InPacket;
import server.MapleStatEffect;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class MagicDamageHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, final MapleClient c) {
        final MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (!chr.isAlive() || chr.getMap() == null) {
            chr.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        final AttackInfo attack = DamageParse.Modify_AttackCrit(DamageParse.parseDmgMa(packet), chr, 3);
        if (attack.portals != chr.getPortalCount(false)) { // Portal count
            // didn't match.
            // Ignore
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!chr.getJob().isSkillBelongToJob(attack.skill, chr.isGameMaster())) {
            chr.dropMessage(5, "This skill cannot be used with the current job.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        final ISkill skill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(attack.skill));
        final int skillLevel = chr.getSkillLevel(skill);
        final MapleStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
        if (effect == null) {
            return;
        }
        if (effect.getCooldown() > 0 && !chr.isGameMaster()) {
            if (chr.skillisCooling(attack.skill)) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
            chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown() * 1000L);
        }
        chr.checkFollow();
        chr.getMap()
                .broadcastMessage(
                        chr,
                        MaplePacketCreator.magicAttack(
                                chr.getId(),
                                attack.tbyte,
                                attack.skill,
                                skillLevel,
                                attack.display,
                                attack.animation,
                                attack.speed,
                                attack.allDamage,
                                attack.charge,
                                chr.getLevel(),
                                attack.unk),
                        chr.getPosition());
        DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect);
    }
}
