package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class PetIgnoreHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final int petId = (int) packet.readLong();
        if (chr == null || chr.getMap() == null || chr.getPetIndex(petId) < 0) {
            return;
        }
        final MaplePet pet = chr.getPetByUID(petId);
        if (pet == null) {
            return;
        }
        if (chr.getPetIndex(petId) != 0) {
            c.getPlayer().dropMessage(1, "Only Boss Pets can use this.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        // Store in quest data will do
        final byte size = packet.readByte();
        if (size <= 0) {
            // TODO: pet ignore
        } else {
            final StringBuilder st = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i > 10) {
                    break;
                }
                st.append(packet.readInt()).append(",");
            }
            st.deleteCharAt(st.length() - 1);
        }
    }
}
