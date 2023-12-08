package server;

@lombok.extern.slf4j.Slf4j
public class StructPotentialItem {

    private byte incSTR;
    private byte incDEX;
    private byte incINT;
    private byte incLUK;
    private byte incACC;
    private byte incEVA;
    private byte incSpeed;
    private byte incJump;
    private byte incPAD;
    private byte incMAD;
    private byte incPDD;
    private byte incMDD;
    private byte prop;
    private byte time;
    private byte incSTRr;
    private byte incDEXr;
    private byte incINTr;
    private byte incLUKr;
    private byte incMHPr;
    private byte incMMPr;
    private byte incACCr;
    private byte incEVAr;
    private byte incPADr;
    private byte incMADr;
    private byte incPDDr;
    private byte incMDDr;
    private byte incCr;
    private byte incDAMr;
    private byte RecoveryHP;
    private byte RecoveryMP;
    private byte HP;
    private byte MP;
    private byte level;
    private byte ignoreTargetDEF;
    private byte ignoreDAM;
    private byte DAMreflect;
    private byte mpconReduce;
    private byte mpRestore;
    private byte incMesoProp;
    private byte incRewardProp;
    private byte incAllskill;
    private byte ignoreDAMr;
    private byte RecoveryUP;
    private boolean boss;
    private short incMHP;
    private short incMMP;
    private short attackType;
    private short potentialID;
    private short skillID;
    private int optionType;
    private int reqLevel; // probably the slot
    private String face; // angry, cheers, love, blaze, glitter

