package client.inventory;

public enum MapleWeaponType {
    NOT_A_WEAPON(4f),
    BOW(3.4f),
    CLAW(3.6f),
    DAGGER(4.0f),
    CROSSBOW(3.6f),
    AXE1H(4.4f),
    SWORD1H(4.0f),
    BLUNT1H(4.4f),
    AXE2H(4.8f), // Note : Swing = 4.8, Stab = 3.4
    SWORD2H(4.6f),
    BLUNT2H(4.8f), // Note : Swing = 4.8, Stab = 3.4
    POLE_ARM(5.0f), // NOTE : Swing = 5.0, stab = 3.0
    SPEAR(5.0f), // NOTE : Stab = 5.0, wing = 3.0
    STAFF(3.6f),
    WAND(3.6f),
    KNUCKLE(4.8f),
    GUN(3.6f),
    KATARA(4.0f);
    private final float damageMultiplier;

    MapleWeaponType(final float maxDamageMultiplier) {
        this.damageMultiplier = maxDamageMultiplier;
    }

    public final float getMaxDamageMultiplier() {
        return damageMultiplier;
    }
}
