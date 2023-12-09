package handling.login.handler;

import client.MapleClient;
import constants.BlockReason;
import database.LoginResult;
import handling.MaplePacketHandler;
import handling.login.LoginServer;
import lombok.extern.slf4j.Slf4j;
import tools.data.input.InPacket;
import tools.packet.LoginPacket;

@Slf4j
public class CharLoginPasswordHandler implements MaplePacketHandler {

    private String normalizeStringPassword(String password) {
        if (password == null) {
            return "";
        }
        return password.replace("\r", "");
    }

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final String login = packet.readMapleAsciiString();
        final String pwd = normalizeStringPassword(packet.readMapleAsciiString());

        LoginResult result = c.login(login, pwd);

        if (result.isPermanentBan()) {
            c.getSession().write(LoginPacket.getPermBan(BlockReason.BOT.getId()));
            return;
        }

        if (result.isLoginError()) {
            c.getSession().write(LoginPacket.getLoginFailed(result.getResult()));
            return;
        }

        c.resetLoginCount();
        c.setAccountData(result.getAccountData());
        LoginServer.getInstance().registerClient(c);
    }

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }
}
