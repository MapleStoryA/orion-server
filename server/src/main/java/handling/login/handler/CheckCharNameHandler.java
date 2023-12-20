package handling.login.handler;

import client.MapleCharacterHelper;
import client.MapleClient;
import handling.login.LoginInformationProvider;
import networking.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class CheckCharNameHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        String name = packet.readMapleAsciiString();
        c.getSession()
                .write(LoginPacket.charNameResponse(
                        name,
                        !MapleCharacterHelper.canCreateChar(name)
                                || LoginInformationProvider.getInstance().isForbiddenName(name)));
    }
}
