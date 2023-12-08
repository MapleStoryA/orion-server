package client.status;

import java.io.Serializable;

public enum MonsterStatus implements Serializable {
    NEUTRALISE(0x02),
    IMPRINT(0x04),
    MONSTER_BOMB(0x08),
    MAGIC_CRASH(0x10),
    WATK(0x100000000L),
    WDEF(0x200000000L),
    MATK(0x400000000L),
    MDEF(0x800000000L),
    ACC(0x1000000000L),
    AVOID(0x2000000000L),
    SPEED(0x4000000000L),
    STUN(0x8000000000L),
    FREEZE(0x10000000000L),
    POISON(0x20000000000L),
    SEAL(0x40000000000L),
    SHOWDOWN(0x80000000000L),
    WEAPON_ATTACK_UP(0x100000000000L),
    WEAPON_DEFENSE_UP(0x200000000000L),
    MAGIC_ATTACK_UP(0x400000000000L),
    MAGIC_DEFENSE_UP(0x800000000000L),
    DOOM(0x1000000000000L),
    SHADOW_WEB(0x2000000000000L),
    WEAPON_IMMUNITY(0x4000000000000L),
    MAGIC_IMMUNITY(0x8000000000000L),
    DAMAGE_IMMUNITY(0x20000000000000L),
    NINJA_AMBUSH(0x40000000000000L),
    VENOMOUS_WEAPON(0x100000000000000L),
    DARKNESS(0x200000000000000L),
    EMPTY(0x800000000000000L),
    HYPNOTIZE(0x1000000000000000L),
    WEAPON_DAMAGE_REFLECT(0x2000000000000000L),
    MAGIC_DAMAGE_REFLECT(0x4000000000000000L),
    SUMMON(0x8000000000000000L) // all summon bag mobs have.
;
    static final long serialVersionUID = 0L;
    private final long i;
    private final boolean first;

    MonsterStatus(long i) {
        this.i = i;
        first = false;
    }

    MonsterStatus(int i, boolean first) {
        this.i = i;
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isEmpty() {
        return this == SUMMON || this == EMPTY;
    }

    public long getValue() {
        return i;
    }
}
