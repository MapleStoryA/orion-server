package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class DenyGuildRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.skip(1);
        String from = packet.readMapleAsciiString();
        final MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
        if (cfrom != null) {
            cfrom.getClient()
                    .getSession()
                    .write(MaplePacketCreator.denyGuildInvitation(c.getPlayer().getName()));
        }
    }
}