    @Override
    public final String toString() {
        final StringBuilder ret = new StringBuilder();
        if (getIncMesoProp() > 0) {
            ret.append("Gives MESO(not coded): ");
            ret.append(getIncMesoProp());
            ret.append(" ");
        }
        if (getIncRewardProp() > 0) {
            ret.append("Gives ITEM(not coded): ");
            ret.append(getIncRewardProp());
            ret.append(" ");
        }
        if (getProp() > 0) {
            ret.append("Probability(not coded): ");
            ret.append(getProp());
            ret.append(" ");
        }
        if (getTime() > 0) {
            ret.append("Duration(not coded): ");
            ret.append(getTime());
            ret.append(" ");
        }
        if (getAttackType() > 0) {
            ret.append("Attack Type(not coded): ");
            ret.append(getAttackType());
            ret.append(" ");
        }
        if (getIncAllskill() > 0) {
            ret.append("Gives ALL SKILLS: ");
            ret.append(getIncAllskill());
            ret.append(" ");
        }
        if (getSkillID() > 0) {
            ret.append("Gives SKILL: ");
            ret.append(getSkillID());
            ret.append(" ");
        }
        if (isBoss()) {
            ret.append("BOSS ONLY, ");
        }
        if (getFace().length() > 0) {
            ret.append("Face Expression: ");
            ret.append(getFace());
            ret.append(" ");
        }
        if (getRecoveryUP() > 0) {
            ret.append("Gives Recovery % on potions: ");
            ret.append(getRecoveryUP());
            ret.append(" ");
        }
        if (getDAMreflect() > 0) {
            ret.append("Reflects Damage when Hit: ");
            ret.append(getDAMreflect());
            ret.append(" ");
        }
        if (getMpconReduce() > 0) {
            ret.append("Reduces MP Needed for skills: ");
            ret.append(getMpconReduce());
            ret.append(" ");
        }
        if (getIgnoreTargetDEF() > 0) {
            ret.append("Ignores Monster DEF %: ");
            ret.append(getIgnoreTargetDEF());
            ret.append(" ");
        }
        if (getRecoveryHP() > 0) {
            ret.append("Recovers HP: ");
            ret.append(getRecoveryHP());
            ret.append(" ");
        }
        if (getRecoveryMP() > 0) {
            ret.append("Recovers MP: ");
            ret.append(getRecoveryMP());
            ret.append(" ");
        }
        if (getHP() > 0) { // no idea
            ret.append("Recovers HP: ");
            ret.append(getHP());
            ret.append(" ");
        }
        if (getMP() > 0) { // no idea
            ret.append("Recovers MP: ");
            ret.append(getMP());
            ret.append(" ");
        }
        if (getMpRestore() > 0) { // no idea
            ret.append("Recovers MP: ");
            ret.append(getMpRestore());
            ret.append(" ");
        }
        if (getIgnoreDAM() > 0) {
            ret.append("Ignores Monster Damage: ");
            ret.append(getIgnoreDAM());
            ret.append(" ");
        }
        if (getIgnoreDAMr() > 0) {
            ret.append("Ignores Monster Damage %: ");
            ret.append(getIgnoreDAMr());
            ret.append(" ");
        }
        if (getIncMHP() > 0) {
            ret.append("Gives HP: ");
            ret.append(getIncMHP());
            ret.append(" ");
        }
        if (getIncMMP() > 0) {
            ret.append("Gives MP: ");
            ret.append(getIncMMP());
            ret.append(" ");
        }
        if (getIncMHPr() > 0) {
            ret.append("Gives HP %: ");
            ret.append(getIncMHPr());
            ret.append(" ");
        }
        if (getIncMMPr() > 0) {
            ret.append("Gives MP %: ");
            ret.append(getIncMMPr());
            ret.append(" ");
        }
        if (getIncSTR() > 0) {
            ret.append("Gives STR: ");
            ret.append(getIncSTR());
            ret.append(" ");
        }
        if (getIncDEX() > 0) {
            ret.append("Gives DEX: ");
            ret.append(getIncDEX());
            ret.append(" ");
        }
        if (getIncINT() > 0) {
            ret.append("Gives INT: ");
            ret.append(getIncINT());
            ret.append(" ");
        }
        if (getIncLUK() > 0) {
            ret.append("Gives LUK: ");
            ret.append(getIncLUK());
            ret.append(" ");
        }
        if (getIncACC() > 0) {
            ret.append("Gives ACC: ");
            ret.append(getIncACC());
            ret.append(" ");
        }
        if (getIncEVA() > 0) {
            ret.append("Gives EVA: ");
            ret.append(getIncEVA());
            ret.append(" ");
        }
        if (getIncSpeed() > 0) {
            ret.append("Gives Speed: ");
            ret.append(getIncSpeed());
            ret.append(" ");
        }
        if (getIncJump() > 0) {
            ret.append("Gives Jump: ");
            ret.append(getIncJump());
            ret.append(" ");
        }
        if (getIncPAD() > 0) {
            ret.append("Gives Attack: ");
            ret.append(getIncPAD());
            ret.append(" ");
        }
        if (getIncMAD() > 0) {
            ret.append("Gives Magic Attack: ");
            ret.append(getIncMAD());
            ret.append(" ");
        }
        if (getIncPDD() > 0) {
            ret.append("Gives Defense: ");
            ret.append(getIncPDD());
            ret.append(" ");
        }
        if (getIncMDD() > 0) {
            ret.append("Gives Magic Defense: ");
            ret.append(getIncMDD());
            ret.append(" ");
        }
        if (getIncSTRr() > 0) {
            ret.append("Gives STR %: ");
            ret.append(getIncSTRr());
            ret.append(" ");
        }
        if (getIncDEXr() > 0) {
            ret.append("Gives DEX %: ");
            ret.append(getIncDEXr());
            ret.append(" ");
        }
        if (getIncINTr() > 0) {
            ret.append("Gives INT %: ");
            ret.append(getIncINTr());
            ret.append(" ");
        }
        if (getIncLUKr() > 0) {
            ret.append("Gives LUK %: ");
            ret.append(getIncLUKr());
            ret.append(" ");
        }
        if (getIncACCr() > 0) {
            ret.append("Gives ACC %: ");
            ret.append(getIncACCr());
            ret.append(" ");
        }
        if (getIncEVAr() > 0) {
            ret.append("Gives EVA %: ");
            ret.append(getIncEVAr());
            ret.append(" ");
        }
        if (getIncPADr() > 0) {
            ret.append("Gives Attack %: ");
            ret.append(getIncPADr());
            ret.append(" ");
        }
        if (getIncMADr() > 0) {
            ret.append("Gives Magic Attack %: ");
            ret.append(getIncMADr());
            ret.append(" ");
        }
        if (getIncPDDr() > 0) {
            ret.append("Gives Defense %: ");
            ret.append(getIncPDDr());
            ret.append(" ");
        }
        if (getIncMDDr() > 0) {
            ret.append("Gives Magic Defense %: ");
            ret.append(getIncMDDr());
            ret.append(" ");
        }
        if (getIncCr() > 0) {
            ret.append("Gives Critical %: ");
            ret.append(getIncCr());
            ret.append(" ");
        }
        if (getIncDAMr() > 0) {
            ret.append("Gives Total Damage %: ");
            ret.append(getIncDAMr());
            ret.append(" ");
        }
        if (getLevel() > 0) {
            ret.append("Level: ");
            ret.append(getLevel());
            ret.append(" ");
        }
        return ret.toString();
    }

