package server.life;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import server.carnival.MapleCarnivalFactory;
import server.carnival.MapleCarnivalFactory.MCSkill;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;

@Slf4j
public class SpawnPoint extends Spawns {

    private final MapleMonster monster;
    private final Point pos;
    private final int mobTime;
    private final AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private final boolean immobile;
    private final String msg;
    private final byte carnivalTeam;
    private final MapleMap map;
    private long nextPossibleSpawn;
    private int carnival = -1;

    public SpawnPoint(
            final MapleMonster monster,
            MapleMap map,
            final Point pos,
            final int mobTime,
            final byte carnivalTeam,
            final String msg) {
        this.monster = monster;
        this.pos = pos;
        this.map = map;
        this.mobTime = (mobTime < 0 ? -1 : (mobTime * 1000));
        this.carnivalTeam = carnivalTeam;
        this.msg = msg;
        this.immobile = !monster.getStats().getMobile();
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public final void setCarnival(int c) {
        this.carnival = c;
    }

    @Override
    public final Point getPosition() {
        return pos;
    }

    @Override
    public final MapleMonster getMonster() {
        return monster;
    }

    @Override
    public final byte getCarnivalTeam() {
        return carnivalTeam;
    }

    @Override
    public final int getCarnivalId() {
        return carnival;
    }

    @Override
    public final boolean shouldSpawn() {
        if (mobTime < 0) {
            return false;
        }
        // regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or
        // spawnpoints with mobtime a
        // maximum of 1
        if (((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) {
            return false;
        }
        return nextPossibleSpawn <= System.currentTimeMillis();
    }

    @Override
    public final MapleMonster spawnMonster(final MapleMap map) {
        final MapleMonster mob = new MapleMonster(monster);
        mob.setPosition(pos);
        mob.setCarnivalTeam(carnivalTeam);
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled() {
                nextPossibleSpawn = System.currentTimeMillis();
                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        map.spawnMonster(mob, -2);
        if (carnivalTeam > -1) {
            for (MapleReactor r : map.getAllReactorsThreadsafe()) { // parsing through everytime a monster is
                // spawned? not good idea
                if (r.getName().startsWith(String.valueOf(carnivalTeam))
                        && r.getReactorId() == (9980000 + carnivalTeam)
                        && r.getState() < 5) {
                    final int num = Integer.parseInt(r.getName().substring(1, 2)); // 00, 01, etc
                    final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
                    if (skil != null) {
                        skil.getSkill().applyEffect(null, mob, false);
                    }
                }
            }
        }
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
