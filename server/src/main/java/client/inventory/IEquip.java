package client.inventory;

public interface IEquip extends IItem {

    int ARMOR_RATIO = 350000;
    int WEAPON_RATIO = 700000;

    byte getUpgradeSlots();

    byte getLevel();

    byte getViciousHammer();

    int getItemEXP();

    int getExpPercentage();

    int getEquipLevel();

    int getEquipExp();

    int getEquipExpForLevel();

    int getBaseLevel();

    short getStr();

    short getDex();

    short getInt();

    short getLuk();

    short getHp();

    short getMp();

    short getWatk();

    short getMatk();

    short getWdef();

    short getMdef();

    short getAcc();

    short getAvoid();

    short getHands();

    short getSpeed();

    short getJump();

    int getDurability();

    byte getEnhance();

    byte getState();

    short getPotential1();

    short getPotential2();

    short getPotential3();

    short getHpR();

    short getMpR();

    int getRequiredJob();

    short getRequiredLevel();

    short getRequiredStr();

    short getRequiredInt();

    short getRequiredLuk();

    short getRequiredDex();

    short getRequiredFame();

    enum ScrollResult {
        SUCCESS,
        FAIL,
        CURSE
    }
}
