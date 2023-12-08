package handling.login.handler;

import client.MapleClient;
import database.CharacterService;
import database.LoginState;
import handling.AbstractMaplePacketHandler;
import handling.ServerMigration;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;
import tools.data.input.CInPacket;

@Slf4j
public class CharSelectedHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(CInPacket packet, MapleClient c) {
        final int characterId = packet.readInt();
        String hardwareID = packet.readMapleAsciiString();
        String macAddress = packet.readMapleAsciiString();
        log.info("HardwareID: " + macAddress);
        log.info("MAC: " + hardwareID);
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
        String[] publicIpAddressAndPort = WorldServer.getInstance()
                .getChannel(c.getChannel())
                .getPublicAddress()
                .split(":");
        int port = Integer.parseInt(publicIpAddressAndPort[1]);
        c.getSession().write(MaplePacketCreator.getServerIP(port, characterId));
    }
}
