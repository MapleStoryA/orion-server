package handling.login.handler;

import client.MapleCharacterHelper;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.login.LoginInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

@lombok.extern.slf4j.Slf4j
public class CheckCharNameHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        c.getSession()
                .write(
                        LoginPacket.charNameResponse(
                                name,
                                !MapleCharacterHelper.canCreateChar(name)
                                        || LoginInformationProvider.getInstance()
                                                .isForbiddenName(name)));
    }
}
