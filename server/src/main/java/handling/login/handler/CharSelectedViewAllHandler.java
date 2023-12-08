package handling.login.handler;

import client.MapleClient;
import database.CharacterService;
import database.LoginState;
import handling.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class CharSelectedViewAllHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int characterId = slea.readInt();
        slea.readInt();
        c.setWorld(0); // world
        int channel = 1;
        c.setChannel(channel);
        String mac = slea.readMapleAsciiString();
        log.info("Mac connected: {}", mac);
        if (c.tooManyLogin()
                || !CharacterService.checkIfCharacterExist(
                        c.getAccountData().getId(), characterId)) {
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        c.updateLoginState(LoginState.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.getSession()
                .write(
                        MaplePacketCreator.getServerIP(
                                Integer.parseInt(
                                        WorldServer.getInstance()
                                                .getChannel(c.getChannel())
                                                .getPublicAddress()
                                                .split(":")[1]),
                                characterId));
    }
}
