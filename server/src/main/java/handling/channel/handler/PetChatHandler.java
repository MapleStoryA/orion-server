package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class PetChatHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (slea.available() < 12) {
      return;
    }
    final int petid = (int) slea.readLong();
    final short command = slea.readShort();
    String text = slea.readMapleAsciiString();
    MapleCharacter chr = c.getPlayer();
    if (chr == null || chr.getMap() == null || chr.getPetIndex(petid) < 0) {
      return;
    }
    chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getId(), command, text, chr.getPetIndex(petid)), true);
  }

}
