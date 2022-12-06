package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class CharSelectedHandler extends AbstractMaplePacketHandler {


    private static final boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }


    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int charId = slea.readInt();
        String hardwareID = slea.readMapleAsciiString();
        String macAddress = slea.readMapleAsciiString();
        log.info("HardwareID: " + macAddress);
        log.info("MAC: " + hardwareID);
        if (loginFailCount(c) || !c.login_Auth(charId)) { // This should not happen unlessplayer is hacking
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.getSession().write(MaplePacketCreator.getServerIP(
                Integer.parseInt(WorldServer.getInstance().getChannel(c.getChannel()).getPublicAddress().split(":")[1]), charId));

    }

}
