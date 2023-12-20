package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.skill.ISkill;
import client.skill.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.AbstractMaplePacketHandler;
import java.util.Random;
import networking.data.input.InPacket;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class MonsterBombHandler extends AbstractMaplePacketHandler {

    private static final int MONSTER_BOMB_SKILL = 4341003;

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final MapleMonster monster = chr.getMap().getMonsterByOid(packet.readInt());
        final int xpos = packet.readInt();
        final int ypos = packet.readInt();
        if (!chr.isGameMaster()) {
            if ((monster == null
                    || chr.getJob().getId() != 434
                    || chr.getMap() == null
                    || !chr.isAlive()
                    || chr.isHidden())) {
                return;
            }
        }
        final ISkill skill = SkillFactory.getSkill(4341003);
        if (skill != null) {
            MapleMapObject mob = c.getPlayer().getMap().getMapObject(monster.getObjectId(), MapleMapObjectType.MONSTER);
            if (mob != null) {
                MapleMonster mob2 = ((MapleMonster) mob);
                int countDown = c.getPlayer().getSkillLevel(4341003);
                countDown = calculateCountDown(countDown);
                int damage = c.getPlayer().getLevel() * 100
                        + new Random().nextInt(100 * c.getPlayer().getLevel());
                c.getSession().write(MaplePacketCreator.skillCooldown(skill.getId(), countDown));
                chr.addCooldown(MONSTER_BOMB_SKILL, System.currentTimeMillis(), countDown);
                int mobPositionX = mob2.getPosition().x;
                MonsterStatusEffect effect =
                        new MonsterStatusEffect(MonsterStatus.MONSTER_BOMB, mobPositionX - 50, 4341003, null, false);
                monster.applyStatus(c.getPlayer(), effect, false, 3000, false);
                c.enableActions();
                TimerManager.getInstance()
                        .schedule(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mob2.isAlive()) {
                                            mob2.damage(c.getPlayer(), damage, true);
                                            c.getPlayer()
                                                    .getMap()
                                                    .broadcastMessage(
                                                            c.getPlayer(),
                                                            MaplePacketCreator.damageMonster(mob.getObjectId(), damage),
                                                            true);
                                            c.getPlayer()
                                                    .getMap()
                                                    .broadcastMessage(
                                                            c.getPlayer(),
                                                            MaplePacketCreator.showMonsterBombEffect(
                                                                    mobPositionX, ypos, chr.getSkillLevel(skill)),
                                                            true);
                                        }
                                        c.enableActions();
                                    }
                                },
                                3000);
            }
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    private int calculateCountDown(int coolDown) {
        return (55 - (coolDown / 6) * 5) * 1000;
    }
}
