package handling.login.handler;

import client.MapleClient;
import database.state.CharacterService;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readMapleAsciiString();
        final int Character_ID = slea.readInt();

        if (!c.login_Auth(Character_ID)) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        final byte state = (byte) CharacterService.deleteCharacter(Character_ID, c.getAccID());

        c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));

    }

}
