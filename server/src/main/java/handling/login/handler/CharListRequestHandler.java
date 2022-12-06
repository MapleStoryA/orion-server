package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
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

        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null) {
            var secondPassword = c.getSecondPassword() != null;
            c.getSession().write(LoginPacket.getCharList(secondPassword, chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }

    }

}
