package server.life;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Randomizer;

@lombok.extern.slf4j.Slf4j
public class SpawnPointAreaBoss extends Spawns {

    private final MapleMonster monster;
    private final Point pos1;
    private final Point pos2;
    private final Point pos3;
    private final int mobTime;
    private final AtomicBoolean spawned = new AtomicBoolean(false);
    private final String msg;
    private long nextPossibleSpawn;

    public SpawnPointAreaBoss(
            final MapleMonster monster,
            final Point pos1,
            final Point pos2,
            final Point pos3,
            final int mobTime,
            final String msg) {
        this.monster = monster;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
        this.mobTime = (mobTime < 0 ? -1 : (mobTime * 1000));
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    @Override
    public final MapleMonster getMonster() {
        return monster;
    }

    @Override
    public final byte getCarnivalTeam() {
        return -1;
    }

    @Override
    public final int getCarnivalId() {
        return -1;
    }

    @Override
    public final boolean shouldSpawn() {
        if (mobTime < 0) {
            return false;
        }
        if (spawned.get()) {
            return false;
        }
        return nextPossibleSpawn <= System.currentTimeMillis();
    }

    @Override
    public final Point getPosition() {
        final int rand = Randomizer.nextInt(3);
        return rand == 0 ? pos1 : rand == 1 ? pos2 : pos3;
    }

    @Override
    public final MapleMonster spawnMonster(final MapleMap map) {
        final MapleMonster mob = new MapleMonster(monster);

        mob.setPosition(getPosition());
        spawned.set(true);
        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();

                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawned.set(false);
            }
        });
        map.spawnMonster(mob, -2);

        if (msg != null) {
            map.broadcastMessage(MaplePacketCreator.serverNotice(6, msg));
        }
        return mob;
    }

    @Override
    public final int getMobTime() {
        return mobTime;
    }
}
