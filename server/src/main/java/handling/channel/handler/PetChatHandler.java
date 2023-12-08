package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.InPacket;
import tools.packet.PetPacket;

@lombok.extern.slf4j.Slf4j
public class PetChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        if (packet.available() < 12) {
            return;
        }
        final int petid = (int) packet.readLong();
        final short command = packet.readShort();
        String text = packet.readMapleAsciiString();
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null || chr.getPetIndex(petid) < 0) {
            return;
        }
        chr.getMap()
                .broadcastMessage(
                        chr,
                        PetPacket.petChat(chr.getId(), command, text, chr.getPetIndex(petid)),
                        true);
    }
}
