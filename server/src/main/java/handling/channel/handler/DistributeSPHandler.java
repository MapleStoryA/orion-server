package handling.channel.handler;

import client.*;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeSPHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    slea.skip(4);
    int skillid = slea.readInt();
    MapleCharacter player = c.getPlayer();
    ISkill skill = SkillFactory.getSkill(skillid);
    int curLevel = player.getSkillLevel(skill);
    boolean beginner = false;
    switch (skill.getId()) {
      case 1000:
      case 1001:
      case 1002:
      case 10001000:
      case 10001001:
      case 10001002:
      case 20001000:
      case 20001001:
      case 20001002:
      case 20011000:
      case 20011001:
      case 20011002:
        beginner = true;
    }
    if (beginner) {
      player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill));
      return;
    }
    if ((!player.getJobValue().isA(MapleJob.getById(skillid / 10000))) || (skillid == 21110008)
        || (skillid == 21120010) || (skillid == 21110007) || (skillid == 21120009)) {
      return;
    }
    if (player.getRemainingSp() > 0 || player.getEvanSP().getSkillPoints().size() > 0) {
      if (curLevel + 1 <= (skill.hasMastery() ? player.getMasterLevel(skill) : skill.getMaxLevel())) {
        if (!c.getPlayer().isEvan()) {
          player.setRemainingSp(player.getRemainingSp() - 1);
          player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
          player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill));
        } else {
          int job = skillid / 10000;
          if (player.getEvanSP().getSkillPoints(job) > 0) {
            player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill));
          }
          player.getEvanSP().addSkillPoints(job, -1);
          c.getSession().write(MaplePacketCreator.updateExtendedSP(player.getEvanSP()));
        }
      }
    }
    c.enableActions();
  }


}
