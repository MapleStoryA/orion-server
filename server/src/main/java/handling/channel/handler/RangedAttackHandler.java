package handling.channel.handler;

import client.*;
import client.anticheat.CheatingOffense;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class RangedAttackHandler extends AbstractMaplePacketHandler {

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
    c.getPlayer().checkForDarkSight();
    final AttackInfo attack = DamageParse.Modify_AttackCrit(DamageParse.parseDmgR(slea), chr, 2);
    if (!chr.isSkillBelongToJob(attack.skill)) {
      chr.dropMessage(5, "This skill cannot be used with the current job.");
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (attack.portals != chr.getPortalCount(false)) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    int bulletCount = 1, skillLevel = 0;
    MapleStatEffect effect = null;
    ISkill skill = null;

    if (attack.skill != 0) {
      skill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(attack.skill));
      skillLevel = chr.getSkillLevel(skill);
      effect = attack.getAttackEffect(chr, skillLevel, skill);
      if (effect == null) {
        return;
      }

      switch (attack.skill) {
        case 21110004: // Ranged but uses attackcount instead
        case 14101006: // Vampure
          bulletCount = effect.getAttackCount();
          break;
        default:
          bulletCount = effect.getBulletCount();
          break;
      }
      if (effect.getCooldown() > 0 && !chr.isGM()) {
        if (chr.skillisCooling(attack.skill)) {
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        }
        c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
        chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown() * 1000);
      }
    }
    final Integer ShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER);
    if (ShadowPartner != null) {
      bulletCount *= 2;
    }
    int projectile = 0, visProjectile = 0;
    if (attack.AOE != 0 && chr.getBuffedValue(MapleBuffStat.SOULARROW) == null && attack.skill != 4111004
        && !GameConstants.isVisitorSkill(attack.skill)) {
      if (chr.getInventory(MapleInventoryType.USE).getItem(attack.slot) == null) {
        return;
      }
      projectile = chr.getInventory(MapleInventoryType.USE).getItem(attack.slot).getItemId();

      if (attack.csstar > 0) {
        if (chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar) == null) {
          return;
        }
        visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar).getItemId();
      } else {
        visProjectile = projectile;
      }
      // Handle bulletcount
      if (chr.getBuffedValue(MapleBuffStat.SPIRIT_CLAW) == null) {
        int bulletConsume = bulletCount;
        if (effect != null && effect.getBulletConsume() != 0) {
          bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
        }
        if (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false,
            true)) {
          chr.dropMessage(5, "You do not have enough arrows/bullets/stars.");
          return;
        }
      }
    }

    double basedamage;
    int projectileWatk = 0;
    if (projectile != 0) {
      projectileWatk = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
    }
    final PlayerStats statst = chr.getStat();
    switch (attack.skill) {
      case 4001344: // Lucky Seven
      case 4121007: // Triple Throw
      case 14001004: // Lucky seven
      case 14111005: // Triple Throw
        basedamage = (float) ((float) ((statst.getTotalLuk() * 5.0f) * (statst.getTotalWatk() + projectileWatk))
            / 100);
        break;
      case 4111004: // Shadow Meso
        // basedamage = ((effect.getMoneyCon() * 10) / 100) *
        // effect.getProb(); // Not sure
        basedamage = 13000;
        break;
      default:
        if (projectileWatk != 0) {
          basedamage = statst.calculateMaxBaseDamage(statst.getTotalWatk() + projectileWatk);
        } else {
          basedamage = statst.getCurrentMaxBaseDamage();
        }
        switch (attack.skill) {
          case 3101005: // arrowbomb is hardcore like that
            basedamage *= effect.getX() / 100.0;
            break;
        }
        break;
    }
    if (effect != null) {
      basedamage *= effect.getDamage() / 100.0;

      int money = effect.getMoneyCon();
      if (money != 0) {
        if (money > chr.getMeso()) {
          money = chr.getMeso();
        }
        chr.gainMeso(-money, false);
      }
    }

    chr.checkFollow();
    chr.getMap().broadcastMessage(chr,
        MaplePacketCreator.rangedAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display,
            attack.animation, attack.speed, visProjectile, attack.allDamage, attack.position,
            chr.getLevel(), chr.getStat().passive_mastery(), attack.unk),
        chr.getPosition());
    DamageParse.applyAttack(attack, skill, chr, bulletCount, basedamage, effect,
        ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED);
  }

}
