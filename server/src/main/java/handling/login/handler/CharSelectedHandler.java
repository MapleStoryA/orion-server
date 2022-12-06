package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

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
        System.out.println("HardwareID: " + macAddress);
        System.out.println("MAC: " + hardwareID);
        if (loginFailCount(c) || !c.login_Auth(charId)) { // This should not happen unlessplayer is hacking
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.getSession().write(MaplePacketCreator.getServerIP(
                Integer.parseInt(WorldServer.getInstance().getChannel(c.getChannel()).getIP().split(":")[1]), charId));

    }

}
