package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.packet.PetPacket;

@Slf4j
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