    public byte getIncSTR() {
        return incSTR;
    }

    public void setIncSTR(byte incSTR) {
        this.incSTR = incSTR;
    }

    public byte getIncDEX() {
        return incDEX;
    }

    public void setIncDEX(byte incDEX) {
        this.incDEX = incDEX;
    }

    public byte getIncINT() {
        return incINT;
    }

    public void setIncINT(byte incINT) {
        this.incINT = incINT;
    }

    public byte getIncLUK() {
        return incLUK;
    }

    public void setIncLUK(byte incLUK) {
        this.incLUK = incLUK;
    }

    public byte getIncACC() {
        return incACC;
    }

    public void setIncACC(byte incACC) {
        this.incACC = incACC;
    }

    public byte getIncEVA() {
        return incEVA;
    }

    public void setIncEVA(byte incEVA) {
        this.incEVA = incEVA;
    }

    public byte getIncSpeed() {
        return incSpeed;
    }

    public void setIncSpeed(byte incSpeed) {
        this.incSpeed = incSpeed;
    }

    public byte getIncJump() {
        return incJump;
    }

    public void setIncJump(byte incJump) {
        this.incJump = incJump;
    }

    public byte getIncPAD() {
        return incPAD;
    }

    public void setIncPAD(byte incPAD) {
        this.incPAD = incPAD;
    }

    public byte getIncMAD() {
        return incMAD;
    }

    public void setIncMAD(byte incMAD) {
        this.incMAD = incMAD;
    }

    public byte getIncPDD() {
        return incPDD;
    }

    public void setIncPDD(byte incPDD) {
        this.incPDD = incPDD;
    }

    public byte getIncMDD() {
        return incMDD;
    }

    public void setIncMDD(byte incMDD) {
        this.incMDD = incMDD;
    }

    public byte getProp() {
        return prop;
    }

    public void setProp(byte prop) {
        this.prop = prop;
    }

    public byte getTime() {
        return time;
    }

    public void setTime(byte time) {
        this.time = time;
    }

    public byte getIncSTRr() {
        return incSTRr;
    }

    public void setIncSTRr(byte incSTRr) {
        this.incSTRr = incSTRr;
    }

    public byte getIncDEXr() {
        return incDEXr;
    }

    public void setIncDEXr(byte incDEXr) {
        this.incDEXr = incDEXr;
    }

    public byte getIncINTr() {
        return incINTr;
    }

    public void setIncINTr(byte incINTr) {
        this.incINTr = incINTr;
    }

    public byte getIncLUKr() {
        return incLUKr;
    }

    public void setIncLUKr(byte incLUKr) {
        this.incLUKr = incLUKr;
    }

    public byte getIncMHPr() {
        return incMHPr;
    }

    public void setIncMHPr(byte incMHPr) {
        this.incMHPr = incMHPr;
    }

    public byte getIncMMPr() {
        return incMMPr;
    }

    public void setIncMMPr(byte incMMPr) {
        this.incMMPr = incMMPr;
    }

    public byte getIncACCr() {
        return incACCr;
    }

    public void setIncACCr(byte incACCr) {
        this.incACCr = incACCr;
    }

    public byte getIncEVAr() {
        return incEVAr;
    }

    public void setIncEVAr(byte incEVAr) {
        this.incEVAr = incEVAr;
    }

    public byte getIncPADr() {
        return incPADr;
    }

