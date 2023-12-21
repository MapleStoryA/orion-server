package provider.drop;

import lombok.extern.slf4j.Slf4j;
import server.life.MapleMonster;

@Slf4j
public class MapleDropData {

    private final String item;

    private final double chance;

    private final MapleMonster monster;

    public MapleDropData(String item, MapleMonster monster, double chance) {
        super();
        this.item = item;
        this.monster = monster;
        this.chance = chance;
    }

    public String getName() {
        return item;
    }

    public MapleMonster getMonster() {
        return monster;
    }

    public double getChance() {
        return chance;
    }
}
