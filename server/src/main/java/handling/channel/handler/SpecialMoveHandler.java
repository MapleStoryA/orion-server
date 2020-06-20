package handling.channel.handler;

import client.*;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SpecialMoveHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null || !chr.isAlive() || chr.getMap() == null) {
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    slea.skip(4); // Old X and Y
    final int skillid = slea.readInt();
    final int skillLevel = slea.readByte();
    final ISkill skill = SkillFactory.getSkill(skillid);
    if (!chr.isSkillBelongToJob(skillid)) {
      chr.dropMessage(5, "This skill cannot be used with the current job");
      c.getSession().write(MaplePacketCreator.enableActions());
      return;
    }
    if (chr.getSkillLevel(skill) <= -1 /*|| chr.getSkillLevel(skill) != skillLevel*/) {//TODO: Fix this
      if (!GameConstants.isMulungSkill(skillid) && !GameConstants.isPyramidSkill(skillid)) {
        c.getSession().close();
        return;
      }
      if (GameConstants.isMulungSkill(skillid)) {
        if (chr.getMapId() / 10000 != 92502) {
          //AutobanManager.getInstance().autoban(c, "Using Mu Lung dojo skill out of dojo maps.");
          return;
        } else {
          chr.mulung_EnergyModify(false);
        }
      } else if (GameConstants.isPyramidSkill(skillid)) {
        if (chr.getMapId() / 10000 != 92602) {
          //AutobanManager.getInstance().autoban(c, "Using Pyramid skill out of pyramid maps.");
          return;
        }
      }
    }
    final MapleStatEffect effect = skill.getEffect(chr.getSkillLevel(GameConstants.getLinkedAranSkill(skillid)));

    if (effect.getCooldown() > 0 && !chr.isGM()) {
      if (chr.skillisCooling(skillid)) {
        c.getSession().write(MaplePacketCreator.enableActions());
        return;
      }
      if (skillid != 5221006) { // Battleship
        c.getSession().write(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
        chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000);
      }
    }
    switch (skillid) {
      case 2311005: //doom priest
        final byte mobsCount = slea.readByte();
        if (mobsCount > 10 || mobsCount == 0) {
          c.enableActions();
          return;
        }
        if (c.getPlayer().getItemQuantity(4006000, false) < 0) {
          c.enableActions();
        } else {
          c.getPlayer().removeItem(4006000, -1);
        }
        MonsterStatusEffect eff = new MonsterStatusEffect(MonsterStatus.DOOM, (int) c.getPlayer().getPosition().getX(), skillid, null, false);
        for (int i = 0; i < mobsCount; i++) {
          List<MapleMonster> mobs = c.getPlayer().getMap().getAllMonster();
          if (mobs == null || mobs.size() <= 0) {
            break;
          }
          MapleMonster monster = c.getPlayer().getMap().getAllMonster().get(i);
          monster.applyStatus(c.getPlayer(), eff, false, 10000 + (1000 * skillLevel), false);
          Collections.shuffle(mobs);
        }
        c.enableActions();
        break;
      case 1121001:
      case 1221001:
      case 1321001:
        final byte number_of_mobs = slea.readByte();
        slea.skip(3);
        for (int i = 0; i < number_of_mobs; i++) {
          int mobId = slea.readInt();

          final MapleMonster mob = chr.getMap().getMonsterByOid(mobId);
          if (mob != null) {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showMagnet(mobId, slea.readByte()), chr.getPosition());
            mob.switchController(chr, mob.isControllerHasAggro());
          }
        }
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showBuffeffect(chr.getId(), skillid, 1, slea.readByte()), chr.getPosition());
        c.getSession().write(MaplePacketCreator.enableActions());
        break;
      case 4341003:
        chr.setKeyDownSkill_Time(0);
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, skillid), false);
        break;
      case 22141003://Evan Slow
        //c.enableActions();
        //break;
      default:
        Point pos = null;
        if (slea.available() == 7) {
          pos = slea.readPos();
        }
        if (effect.isMagicDoor()) { // Mystic Door
          if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
            effect.applyTo(c.getPlayer(), pos);
          } else {
            c.getSession().write(MaplePacketCreator.enableActions());
          }
        } else {
          final int mountid = MapleStatEffect.parseMountInfo(c.getPlayer(), skill.getId());
          if (mountid != 0 && mountid != GameConstants.getMountItem(skill.getId()) && !c.getPlayer().isGM() && c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118) == null) {
            if (!GameConstants.isMountItemAvailable(mountid, c.getPlayer().getJob())) {
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
