package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import database.CharacterService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
public class CharlistViewAllHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {

        c.setWorld(0);
        c.setChannel(1);

        List<MapleCharacter> chars =
                CharacterService.loadCharacters(c, 0, c.getAccountData().getId());

        if (chars != null) {
            c.getSession().write(MaplePacketCreator.viewAllChar(1, chars.size()));
            c.getSession().write(MaplePacketCreator.viewAllCharShowChars(0, chars));
        } else {
            c.getSession().close();
        }
    }
}
