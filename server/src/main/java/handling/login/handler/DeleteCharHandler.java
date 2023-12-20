package handling.login.handler;

import client.MapleClient;
import database.CharacterService;
import handling.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class DeleteCharHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        packet.readMapleAsciiString();
        final int characterId = packet.readInt();

        if (!CharacterService.checkIfCharacterExist(c.getAccountData().getId(), characterId)) {
            c.getSession().close();
            return;
        }
        final byte state = (byte)
                CharacterService.deleteCharacter(characterId, c.getAccountData().getId());

        c.getSession().write(LoginPacket.deleteCharResponse(characterId, state));
    }
}
