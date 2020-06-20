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

package client;

import java.io.Serializable;

public enum MapleBuffStat implements Serializable {
  ENHANCED_WDEF(0x1, 2),
  ENHANCED_MDEF(0x2, 2),
  PERFECT_ARMOR(0x4, 2),
  SATELLITESAFE_PROC(0x8, 2),
  SATELLITESAFE_ABSORB(0x10, 2),
  CRITICAL_RATE_BUFF(0x40, 2),
  MP_BUFF(0x80, 2),
  DAMAGE_TAKEN_BUFF(0x100, 2),
  DODGE_CHANGE_BUFF(0x200, 2),
  CONVERSION(0x400, 2),
  REAPER(0x800, 2),
  MECH_CHANGE(0x2000, 2), //determined in packet by [skillLevel or something] [skillid] 1E E0 58 52???
  DARK_AURA(0x8000, 2),
  BLUE_AURA(0x10000, 2),
  YELLOW_AURA(0x20000, 2),
  ENERGY_CHARGE(0x80000, 2),
  DASH_SPEED(0x100000, 2),
  DASH_JUMP(0x200000, 2),
  MONSTER_RIDING(0x400000, 2),
  SPEED_INFUSION(0x800000, 2),
  HOMING_BEACON(0x1000000, 2),
  ELEMENT_RESET(0x200000000L, 2),
  ARAN_COMBO(0x1000000000L, 2),
  COMBO_DRAIN(0x2000000000L, 2),
  BODY_PRESSURE(0x8000000000L, 2),
  SMART_KNOCKBACK(0x10000000000L, 2),
  PYRAMID_PQ(0x20000000000L, 2),
  SOUL_STONE(0x20000000000L, 2), //same as pyramid_pq
  MAGIC_SHIELD(0x800000000000L, 2),
  MAGIC_RESISTANCE(0x1000000000000L, 2),
  SOARING(0x4000000000000L, 2),
  LIGHTNING_CHARGE(0x10000000000000L, 2),
  //db stuff
  MIRROR_IMAGE(0x20000000000000L, 2),
  OWL_SPIRIT(0x40000000000000L, 3),
  FINAL_CUT(0x100000000000000L, 3),
  THORNS(0x200000000000000L, 2),
  DAMAGE_BUFF(0x400000000000000L, 2),
  RAINING_MINES(0x1000000000000000L, 2),
  ENHANCED_MAXHP(0x2000000000000000L, 2),
  ENHANCED_MAXMP(0x4000000000000000L, 2),
  ENHANCED_WATK(0x8000000000000000L, 2),
  MORPH(0x2),
  RECOVERY(0x4),
  MAPLE_WARRIOR(0x8),
  STANCE(0x10),
  SHARP_EYES(0x20),
  MANA_REFLECTION(0x40),
  DRAGON_ROAR(0x80), // Stuns the user

  SPIRIT_CLAW(0x100),
  INFINITY(0x200),
  HOLY_SHIELD(0x400),
  HAMSTRING(0x800),
  BLIND(0x1000),
  CONCENTRATE(0x2000),
  ECHO_OF_HERO(0x8000),
  UNKNOWN3(0x10000),
  GHOST_MORPH(0x20000),
  ARIANT_COSS_IMU(0x40000), // The white ball around you

  DROP_RATE(0x100000),
  MESO_RATE(0x200000),
  EXPRATE(0x400000),
  ACASH_RATE(0x800000),
  GM_HIDE(0x1000000),
  UNKNOWN7(0x2000000),
  ILLUSION(0x4000000),
  BERSERK_FURY(0x8000000),
  DIVINE_BODY(0x10000000),
  SPARK(0x20000000),
  ARIANT_COSS_IMU2(0x40000000), // no idea, seems the same
  FINALATTACK(0x80000000L),
  WATK(0x100000000L),
  WDEF(0x200000000L),
  MATK(0x400000000L),
  MDEF(0x800000000L),
  ACC(0x1000000000L),
  AVOID(0x2000000000L),
  HANDS(0x4000000000L),
  SPEED(0x8000000000L),
  JUMP(0x10000000000L),
  MAGIC_GUARD(0x20000000000L),
  DARKSIGHT(0x40000000000L),
  BOOSTER(0x80000000000L),
  POWERGUARD(0x100000000000L),
  MAXHP(0x200000000000L),
  MAXMP(0x400000000000L),
  INVINCIBLE(0x800000000000L),
  SOULARROW(0x1000000000000L),
  COMBO(0x20000000000000L),
  SUMMON(0x20000000000000L), //hack buffstat for summons ^.- (does/should not increase damage... hopefully <3)
  WK_CHARGE(0x40000000000000L),
  DRAGONBLOOD(0x80000000000000L),
  HOLY_SYMBOL(0x100000000000000L),
  MESOUP(0x200000000000000L),
  SHADOWPARTNER(0x400000000000000L),
  PICKPOCKET(0x800000000000000L),
  PUPPET(0x800000000000000L), // HACK - shares buffmask with pickpocket - odin special ^.-
  MESOGUARD(0x1000000000000000L),
  HP_LOSS_GUARD(0x2000000000000000L);
  private static final long serialVersionUID = 0L;
  private final long buffstat;
  private final int first;

  private MapleBuffStat(long buffstat) {
    this.buffstat = buffstat;
    this.first = 1;
  }

  private MapleBuffStat(long buffstat, int first) {
    this.buffstat = buffstat;
    this.first = first;
  }

  public final int isFirst() {
    return first;
  }

  public final long getValue() {
    return buffstat;
  }
}
