package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.skill.ISkill;
import client.skill.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class SpecialMoveHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, final MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || !chr.isAlive() || chr.getMap() == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        packet.skip(4); // Old X and Y
        final int skill_id = packet.readInt();
        final int skillLevel = packet.readByte();
        final ISkill skill = SkillFactory.getSkill(skill_id);
        if (!chr.getJob().isSkillBelongToJob(skill_id, chr.isGameMaster())) {
            chr.dropMessage(5, "This skill cannot be used with the current job");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (chr.getSkillLevel(skill) <= -1 /*|| chr.getSkillLevel(skill) != skillLevel*/) { // TODO: Fix this
            if (!GameConstants.isMulungSkill(skill_id) && !GameConstants.isPyramidSkill(skill_id)) {
                c.getSession().close();
                return;
            }
            if (GameConstants.isMulungSkill(skill_id)) {
                if (chr.getMapId() / 10000 != 92502) {
                    // AutobanManager.getInstance().autoban(c, "Using Mu Lung dojo skill out of dojo
                    // maps.");
                    return;
                } else {
                    chr.mulung_EnergyModify(false);
                }
            } else if (GameConstants.isPyramidSkill(skill_id)) {
                if (chr.getMapId() / 10000 != 92602) {
                    // AutobanManager.getInstance().autoban(c, "Using Pyramid skill out of pyramid
                    // maps.");
                    return;
                }
            }
        }
        final MapleStatEffect effect = skill.getEffect(chr.getSkillLevel(GameConstants.getLinkedAranSkill(skill_id)));

        if (effect.getCooldown() > 0 && !chr.isGameMaster()) {
            if (chr.skillisCooling(skill_id)) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (skill_id != 5221006) { // Battleship
                c.getSession().write(MaplePacketCreator.skillCooldown(skill_id, effect.getCooldown()));
                chr.addCooldown(skill_id, System.currentTimeMillis(), effect.getCooldown() * 1000L);
            }
        }
        switch (skill_id) {
            case 2311005: // doom priest
                final byte mobsCount = packet.readByte();
                if (mobsCount > 10 || mobsCount == 0) {
                    c.enableActions();
                    return;
                }
                if (c.getPlayer().getItemQuantity(4006000, false) < 0) {
                    c.enableActions();
                } else {
                    c.getPlayer().removeItem(4006000, -1);
                }
                MonsterStatusEffect eff = new MonsterStatusEffect(
                        MonsterStatus.DOOM, (int) c.getPlayer().getPosition().getX(), skill_id, null, false);
                for (int i = 0; i < mobsCount; i++) {
                    List<MapleMonster> mobs = c.getPlayer().getMap().getAllMonster();
                    if (mobs == null || mobs.size() <= 0) {
                        break;
                    }
                    MapleMonster monster =
                            c.getPlayer().getMap().getAllMonster().get(i);
                    monster.applyStatus(c.getPlayer(), eff, false, 10000 + (1000 * skillLevel), false);
                    Collections.shuffle(mobs);
                }
                c.enableActions();
                break;
            case 1121001:
            case 1221001:
            case 1321001:
                final byte number_of_mobs = packet.readByte();
                packet.skip(3);
                for (int i = 0; i < number_of_mobs; i++) {
                    int mobId = packet.readInt();

                    final MapleMonster mob = chr.getMap().getMonsterByOid(mobId);
                    if (mob != null) {
                        chr.getMap()
                                .broadcastMessage(
                                        chr,
                                        MaplePacketCreator.showMagnet(mobId, packet.readByte()),
                                        chr.getPosition());
                        mob.switchController(chr, mob.isControllerHasAggro());
                    }
                }
                chr.getMap()
                        .broadcastMessage(
                                chr,
                                MaplePacketCreator.showBuffeffect(chr.getId(), skill_id, 1, packet.readByte()),
                                chr.getPosition());
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            case 4341003:
                chr.setKeyDownSkill_Time(0);
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, skill_id), false);
                break;
            case 22141003: // Evan Slow
                // c.enableActions();
                // break;
            default:
                Point pos = null;
                if (packet.available() == 7) {
                    pos = packet.readPos();
                }
                if (effect.isMagicDoor()) { // Mystic Door
                    if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                        effect.applyTo(c.getPlayer(), pos);
                    } else {
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                } else {
                    final int mountid = MapleStatEffect.parseMountInfo(c.getPlayer(), skill.getId());
                    if (mountid != 0
                            && mountid != GameConstants.getMountItem(skill.getId())
                            && !c.getPlayer().isGameMaster()
                            && c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null
                            && c.getPlayer()
                                            .getInventory(MapleInventoryType.EQUIPPED)
                                            .getItem((byte) -118)
                                    == null) {
                        if (!GameConstants.isMountItemAvailable(
                                mountid, c.getPlayer().getJob().getId())) {
                            c.getSession().write(MaplePacketCreator.enableActions());
                            return;
                        }
                    }

                    effect.applyTo(chr, pos);
                }
                break;
        }
    }
}
