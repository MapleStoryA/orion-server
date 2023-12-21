package handling.login.handler;

import client.MapleCharacterHelper;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginInformationProvider;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
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
