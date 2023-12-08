package server.life;

import java.lang.ref.WeakReference;

@lombok.extern.slf4j.Slf4j
public class SummonAttackEntry {

    private final WeakReference<MapleMonster> mob;
    private final int damage;

    public SummonAttackEntry(MapleMonster mob, int damage) {
        super();
        this.mob = new WeakReference<MapleMonster>(mob);
        this.damage = damage;
    }

    public MapleMonster getMonster() {
        return mob.get();
    }

    public int getDamage() {
        return damage;
    }
}
