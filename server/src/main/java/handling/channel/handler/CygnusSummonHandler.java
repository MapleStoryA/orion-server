package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import tools.data.input.SeekableLittleEndianAccessor;

public class CygnusSummonHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getJob() == 2000) {
      NPCScriptManager.getInstance().start(c, 1202000);
    } else if (c.getPlayer().getJob() == 1000) {
      NPCScriptManager.getInstance().start(c, 1101008);
    }

  }

}
