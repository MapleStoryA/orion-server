package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMonsterCoverHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    int bookid = slea.readInt();
    if (bookid == 0 || GameConstants.isMonsterCard(bookid)) {
      chr.setMonsterBookCover(bookid);
      chr.getMonsterBook().updateCard(c, bookid);
    }

  }

}
