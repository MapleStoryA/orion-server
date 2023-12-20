package client;

import constants.ServerConstants;
import handling.session.NetworkSession;
import java.util.concurrent.ScheduledFuture;
import lombok.Getter;
import server.Timer;
import tools.MaplePacketCreator;
import tools.encryption.MapleAESOFB;
import tools.packet.LoginPacket;

public class BaseMapleClient {
    public static final String CLIENT_KEY = "CLIENT";

    protected final MapleAESOFB send;
    protected final MapleAESOFB receive;
    protected NetworkSession session;
    private long lastPong = 0;

    @Getter
    private short loginAttempt = 0;

    private ScheduledFuture<?> idleTask = null;

    public BaseMapleClient(MapleAESOFB send, MapleAESOFB receive, NetworkSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public BaseMapleClient(byte[] ivSend, byte[] ivRecv, NetworkSession session) {
        this(
                new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION)),
                new MapleAESOFB(ivRecv, ServerConstants.MAPLE_VERSION),
                session);
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

    public String getSessionIPAddress() {
        return session.getRemoteAddress().toString().split(":")[0];
    }

    public final ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public final void setIdleTask(final ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }
}
