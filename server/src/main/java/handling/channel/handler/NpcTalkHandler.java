package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import scripting.v1.game.helper.NpcTalkHelper;
import server.life.MapleNPC;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
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
            if (NpcTalkHelper.isNewNpcScriptAvailable(npc.getId())) {
                NpcTalkHelper.startConversation(npc.getId(), c);
                return;
            }
            c.setNpcScript(null);
            NPCScriptManager.getInstance().start(c, npc.getId());
        }

    }

}
