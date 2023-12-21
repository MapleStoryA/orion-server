package client;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleCoolDownValueHolder {

    private int skillId;
    private long startTime;
    private long length;

    public MapleCoolDownValueHolder(int skillId, long startTime, long length) {
        super();
        this.setSkillId(skillId);
        this.setStartTime(startTime);
        this.setLength(length);
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
