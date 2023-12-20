package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.InPacket;
import tools.packet.PetPacket;

@lombok.extern.slf4j.Slf4j
public class CharInfoRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());

        int objectid = packet.readInt();
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        }
        final MapleCharacter player = c.getPlayer().getMap().getCharacterById(objectid);
        c.getSession().write(MaplePacketCreator.enableActions());
        if (player != null) {
            if (!player.isGameMaster() || c.getPlayer().isGameMaster()) {
                if (c.getPlayer().getId() == objectid) {
                    if (player.getPet(0) != null) {
                        c.getSession()
                                .write(PetPacket.loadExceptionList(
                                        player.getId(), player.getPet(0).getUniqueId(), ""));
                    }
                }
                c.getSession()
                        .write(MaplePacketCreator.charInfo(player, c.getPlayer().getId() == objectid));
            }
        }
    }
}
