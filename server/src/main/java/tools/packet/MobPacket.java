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

package tools.packet;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.SendPacketOpcode;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.movement.MovePath;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MobPacket {

  public static byte[] damageMonster(final int oid, final long damage) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.write(0);
    if (damage > Integer.MAX_VALUE) {
      mplew.writeInt(Integer.MAX_VALUE);
    } else {
      mplew.writeInt((int) damage);
    }

    return mplew.getPacket();
  }

  public static byte[] damageFriendlyMob(final MapleMonster mob, final long damage, final boolean display) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
    mplew.writeInt(mob.getObjectId());
    mplew.write(display ? 1 : 2); //false for when shammos changes map!
    if (damage > Integer.MAX_VALUE) {
      mplew.writeInt(Integer.MAX_VALUE);
    } else {
      mplew.writeInt((int) damage);
    }
    if (mob.getHp() > Integer.MAX_VALUE) {
      mplew.writeInt((int) (((double) mob.getHp() / mob.getMobMaxHp()) * Integer.MAX_VALUE));
    } else {
      mplew.writeInt((int) mob.getHp());
    }
    if (mob.getMobMaxHp() > Integer.MAX_VALUE) {
      mplew.writeInt(Integer.MAX_VALUE);
    } else {
      mplew.writeInt((int) mob.getMobMaxHp());
    }

    return mplew.getPacket();
  }

  public static byte[] killMonster(final int oid, final int animation) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.write(animation); // 0 = dissapear, 1 = fade out, 2+ = special
    if (animation == 4) {
      mplew.writeInt(-1);
    }
    mplew.writeLong(0);
    mplew.writeLong(0);
    return mplew.getPacket();
  }

  public static byte[] healMonster(final int oid, final int heal) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.write(0);
    mplew.writeInt(-heal);

    return mplew.getPacket();
  }

  public static byte[] showMonsterHP(int oid, int remhppercentage) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
    mplew.writeInt(oid);
    mplew.write(remhppercentage);
    mplew.writeLong(0);
    mplew.writeLong(0);
    return mplew.getPacket();
  }

  public static byte[] showBossHP(final MapleMonster mob) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
    mplew.write(5);
    mplew.writeInt(mob.getId());
    if (mob.getHp() > Integer.MAX_VALUE) {
      mplew.writeInt((int) (((double) mob.getHp() / mob.getMobMaxHp()) * Integer.MAX_VALUE));
    } else {
      mplew.writeInt((int) mob.getHp());
    }
    if (mob.getMobMaxHp() > Integer.MAX_VALUE) {
      mplew.writeInt(Integer.MAX_VALUE);
    } else {
      mplew.writeInt((int) mob.getMobMaxHp());
    }
    mplew.write(mob.getStats().getTagColor());
    mplew.write(mob.getStats().getTagBgColor());

    return mplew.getPacket();
  }

  public static byte[] moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, MovePath path) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.writeShort(0); //moveid but always 0
    mplew.write(useskill ? 1 : 0); //?? I THINK
    mplew.write(skill);
    mplew.write(skill1);
    mplew.write(skill2);
    mplew.write(skill3);
    mplew.write(skill4);
    mplew.writeZeroBytes(8); //o.o?
    path.encode(mplew);
    return mplew.getPacket();
  }


  public static byte[] spawnMonster(MapleMonster life, int spawnType, int effect, int link) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
    mplew.writeInt(life.getObjectId());
    mplew.write(1); // 1 = Control normal, 5 = Control none
    mplew.writeInt(life.getId());
    addMonsterStatus(mplew, life);
    mplew.writeShort(life.getPosition().x);
    mplew.writeShort(life.getPosition().y);
    mplew.write(life.getStance());
    mplew.writeShort(0); // FH
    mplew.writeShort(life.getFh()); // Origin FH
    mplew.write(spawnType);
    if (spawnType == -3 || spawnType >= 0) {
      mplew.writeInt(link);
    }
    mplew.write(life.getCarnivalTeam());
    mplew.writeInt(63000); //v102 - another int here
    mplew.writeInt(0); // v89
    mplew.writeInt(0);
    mplew.write(-1);
    mplew.writeLong(0);
    mplew.writeLong(0);
    return mplew.getPacket();
  }

  public static void addMonsterStatus(MaplePacketLittleEndianWriter mplew, MapleMonster life) {
    if (life.getStati().size() <= 0) {
      life.addEmpty(); //not done yet lulz ok so we add it now for the lulz
    }
    mplew.writeLong(getSpecialLongMask(life.getStati().keySet()));
    mplew.writeLong(getLongMask_NoRef(life.getStati().keySet()));
    boolean ignore_imm = false;
    for (MonsterStatusEffect buff : life.getStati().values()) {
      if (buff.getStati() == MonsterStatus.MAGIC_DAMAGE_REFLECT || buff.getStati() == MonsterStatus.WEAPON_DAMAGE_REFLECT) {
        ignore_imm = true;
        break;
      }
    }
    for (MonsterStatusEffect buff : life.getStati().values()) {
      if (buff.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT && buff.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT) {
        if (ignore_imm) {
          if (buff.getStati() == MonsterStatus.MAGIC_IMMUNITY || buff.getStati() == MonsterStatus.WEAPON_IMMUNITY) {
            continue;
          }
        }
        mplew.writeShort(buff.getX().shortValue());
        if (buff.getStati() != MonsterStatus.SUMMON) {
          if (buff.getMobSkill() != null) {
            mplew.writeShort(buff.getMobSkill().getSkillId());
            mplew.writeShort(buff.getMobSkill().getSkillLevel());
          } else if (buff.getSkill() > 0) {
            mplew.writeInt(buff.getSkill());
          }
          mplew.writeShort(buff.getStati().isEmpty() ? 0 : 1);
        }
      }
    }
    //wh spawn - 15 zeroes instead of 16, then 98 F4 56 A6 C7 C9 01 28, then 7 zeroes
  }

  public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
    mplew.write(aggro ? 2 : 1);
    mplew.writeInt(life.getObjectId());
    mplew.write(1); // 1 = Control normal, 5 = Control none
    mplew.writeInt(life.getId());
    addMonsterStatus(mplew, life);
    mplew.writeShort(life.getPosition().x);
    mplew.writeShort(life.getPosition().y);
    mplew.write(life.getStance()); // Bitfield
    mplew.writeShort(0); // FH
    mplew.writeShort(life.getFh()); // Origin FH
    mplew.write(life.isFake() ? 0xfc : newSpawn ? -2 : -1);
    mplew.write(life.getCarnivalTeam());
    mplew.writeInt(63000);
    mplew.writeInt(0);
    mplew.writeInt(0);
    mplew.write(-1);
    mplew.writeLong(0);
    mplew.writeLong(0);
    mplew.writeLong(0);

    return mplew.getPacket();
  }

  public static byte[] stopControllingMonster(int oid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
    mplew.write(0);
    mplew.writeInt(oid);
    mplew.writeLong(0);
    mplew.writeLong(0);
    mplew.writeLong(0);

    return mplew.getPacket();
  }

  public static byte[] makeMonsterInvisible(MapleMonster life) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
    mplew.write(0);
    mplew.writeInt(life.getObjectId());

    return mplew.getPacket();
  }

  public static byte[] makeMonsterReal(MapleMonster life) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
    mplew.writeInt(life.getObjectId());
    mplew.write(1); // 1 = Control normal, 5 = Control none
    mplew.writeInt(life.getId());
    addMonsterStatus(mplew, life);
    mplew.writeShort(life.getPosition().x);
    mplew.writeShort(life.getPosition().y);
    mplew.write(life.getStance());
    mplew.writeShort(0); // FH
    mplew.writeShort(life.getFh()); // Origin FH
    mplew.writeShort(-1);
    mplew.writeInt(0);
    mplew.writeInt(0); // v89

    return mplew.getPacket();
  }

  public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
    mplew.writeInt(objectid);
    mplew.writeShort(moveid);
    mplew.write(useSkills ? 1 : 0);
    mplew.writeShort(currentMp);
    mplew.write(skillId);
    mplew.write(skillLevel);
    mplew.writeInt(0);
    mplew.writeLong(0);
    mplew.writeLong(0);
    mplew.writeLong(0);

    return mplew.getPacket();
  }

  private static long getSpecialLongMask(Collection<MonsterStatus> statups) {
    long mask = 0;
    for (MonsterStatus statup : statups) {
      if (statup.isFirst()) {
        mask |= statup.getValue();
      }
    }
    return mask;
  }

  private static long getLongMask(Collection<MonsterStatus> statups) {
    long mask = 0;
    for (MonsterStatus statup : statups) {
      if (!statup.isFirst()) {
        mask |= statup.getValue();
      }
    }
    return mask;
  }

  private static long getLongMask_NoRef(Collection<MonsterStatus> statups) {
    long mask = 0;
    boolean ignore_imm = false;
    for (MonsterStatus statup : statups) {
      if (statup == MonsterStatus.MAGIC_DAMAGE_REFLECT || statup == MonsterStatus.WEAPON_DAMAGE_REFLECT) {
        ignore_imm = true;
        break;
      }
    }
    for (MonsterStatus statup : statups) {
      if (statup != MonsterStatus.MAGIC_DAMAGE_REFLECT && statup != MonsterStatus.WEAPON_DAMAGE_REFLECT) {
        if (ignore_imm) {
          if (statup == MonsterStatus.MAGIC_IMMUNITY || statup == MonsterStatus.WEAPON_IMMUNITY) {
            continue;
          }
        }

        if (!statup.isFirst()) {
          mask |= statup.getValue();
        }
      }
    }
    return mask;
  }

  public static byte[] applyMonsterStatus(final int oid, final MonsterStatus mse, int x, MobSkill skil) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
    mplew.writeInt(oid);
    mplew.writeLong(getSpecialLongMask(Collections.singletonList(mse)));
    mplew.writeLong(getLongMask(Collections.singletonList(mse)));

    mplew.writeShort(x);
    mplew.writeShort(skil.getSkillId());
    mplew.writeShort(skil.getSkillLevel());
    mplew.writeShort(mse.isEmpty() ? 1 : 0); // might actually be the buffTime but it's not displayed anywhere
    mplew.writeShort(0); // delay in ms
    mplew.write(1); // size
    mplew.write(1); // ? v97

    return mplew.getPacket();
  }

  public static byte[] applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
    mplew.writeInt(oid);
    mplew.writeLong(getSpecialLongMask(Collections.singletonList(mse.getStati())));
    mplew.writeLong(getLongMask(Collections.singletonList(mse.getStati())));

    mplew.writeShort(mse.getX());
    if (mse.isMonsterSkill()) {
      mplew.writeShort(mse.getMobSkill().getSkillId());
      mplew.writeShort(mse.getMobSkill().getSkillLevel());
    } else if (mse.getSkill() > 0) {
      mplew.writeInt(mse.getSkill());
    }
    mplew.writeShort(mse.getStati().isEmpty() ? 1 : 0); // might actually be the buffTime but it's not displayed anywhere
    mplew.writeShort(0); // delay in ms
    mplew.write(1); // size
    mplew.write(1); // ? v97

    return mplew.getPacket();
  }

  public static byte[] applyMonsterStatus(final int oid, final Map<MonsterStatus, Integer> stati, final List<Integer> reflection, MobSkill skil) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
    mplew.writeInt(oid);
    mplew.writeLong(getSpecialLongMask(stati.keySet()));
    mplew.writeLong(getLongMask(stati.keySet()));

    for (Map.Entry<MonsterStatus, Integer> mse : stati.entrySet()) {
      mplew.writeShort(mse.getValue());
      mplew.writeShort(skil.getSkillId());
      mplew.writeShort(skil.getSkillLevel());
      mplew.writeShort(mse.getKey().isEmpty() ? 1 : 0); // might actually be the buffTime but it's not displayed anywhere
    }
    for (Integer ref : reflection) {
      mplew.writeInt(ref);
    }
    mplew.writeInt(0);
    mplew.writeShort(0); // delay in ms

    int size = stati.size(); // size
    if (reflection.size() > 0) {
      size /= 2; // This gives 2 buffs per reflection but it's really one buff
    }
    mplew.write(size); // size
    mplew.write(1); // ? v97

    return mplew.getPacket();
  }

  public static byte[] cancelMonsterStatus(int oid, MonsterStatus stat) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
    mplew.writeInt(oid);
    mplew.writeLong(getSpecialLongMask(Collections.singletonList(stat)));
    mplew.writeLong(getLongMask(Collections.singletonList(stat)));
    mplew.write(1); // reflector is 3~!??
    mplew.write(2); // ? v97

    return mplew.getPacket();
  }

  public static byte[] talkMonster(int oid, int itemId, String msg) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.TALK_MONSTER.getValue());
    mplew.writeInt(oid);
    mplew.writeInt(500); //?
    mplew.writeInt(itemId);
    mplew.write(itemId <= 0 ? 0 : 1);
    mplew.write(msg == null || msg.length() <= 0 ? 0 : 1);
    if (msg != null && msg.length() > 0) {
      mplew.writeMapleAsciiString(msg);
    }
    mplew.writeInt(1); //?

    return mplew.getPacket();
  }

  public static byte[] removeTalkMonster(int oid) {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

    mplew.writeShort(SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
    mplew.writeInt(oid);
    return mplew.getPacket();
  }
}
