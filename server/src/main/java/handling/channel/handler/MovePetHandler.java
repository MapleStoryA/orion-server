package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import handling.AbstractMaplePacketHandler;
import server.movement.MovePath;
import tools.data.input.CInPacket;
import tools.packet.PetPacket;

@lombok.extern.slf4j.Slf4j
public class MovePetHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        final long petId = packet.readLong();
        MovePath res = new MovePath();
        res.decode(packet);

        MapleCharacter chr = c.getPlayer();

        if (res != null && chr != null) { // map crash hack
            final byte slot = chr.getPetIndex((int) petId);
            MaplePet pet = chr.getPet(slot);
            pet.updatePosition(res);
            if (slot == -1) {
                return;
            }
            chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), slot, res), false);
        }
    }
}
