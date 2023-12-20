package handling.channel.handler;

import client.MapleClient;
import networking.packet.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class CygnusSummonHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (c.getPlayer().getJob().getId() == 2000) {
            NPCScriptManager.getInstance().start(c, 1202000);
        } else if (c.getPlayer().getJob().getId() == 1000) {
            NPCScriptManager.getInstance().start(c, 1101008);
        }
    }
}
