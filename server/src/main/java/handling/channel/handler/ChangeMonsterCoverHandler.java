package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import tools.data.input.CInPacket;

@lombok.extern.slf4j.Slf4j
public class ChangeMonsterCoverHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int bookid = packet.readInt();
        if (bookid == 0 || GameConstants.isMonsterCard(bookid)) {
            chr.setMonsterBookCover(bookid);
            chr.getMonsterBook().updateCard(c, bookid);
        }

    }

}
