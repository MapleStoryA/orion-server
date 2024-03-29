package client.inventory;

import constants.GameConstants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import tools.helper.Randomizer;

@Slf4j
public class Equip extends Item implements IEquip {

    private byte upgradeSlots = 0, level = 0, vicioushammer = 0, enhance = 0;
    private short str = 0,
            dex = 0,
            _int = 0,
            luk = 0,
            hp = 0,
            mp = 0,
            watk = 0,
            matk = 0,
            wdef = 0,
            mdef = 0,
            acc = 0,
            avoid = 0,
            hands = 0,
            speed = 0,
            jump = 0,
            potential1 = 0,
            potential2 = 0,
            potential3 = 0,
            hpR = 0,
            mpR = 0;
    private int itemEXP = 0, durability = -1;
    private short requiredStr;
    private short requiredDex, requiredLuk, requiredInt;
    private short requiredLevel;
    private int requiredJob;
    private MapleItemInformationProvider provider;

    public Equip(int id, short position, byte flag) {
        super(id, position, (short) 1, flag);
        loadRequiredStats();
    }

    public Equip(int id, short position, int uniqueid, byte flag) {
        super(id, position, (short) 1, flag, uniqueid);
        loadRequiredStats();
    }

    public void loadRequiredStats() {
        provider = MapleItemInformationProvider.getInstance();
        Map<String, Integer> required = provider.getEquipStats(this.getItemId());
        this.requiredJob = required.get("reqJob");
        this.requiredStr = required.get("reqSTR").shortValue();
        this.requiredDex = required.get("reqDEX").shortValue();
        this.requiredInt = required.get("reqINT").shortValue();
        this.requiredLuk = required.get("reqLUK").shortValue();
        this.requiredLevel = required.get("reqLevel").shortValue();
    }

