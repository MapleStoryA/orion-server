package handling.channel.handler;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MagicDamageHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) {
    final MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    if (!chr.isAlive() || chr.getMap() == null) {
      chr.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
      return;
    }
    final AttackInfo attack = DamageParse.Modify_AttackCrit(DamageParse.parseDmgMa(slea), chr, 3);
    if (attack.portals != chr.getPortalCount(false)) { // Portal count
      // didn't match.
      // Ignore
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (!chr.isSkillBelongToJob(attack.skill)) {
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
    if (effect.getCooldown() > 0 && !chr.isGM()) {
      if (chr.skillisCooling(attack.skill)) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
      chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown() * 1000);
    }
    chr.checkFollow();
    chr.getMap().broadcastMessage(chr,
        MaplePacketCreator.magicAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
            attack.animation, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk),
        chr.getPosition());
    DamageParse.applyAttackMagic(attack, skill, c.getPlayer(), effect);


  }

}
