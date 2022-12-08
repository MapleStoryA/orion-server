package handling.login.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginServer;
import lombok.extern.slf4j.Slf4j;
import tools.KoreanDateUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.util.Calendar;

@Slf4j
public class CharLoginPasswordHandler implements MaplePacketHandler {

    private static final boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 5;
    }


    private String normalizeStringPassword(String password) {
        if (password == null) {
            return "";
        }
        return password.replace("\r", "");
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final String login = slea.readMapleAsciiString();
        final String pwd = normalizeStringPassword(slea.readMapleAsciiString());

        c.setAccountName(login);
        final boolean ipBan = c.hasBannedIP();

        int loginok = c.login(login, pwd, ipBan);
        final Calendar tempbannedTill = c.getTempBanCalendar();

        if (loginok == 0 && (ipBan) && !c.isGm()) {
            loginok = 3;
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.getSession().write(LoginPacket.getLoginFailed(loginok));
            }
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            if (!loginFailCount(c)) {
                c.getSession().write(LoginPacket.getTempBan(
                        KoreanDateUtil.getTempBanTimestamp(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            }
        } else {
            c.loginAttempt = 0;
            LoginServer.getInstance().registerClient(c);
        }

    }

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

}
