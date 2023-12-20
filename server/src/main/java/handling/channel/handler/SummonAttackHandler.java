package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.SummonSkillEntry;
import client.anticheat.CheatingOffense;
import client.skill.ISkill;
import client.skill.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import networking.packet.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.SummonAttackEntry;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class SummonAttackHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || !chr.isAlive()) {
            return;
        }
        final MapleMap map = chr.getMap();
        final MapleMapObject obj = map.getMapObject(packet.readInt(), MapleMapObjectType.SUMMON);
        if (obj == null) {
            return;
        }
        final MapleSummon summon = (MapleSummon) obj;
        if (summon.getOwnerId() != chr.getId() || summon.getSkillLevel() <= 0) {
            return;
        }
        final SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkill());
        if (sse == null) {
            return;
        }
        packet.skip(8);
        int tick = packet.readInt();
        chr.updateTick(tick);
        summon.CheckSummonAttackFrequency(chr, tick);
        packet.skip(8);
        final byte animation = packet.readByte();
        packet.skip(8);
        final byte numAttacked = packet.readByte();
        if (numAttacked > sse.mobCount) {
            chr.getCheatTracker().registerOffense(CheatingOffense.SUMMON_HACK_MOBS);
            // AutobanManager.getInstance().autoban(c, "Attacking more monster
            // that summon can do (Skillid : "+summon.getSkill()+" Count : " +
            // numAttacked + ", allowed : " + sse.mobCount + ")");
            return;
        }
        packet.skip(8); // some pos stuff
        final List<SummonAttackEntry> allDamage = new ArrayList<SummonAttackEntry>();
        chr.getCheatTracker().checkSummonAttack();

        for (int i = 0; i < numAttacked; i++) {
            final MapleMonster mob = map.getMonsterByOid(packet.readInt());

            if (mob == null) {
                continue;
            }
            if (chr.getPosition().distanceSq(mob.getPosition()) > 400000.0) {
                chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_SUMMON);
            }
            packet.skip(18); // who knows
            final int damage = packet.readInt();
            allDamage.add(new SummonAttackEntry(mob, damage));
            mob.damage(c.getPlayer(), damage, true);
        }
        if (!summon.isChangedMap()) {
            map.broadcastMessage(
                    chr,
                    MaplePacketCreator.summonAttack(
                            summon.getOwnerId(), summon.getObjectId(), animation, allDamage, chr.getLevel()),
                    summon.getPosition());
        }
        final ISkill summonSkill = SkillFactory.getSkill(summon.getSkill());
        final MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());

        if (summonEffect == null) {
            return;
        }
        for (SummonAttackEntry attackEntry : allDamage) {
            final int toDamage = attackEntry.getDamage();
            final MapleMonster mob = attackEntry.getMonster();

            if (toDamage > 0 && summonEffect.getMonsterStati().size() > 0) {
                if (summonEffect.makeChanceResult()) {
                    for (Map.Entry<MonsterStatus, Integer> z :
                            summonEffect.getMonsterStati().entrySet()) {
                        mob.applyStatus(
                                chr,
                                new MonsterStatusEffect(z.getKey(), z.getValue(), summonSkill.getId(), null, false),
                                summonEffect.isPoison(),
                                4000,
                                false);
                    }
                }
            }
        }
        if (summon.isGaviota()) {
            chr.getMap().broadcastMessage(MaplePacketCreator.removeSummon(summon, true));
            chr.getMap().removeMapObject(summon);
            chr.removeVisibleMapObject(summon);
            chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
            chr.cancelEffectFromBuffStat(MapleBuffStat.REAPER);
            // TODO: Multi Summoning, must do something about hack buffstat
        }
    }
}
