/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.channel.handler;

import client.*;
import client.inventory.MapleInventoryType;
import constants.MapConstants;
import handling.channel.ChannelServer;
import server.*;
import server.life.*;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.UIPacket;

public class PlayerHandler {


  public static final void CharInfoRequest(final int objectid, final MapleClient c, final MapleCharacter chr) {
    if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
      return;
    }
    final MapleCharacter player = c.getPlayer().getMap().getCharacterById(objectid);
    c.getSession().write(MaplePacketCreator.enableActions());
    if (player != null) {
      c.getSession().write(MaplePacketCreator.charInfo(player, c.getPlayer().getId() == objectid));
    }
  }

  public static final void TakeDamage(final SeekableLittleEndianAccessor slea, final MapleClient c,
                                      final MapleCharacter chr) {
    // System.out.println(slea.toString());
    chr.updateTick(slea.readInt());
    final byte type = slea.readByte(); // -4 is mist, -3 and -2 are map
    // damage.
    slea.skip(1); // Element - 0x00 = elementless, 0x01 = ice, 0x02 = fire,
    // 0x03 = lightning
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
          final MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(),
              attackInfo.getDiseaseLevel());
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
      AutobanManager.getInstance().addPoints(c, 1000, 60000,
          "Taking abnormal amounts of damge from " + monsteridfrom + ": " + damage);
      return;
    }
    chr.getCheatTracker().checkTakeDamage(damage);

    if (damage > 0) {
      chr.getCheatTracker().setAttacksWithoutHit(false);

      if (MapConstants.isStorylineMap(chr.getMapId())) {
        if (chr.getMapId() == 502010200) {
          chr.dropMessage(-1,
              "You're being protected by the powerful visitor suit, thus does not gain any damage.");
          c.getSession().write(MaplePacketCreator.enableActions());
          return;
        } else if (chr.getMapId() == 502040100 || chr.getMapId() == 502030004) {
          chr.dropMessage(-1,
              "You're being protected by the powerful visitor suit, thus reducing damage gained.");
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
        final int bouncedam_ = (Randomizer.nextInt(100) < chr.getStat().DAMreflect_rate
            ? chr.getStat().DAMreflect : 0)
            + (type == -1 && chr.getBuffedValue(MapleBuffStat.POWERGUARD) != null
            ? chr.getBuffedValue(MapleBuffStat.POWERGUARD) : 0)
            + (type == -1 && chr.getBuffedValue(MapleBuffStat.PERFECT_ARMOR) != null
            ? chr.getBuffedValue(MapleBuffStat.PERFECT_ARMOR) : 0);
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
      if (chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_PROC) != null
          && chr.getBuffedValue(MapleBuffStat.SATELLITESAFE_ABSORB) != null) {
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
          // } else if (mpattack > 0) {
          // chr.addMPHP(-damage, -mpattack);
        } else {
          mploss = (int) (damage * (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0))
              + mpattack;
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

        final int mesoloss = (int) (damage
            * (chr.getBuffedValue(MapleBuffStat.MESOGUARD).doubleValue() / 100.0));
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
          chr.addMPHP(stats.getHp() > 1 ? -(stats.getHp() - 1) : 0,
              stats.getMp() > 1 ? -(stats.getMp() - 1) : 0);
        } else {
          chr.addMPHP(-damage, -mpattack);
        }
      }
      chr.handleBattleshipHP(-damage);
    }
    if (!chr.isHidden()) {
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.damagePlayer(type, monsteridfrom, chr.getId(), damage,
          fake, direction, reflect, is_pg, oid, pos_x, pos_y), false);
    }
  }

  public static final void CancelItemEffect(final int id, final MapleCharacter chr) {
    if (id == 2211000 || id == 2212000) {
      chr.setMorphId((byte) 0);
    }
    chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1);
  }

  public static final void CancelBuffHandler(final int sourceid, final MapleCharacter chr) {
    if (chr == null) {
      return;
    }
    final ISkill skill = SkillFactory.getSkill(sourceid);

    if (skill.isChargeSkill()) {
      chr.setKeyDownSkill_Time(0);
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
    } else {
      chr.cancelEffect(skill.getEffect(1), false, -1);
    }
  }

  public static final void ChangeMap(final SeekableLittleEndianAccessor slea, final MapleClient c,
                                     final MapleCharacter chr) {
    if (chr == null) {
      return;
    }
    if (slea.available() != 0) {
      slea.readByte(); // 1 = from dying 2 = regular portals
      int targetid = slea.readInt(); // FF FF FF FF
      final MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
      if (slea.available() >= 7) {
        chr.updateTick(slea.readInt());
      }
      slea.skip(1);
      final boolean wheel = slea.readShort() > 0 && !MapConstants.isEventMap(chr.getMapId())
          && chr.haveItem(5510000, 1, false, true);

      if (targetid != -1 && !chr.isAlive()) {
        chr.setStance(0);
        if (chr.getEventInstance() != null && chr.getEventInstance().revivePlayer(chr) && chr.isAlive()) {
          return;
        }
        if (chr.getPyramidSubway() != null) {
          chr.getStat().setHp((short) 50);
          chr.getPyramidSubway().fail(chr);
          return;
        }
        if (!wheel) {
          chr.getStat().setHp((short) 50);
          final MapleMap to = chr.getMap().getReturnMap();
          chr.changeMap(to, to.getPortal(0));
        } else {
          c.getSession().write(MTSCSPacket
              .useWheel((byte) (chr.getInventory(MapleInventoryType.CASH).countById(5510000) - 1)));
          chr.getStat().setHp(((chr.getStat().getMaxHp() / 100) * 40));
          MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
          final MapleMap to = chr.getMap();
          chr.changeMap(to, to.getPortal(0));
        }
      } else if (targetid != -1 && chr.isGM()) {
        if (targetid == 502050000 || targetid == 502050001) {
          c.getSession().write(UIPacket.IntroDisableUI(false));
          c.getSession().write(UIPacket.IntroLock(false));
          c.getSession().write(MaplePacketCreator.enableActions());
          if (targetid == 502050001) {
            targetid = 970030020;
          } else if (targetid == 502050000) {
            targetid = 502010000;
          }
        }
        final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
        chr.changeMap(to, to.getPortal(0));

      } else if (targetid != -1 && !chr.isGM()) {
        final int divi = chr.getMapId() / 100;
        boolean unlock = false;
        boolean warp = false;
        if (divi == 9130401) { // Only allow warp if player is already
          // in Intro map, or else = hack
          warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
          if (targetid / 10000 != 91304) {
            warp = true;
            unlock = true;
            targetid = 130030000;
          }
        } else if (divi == 9130400) {
          warp = (targetid / 100 == 9130400) || (targetid / 100 == 9130401);
          if (targetid / 10000 != 91304) {
            warp = true;
            unlock = true;
            targetid = 130030000;
          }
        } else if (divi == 9140900) { // Aran Introduction
          warp = (targetid == 914090011 || targetid == 914090012 || targetid == 914090013
              || targetid == 140090000);
        } else if (divi == 9120601 || divi == 9140602 || divi == 9140603 || divi == 9140604 || divi == 9140605) {
          warp = (targetid == 912060100 || targetid == 912060200 || targetid == 912060300
              || targetid == 912060400 || targetid == 912060500 || targetid == 3000100);
          unlock = true;
        } else if (divi == 9140901 && targetid == 140000000) {
          unlock = true;
          warp = true;
        } else if (targetid == 980040000 && divi >= 9800410 && divi <= 9800450) {
          warp = true;
        } else if (divi == 9140902 && (targetid == 140030000 || targetid == 140000000)) {
          unlock = true;
          warp = true;
        } else if (divi == 9000900 && targetid / 100 == 9000900 && targetid > chr.getMapId()) {
          warp = true;
        } else if (divi / 1000 == 9000 && targetid / 100000 == 9000) {
          unlock = (targetid < 900090000 || targetid > 900090004);
          warp = true;
        } else if (divi / 10 == 1020 && targetid == 1020000) { // Adventurer
          // movie
          // clip
          // Intro
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 900090101 && targetid == 100030100) {
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 2010000 && targetid == 104000000) {
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 106020001 || chr.getMapId() == 106020502) {
          if (targetid == chr.getMapId() - 1) {
            unlock = true;
            warp = true;
          }
        } else if (chr.getMapId() == 0 && targetid == 10000) {
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 931000011 && targetid == 931000012) {
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 931000021 && targetid == 931000030) {
          unlock = true;
          warp = true;
        } else if (chr.getMapId() == 970030020 && (targetid == 502050000 || targetid == 502050001)) {
          unlock = true;
          warp = true;
          if (targetid == 502050001) {
            targetid = 970030020;
          } else if (targetid == 502050000) {
            targetid = 502010000;
          }
        }
        if (unlock) {
          c.getSession().write(UIPacket.IntroDisableUI(false));
          c.getSession().write(UIPacket.IntroLock(false));
          c.getSession().write(MaplePacketCreator.enableActions());
        }
        if (warp) {
          final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
          chr.changeMap(to, to.getPortal(0));
        }
      } else {
        if (portal != null) {
          portal.enterPortal(c);
        } else {
          c.getSession().write(MaplePacketCreator.enableActions());
        }
      }
    }
  }


}