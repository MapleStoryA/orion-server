package tools.packet;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.SendPacketOpcode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.movement.MovePath;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class MobPacket {

    public static byte[] damageMonster(final int oid, final long damage) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(0);
        if (damage > Integer.MAX_VALUE) {
            packet.writeInt(Integer.MAX_VALUE);
        } else {
            packet.writeInt((int) damage);
        }

        return packet.getPacket();
    }

    public static byte[] damageFriendlyMob(
            final MapleMonster mob, final long damage, final boolean display) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(mob.getObjectId());
        packet.write(display ? 1 : 2); // false for when shammos changes map!
        if (damage > Integer.MAX_VALUE) {
            packet.writeInt(Integer.MAX_VALUE);
        } else {
            packet.writeInt((int) damage);
        }
        if (mob.getHp() > Integer.MAX_VALUE) {
            packet.writeInt((int) (((double) mob.getHp() / mob.getMobMaxHp()) * Integer.MAX_VALUE));
        } else {
            packet.writeInt((int) mob.getHp());
        }
        if (mob.getMobMaxHp() > Integer.MAX_VALUE) {
            packet.writeInt(Integer.MAX_VALUE);
        } else {
            packet.writeInt((int) mob.getMobMaxHp());
        }

        return packet.getPacket();
    }

    public static byte[] killMonster(final int oid, final int animation) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(animation); // 0 = dissapear, 1 = fade out, 2+ = special
        if (animation == 4) {
            packet.writeInt(-1);
        }
        packet.writeLong(0);
        packet.writeLong(0);
        return packet.getPacket();
    }

    public static byte[] healMonster(final int oid, final int heal) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(0);
        packet.writeInt(-heal);

        return packet.getPacket();
    }

    public static byte[] showMonsterHP(int oid, int remhppercentage) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        packet.writeInt(oid);
        packet.write(remhppercentage);
        packet.writeLong(0);
        packet.writeLong(0);
        return packet.getPacket();
    }

    public static byte[] showBossHP(final MapleMonster mob) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(5);
        packet.writeInt(mob.getId());
        if (mob.getHp() > Integer.MAX_VALUE) {
            packet.writeInt((int) (((double) mob.getHp() / mob.getMobMaxHp()) * Integer.MAX_VALUE));
        } else {
            packet.writeInt((int) mob.getHp());
        }
        if (mob.getMobMaxHp() > Integer.MAX_VALUE) {
            packet.writeInt(Integer.MAX_VALUE);
        } else {
            packet.writeInt((int) mob.getMobMaxHp());
        }
        packet.write(mob.getStats().getTagColor());
        packet.write(mob.getStats().getTagBgColor());

        return packet.getPacket();
    }

    public static byte[] moveMonster(
            boolean useskill,
            int skill,
            int skill1,
            int skill2,
            int skill3,
            int skill4,
            int oid,
            MovePath path) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.writeShort(0); // moveid but always 0
        packet.write(useskill ? 1 : 0); // ?? I THINK
        packet.write(skill);
        packet.write(skill1);
        packet.write(skill2);
        packet.write(skill3);
        packet.write(skill4);
        packet.writeZeroBytes(8); // o.o?
        path.encode(packet);
        return packet.getPacket();
    }

    public static byte[] spawnMonster(MapleMonster life, int spawnType, int effect, int link) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        packet.writeInt(life.getObjectId());
        packet.write(1); // 1 = Control normal, 5 = Control none
        packet.writeInt(life.getId());
        addMonsterStatus(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance());
        packet.writeShort(0); // FH
        packet.writeShort(life.getFh()); // Origin FH
        packet.write(spawnType);
        if (spawnType == -3 || spawnType >= 0) {
            packet.writeInt(link);
        }
        packet.write(life.getCarnivalTeam());
        packet.writeInt(63000); // v102 - another int here
        packet.writeInt(0); // v89
        packet.writeInt(0);
        packet.write(-1);
        packet.writeLong(0);
        packet.writeLong(0);
        return packet.getPacket();
    }

    public static void addMonsterStatus(OutPacket packet, MapleMonster life) {
        if (life.getStati().size() <= 0) {
            life.addEmpty(); // not done yet lulz ok so we add it now for the lulz
        }
        packet.writeLong(getSpecialLongMask(life.getStati().keySet()));
        packet.writeLong(getLongMask_NoRef(life.getStati().keySet()));
        boolean ignore_imm = false;
        for (MonsterStatusEffect buff : life.getStati().values()) {
            if (buff.getStati() == MonsterStatus.MAGIC_DAMAGE_REFLECT
                    || buff.getStati() == MonsterStatus.WEAPON_DAMAGE_REFLECT) {
                ignore_imm = true;
                break;
            }
        }
        for (MonsterStatusEffect buff : life.getStati().values()) {
            if (buff.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT
                    && buff.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT) {
                if (ignore_imm) {
                    if (buff.getStati() == MonsterStatus.MAGIC_IMMUNITY
                            || buff.getStati() == MonsterStatus.WEAPON_IMMUNITY) {
                        continue;
                    }
                }
                packet.writeShort(buff.getX().shortValue());
                if (buff.getStati() != MonsterStatus.SUMMON) {
                    if (buff.getMobSkill() != null) {
                        packet.writeShort(buff.getMobSkill().getSkillId());
                        packet.writeShort(buff.getMobSkill().getSkillLevel());
                    } else if (buff.getSkill() > 0) {
                        packet.writeInt(buff.getSkill());
                    }
                    packet.writeShort(buff.getStati().isEmpty() ? 0 : 1);
                }
            }
        }
        // wh spawn - 15 zeroes instead of 16, then 98 F4 56 A6 C7 C9 01 28, then 7 zeroes
    }

    public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(aggro ? 2 : 1);
        packet.writeInt(life.getObjectId());
        packet.write(1); // 1 = Control normal, 5 = Control none
        packet.writeInt(life.getId());
        addMonsterStatus(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance()); // Bitfield
        packet.writeShort(0); // FH
        packet.writeShort(life.getFh()); // Origin FH
        packet.write(life.isFake() ? 0xfc : newSpawn ? -2 : -1);
        packet.write(life.getCarnivalTeam());
        packet.writeInt(63000);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(-1);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);

        return packet.getPacket();
    }

    public static byte[] stopControllingMonster(int oid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(0);
        packet.writeInt(oid);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);

        return packet.getPacket();
    }

    public static byte[] makeMonsterInvisible(MapleMonster life) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        packet.write(0);
        packet.writeInt(life.getObjectId());

        return packet.getPacket();
    }

    public static byte[] makeMonsterReal(MapleMonster life) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        packet.writeInt(life.getObjectId());
        packet.write(1); // 1 = Control normal, 5 = Control none
        packet.writeInt(life.getId());
        addMonsterStatus(packet, life);
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getPosition().y);
        packet.write(life.getStance());
        packet.writeShort(0); // FH
        packet.writeShort(life.getFh()); // Origin FH
        packet.writeShort(-1);
        packet.writeInt(0);
        packet.writeInt(0); // v89

        return packet.getPacket();
    }

    public static byte[] moveMonsterResponse(
            int objectid,
            short moveid,
            int currentMp,
            boolean useSkills,
            int skillId,
            int skillLevel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        packet.writeInt(objectid);
        packet.writeShort(moveid);
        packet.write(useSkills ? 1 : 0);
        packet.writeShort(currentMp);
        packet.write(skillId);
        packet.write(skillLevel);
        packet.writeInt(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);

        return packet.getPacket();
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
            if (statup == MonsterStatus.MAGIC_DAMAGE_REFLECT
                    || statup == MonsterStatus.WEAPON_DAMAGE_REFLECT) {
                ignore_imm = true;
                break;
            }
        }
        for (MonsterStatus statup : statups) {
            if (statup != MonsterStatus.MAGIC_DAMAGE_REFLECT
                    && statup != MonsterStatus.WEAPON_DAMAGE_REFLECT) {
                if (ignore_imm) {
                    if (statup == MonsterStatus.MAGIC_IMMUNITY
                            || statup == MonsterStatus.WEAPON_IMMUNITY) {
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

    public static byte[] applyMonsterStatus(
            final int oid, final MonsterStatus mse, int x, MobSkill skil) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        packet.writeLong(getSpecialLongMask(Collections.singletonList(mse)));
        packet.writeLong(getLongMask(Collections.singletonList(mse)));

        packet.writeShort(x);
        packet.writeShort(skil.getSkillId());
        packet.writeShort(skil.getSkillLevel());
        packet.writeShort(
                mse.isEmpty()
                        ? 1
                        : 0); // might actually be the buffTime but it's not displayed anywhere
        packet.writeShort(0); // delay in ms
        packet.write(1); // size
        packet.write(1); // ? v97

        return packet.getPacket();
    }

    public static byte[] applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        packet.writeLong(getSpecialLongMask(Collections.singletonList(mse.getStati())));
        packet.writeLong(getLongMask(Collections.singletonList(mse.getStati())));

        packet.writeShort(mse.getX());
        if (mse.isMonsterSkill()) {
            packet.writeShort(mse.getMobSkill().getSkillId());
            packet.writeShort(mse.getMobSkill().getSkillLevel());
        } else if (mse.getSkill() > 0) {
            packet.writeInt(mse.getSkill());
        }
        packet.writeShort(
                mse.getStati().isEmpty()
                        ? 1
                        : 0); // might actually be the buffTime but it's not displayed anywhere
        packet.writeShort(0); // delay in ms
        packet.write(1); // size
        packet.write(1); // ? v97

        return packet.getPacket();
    }

    public static byte[] applyMonsterStatus(
            final int oid,
            final Map<MonsterStatus, Integer> stati,
            final List<Integer> reflection,
            MobSkill skil) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        packet.writeLong(getSpecialLongMask(stati.keySet()));
        packet.writeLong(getLongMask(stati.keySet()));

        for (Map.Entry<MonsterStatus, Integer> mse : stati.entrySet()) {
            packet.writeShort(mse.getValue());
            packet.writeShort(skil.getSkillId());
            packet.writeShort(skil.getSkillLevel());
            packet.writeShort(
                    mse.getKey().isEmpty()
                            ? 1
                            : 0); // might actually be the buffTime but it's not displayed anywhere
        }
        for (Integer ref : reflection) {
            packet.writeInt(ref);
        }
        packet.writeInt(0);
        packet.writeShort(0); // delay in ms

        int size = stati.size(); // size
        if (reflection.size() > 0) {
            size /= 2; // This gives 2 buffs per reflection but it's really one buff
        }
        packet.write(size); // size
        packet.write(1); // ? v97

        return packet.getPacket();
    }

    public static byte[] cancelMonsterStatus(int oid, MonsterStatus stat) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        packet.writeInt(oid);
        packet.writeLong(getSpecialLongMask(Collections.singletonList(stat)));
        packet.writeLong(getLongMask(Collections.singletonList(stat)));
        packet.write(1); // reflector is 3~!??
        packet.write(2); // ? v97

        return packet.getPacket();
    }

    public static byte[] talkMonster(int oid, int itemId, String msg) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TALK_MONSTER.getValue());
        packet.writeInt(oid);
        packet.writeInt(500); // ?
        packet.writeInt(itemId);
        packet.write(itemId <= 0 ? 0 : 1);
        packet.write(msg == null || msg.length() <= 0 ? 0 : 1);
        if (msg != null && msg.length() > 0) {
            packet.writeMapleAsciiString(msg);
        }
        packet.writeInt(1); // ?

        return packet.getPacket();
    }

    public static byte[] removeTalkMonster(int oid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
        packet.writeInt(oid);
        return packet.getPacket();
    }
}