    @Override
    public IItem copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getSN(), getFlag());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.enhance = enhance;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.itemEXP = itemEXP;
        ret.durability = durability;
        ret.vicioushammer = vicioushammer;
        ret.potential1 = potential1;
        ret.potential2 = potential2;
        ret.potential3 = potential3;
        ret.hpR = hpR;
        ret.mpR = mpR;
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setInventoryId(getInventoryId());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());

        ret.requiredJob = requiredJob;
        ret.requiredStr = requiredStr;
        ret.requiredDex = requiredDex;
        ret.requiredInt = requiredInt;
        ret.requiredLuk = requiredLuk;
        ret.requiredLevel = requiredLevel;

        return ret;
    }

    @Override
    public byte getType() {
        return 1;
    }

    @Override
    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    @Override
    public short getStr() {
        return str;
    }

    public void setStr(short str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    @Override
    public short getDex() {
        return dex;
    }

    public void setDex(short dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    @Override
    public short getInt() {
        return _int;
    }

    public void setInt(short _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    @Override
    public short getLuk() {
        return luk;
    }

    public void setLuk(short luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    @Override
    public short getHp() {
        return hp;
    }

    public void setHp(short hp) {
        if (hp < 0) {
            hp = 0;
        }
        this.hp = hp;
    }

    @Override
    public short getMp() {
        return mp;
    }

    public void setMp(short mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    @Override
    public short getWatk() {
        return watk;
    }

    public void setWatk(short watk) {
        if (watk < 0) {
            watk = 0;
        }
        this.watk = watk;
    }

    @Override
    public short getMatk() {
        return matk;
    }

    public void setMatk(short matk) {
        if (matk < 0) {
            matk = 0;
        }
        this.matk = matk;
    }

    @Override
    public short getWdef() {
        return wdef;
    }

    public void setWdef(short wdef) {
        if (wdef < 0) {
            wdef = 0;
        }
        this.wdef = wdef;
    }

    @Override
    public short getMdef() {
        return mdef;
    }

    public void setMdef(short mdef) {
        if (mdef < 0) {
            mdef = 0;
        }
        this.mdef = mdef;
    }

    @Override
    public short getAcc() {
        return acc;
    }

    public void setAcc(short acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    @Override
    public short getAvoid() {
        return avoid;
    }

    public void setAvoid(short avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    @Override
    public short getHands() {
        return hands;
    }

    public void setHands(short hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    @Override
    public short getSpeed() {
        return speed;
    }

    public void setSpeed(short speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    @Override
    public short getJump() {
        return jump;
    }

    public void setJump(short jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    @Override
    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    @Override
    public byte getViciousHammer() {
        return vicioushammer;
    }

    public void setViciousHammer(byte ham) {
        vicioushammer = ham;
    }

    @Override
    public int getItemEXP() {
        return itemEXP;
    }

    public void setItemEXP(int itemEXP) {
        if (itemEXP < 0) {
            itemEXP = 0;
        }
        this.itemEXP = itemEXP;
    }

    @Override
    public int getEquipExp() {
        if (itemEXP <= 0) {
            return 0;
        }
        // aproximate value
        if (GameConstants.isWeapon(getItemId())) {
            return itemEXP / IEquip.WEAPON_RATIO;
        } else {
            return itemEXP / IEquip.ARMOR_RATIO;
        }
    }

    @Override
    public int getEquipExpForLevel() {
        if (getEquipExp() <= 0) {
            return 0;
        }
        int expz = getEquipExp();
        for (int i = getBaseLevel(); i <= GameConstants.getMaxLevel(getItemId()); i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { // for 0, dont continue;
                break;
            }
        }
        return expz;
    }

    @Override
    public int getExpPercentage() {
        if (getEquipLevel() < getBaseLevel()
                || getEquipLevel() > GameConstants.getMaxLevel(getItemId())
                || GameConstants.getExpForLevel(getEquipLevel(), getItemId()) <= 0) {
            return 0;
        }
        return getEquipExpForLevel() * 100 / GameConstants.getExpForLevel(getEquipLevel(), getItemId());
    }

    @Override
    public int getEquipLevel() {
        if (GameConstants.getMaxLevel(getItemId()) <= 0) {
            return 0;
        } else if (getEquipExp() <= 0) {
            return getBaseLevel();
        }
        int levelz = getBaseLevel();
        int expz = getEquipExp();
        for (int i = levelz;
                (GameConstants.getStatFromWeapon(getItemId()) == null
                        ? (i <= GameConstants.getMaxLevel(getItemId()))
                        : (i < GameConstants.getMaxLevel(getItemId())));
                i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                levelz++;
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { // for 0, dont continue;
                break;
            }
        }
        return levelz;
    }

    @Override
    public int getBaseLevel() {
        return (GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0);
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException(
                    "Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    @Override
    public int getDurability() {
        return durability;
    }

    public void setDurability(final int dur) {
        this.durability = dur;
    }

    @Override
    public byte getEnhance() {
        return enhance;
    }

    public void setEnhance(final byte en) {
        this.enhance = en;
    }

    @Override
    public short getPotential1() {
        return potential1;
    }

    public void setPotential1(final short en) {
        this.potential1 = en;
    }

    @Override
    public short getPotential2() {
        return potential2;
    }

    public void setPotential2(final short en) {
        this.potential2 = en;
    }

    @Override
    public short getPotential3() {
        return potential3;
    }

    public void setPotential3(final short en) {
        this.potential3 = en;
    }

    @Override
    public byte getState() {
        final int pots = potential1 + potential2 + potential3;
        if (potential1 >= 30000 || potential2 >= 30000 || potential3 >= 30000) {
            return 7;
        } else if (potential1 >= 20000 || potential2 >= 20000 || potential3 >= 20000) {
            return 6;
        } else if (pots >= 1) {
            return 5;
        } else if (pots < 0) {
            return 1;
        }
        return 0;
    }

    public void resetPotential() { // equip first receive
        // 0.04% chance unique, 4% chance epic, else rare
        final int rank = Randomizer.nextInt(100) < 4 ? (Randomizer.nextInt(100) < 4 ? -7 : -6) : -5;
        setPotential1((short) rank);
        setPotential2((short) (Randomizer.nextInt(10) < 3 ? rank : 0)); // 1/10 chance of 3 line
        setPotential3((short) 0); // just set it theoretically
    }

    public void renewPotential() {
        // 4% chance upgrade
        final int rank = Randomizer.nextInt(100) < 4 && getState() != 7 ? -(getState() + 1) : -(getState());
        setPotential1((short) rank);
        setPotential2((short) (getPotential3() > 0 ? rank : 0)); // 1/10 chance of 3 line
        setPotential3((short) 0); // just set it theoretically
    }

    @Override
    public short getHpR() {
        return hpR;
    }

    public void setHpR(final short hp) {
        this.hpR = hp;
    }

    @Override
    public short getMpR() {
        return mpR;
    }

    public void setMpR(final short mp) {
        this.mpR = mp;
    }

    @Override
    public int getRequiredJob() {
        if (requiredJob == 8) {
            return requiredJob / 2;
        }
        return requiredJob;
    }

    @Override
    public short getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public short getRequiredStr() {

        return requiredStr;
    }

    @Override
    public short getRequiredInt() {

        return requiredInt;
    }

    @Override
    public short getRequiredLuk() {

        return requiredLuk;
    }

    @Override
    public short getRequiredDex() {

        return requiredDex;
    }

    @Override
    public short getRequiredFame() {

        return 0;
    }
}
