package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillMacro;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeSkillMacroHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final int num = slea.readByte();
    String name;
    int shout, skill1, skill2, skill3;
    SkillMacro macro;

    for (int i = 0; i < num; i++) {
      name = slea.readMapleAsciiString();
      shout = slea.readByte();
      skill1 = slea.readInt();
      skill2 = slea.readInt();
      skill3 = slea.readInt();

      macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
      chr.updateMacros(i, macro);
    }

  }

}
