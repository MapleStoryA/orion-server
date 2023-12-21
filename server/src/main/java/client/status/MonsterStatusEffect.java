package client.status;

import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import server.life.MobSkill;

@Slf4j
public class MonsterStatusEffect {

    private final int skill;
    private final MobSkill mobSkill;
    private final boolean monsterSkill;
    private MonsterStatus status;
    private Integer multiplier;
    private ScheduledFuture<?> cancelTask;
    private ScheduledFuture<?> poisonSchedule;

    public MonsterStatusEffect(
            final MonsterStatus status,
            final Integer multiplier,
            final int skillId,
            final MobSkill mobskill,
            final boolean monsterSkill) {
        this.status = status;
        this.skill = skillId;
        this.monsterSkill = monsterSkill;
        this.mobSkill = mobskill;
        this.multiplier = multiplier;
    }

    public final MonsterStatus getStatus() {
        return status;
    }

    public final Integer getMultiplier() {
        return multiplier;
    }

    public final void setValue(final MonsterStatus status, final Integer newVal) {
        this.status = status;
        multiplier = newVal;
    }

    public final int getSkill() {
        return skill;
    }

    public final MobSkill getMobSkill() {
        return mobSkill;
    }

    public final boolean isMonsterSkill() {
        return monsterSkill;
    }

    public final void setCancelTask(final ScheduledFuture<?> cancelTask) {
        this.cancelTask = cancelTask;
    }

    public final void setPoisonSchedule(final ScheduledFuture<?> poisonSchedule) {
        this.poisonSchedule = poisonSchedule;
    }

    public final void cancelTask() {
        if (this.cancelTask != null) {
            this.cancelTask.cancel(false);
        }
        this.cancelTask = null;
    }

    public final void cancelPoisonSchedule() {
        if (this.poisonSchedule != null) {
            try {
                this.poisonSchedule.cancel(false);
            } catch (NullPointerException e) {
            } // set to null anyway.
        }
        this.poisonSchedule = null;
    }
}
