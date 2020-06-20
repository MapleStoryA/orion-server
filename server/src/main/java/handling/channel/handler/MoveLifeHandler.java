package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.movement.MovePath;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.MobPacket;

import java.awt.*;

public class MoveLifeHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null || chr.getMap() == null) {
      return; // ?
    }
    final int oid = slea.readInt();
    final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

    if (monster == null) { // movin something which is not a monster
      return;
    }
    final short moveid = slea.readShort();
    final boolean useSkill = slea.readByte() > 0;
    final byte skill = slea.readByte();
    final int skill1 = slea.readByte() & 0xFF; // unsigned?
    final int skill2 = slea.readByte();
    final int skill3 = slea.readByte();
    final int skill4 = slea.readByte();
    int realskill = 0;
    int level = 0;

    if (useSkill) {// && (skill == -1 || skill == 0)) {
      final byte size = monster.getNoSkills();
      boolean used = false;

      if (size > 0) {
        final Pair<Integer, Integer> skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
        realskill = skillToUse.getLeft();
        level = skillToUse.getRight();
        // Skill ID and Level
        final MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);

        if (mobSkill != null && !mobSkill.checkCurrentBuff(chr, monster)) {
          final long now = System.currentTimeMillis();
          final long ls = monster.getLastSkillUsed(realskill);

          if (ls == 0 || ((now - ls) > mobSkill.getCoolTime())) {
            monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

            final int reqHp = (int) (((float) monster.getHp() / monster.getMobMaxHp()) * 100); // In
            // case
            // this
            // monster
            // have
            // 2.1b
            // and
            // above
            // HP
            if (reqHp <= mobSkill.getHP()) {
              used = true;
              mobSkill.applyEffect(chr, monster, true);
            }
          }
        }
      }
      if (!used) {
        realskill = 0;
        level = 0;
      }
    }


    int size = slea.readInt();
    for(int i = 0; i < size; i++){
      slea.readInt();
      slea.readInt();
    }
    size = slea.readInt();// ?
    for(int i = 0; i < size; i++){
      slea.readInt();
    }
    slea.readByte();
    slea.readInt();
    slea.readInt();
    slea.readInt();
    slea.readInt();
    final MovePath res = new MovePath();
    final MapleMap map = chr.getMap();
    res.decode(slea);
    if (res != null && chr != null) {
      byte[] packet = MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level);
      c.getSession().write(packet);
      updatePosition(res, monster, -1);
      final Point endPos = monster.getPosition();
      map.moveMonster(monster, endPos);
      map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, skill, skill1, skill2, skill3, skill4,
          monster.getObjectId(), res), endPos);
      chr.getCheatTracker().checkMoveMonster(endPos);
    }

  }

}
