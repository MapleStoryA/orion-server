package handling.channel.handler;

import client.*;
import constants.MapConstants;
import handling.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.*;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MobPacket;

public class TakeDamageHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    //System.out.println(slea.toString());
    final MapleCharacter chr = c.getPlayer();
    chr.updateTick(slea.readInt());
    final byte type = slea.readByte(); //-4 is mist, -3 and -2 are map damage.
    slea.skip(1); // Element - 0x00 = elementless, 0x01 = ice, 0x02 = fire, 0x03 = lightning
    int damage = slea.readInt();

    int oid = 0;
    int monsteridfrom = 0;
    int reflect = 0;
    byte direction = 0;
    int pos_x = 0;
    int pos_y = 0;
    int fake = 0;
    int mpattack = 0;
    boolean is_pg = false;
    boolean isDeadlyAttack = false;
    MapleMonster attacker = null;
    if (chr == null || chr.isHidden() || chr.getMap() == null) {
      return;
    }

    if (chr.isGM() && chr.isInvincible()) {
      return;
    }
    final PlayerStats stats = chr.getStat();
    if (type != -2 && type != -3 && type != -4) { // Not map damage
      monsteridfrom = slea.readInt();
      oid = slea.readInt();
      attacker = chr.getMap().getMonsterByOid(oid);
      direction = slea.readByte();

      if (attacker == null) {
        return;
      }
      if (type != -1) { // Bump damage
        final MobAttackInfo attackInfo = MobAttackInfoFactory.getInstance().getMobAttackInfo(attacker, type);
        if (attackInfo != null) {
          if (attackInfo.isDeadlyAttack()) {
            isDeadlyAttack = true;
            mpattack = stats.getMp() - 1;
          } else {
            mpattack += attackInfo.getMpBurn();
          }
          final MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
          if (skill != null && (damage == -1 || damage > 0)) {
            skill.applyEffect(chr, attacker, false);
          }
          attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
        }
      }
    }

    if (damage == -1) {
      fake = 4020002 + ((chr.getJob() / 10 - 40) * 100000);
    } else if (damage < -1 || damage > 60000) {
      AutobanManager.getInstance().addPoints(c, 1000, 60000, "Taking abnormal amounts of damge from " + monsteridfrom + ": " + damage);
      return;
    }
    chr.getCheatTracker().checkTakeDamage(damage);

    if (damage > 0) {
      chr.getCheatTracker().setAttacksWithoutHit(false);

      if (MapConstants.isStorylineMap(chr.getMapId())) {
        if (chr.getMapId() == 502010200) {
          chr.dropMessage(-1, "You're being protected by the powerful visitor suit, thus does not gain any damage.");
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        } else if (chr.getMapId() == 502040100 || chr.getMapId() == 502030004) {
          chr.dropMessage(-1, "You're being protected by the powerful visitor suit, thus reducing damage gained.");
          if (chr.getMapId() == 502030004) {
            damage = 50;
          } else {
            damage = 10;
          }
        }
      }
      if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
        chr.cancelMorphs(); // die = cancel
      }
      if (slea.available() == 3) {
        byte level = slea.readByte();
        if (level > 0) {
          final MobSkill skill = MobSkillFactory.getMobSkill(slea.readShort(), level);
          if (skill != null) {
            skill.applyEffect(chr, attacker, false);
          }
        }
      }
      if (type != -2 && type != -3 && type != -4) {
        final int bouncedam_ = (Randomizer.nextInt(100) < chr.getStat().DAMreflect_rate ? chr.getStat().DAMreflect : 0) + (type == -1 && chr.getBuffedValue(MapleBuffStat.POWERGUARD) != null ? chr.getBuffedValue(MapleBuffStat.POWERGUARD) : 0) + (type == -1 && chr.getBuffedValue(MapleBuffStat.PERFECT_ARMOR) != null ? chr.getBuffedValue(MapleBuffStat.PERFECT_ARMOR) : 0);
        if (bouncedam_ > 0 && attacker != null) {
          long bouncedamage = (long) (damage * bouncedam_ / 100);
          bouncedamage = Math.min(bouncedamage, attacker.getMobMaxHp() / 10);
          attacker.damage(chr, bouncedamage, true);
          damage -= bouncedamage;
          chr.getMap().broadcastMessage(chr, MobPacket.damageMonster(oid, bouncedamage), chr.getPosition());
          is_pg = true;
        }
      }
      if (type != -1 && type != -2 && type != -3 && type != -4) {
        switch (chr.getJob()) {
          case 112: {
            final ISkill skill = SkillFactory.getSkill(1120004);
            if (chr.getSkillLevel(skill) > 0) {
              damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
            }
            break;
          }
          case 122: {
            final ISkill skill = SkillFactory.getSkill(1220005);
            if (chr.getSkillLevel(skill) > 0) {
              damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
            }
            break;
          }
          case 132: {
            final ISkill skill = SkillFactory.getSkill(1320005);
            if (chr.getSkillLevel(skill) > 0) {
              damage = (int) ((skill.getEffect(chr.getSkillLevel(skill)).getX() / 1000.0) * damage);
            }
            break;
          }
        }
      }
      final MapleStatEffect magicShield = chr.getStatForBuff(MapleBuffStat.MAGIC_SHIELD);
      if (magicShield != null) {
        damage -= (int) ((magicShield.getX() / 100.0) * damage);
      }
      final MapleStatEffect blueAura = chr.getStatForBuff(MapleBuffStat.BLUE_AURA);
      if (blueAura != null) {
        damage -= (int) ((blueAura.getY() / 100.0) * damage);
      }
      if (chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_PROC) != null && chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_ABSORB) != null) {
        double buff = chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_PROC).doubleValue();
        double buffz = chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_ABSORB).doubleValue();
        if ((int) ((buff / 100.0) * chr.getStat().getMaxHp()) <= damage) {
          damage -= (int) ((buffz / 100.0) * damage);
          chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
          chr.cancelEffectFromBuffStat(MapleBuffStat.REAPER);
        }
      }
      if (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null) {
        int hploss = 0, mploss = 0;
        if (isDeadlyAttack) {
          if (stats.getHp() > 1) {
            hploss = stats.getHp() - 1;
          }
          if (stats.getMp() > 1) {
            mploss = stats.getMp() - 1;
          }
          if (chr.getBuffedValue(MapleBuffStat.INFINITY) != null) {
            mploss = 0;
          }
          chr.addMPHP(-hploss, -mploss);
          //} else if (mpattack > 0) {
          //    chr.addMPHP(-damage, -mpattack);
        } else {
          mploss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0)) + mpattack;
          hploss = damage - mploss;
          if (chr.getBuffedValue(MapleBuffStat.INFINITY) != null) {
            mploss = 0;
          } else if (mploss > stats.getMp()) {
            mploss = stats.getMp();
            hploss = damage - mploss + mpattack;
          }
          chr.addMPHP(-hploss, -mploss);
        }

      } else if (chr.getBuffedValue(MapleBuffStat.MESOGUARD) != null) {
        damage = (damage % 2 == 0) ? damage / 2 : (damage / 2 + 1);

        final int mesoloss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.MESOGUARD).doubleValue() / 100.0));
        if (chr.getMeso() < mesoloss) {
          chr.gainMeso(-chr.getMeso(), false);
          chr.cancelBuffStats(MapleBuffStat.MESOGUARD);
        } else {
          chr.gainMeso(-mesoloss, false);
        }
        if (isDeadlyAttack && stats.getMp() > 1) {
          mpattack = stats.getMp() - 1;
        }
        chr.addMPHP(-damage, -mpattack);
      } else {
        if (isDeadlyAttack) {
          chr.addMPHP(stats.getHp() > 1 ? -(stats.getHp() - 1) : 0, stats.getMp() > 1 ? -(stats.getMp() - 1) : 0);
        } else {
          chr.addMPHP(-damage, -mpattack);
        }
      }
      chr.handleBattleshipHP(-damage);
    }
    if (!chr.isHidden()) {
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.damagePlayer(type, monsteridfrom, chr.getId(), damage, fake, direction, reflect, is_pg, oid, pos_x, pos_y), false);
    }

  }

}
