package server.life;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import tools.collection.Pair;

@Slf4j
public class MapleMonsterStats {

    private final Map<Element, ElementalEffectiveness> resistance = new HashMap<>();
    private final List<Pair<Integer, Integer>> skills = new ArrayList<>();
    private byte cp, selfDestruction_action, tagColor, tagBgColor, rareItemDropLevel, HPDisplayType;
    private short level, PhysicalDefense, MagicDefense, eva;
    private long hp;
    private int exp, mp, removeAfter, buffToGive, fixedDamage, selfDestruction_hp, dropItemPeriod, point;
    private boolean boss,
            undead,
            ffaLoot,
            firstAttack,
            isExplosiveReward,
            mobile,
            fly,
            onlyNormalAttack,
            friendly,
            noDoom;
    private String name;
    private List<Integer> revives = new ArrayList<>();
    private BanishInfo banish;

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp; // (hp * 3L / 2L);
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public byte getSelfD() {
        return selfDestruction_action;
    }

    public void setSelfD(byte selfDestruction_action) {
        this.selfDestruction_action = selfDestruction_action;
    }

    public void setSelfDHP(int selfDestruction_hp) {
        this.selfDestruction_hp = selfDestruction_hp;
    }

    public int getSelfDHp() {
        return selfDestruction_hp;
    }

    public int getFixedDamage() {
        return fixedDamage;
    }

    public void setFixedDamage(int damage) {
        this.fixedDamage = damage;
    }

    public short getPhysicalDefense() {
        return PhysicalDefense;
    }

    public void setPhysicalDefense(final short PhysicalDefense) {
        this.PhysicalDefense = PhysicalDefense;
    }

    public final short getMagicDefense() {
        return MagicDefense;
    }

    public final void setMagicDefense(final short MagicDefense) {
        this.MagicDefense = MagicDefense;
    }

    public final short getEva() {
        return eva;
    }

    public final void setEva(final short eva) {
        this.eva = eva;
    }

    public void setOnlyNormalAttack(boolean onlyNormalAttack) {
        this.onlyNormalAttack = onlyNormalAttack;
    }

    public boolean getOnlyNoramlAttack() {
        return onlyNormalAttack;
    }

    public BanishInfo getBanishInfo() {
        return banish;
    }

    public void setBanishInfo(BanishInfo banish) {
        this.banish = banish;
    }

    public int getRemoveAfter() {
        return removeAfter;
    }

    public void setRemoveAfter(int removeAfter) {
        this.removeAfter = removeAfter;
    }

    public byte getrareItemDropLevel() {
        return rareItemDropLevel;
    }

    public void setrareItemDropLevel(byte rareItemDropLevel) {
        this.rareItemDropLevel = rareItemDropLevel;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public boolean isFfaLoot() {
        return ffaLoot;
    }

    public void setFfaLoot(boolean ffaLoot) {
        this.ffaLoot = ffaLoot;
    }

    public boolean isExplosiveReward() {
        return isExplosiveReward;
    }

    public void setExplosiveReward(boolean isExplosiveReward) {
        this.isExplosiveReward = isExplosiveReward;
    }

    public boolean getMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean getFly() {
        return fly;
    }

    public void setFly(boolean fly) {
        this.fly = fly;
    }

    public List<Integer> getRevives() {
        return revives;
    }

    public void setRevives(List<Integer> revives) {
        this.revives = revives;
    }

    public boolean getUndead() {
        return undead;
    }

    public void setUndead(boolean undead) {
        this.undead = undead;
    }

    public void setEffectiveness(Element e, ElementalEffectiveness ee) {
        resistance.put(e, ee);
    }

    public void removeEffectiveness(Element e) {
        resistance.remove(e);
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
        ElementalEffectiveness elementalEffectiveness = resistance.get(e);
        if (elementalEffectiveness == null) {
            return ElementalEffectiveness.NORMAL;
        } else {
            return elementalEffectiveness;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = (byte) tagColor;
    }

    public byte getTagBgColor() {
        return tagBgColor;
    }

    public void setTagBgColor(int tagBgColor) {
        this.tagBgColor = (byte) tagBgColor;
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return Collections.unmodifiableList(this.skills);
    }

    public void setSkills(List<Pair<Integer, Integer>> skill_) {
        for (Pair<Integer, Integer> skill : skill_) {
            skills.add(skill);
        }
    }

    public byte getNoSkills() {
        return (byte) skills.size();
    }

    public boolean hasSkill(int skillId, int level) {
        for (Pair<Integer, Integer> skill : skills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstAttack() {
        return firstAttack;
    }

    public void setFirstAttack(boolean firstAttack) {
        this.firstAttack = firstAttack;
    }

    public byte getCP() {
        return cp;
    }

    public void setCP(byte cp) {
        this.cp = cp;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int cp) {
        this.point = cp;
    }

    public boolean isFriendly() {
        return friendly;
    }

    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    public boolean isNoDoom() {
        return noDoom;
    }

    public void setNoDoom(boolean doom) {
        this.noDoom = doom;
    }

    public int getBuffToGive() {
        return buffToGive;
    }

    public void setBuffToGive(int buff) {
        this.buffToGive = buff;
    }

    public byte getHPDisplayType() {
        return HPDisplayType;
    }

    public void setHPDisplayType(byte HPDisplayType) {
        this.HPDisplayType = HPDisplayType;
    }

    public int getDropItemPeriod() {
        return dropItemPeriod;
    }

    public void setDropItemPeriod(int d) {
        this.dropItemPeriod = d;
    }

    public int dropsMeso() {
        if (getRemoveAfter() != 0
                || getOnlyNoramlAttack()
                || getDropItemPeriod() > 0
                || getCP() > 0
                || getPoint() > 0
                || getFixedDamage() > 0
                || getSelfD() != -1) {
            return 0;
        } else if (isExplosiveReward()) {
            return 7;
        } else if (isBoss()) {
            return 2;
        }
        return 1;
    }
}
