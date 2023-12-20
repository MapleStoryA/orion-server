package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import scripting.v1.NpcTalkHelper;
import server.life.MapleNPC;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class NpcTalkHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null || chr.getConversation() == 1) {
            return;
        }
        final MapleNPC npc = chr.getMap().getNPCByOid(packet.readInt());

        if (npc == null || npc.isHidden() || (c.getLastNPCTalk() > System.currentTimeMillis() - 1000)) { // 1
            // sec
            return;
        }
        c.setLastNPCTalk();
        if (npc.hasShop()) {
            chr.setConversation(1);
            npc.sendShop(c);
        } else {
            if (NpcTalkHelper.isNewNpcScriptAvailable(npc.getId(), npc.getScript())) {
                NpcTalkHelper.startConversation(npc.getId(), c, npc.getScript());
                return;
            }
            c.setCurrentNpcScript(null);
            NPCScriptManager.getInstance().start(c, npc.getId());
        }
    }
}
