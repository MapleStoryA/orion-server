package handling.channel.handler;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelBuffHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    int sourceid = slea.readInt();
    MapleCharacter chr = c.getPlayer();
    if (chr == null) {
      return;
    }
    final ISkill skill = SkillFactory.getSkill(sourceid);

    if (skill.isChargeSkill()) {
      chr.setKeyDownSkill_Time(0);
      chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillCancel(chr, sourceid), false);
    }
    chr.cancelEffect(skill.getEffect(1), false, -1);

  }

}
