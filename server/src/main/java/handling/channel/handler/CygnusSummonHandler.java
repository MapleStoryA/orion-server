package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import scripting.NPCScriptManager;

@Slf4j
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
