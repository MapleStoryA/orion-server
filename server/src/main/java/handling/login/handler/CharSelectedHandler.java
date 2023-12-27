package handling.login.handler;

import client.MapleClient;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
public class CharSelectedHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final int characterId = packet.readInt();
        String hardwareID = packet.readMapleAsciiString();
        String macAddress = packet.readMapleAsciiString();
        log.debug("HardwareID: " + macAddress);
        log.debug("MAC: " + hardwareID);
        if (CharSelectedViewAllHandler.checkIfCharacterExists(c, characterId)) {
            return;
        }

        String[] publicIpAddressAndPort = WorldServer.getInstance()
                .getChannel(c.getChannel())
                .getPublicAddress()
                .split(":");
        int port = Integer.parseInt(publicIpAddressAndPort[1]);
        c.getSession().write(MaplePacketCreator.getServerIP(port, characterId));
    }
}
