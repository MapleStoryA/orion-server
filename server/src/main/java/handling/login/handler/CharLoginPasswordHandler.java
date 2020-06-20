package handling.login.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacketHandler;
import handling.login.LoginWorker;
import tools.KoreanDateUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.LoginPacket;

import java.util.Calendar;

public class CharLoginPasswordHandler implements MaplePacketHandler {

  private static final boolean loginFailCount(final MapleClient c) {
    c.loginAttempt++;
    if (c.loginAttempt > 5) {
      return true;
    }
    return false;
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
    final boolean macBan = c.hasBannedMac();

    int loginok = c.login(login, pwd, ipBan || macBan);
    final Calendar tempbannedTill = c.getTempBanCalendar();

    if (loginok == 0 && (ipBan || macBan) && !c.isGm()) {
      loginok = 3;
      if (macBan) {
        MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0],
            "Enforcing account ban, account " + login, false, 4, false);
      }
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
      LoginWorker.registerClient(c);
    }

  }

  @Override
  public boolean validateState(MapleClient c) {
    return c.isLoggedIn() == false;
  }

}
