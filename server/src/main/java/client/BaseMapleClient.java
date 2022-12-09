package client;

import constants.ServerConstants;
import database.DatabaseConnection;
import handling.session.NetworkSession;
import lombok.Getter;
import server.Timer;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.packet.LoginPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseMapleClient {
    public static final String CLIENT_KEY = "CLIENT";

    public static final byte LOGIN_NOTLOGGEDIN = 0,
            LOGIN_SERVER_TRANSITION = 1,
            LOGIN_LOGGEDIN = 2,
            LOGIN_WAITING = 3,
            CASH_SHOP_TRANSITION = 4,
            LOGIN_CS_LOGGEDIN = 5,
            CHANGE_CHANNEL = 6;

    protected final MapleAESOFB send;
    protected final MapleAESOFB receive;
    protected NetworkSession session;
    private long lastPong = 0;
    @Getter
    private short loginAttempt = 0;

    public BaseMapleClient(MapleAESOFB send, MapleAESOFB receive, NetworkSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }


    public BaseMapleClient(byte[] ivSend, byte[] ivRecv, NetworkSession session) {
        this(new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)),
                new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION), session);
    }

    public final MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public final MapleAESOFB getSendCrypto() {
        return send;
    }

    public final NetworkSession getSession() {
        return session;
    }

    public void sendPacket(byte[] packet) {
        getSession().write(packet);
    }

    public void enableActions() {
        getSession().write(MaplePacketCreator.enableActions());
    }

    public final long getLastPong() {
        return lastPong;
    }

    public final void pongReceived() {
        lastPong = System.currentTimeMillis();
    }

    public void sendPing() {
        session.write(LoginPacket.getPing());
        Timer.PingTimer.getInstance().schedule(new PingThread(this), 15000);
    }

    public void resetLoginCount() {
        this.loginAttempt = 0;
    }

    public boolean tooManyLogin() {
        this.loginAttempt++;
        return this.getLoginAttempt() > 5;
    }

    //TODO: Remove from here
    public boolean hasBannedIP() {
        boolean ret = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')");
            ps.setString(1, session.getRemoteAddress().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Error checking ip bans" + ex);
        }
        return ret;
    }

    public String getSessionIPAddress() {
        return session.getRemoteAddress().toString().split(":")[0];
    }

}
