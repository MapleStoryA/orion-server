package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import scripting.v1.NewNpcTalkHandler;
import server.life.MapleNPC;
import tools.data.input.SeekableLittleEndianAccessor;

public class NpcTalkHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr == null || chr.getMap() == null || chr.getConversation() == 1) {
      return;
    }
    final MapleNPC npc = chr.getMap().getNPCByOid(slea.readInt());

    if (npc == null || npc.isHidden() || (c.getLastNPCTalk() > System.currentTimeMillis() - 1000)) { // 1
      // sec
      return;
    }
    c.setLastNPCTalk();
    if (npc.hasShop()) {
      chr.setConversation(1);
      npc.sendShop(c);
    } else {
      if (NewNpcTalkHandler.isNewNpcScriptAvailable(npc.getId())) {
        NewNpcTalkHandler.startConversation(npc.getId(), c);
        return;
      }
      c.setNpcScript(null);
      NPCScriptManager.getInstance().start(c, npc.getId());
    }

  }

}
