package handling.login.handler;

import client.MapleClient;
import database.CharacterService;
import database.LoginState;
import handling.ServerMigration;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
public class CharSelectedViewAllHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        int characterId = packet.readInt();
        packet.readInt();
        c.setWorld(0); // world
        int channel = 1;
        c.setChannel(channel);
        String mac = packet.readMapleAsciiString();
        log.info("Mac connected: {}", mac);
        if (c.tooManyLogin()
                || !CharacterService.checkIfCharacterExist(c.getAccountData().getId(), characterId)) {
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }

        WorldServer.getInstance()
                .getMigrationService()
                .putMigrationEntry(new ServerMigration(characterId, c.getAccountData(), c.getSessionIPAddress()));

        c.updateLoginState(LoginState.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.getSession()
                .write(MaplePacketCreator.getServerIP(
                        Integer.parseInt(WorldServer.getInstance()
                                .getChannel(c.getChannel())
                                .getPublicAddress()
                                .split(":")[1]),
                        characterId));
    }
}
