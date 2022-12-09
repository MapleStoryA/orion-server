package handling.login.handler;

import client.MapleClient;
import database.state.CharacterService;
import database.state.LoginService;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readMapleAsciiString();
        final int characterId = slea.readInt();

        if (!CharacterService.checkIfCharacterExist(c.getAccID(), characterId)) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        final byte state = (byte) CharacterService.deleteCharacter(characterId, c.getAccID());

        c.getSession().write(LoginPacket.deleteCharResponse(characterId, state));

    }

}
