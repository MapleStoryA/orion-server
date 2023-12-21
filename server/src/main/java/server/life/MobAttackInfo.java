package server.life;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobAttackInfo {

    private boolean isDeadlyAttack;
    private int mpBurn, mpCon;
    private int diseaseSkill, diseaseLevel;

    public MobAttackInfo() {}

    public boolean isDeadlyAttack() {
        return isDeadlyAttack;
    }

    public void setDeadlyAttack(boolean isDeadlyAttack) {
        this.isDeadlyAttack = isDeadlyAttack;
    }

    public int getMpBurn() {
        return mpBurn;
    }

    public void setMpBurn(int mpBurn) {
        this.mpBurn = mpBurn;
    }

    public int getDiseaseSkill() {
        return diseaseSkill;
    }

    public void setDiseaseSkill(int diseaseSkill) {
        this.diseaseSkill = diseaseSkill;
    }

    public int getDiseaseLevel() {
        return diseaseLevel;
    }

    public void setDiseaseLevel(int diseaseLevel) {
        this.diseaseLevel = diseaseLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }
}