    public void setIncPADr(byte incPADr) {
        this.incPADr = incPADr;
    }

    public byte getIncMADr() {
        return incMADr;
    }

    public void setIncMADr(byte incMADr) {
        this.incMADr = incMADr;
    }

    public byte getIncPDDr() {
        return incPDDr;
    }

    public void setIncPDDr(byte incPDDr) {
        this.incPDDr = incPDDr;
    }

    public byte getIncMDDr() {
        return incMDDr;
    }

    public void setIncMDDr(byte incMDDr) {
        this.incMDDr = incMDDr;
    }

    public byte getIncCr() {
        return incCr;
    }

    public void setIncCr(byte incCr) {
        this.incCr = incCr;
    }

    public byte getIncDAMr() {
        return incDAMr;
    }

    public void setIncDAMr(byte incDAMr) {
        this.incDAMr = incDAMr;
    }

    public byte getRecoveryHP() {
        return RecoveryHP;
    }

    public void setRecoveryHP(byte recoveryHP) {
        RecoveryHP = recoveryHP;
    }

    public byte getRecoveryMP() {
        return RecoveryMP;
    }

    public void setRecoveryMP(byte recoveryMP) {
        RecoveryMP = recoveryMP;
    }

    public byte getHP() {
        return HP;
    }

    public void setHP(byte HP) {
        this.HP = HP;
    }

    public byte getMP() {
        return MP;
    }

    public void setMP(byte MP) {
        this.MP = MP;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public byte getIgnoreTargetDEF() {
        return ignoreTargetDEF;
    }

    public void setIgnoreTargetDEF(byte ignoreTargetDEF) {
        this.ignoreTargetDEF = ignoreTargetDEF;
    }

    public byte getIgnoreDAM() {
        return ignoreDAM;
    }

    public void setIgnoreDAM(byte ignoreDAM) {
        this.ignoreDAM = ignoreDAM;
    }

    public byte getDAMreflect() {
        return DAMreflect;
    }

    public void setDAMreflect(byte DAMreflect) {
        this.DAMreflect = DAMreflect;
    }

    public byte getMpconReduce() {
        return mpconReduce;
    }

    public void setMpconReduce(byte mpconReduce) {
        this.mpconReduce = mpconReduce;
    }

    public byte getMpRestore() {
        return mpRestore;
    }

    public void setMpRestore(byte mpRestore) {
        this.mpRestore = mpRestore;
    }

    public byte getIncMesoProp() {
        return incMesoProp;
    }

    public void setIncMesoProp(byte incMesoProp) {
        this.incMesoProp = incMesoProp;
    }

    public byte getIncRewardProp() {
        return incRewardProp;
    }

    public void setIncRewardProp(byte incRewardProp) {
        this.incRewardProp = incRewardProp;
    }

    public byte getIncAllskill() {
        return incAllskill;
    }

    public void setIncAllskill(byte incAllskill) {
        this.incAllskill = incAllskill;
    }

    public byte getIgnoreDAMr() {
        return ignoreDAMr;
    }

    public void setIgnoreDAMr(byte ignoreDAMr) {
        this.ignoreDAMr = ignoreDAMr;
    }

    public byte getRecoveryUP() {
        return RecoveryUP;
    }

    public void setRecoveryUP(byte recoveryUP) {
        RecoveryUP = recoveryUP;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public short getIncMHP() {
        return incMHP;
    }

    public void setIncMHP(short incMHP) {
        this.incMHP = incMHP;
    }

    public short getIncMMP() {
        return incMMP;
    }

    public void setIncMMP(short incMMP) {
        this.incMMP = incMMP;
    }

    public short getAttackType() {
        return attackType;
    }

    public void setAttackType(short attackType) {
        this.attackType = attackType;
    }

    public short getPotentialID() {
        return potentialID;
    }

    public void setPotentialID(short potentialID) {
        this.potentialID = potentialID;
    }

    public short getSkillID() {
        return skillID;
    }

    public void setSkillID(short skillID) {
        this.skillID = skillID;
    }

    public int getOptionType() {
        return optionType;
    }

    public void setOptionType(int optionType) {
        this.optionType = optionType;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }
}
