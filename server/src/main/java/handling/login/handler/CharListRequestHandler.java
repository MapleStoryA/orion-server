package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import database.CharacterService;
import handling.AbstractMaplePacketHandler;
import lombok.extern.slf4j.Slf4j;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.util.List;

@Slf4j
public class CharListRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;

        c.setWorld(server);
        c.setChannel(channel);

        final List<MapleCharacter> chars = CharacterService.loadCharacters(c, c.getWorld(), c.getAccountData().getId());
        if (chars != null) {
            c.getSession().write(LoginPacket.getCharList(true, chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }

    }

}
