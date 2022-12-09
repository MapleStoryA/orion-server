package handling.login.handler;

import client.MapleClient;
import database.state.CharacterService;
import handling.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class CharSelectedViewAllHandler extends AbstractMaplePacketHandler {

    private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int characterId = slea.readInt();
        slea.readInt();
        c.setWorld(0); // world
        int channel = 1;
        c.setChannel(channel);
        String mac = slea.readMapleAsciiString();
        c.updateMacs(mac);
        if (loginFailCount(c) || !CharacterService.checkIfCharacterExist(c.getAccID(), characterId)) {
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.getSession().write(MaplePacketCreator.getServerIP(
                Integer.parseInt(WorldServer.getInstance().getChannel(c.getChannel()).getPublicAddress().split(":")[1]), characterId));

    }

}
